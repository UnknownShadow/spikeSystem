package org.spike.Exception;

/**
 *  所有秒杀异常
 */
public class SpikeException extends RuntimeException {

    public SpikeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpikeException(String message) {
        super(message);
    }
}
