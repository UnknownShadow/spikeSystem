package org.spike.dto;

import org.spike.entity.SuccessSpiked;
import org.spike.enums.SpikeStateEnum;

/**
 *  DTO类，用于 service 层数据传输
 *  描述秒杀执行情况信息
 */
public class SpikeExecution {

    private long spikeId;

    private int state;

    private String stateInfo;

    private SuccessSpiked successSpiked;

    // 秒杀成功时返回所有信息
    public SpikeExecution(long spikeId, SpikeStateEnum stateEnum, SuccessSpiked successSpiked) {
        this.spikeId = spikeId;
        this.state = stateEnum.getState();
        this.stateInfo = stateEnum.getStateInfo();
        this.successSpiked = successSpiked;
    }

    // 秒杀失败时返回状态值
    public SpikeExecution(long spikeId, SpikeStateEnum stateEnum) {
        this.spikeId = spikeId;
        this.state = stateEnum.getState();
        this.stateInfo = stateEnum.getStateInfo();
    }

    public long getSpikeId() {
        return spikeId;
    }

    public void setSpikeId(long spikeId) {
        this.spikeId = spikeId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public void setStateInfo(String stateInfo) {
        this.stateInfo = stateInfo;
    }

    public SuccessSpiked getSuccessSpiked() {
        return successSpiked;
    }

    public void setSuccessSpiked(SuccessSpiked successSpiked) {
        this.successSpiked = successSpiked;
    }

    @Override
    public String toString() {
        return "SpikeExecution{" +
                "spikeId=" + spikeId +
                ", state=" + state +
                ", stateInfo='" + stateInfo + '\'' +
                ", successSpiked=" + successSpiked +
                '}';
    }
}
