package com.jay.dove.transport.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * <p>
 *  Protocol decoder interface
 * </p>
 *
 * @author Jay
 * @date 2021/12/31 14:38
 */
public interface ProtocolDecoder {
    /**
     * decode
     * @param context context
     * @param in in
     * @param out out
     */
    void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out);
}
