package org.spike.dao.cache;

import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spike.dao.SpikeDAO;
import org.spike.entity.Spike;
import org.spike.entity.SuccessSpiked;
import org.spike.enums.SpikeStateEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import sun.security.provider.ConfigFile;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 *  RedisDAO 单元测试
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class RedisDAOTest {

    private long id = 1001;
    @Autowired
    private RedisDAO redisDAO;

    @Autowired
    private SpikeDAO spikeDAO;

    @Test
    public void testSpike() throws Exception {
        // get and put
        Spike spike = redisDAO.getSpike(id);
        if (spike == null) {
            spike = spikeDAO.queryById(id);
            if (spike != null){
                String result = redisDAO.putSpike(spike);
                System.out.println(result);
                spike = redisDAO.getSpike(id);
                System.out.println("spike:" + spike);
            }
        } else {
            System.out.println("spike:" + spike);
        }
    }

    @Test
    public void updateTest(){

        long phone = 17544688236L;

//        long num = redisDAO.putSuccess(successSpiked);
//        System.out.println("num: " + num);
        SpikeStateEnum spikeStateEnum = redisDAO.updateSpike(id, phone);
        System.out.println("Result: " + spikeStateEnum.getState() + ", " + spikeStateEnum.getStateInfo());
    }

    @Test
    public void countTest(){
        int count = redisDAO.getCount(id);
        System.out.println("库存数： " + count);
    }


    @Test
    public void threadTest() throws Throwable {

        TestRunnable[] tr = new TestRunnable[10];

        int i = 0;
        for (; i < 10; i++){
            tr[i] = new TestRunnable() {
                @Override
                public void runTest() throws Throwable {
                    SpikeStateEnum spikeStateEnum = redisDAO.updateSpike(id, 12340000000L + new Random().nextInt(1000));
                    System.out.println("State: " + spikeStateEnum.getState() + "/" + spikeStateEnum.getStateInfo());
                }
            };
        }

        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(tr);
        mttr.runTestRunnables();
    }

}