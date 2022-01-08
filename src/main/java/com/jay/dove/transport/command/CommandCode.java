package com.jay.dove.transport.command;

/**
 * <p>
 *  Command Code interface
 * </p>
 *
 * @author Jay
 * @date 2022/01/08 11:05
 */
public class CommandCode {
    private final short code;

    public CommandCode(short code) {
        this.code = code;
    }

    /**
     * the value of a CommandCode
     * @return short
     */
    public short value(){
        return code;
    }
}
