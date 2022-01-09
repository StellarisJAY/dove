package com.jay.dove.transport.command;

import io.netty.util.internal.ObjectUtil;

import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public boolean equals(Object other){
        if(other instanceof CommandCode){
            return code == ((CommandCode) other).code;
        }
        return false;
    }
}
