package org.spike.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.spike.entity.Spike;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 *  SpikeDAO 单元测试
 */
@RunWith(SpringJUnit4ClassRunner.class)
// spring 配置文件路径
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SpikeDAOTest {

    @Resource
    // 注入DAO实现类依赖
    private SpikeDAO spikeDAO;

    @Test
    public void reduceNumber() throws Exception {
        long id = 1005;
        Date spikeTime = new Date();
        int reduceNumber = spikeDAO.reduceNumber(1, id, spikeTime);
        System.out.println("reduce number: " + reduceNumber);
    }

    @Test
    public void queryAll() throws Exception {
        List<Spike> list = spikeDAO.queryAll(0, 100);
        for (Spike spike : list){
            System.out.println("spike: " + spike);
        }

    }

    @Test
    public void queryById() throws Exception {
        long id = 1005;
        Spike spike = spikeDAO.queryById(id);
        System.out.println("spike: " + spike);
    }

}