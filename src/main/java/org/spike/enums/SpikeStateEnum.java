package org.spike.enums;

/**
 *  枚举类，记录秒杀状态
 */
public enum SpikeStateEnum {
    SUCCESS(1, "秒杀成功"),
    END(0, "秒杀失败"),
    REPEATED_SPIKE(-1, "重复秒杀"),
    INNER_ERROR(-2, "内部错误"),
    DATA_REWRITE(-3, "数据篡改"),
    NEED_RESPIK(-4, "重新秒杀");

    private int state;
    private String stateInfo;

    SpikeStateEnum(int state, String stateInfo) {
        this.state = state;
        this.stateInfo = stateInfo;
    }

    public int getState() {
        return state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public static SpikeStateEnum stateOf(int index){
        for (SpikeStateEnum state : values()){
            if (state.getState() == index)
                return state;
        }

        return null;
    }
}
