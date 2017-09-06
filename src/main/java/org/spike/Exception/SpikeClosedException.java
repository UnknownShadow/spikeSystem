package org.spike.Exception;

/**
 *  秒杀关闭异常，继承秒杀异常类
 */
public class SpikeClosedException extends SpikeException {

    public SpikeClosedException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpikeClosedException(String message) {
        super(message);
    }
}
