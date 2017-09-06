package org.spike.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.omg.PortableInterceptor.SUCCESSFUL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spike.entity.Spike;
import org.spike.entity.SuccessSpiked;
import org.spike.enums.SpikeStateEnum;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

/**
 *
 */
public class RedisDAO {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JedisPool jedisPool;

    private final static String SET = "checkRecord";

    public RedisDAO(String ip, int port){
        jedisPool = new JedisPool(ip, port);
    }

    private RuntimeSchema<Spike> schemaSpike = RuntimeSchema.createFrom(Spike.class);
    private RuntimeSchema<SuccessSpiked> schemaSucess = RuntimeSchema.createFrom(SuccessSpiked.class);


    public Jedis getJedis(){

        try{

            return jedisPool.getResource();
        } catch (Exception e){
            logger.error(e.getMessage(), e);
        }

        return null;
    }


    // 获取秒杀商品
    public Spike getSpike(long spikeId){
        // redis 操作逻辑
        try{
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "spike:" + spikeId;
                // 没有内部序列化
                // get->byte[] -> 反序列化 -> Object(Spike)
                // 使用自定义序列化
                // protostuff : pojo
                byte[] bytes = jedis.get(key.getBytes());

                // 从缓存中重新获取到商品的字节序列
                if (bytes != null){
                    // 空对象
                    Spike spike = schemaSpike.newMessage();
                    // 为 Spike 赋值
                    ProtostuffIOUtil.mergeFrom(bytes, spike, schemaSpike);
                    // Spike被反序列化
                    return spike;
                }
            } finally {
                // 关闭连接
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    // 存入秒杀商品
    public String putSpike(Spike spike){
        // set Object(Spike) -> 序列化 -> byte[]
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "spike:" + spike.getSpikeId();
                byte[] bytes = ProtostuffIOUtil.toByteArray(spike, schemaSpike,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                // 超时缓存（key, timeout, bytes）
                int timeout = 60 * 60; // 缓存一小时
                String result = jedis.setex(key.getBytes(), timeout, bytes);
                return result;
            } finally {
                jedis.close();
            }
        } catch (Exception e){
            logger.error(e.getMessage(), e);
        }

        return null;
    }


    /**
     * 检查是否已经存在秒杀记录
     * @param spikeId
     * @param phoneNumber
     * @return
     */
    public boolean isRepeatedSpike(long spikeId, long phoneNumber){

        Jedis jedis = getJedis();
        if (jedis != null){
            String key = spikeId + "/" + phoneNumber;
            // 检查集合中是否存在记录
            boolean exist = jedis.sismember(SET, key);

            jedis.close();
            return exist;
        }

        return true;
    }

    /**
     * 更新逻辑
     * @param successSpiked
     */
    public SpikeStateEnum updateSpike(SuccessSpiked successSpiked){

        long start = System.currentTimeMillis();
        try {
            // 设置监控 key
            String watchKey = "watch:" + successSpiked.getSpikeId();
            Jedis jedis = jedisPool.getResource();
            // 获取商品总库存
            int total = getSpike(successSpiked.getSpikeId()).getNumber();
            // 设置库存 key
            String countKey = "count:" + successSpiked.getSpikeId();
            // 如果不存在 key，则设置一个
            jedis.setnx(watchKey, 0 + "");
            jedis.setnx(countKey, total + "");
            // 检查库存的余量
            int count = Integer.parseInt(jedis.get(countKey));
            // 如果还有库存
            if (count > 0) {
                // 检查是否重复秒杀
                boolean exist = isRepeatedSpike(successSpiked.getSpikeId(), successSpiked.getPhoneNumber());
                if (!exist) {
                    try {
                        // 开始监控 key
                        jedis.watch(watchKey);
                        // 库存大于0，开启事务
                        Transaction tx = jedis.multi();
                        // 更新 watchKey
                        tx.incr(watchKey);
                        // 提交事务，如果此时 watchKey 被更改了，则返回 null
                        List<Object> list = tx.exec();
                        // 获取锁成功
                        if (list != null) {
                            // 获取返回值
                            Long result = jedis.decr(countKey);
                            // 避免超卖
                            if (result >= 0) {
                                // 存入防重集合
                                putRecord(successSpiked.getSpikeId(), successSpiked.getPhoneNumber());
                                // 存入秒杀记录
                                putSuccess(successSpiked);
                            } else {
                                return SpikeStateEnum.END;
                            }
                            long end = System.currentTimeMillis() - start;
                            System.out.println("Time: " + end);
                            return SpikeStateEnum.SUCCESS;
                        } else {
                            // 返回需重新秒杀
                            long end = System.currentTimeMillis() - start;
                            System.out.println("Time: " + end);
                            return SpikeStateEnum.NEED_RESPIK;
                        }

                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    } finally {
                        jedis.close();
                    }

                } else {
                    // 返回重复秒杀
                    long end = System.currentTimeMillis() - start;
                    System.out.println("Time: " + end);
                    return SpikeStateEnum.REPEATED_SPIKE;
                }
            } else {
                // 库存为0，返回秒杀结束
                return SpikeStateEnum.END;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        // 返回内部错误
        return SpikeStateEnum.INNER_ERROR;
    }

    /**
     * 防重复集合
     * 由商品ID 和手机号组成
     * @param spikeId
     * @param phoneNumber
     * @return
     */
    public Long putRecord(long spikeId, long phoneNumber){

        Jedis jedis = getJedis();
        if (jedis != null){
            String key = spikeId + "/" + phoneNumber;
            Long result = jedis.sadd(SET, key);

            jedis.close();
            return result;
        }
        return null;
    }


    /**
     * 往 redis 列表中插入抢购成功记录
     * @param successSpiked
     * @return
     */
    public Long putSuccess(SuccessSpiked successSpiked){

        try {

            Jedis jedis = jedisPool.getResource();
            try{

                String listKey = "list:" + successSpiked.getSpikeId();
                // 序列化 successSpiked
                byte[] bytes = ProtostuffIOUtil.toByteArray(successSpiked, schemaSucess,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));

                // jedis 列表中插入秒杀成功记录
                Long result = jedis.lpush(listKey.getBytes(), bytes);

                // 返回插入结果
                return result;

            } catch (Exception e){
                logger.error(e.getMessage(), e);
            } finally {
                jedis.close();
            }

        } catch (Exception e){
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * 取出某一商品的所有秒杀记录
     * @return
     */
    public List<SuccessSpiked> getSuccess(long spikeId){

        List<SuccessSpiked> list = new ArrayList<SuccessSpiked>();
        try {
            Jedis jedis = jedisPool.getResource();

            try {
                String listKey = "list:" + spikeId;
                long length = jedis.llen(listKey.getBytes());

                // 取出所有成功秒杀记录
                for (int i = 0; i < length; i++) {
                    // 弹出并获得秒杀记录字节序列
                    byte[] bytes = jedis.rpop(listKey.getBytes());
                    // 新建空白秒杀
                    SuccessSpiked successSpiked = schemaSucess.newMessage();
                    // 注入信息
                    ProtostuffIOUtil.mergeFrom(bytes, successSpiked, schemaSucess);
                    // 插入列表
                    list.add(successSpiked);
                }
                // 取出记录后删除列表和集合
                jedis.del(listKey.getBytes());
//                jedis.del("count:" + spikeId);
            } catch (Exception e){
                logger.error(e.getMessage(), e);
            } finally {
                jedis.close();
            }
        } catch (Exception e){
            logger.error(e.getMessage(), e);
        }

        return list;
    }

    /**
     * 获得库存数
     * @param spikeId
     * @return
     */
    public int getCount(long spikeId){

        try {

            Jedis jedis = jedisPool.getResource();
            try {
                String key = "count:" + spikeId;
                return Integer.parseInt(jedis.get(key));
            } finally {
                jedis.close();
            }

        } catch (Exception e){
            logger.error(e.getMessage(), e);
        }

        return -1;
    }

}
