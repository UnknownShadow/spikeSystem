package org.spike.service.impl;

import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spike.Exception.RepeatSpikeException;
import org.spike.Exception.SpikeClosedException;
import org.spike.dto.Exposer;
import org.spike.dto.SpikeExecution;
import org.spike.entity.Spike;
import org.spike.service.SpikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 *  SpikeService 单元测试
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml", "classpath:spring/spring-service.xml"})
public class SpikeServiceImplTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SpikeService spikeService;

    @Test
    public void getSpikeList() throws Exception {
        List<Spike> list = spikeService.getSpikeList();
        logger.info("list={}", list);
    }

    @Test
    public void getSpikeById() throws Exception {
        long id = 1005;
        Spike spike = spikeService.getSpikeById(id);
        logger.info("spike={}", spike);
    }

    @Test
    public void SpikeLogicTest() throws Exception {

        long id = 1005;
        Exposer exposer = spikeService.exposeSpikeUrl(id);
        // 判断是否暴露地址
        if (exposer.isExposer()){
            logger.info("exposer={}", exposer);
            String md5 = exposer.getMd5();
            long phoneNumber = 12557741257L;
            try {
                // 执行秒杀操作
                SpikeExecution execution = spikeService.executeSpike(id, phoneNumber, md5);
                logger.info("execution={}", execution);
            } catch(RepeatSpikeException e){
                logger.error(e.getMessage(), e);
            } catch(SpikeClosedException e){
                logger.error(e.getMessage(), e);
            }
        } else {
            logger.warn("exposer={}", exposer);
        }
    }

    @Test
    public void SpikeLogicByProcedureTest(){
        long spikeId = 1005;
        long phoneNumber = 12547751256L;
        Exposer exposer = spikeService.exposeSpikeUrl(spikeId);
        if (exposer.isExposer()){
            String md5 = exposer.getMd5();
            SpikeExecution execution = spikeService.executeSpikeByProcedure(spikeId, phoneNumber, md5);
            logger.info(execution.getStateInfo());
        }
    }

    @Test
    public void SpikeLogicByRedisTest(){

        final long id = 1005;
        final Exposer exposer = spikeService.exposeSpikeUrl(id);
        // 判断是否暴露地址
        if (exposer.isExposer()){
            logger.info("exposer={}", exposer);
            final String md5 = exposer.getMd5();
            final long phoneNumber = 12547751257L;
            try {
                // 执行秒杀操作
                TestRunnable runnable = new TestRunnable() {
                    @Override
                    public void runTest() throws Throwable {
                        SpikeExecution execution = spikeService.executeSpikeByRedisLock(id,
                                phoneNumber + new Random().nextInt(1000), md5);
                        logger.info("execution={}", execution);
                    }
                };

                TestRunnable[] tr = new TestRunnable[5];
                for (int i = 0; i < 5; i++){
                    tr[i] = runnable;
                }

                MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(tr);
                mttr.runTestRunnables();

            } catch(RepeatSpikeException e){
                logger.error(e.getMessage(), e);
            } catch(SpikeClosedException e){
                logger.error(e.getMessage(), e);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } else {
            logger.warn("exposer={}", exposer);
        }
    }

}