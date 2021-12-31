package com.jay.dove.transport.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * <p>
 *  Protocol encoder interface
 * </p>
 *
 * @author Jay
 * @date 2021/12/31 14:37
 */
public interface ProtocolEncoder {
    /**
     * encode
     * @param context context
     * @param object object
     * @param out out
     */
    void encode(ChannelHandlerContext context, Object object, ByteBuf out);
}
