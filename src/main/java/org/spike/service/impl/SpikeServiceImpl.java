package org.spike.service.impl;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spike.Exception.RepeatSpikeException;
import org.spike.Exception.SpikeClosedException;
import org.spike.Exception.SpikeException;
import org.spike.dao.SpikeDAO;
import org.spike.dao.SuccessSpikedDAO;
import org.spike.dao.cache.RedisDAO;
import org.spike.dto.Exposer;
import org.spike.dto.SpikeExecution;
import org.spike.entity.Spike;
import org.spike.entity.SuccessSpiked;
import org.spike.enums.SpikeStateEnum;
import org.spike.service.SpikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Service 接口实现类，对应具体逻辑
 */
@Service
public class SpikeServiceImpl implements SpikeService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    // 注入Service依赖
    @Autowired
    private SpikeDAO spikeDAO;
    @Autowired
    private SuccessSpikedDAO successSpikedDAO;
    @Autowired
    private RedisDAO redisDAO;

    // 盐值用于混淆
    private final String salt = "asdf5a545asDF*FA*e8394qa5s6**(%266asdf";

    public List<Spike> getSpikeList() {
        return spikeDAO.queryAll(0, 100);
    }

    public Spike getSpikeById(long spikeId) {
        return spikeDAO.queryById(spikeId);
    }

    /**
     * 判断是否暴露秒杀接口
     * @param spikeId
     * @return
     */
    public Exposer exposeSpikeUrl(long spikeId) {

        // 缓存优化, 一致性维护：建立在超时的基础上
        // 1: 访问redis
        Spike spike = redisDAO.getSpike(spikeId);

        if (spike == null){
            // 2: 访问数据库
            spike = spikeDAO.queryById(spikeId);
            // 判断秒杀商品是否存在
            if (spike == null)
                return new Exposer(false, spikeId);
            else {
                // 3: 放入redis
                redisDAO.putSpike(spike);
            }
        }

        long current = new Date().getTime();
        long start = spike.getStartTime().getTime();
        long end = spike.getEndTime().getTime();

        // 判断是否在秒杀时间内
        if (current < start || current > end)
            return new Exposer(false, spikeId, current, start, end);

        String md5 = getMD5(spikeId);

        // 返回确认秒杀信息
        return new Exposer(true, md5, spikeId);
    }

    // 生成MD5值
    private String getMD5(long spikeId){
        String base = salt + "/" + spikeId;

        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());

        return md5;
    }

    /**
     * 注解开启事务管理
     * @param spikeId
     * @param phoneNumber
     * @param md5
     * @return
     * @throws SpikeClosedException
     * @throws RepeatSpikeException
     * @throws SpikeException
     */
    @Transactional
    public SpikeExecution executeSpike(long spikeId, long phoneNumber, String md5)
            throws SpikeClosedException, RepeatSpikeException, SpikeException{

        try {
            // 对比MD5值
            if (md5 == null || !md5.equals(getMD5(spikeId)))
                throw new SpikeException("Spike data rewrite");

            // 插入秒杀信息
            int insertNumber = successSpikedDAO.insertSpikedRecord(spikeId, phoneNumber);
            if (insertNumber <= 0)
                throw new RepeatSpikeException("Repeat spike");
            else {
                // 执行减库存操作, 每次减1， 热点商品的竞争
                int reduceCount = spikeDAO.reduceNumber(1, spikeId, new Date());
                if (reduceCount <= 0)
                    // 没有更新记录，秒杀结束，rollback
                    throw new SpikeClosedException("Spike is closed");
                else {
                    // 获取秒杀记录， 返回成功秒杀信息, commit
                    SuccessSpiked successSpiked = successSpikedDAO.querySpikeRecord(spikeId, phoneNumber);
                    return new SpikeExecution(spikeId, SpikeStateEnum.SUCCESS, successSpiked);
                }
            }
        } catch (SpikeClosedException e){
            throw e;
        } catch (RepeatSpikeException e){
            throw e;
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            // 将所有编译期异常转化为运行期异常
            throw new SpikeException(e.getMessage());
        }
    }

    /**
     * 使用存储过程更新秒杀记录
     * @param spikeId
     * @param phoneNumber
     * @param md5
     * @return
     */
    public SpikeExecution executeSpikeByProcedure(long spikeId, long phoneNumber, String md5) {

        if (md5 == null || !md5.equals(getMD5(spikeId))){
            return new SpikeExecution(spikeId, SpikeStateEnum.DATA_REWRITE);
        }
        Date spikeTime = new Date();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("spikeId", spikeId);
        map.put("phone", phoneNumber);
        map.put("spikeTime", spikeTime);
        map.put("result", null);
        // 执行存储过程，result被赋值
        try {
            spikeDAO.spikeByProcedure(map);
            // 获取result
            int result = MapUtils.getInteger(map, "result", -2);
            if (result == 1){
                SuccessSpiked successSpiked = successSpikedDAO.
                        querySpikeRecord(spikeId, phoneNumber);
                return new SpikeExecution(spikeId, SpikeStateEnum.SUCCESS);
            } else {
                return new SpikeExecution(spikeId, SpikeStateEnum.stateOf(result));
            }
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            return new SpikeExecution(spikeId, SpikeStateEnum.INNER_ERROR);
        }
    }

    /**
     * 通过 redis 事务及锁执行秒杀
     * 使用队列存储成功秒杀记录
     * @param spikeId
     * @param phoneNumber
     * @param md5
     * @return
     */
    public SpikeExecution executeSpikeByRedisLock(long spikeId, long phoneNumber, String md5) {

        // 对比 md5 值
        if (md5 == null || !md5.equals(getMD5(spikeId)))
            return new SpikeExecution(spikeId, SpikeStateEnum.DATA_REWRITE);

        try {

            // 获得当前时间
            long time = System.currentTimeMillis();
            // 超时 2秒，循环更新列表
            while (System.currentTimeMillis() - time < 1000){
                // 新建 successSpike 用于更新 redis
                // 获得返回值
                SpikeStateEnum spikeStateEnum = redisDAO.updateSpike(spikeId, phoneNumber);
                // 如果不是需要重新秒杀，直接返回状态，否则继续循环
                if (spikeStateEnum.getState() != -4) {
                    // 如果秒杀结束，将数据持久化到数据库
                    if (spikeStateEnum.getState() == 0) {
                        List<String> successSpikeds = redisDAO.getSuccess(spikeId);
                        // list 中存在值时，循环插入
                        if (successSpikeds != null && successSpikeds.size() > 0) {
                            for (String success : successSpikeds){
                                String[] data = success.split("/");
                                Long id = Long.parseLong(data[0]);
                                Long phone = Long.parseLong(data[1]);
                                Date date = new Date(Long.parseLong(data[2]));
                                // 往数据库插入
                                successSpikedDAO.insertSpikedRecordFromRedis(id, phone, date);
                            }
                            // 秒杀结束, 更新库存为 0
                            spikeDAO.reduceNumberToZero(spikeId, new Date(System.currentTimeMillis()));
                        }
                    }

                    if (spikeStateEnum.getState() == 1){
                        // 成功秒杀, 更新库存
                        spikeDAO.reduceNumber(1, spikeId, new Date(System.currentTimeMillis()));
                    }
                    return new SpikeExecution(spikeId, spikeStateEnum);
                }
            }

            // 超时，返回内部错误
            return new SpikeExecution(spikeId, SpikeStateEnum.INNER_ERROR);

        } catch (Exception e){
            return new SpikeExecution(spikeId, SpikeStateEnum.INNER_ERROR);
        }
    }


}
