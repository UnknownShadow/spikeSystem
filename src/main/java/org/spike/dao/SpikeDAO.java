package org.spike.dao;

import org.apache.ibatis.annotations.Param;
import org.spike.entity.Spike;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *  DAO类，通过 mybatis 用于 Mysql 数据库通信
 *  对秒杀商品表的处理
 */
public interface SpikeDAO {

    /**
     * 减库存
     * @param reduceNumber
     * @param spikeId
     * @param spikeTime
     * @return
     */
    int reduceNumber(@Param("reduceNumber") int reduceNumber, @Param("spikeId") long spikeId, @Param("spikeTime") Date spikeTime);

    int reduceNumberToZero(@Param("spikeId") long spikeId, @Param("spikeTime") Date spikeTime);

    /**
     * 根据偏移量查询一定库存
     * @param offset
     * @param limit
     * @return
     */
    List<Spike> queryAll(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 根据ID查询
     * @param spikeId
     * @return
     */
    Spike queryById(long spikeId);

    /**
     * 使用存储过程执行秒杀
     * @param paramMap
     */
    void spikeByProcedure(Map<String, Object> paramMap);

}
