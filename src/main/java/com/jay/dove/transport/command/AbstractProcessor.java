package com.jay.dove.transport.command;

import io.netty.channel.ChannelHandlerContext;

/**
 * <p>
 *  Abstract processor.
 *  overrides the sendResponse method
 * </p>
 *
 * @author Jay
 * @date 2022/01/27 10:53
 */
public abstract class AbstractProcessor implements Processor {
    @Override
    public final void sendResponse(ChannelHandlerContext context, RemotingCommand response) {
        if(response != null && response.getTimeoutMillis() > System.currentTimeMillis()){
            context.channel().writeAndFlush(response);
        }
    }
}
