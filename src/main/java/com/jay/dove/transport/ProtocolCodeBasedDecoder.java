package com.jay.dove.transport;

import com.jay.dove.exception.DecoderException;
import com.jay.dove.transport.protocol.Protocol;
import com.jay.dove.transport.protocol.ProtocolCode;
import com.jay.dove.transport.protocol.ProtocolManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

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

    private int protocolCodeLength;

    public static final int DEFAULT_PROTOCOL_VERSION_LENGTH         = 1;
    public static final int DEFAULT_ILLEGAL_PROTOCOL_VERSION_LENGTH = -1;

    public ProtocolCodeBasedDecoder(int protocolCodeLength) {
        this.protocolCodeLength = protocolCodeLength;
    }

    /**
     * decode the protocol code from input.
     * {@link #decode} will use this code to locate protocol decoder
     * @param in input
     * @return {@link ProtocolCode}
     */
    protected ProtocolCode decodeProtocolCode(ByteBuf in){
        byte[] bytes = new byte[protocolCodeLength];
        if(in.readableBytes() > protocolCodeLength) {
            in.readBytes(bytes);
        }
        return ProtocolCode.fromBytes(bytes);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();
        Protocol protocol;
        try{
            ProtocolCode protocolCode = decodeProtocolCode(in);
            if(protocolCode == null){
                return;
            }
            protocol = ProtocolManager.getProtocol(protocolCode);
        }finally {
            in.resetReaderIndex();
        }
        if(protocol == null){
            throw new DecoderException("unknown protocol");
        }
        protocol.getDecoder().decode(ctx, in, out);
    }
}
