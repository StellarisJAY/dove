package com.jay.dove.transport.codec;

import com.jay.dove.transport.connection.Connection;
import com.jay.dove.transport.protocol.Protocol;
import com.jay.dove.transport.protocol.ProtocolCode;
import com.jay.dove.transport.protocol.ProtocolManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * <p>
 *  Protocol code based Encoder.
 *  Encodes a Remoting Command with protocol's encoder.
 * </p>
 *
 * @author Jay
 * @date 2021/12/31 15:04
 */
public class ProtocolCodeBasedEncoder extends MessageToByteEncoder<Object> {
    /**
     * the default protocol code, if the request is missing a custom one.
     */
    private final ProtocolCode defaultProtocolCode;

    public ProtocolCodeBasedEncoder(ProtocolCode defaultProtocolCode) {
        this.defaultProtocolCode = defaultProtocolCode;
    }

    @Override
    public void encode(ChannelHandlerContext context, Object o, ByteBuf byteBuf) throws Exception {
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
        byteBuf.writeByte(protocolCode.value());
        // call protocol's encoder
        protocol.getEncoder().encode(context, o, byteBuf);
    }
}
