package com.jay.dove.transport.codec;

import com.jay.dove.exception.DecoderException;
import com.jay.dove.transport.connection.Connection;
import com.jay.dove.transport.protocol.Protocol;
import com.jay.dove.transport.protocol.ProtocolCode;
import com.jay.dove.transport.protocol.ProtocolManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;

import java.util.List;

/**
 * <p>
 *  Protocol code based decoder.
 *  This decoder recognize protocol by reading protocol code
 * </p>
 *
 * @author Jay
 * @date 2021/12/31 14:05
 */
public class ProtocolCodeBasedDecoder extends AbstractBatchDecoder{

    public static final int DEFAULT_PROTOCOL_VERSION_LENGTH         = 1;
    public static final int DEFAULT_ILLEGAL_PROTOCOL_VERSION_LENGTH = -1;
    public static final int DEFAULT_PROTOCOL_CODE_LENGTH = 2;


    /**
     * decode the protocol code from input.
     * {@link #decode} will use this code to locate protocol decoder
     * @param in input
     * @return {@link ProtocolCode}
     */
    protected ProtocolCode decodeProtocolCode(ByteBuf in){
        byte[] bytes = new byte[DEFAULT_PROTOCOL_CODE_LENGTH];
        if(in.readableBytes() > DEFAULT_PROTOCOL_CODE_LENGTH) {
            in.readBytes(bytes);
        }
        return ProtocolCode.fromBytes(bytes);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // start index
        in.markReaderIndex();
        Protocol protocol;
        try{
            // decode protocol code
            ProtocolCode protocolCode = decodeProtocolCode(in);
            if(protocolCode == null){
                return;
            }

            // check this channel's protocol
            Attribute<ProtocolCode> attr = ctx.channel().attr(Connection.PROTOCOL);
            if(attr.get() == null){
                attr.set(protocolCode);
            }
            else if(!attr.get().equals(protocolCode)){
                throw new DecoderException("channel doesn't support this protocol");
            }

            // get protocol
            protocol = ProtocolManager.getProtocol(protocolCode);
        }finally {
            // reset reader index
            in.resetReaderIndex();
        }
        if(protocol == null){
            throw new DecoderException("unknown protocol");
        }
        // call protocol decoder
        protocol.getDecoder().decode(ctx, in, out);
    }
}
