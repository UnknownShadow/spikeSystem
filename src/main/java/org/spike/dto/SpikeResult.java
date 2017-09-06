package org.spike.dto;

/**
 *  DTO类，携带所有秒杀（当前系统时间，是否暴露秒杀地址，秒杀执行结果）信息
 *  用于 service 层和 web 层通信
 */

// 所有ajax请求返回类型，封装json结果
public class SpikeResult<T> {

    private boolean success;

    private T data;

    private String error;

    // 秒杀成功，返回数据
    public SpikeResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    // 秒杀失败，返回错误信息
    public SpikeResult(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
