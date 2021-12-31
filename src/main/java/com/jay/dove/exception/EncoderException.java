package com.jay.dove.exception;

/**
 * <p>
 *  Encoder exception
 * </p>
 *
 * @author Jay
 * @date 2021/12/31 15:05
 */
public class EncoderException extends Exception{
    public EncoderException(String message) {
        super(message);
    }

    public EncoderException(Throwable cause) {
        super(cause);
    }
}
