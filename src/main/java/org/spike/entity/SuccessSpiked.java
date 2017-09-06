package org.spike.entity;

import java.util.Date;

/**
 *  成功秒杀记录实体类
 *  包括秒杀商品，创建日期，秒杀者电话和秒杀状态
 */
public class SuccessSpiked {

    private long spikeId, phoneNumber;
    private short state;
    private Date createTime;

    private Spike spike;

    public long getSpikeId() {
        return spikeId;
    }

    public void setSpikeId(long spikeId) {
        this.spikeId = spikeId;
    }

    public long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public short getState() {
        return state;
    }

    public void setState(short state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Spike getSpike() {
        return spike;
    }

    public void setSpike(Spike spike) {
        this.spike = spike;
    }

    @Override
    public String toString() {
        return "SuccessSpiked{" +
                "spikeId=" + spikeId +
                ", phoneNumber=" + phoneNumber +
                ", state=" + state +
                ", createTime=" + createTime +
                ", spike=" + spike +
                '}';
    }
}
