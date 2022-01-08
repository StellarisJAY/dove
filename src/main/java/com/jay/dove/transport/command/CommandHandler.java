package com.jay.dove.transport.command;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ExecutorService;

/**
 * <p>
 *  Command handler interface.
 * </p>
 *
 * @author Jay
 * @date 2022/01/08 11:05
 */
public interface CommandHandler {

    /**
     * register a CommandCode's processor
     * @param cmd {@link CommandCode}
     * @param processor {@link Processor}
     */
    void registerProcessor(CommandCode cmd, Processor processor);

    /**
     * handle command, implement this method to handle a custom command
     * @param context context
     * @param msg Command msg, possibly a List
     */
    void handleCommand(ChannelHandlerContext context, Object msg);

    /**
     * get the default executor for this handler
     * @return {@link ExecutorService}
     */
    ExecutorService getDefaultExecutor();

    /**
     * set the default executor
     * @param executor {@link ExecutorService}
     */
    void registerDefaultExecutor(ExecutorService executor);
}
