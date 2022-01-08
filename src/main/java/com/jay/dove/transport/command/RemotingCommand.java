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

    int getId();

    void setId(int id);

    ProtocolCode getProtocolCode();

    Serializer getSerializer();

    CommandCode getCommandCode();
}
