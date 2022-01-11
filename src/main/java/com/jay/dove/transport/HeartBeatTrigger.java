package com.jay.dove.transport;

import io.netty.channel.ChannelHandlerContext;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2022/01/11 13:28
 */
public interface HeartBeatTrigger {
    /**
     * heart beat triggered
     * @param context {@link ChannelHandlerContext}
     */
    void heartBeatTriggered(ChannelHandlerContext context);
}
