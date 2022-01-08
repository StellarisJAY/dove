package com.jay.dove.transport.callback;

import com.jay.dove.transport.command.RemotingCommand;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2022/01/07 19:30
 */
public interface InvokeFuture {

    /**
     * wait for response
     * @return {@link RemotingCommand}
     */
    RemotingCommand awaitResponse() throws InterruptedException;

    /**
     * wait for response, checks for timeout
     * @param timeout timeout
     * @param timeUnit TimeUnit
     * @return {@link RemotingCommand}
     */
    RemotingCommand awaitResponse(long timeout, TimeUnit timeUnit) throws TimeoutException, InterruptedException;

    /**
     * put response, completes this future
     * @param response {@link RemotingCommand}
     */
    void putResponse(RemotingCommand response);

    /**
     * async execute callback
     */
    void executeCallback();
}
