package com.jay.dove.transport.protocol;

import java.util.Arrays;

/**
 * <p>
 *  Protocol code. Identification for custom protocols
 * </p>
 *
 * @author Jay
 * @date 2021/12/31 14:10
 */
public class ProtocolCode {
    private final byte[] code;

    private ProtocolCode(byte[] code) {
        this.code = code;
    }

    public static ProtocolCode fromBytes(byte[] bytes){
        return new ProtocolCode(bytes);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this){
            return true;
        }
        if(!(obj instanceof ProtocolCode)){
            return false;
        }
        ProtocolCode protocolCode = (ProtocolCode)obj;
        return Arrays.equals(protocolCode.code, code);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(code);
    }

    @Override
    public String toString() {
        return "ProtocolCode{" +
                "code=" + Arrays.toString(code) +
                '}';
    }
}
