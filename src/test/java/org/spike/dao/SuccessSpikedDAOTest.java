package org.spike.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.spike.entity.SuccessSpiked;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.*;

/**
 *  SuccessSpikedDAO 单元测试
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessSpikedDAOTest {

    @Resource
    private SuccessSpikedDAO successSpikedDAO;

    @Test
    public void insertSpikedRecord() throws Exception {

        long id = 1005;
        long phoneNumber = 12457841456L;
        int insertNumber = successSpikedDAO.insertSpikedRecord(id, phoneNumber);

        System.out.println("insertNumber: " + insertNumber);
    }

    @Test
    public void insertSpikedRecordFromRedis() throws Exception {

        long id = 1005;
        long phoneNumber = 12121212121L;
        int insertNumber = successSpikedDAO.insertSpikedRecordFromRedis(id, phoneNumber, new Timestamp(new Date().getTime()));

        System.out.println("insertNumber: " + insertNumber);
    }

    @Test
    public void querySpikeRecord() throws Exception {

        long id = 1005;
        long phoneNumber = 12457841456L;
        SuccessSpiked successSpiked = successSpikedDAO.querySpikeRecord(id, phoneNumber);

        System.out.println("successSpiked: " + successSpiked);
    }

}