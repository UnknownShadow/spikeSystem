package org.spike.dto;

import java.util.Date;

/**
 *  DTO类，用于 service 层数据传输
 *  携带是否开启秒杀相关信息
 */
public class Exposer {

    // 是否开启秒杀
    private boolean isExposer;
    // 混淆
    private String md5;
    // ID
    private long spikeId;
    // 系统当前时间
    private long current;
    // 开启时间
    private long start;
    // 结束时间
    private long end;

    // 秒杀时间未到，返回不开启秒杀，
    public Exposer(boolean isExposer, long spikeId, long current, long start, long end) {
        this.isExposer = isExposer;
        this.spikeId = spikeId;
        this.current = current;
        this.start = start;
        this.end = end;
    }

    // 秒杀时间，返回开启秒杀，并携带 MD5 用于对比
    public Exposer(boolean isExposer, String md5, long spikeId) {
        this.isExposer = isExposer;
        this.md5 = md5;
        this.spikeId = spikeId;
    }

    // 秒杀时间结束，返回不开启秒杀
    public Exposer(boolean isExposer, long spikeId) {
        this.isExposer = isExposer;
        this.spikeId = spikeId;
    }

    public boolean isExposer() {
        return isExposer;
    }

    public void setExposer(boolean exposer) {
        isExposer = exposer;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getSpikeId() {
        return spikeId;
    }

    public void setSpikeId(long spikeId) {
        this.spikeId = spikeId;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "Exposer{" +
                "isExposer=" + isExposer +
                ", md5='" + md5 + '\'' +
                ", spikeId=" + spikeId +
                ", current=" + current +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
