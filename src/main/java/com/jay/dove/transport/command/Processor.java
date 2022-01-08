package com.jay.dove.transport.command;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2022/01/08 11:05
 */
public interface Processor {
    void process(ChannelHandlerContext context, Object msg);
}
