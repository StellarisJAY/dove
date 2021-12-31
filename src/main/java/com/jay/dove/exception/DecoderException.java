package com.jay.dove.exception;

/**
 * <p>
 *  Decoder thrown exception
 * </p>
 *
 * @author Jay
 * @date 2021/12/31 13:34
 */
public class DecoderException extends Exception{
    public DecoderException(String message) {
        super(message);
    }

    public DecoderException(Throwable cause) {
        super(cause);
    }
}
