package com.jay.dove.transport.command;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * <p>
 *  Command Processor Interface
 * </p>
 *
 * @author Jay
 * @date 2022/01/08 11:05
 */
public interface Processor {
    /**
     * process a command
     * @param context {@link ChannelHandlerContext}
     * @param msg {@link RemotingCommand}
     */
    void process(ChannelHandlerContext context, Object msg);

    /**
     * send a response
     * @param context {@link ChannelHandlerContext}
     * @param response {@link RemotingCommand}
     */
    void sendResponse(ChannelHandlerContext context, RemotingCommand response);
}
