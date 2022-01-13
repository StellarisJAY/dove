package com.jay.dove.transport.protocol;

import java.util.Objects;

/**
 * <p>
 *  Protocol code. Identification for custom protocols
 * </p>
 *
 * @author Jay
 * @date 2021/12/31 14:10
 */
public class ProtocolCode {
    private final short code;

    private ProtocolCode(short code) {
        this.code = code;
    }

    public static ProtocolCode fromValue(short code){
        return new ProtocolCode(code);
    }


    public short value(){
        return code;
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
        return protocolCode.code == code;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }

    @Override
    public String toString() {
        return "ProtocolCode{" +
                "code=" + code +
                '}';
    }
}
