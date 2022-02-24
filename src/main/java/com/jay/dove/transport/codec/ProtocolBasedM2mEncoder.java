package com.jay.dove.transport.codec;

import com.jay.dove.transport.connection.Connection;
import com.jay.dove.transport.protocol.Protocol;
import com.jay.dove.transport.protocol.ProtocolCode;
import com.jay.dove.transport.protocol.ProtocolManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.Attribute;

import java.util.List;

/**
 * <p>
 *  Protocol coded based Message To Message Encoder
 * </p>
 *
 * @author Jay
 * @date 2022/02/13 12:55
 */
public class ProtocolBasedM2mEncoder extends MessageToMessageEncoder<Object> {

    private final ProtocolCode defaultProtocolCode;

    public ProtocolBasedM2mEncoder(ProtocolCode defaultProtocolCode) {
        this.defaultProtocolCode = defaultProtocolCode;
    }

    @Override
    protected void encode(ChannelHandlerContext context, Object o, List<Object> out) throws Exception {
        ProtocolCode protocolCode;
        // get protocol code from channel
        Attribute<ProtocolCode> attr = context.channel().attr(Connection.PROTOCOL);
        if(attr != null){
            protocolCode = attr.get();
        }else{
            // no protocol code found, use default protocol
            protocolCode = defaultProtocolCode;
        }
        // get the protocol instance
        Protocol protocol = ProtocolManager.getProtocol(protocolCode);
        if(protocol == null){
            throw new EncoderException("unknown protocol, please register protocol to ProtocolManager");
        }
        protocol.getM2mEncoder().encode(context, o, out);
    }
}
