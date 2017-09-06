package org.spike.Exception;

/**
 *  重复秒杀异常，继承秒杀异常类
 */
public class RepeatSpikeException extends SpikeException {

    public RepeatSpikeException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepeatSpikeException(String message) {
        super(message);
    }
}
