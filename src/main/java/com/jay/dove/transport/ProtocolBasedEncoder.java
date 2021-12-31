package com.jay.dove.transport;

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
 *
 * </p>
 *
 * @author Jay
 * @date 2021/12/31 15:04
 */
public abstract class ProtocolBasedEncoder extends MessageToByteEncoder<Object> {
    /**
     * the default protocol code, if the request is missing a custom one.
     */
    private final ProtocolCode defaultProtocolCode;

    protected ProtocolBasedEncoder(ProtocolCode defaultProtocolCode) {
        this.defaultProtocolCode = defaultProtocolCode;
    }

    @Override
    protected void encode(ChannelHandlerContext context, Object o, ByteBuf byteBuf) throws Exception {
        ProtocolCode protocolCode;
        Attribute<ProtocolCode> attr = context.channel().attr(AttributeKey.valueOf("protocol"));
        if(attr != null){
            protocolCode = attr.get();
        }else{
            protocolCode = defaultProtocolCode;
        }

        Protocol protocol = ProtocolManager.getProtocol(protocolCode);
        if(protocol == null){
            throw new EncoderException("unknown protocol, please register protocol to ProtocolManager");
        }
        protocol.getEncoder().encode(context, o, byteBuf);
    }
}
