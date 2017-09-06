package org.spike.service;

import org.spike.Exception.RepeatSpikeException;
import org.spike.Exception.SpikeClosedException;
import org.spike.Exception.SpikeException;
import org.spike.dto.Exposer;
import org.spike.dto.SpikeExecution;
import org.spike.entity.Spike;
import sun.security.provider.ConfigFile;

import java.util.List;

/**
 *  Service 接口，描述 spring 中服务逻辑
 */
public interface SpikeService {

    /**
     * 查询所有秒杀商品
     * @return
     */
    List<Spike> getSpikeList();

    /**
     * 根据ID查询秒杀商品
     * @param spikeId
     * @return
     */
    Spike getSpikeById(long spikeId);

    /**
     * 是否暴露秒杀地址
     * @param spikeId
     * @return
     */
    Exposer exposeSpikeUrl(long spikeId);

    /**
     * 执行秒杀逻辑
     * @param spikeId
     * @param phoneNumber
     * @param md5
     * @return
     */
    SpikeExecution executeSpike(long spikeId, long phoneNumber, String md5)
        throws SpikeClosedException, RepeatSpikeException, SpikeException;


    /**
     * 通过存储过程执行秒杀逻辑
     * @param spikeId
     * @param phoneNumber
     * @param md5
     * @return
     */
    SpikeExecution executeSpikeByProcedure(long spikeId, long phoneNumber, String md5);

    /**
     * 通过 redis 锁执行秒杀逻辑
     * @param spikeId
     * @param phoneNumber
     * @param md5
     * @return
     */
    SpikeExecution executeSpikeByRedisLock(long spikeId, long phoneNumber, String md5);

}
