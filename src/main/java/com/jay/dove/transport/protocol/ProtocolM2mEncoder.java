package com.jay.dove.transport.protocol;

import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * <p>
 *  Protocol Message to Message Encoder
 * </p>
 *
 * @author Jay
 * @date 2022/02/13 12:57
 */
public interface ProtocolM2mEncoder {
    /**
     * encode the Object o and add encoded parts into {@link List}out
     * @param context context
     * @param o Object likely to be {@link com.jay.dove.transport.command.RemotingCommand}
     * @param out {@link List<Object>} output objects like {@link io.netty.buffer.ByteBuf} {@link io.netty.channel.FileRegion}
     */
    void encode(ChannelHandlerContext context, Object o, List<Object> out);
}
