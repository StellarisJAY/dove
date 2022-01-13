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

    public static final int DEFAULT_PROTOCOL_VERSION_LENGTH         = 1;
    public static final int DEFAULT_ILLEGAL_PROTOCOL_VERSION_LENGTH = -1;
    public static final int DEFAULT_PROTOCOL_CODE_LENGTH = 2;
    public static final short DEFAULT_PROTOCOL = 22;


    /**
     * decode the protocol code from input.
     * {@link #decode} will use this code to locate protocol decoder
     * @param in input
     * @return {@link ProtocolCode}
     */
    protected ProtocolCode decodeProtocolCode(ByteBuf in){
        if(in.readableBytes() > DEFAULT_PROTOCOL_CODE_LENGTH) {
            short code = in.readShort();
            return ProtocolCode.fromValue(code);
        }
        return ProtocolCode.fromValue(DEFAULT_PROTOCOL);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Protocol protocol = null;
        in.markReaderIndex();
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
        }catch (Exception e){
            in.resetReaderIndex();
        }
        if(protocol == null){
            throw new DecoderException("unknown protocol");
        }
        // call protocol decoder
        protocol.getDecoder().decode(ctx, in, out);
    }
}
