package org.spike.dao;

import org.apache.ibatis.annotations.Param;
import org.spike.entity.SuccessSpiked;

import java.util.Date;


/**
 *  DAO类，对成功秒杀表的处理
 */
public interface SuccessSpikedDAO {

    /**
     * 插入秒杀记录，联合主键过滤重复，通过商品ID和秒杀手机号确认唯一秒杀记录
     * @param spikeId
     * @param phoneNumber
     * @return
     */
    int insertSpikedRecord(@Param("spikeId") long spikeId, @Param("phoneNumber") long phoneNumber);

    /**
     * 读取 redis 缓存，插入秒杀记录
     * @param spikeId
     * @param phoneNumber
     * @param createTime
     * @return
     */
    int insertSpikedRecordFromRedis(@Param("spikeId") long spikeId, @Param("phoneNumber") long phoneNumber, @Param("createTime") Date createTime);

    /**
     * 根据主键查询秒杀记录
     * @param spikeId
     * @param phoneNumber
     * @return
     */
    SuccessSpiked querySpikeRecord(@Param("spikeId") long spikeId, @Param("phoneNumber") long phoneNumber);
}
