package com.jay.dove.transport.command;

import com.jay.dove.serialize.Serializer;
import com.jay.dove.transport.protocol.ProtocolCode;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2022/01/07 19:39
 */
public interface RemotingCommand extends Serializable {
    /**
     * get command id
     * @return int
     */
    int getId();

    /**
     * set command id
     * @param id int
     */
    void setId(int id);

    /**
     * get serializer
     * @return byte serializer id code
     */
    byte getSerializer();

    /**
     * get CommandCode
     * @return {@link CommandCode}
     */
    CommandCode getCommandCode();

    /**
     * get the timeout milliseconds
     * @return long
     */
    long getTimeoutMillis();

    /**
     *  set timeout
     * @param time long
     */
    void setTimeoutMillis(long time);

    byte[] getContent();
}
