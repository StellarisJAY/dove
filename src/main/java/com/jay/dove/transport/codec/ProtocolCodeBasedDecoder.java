package com.jay.dove.transport.codec;

import com.jay.dove.exception.DecoderException;
import com.jay.dove.transport.connection.Connection;
import com.jay.dove.transport.protocol.Protocol;
import com.jay.dove.transport.protocol.ProtocolCode;
import com.jay.dove.transport.protocol.ProtocolManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 *  Protocol code based decoder.
 *  This decoder recognize protocol by reading protocol code
 * </p>
 *
 * @author Jay
 * @date 2021/12/31 14:05
 */
@Slf4j
public class ProtocolCodeBasedDecoder extends AbstractBatchDecoder{
    /**
     * PROTOCOL CODE LENGTH
     */
    public static final int DEFAULT_PROTOCOL_CODE_LENGTH = 1;
    private final ProtocolCode rpcCode = ProtocolCode.fromValue((byte)22);
    /**
     * decode the protocol code from input.
     * {@link #decode} will use this code to locate protocol decoder
     * @param in input
     * @return {@link ProtocolCode}
     */
    protected ProtocolCode decodeProtocolCode(ByteBuf in){
        if(in.readableBytes() > DEFAULT_PROTOCOL_CODE_LENGTH) {
            byte code = in.readByte();
            return ProtocolCode.fromValue(code);
        }
        return null;
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Protocol protocol = null;
        ProtocolCode protocolCode = null;
        in.markReaderIndex();
        try{
            protocolCode = decodeProtocolCode(in);
            if(protocolCode == null){
                return;
            }
            protocol = ProtocolManager.getProtocol(protocolCode);
        }finally {
            in.resetReaderIndex();
        }

        if(protocol != null){
            Attribute<ProtocolCode> attr = ctx.channel().attr(Connection.PROTOCOL);
            if(attr.get() == null){
                attr.set(protocolCode);
            }
            protocol.getDecoder().decode(ctx, in, out);
        }else{
            log.error("unregistered protocol: {}", protocolCode.value());
        }
    }
}
