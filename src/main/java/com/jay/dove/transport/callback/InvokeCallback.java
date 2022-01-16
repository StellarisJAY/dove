package com.jay.dove.transport.callback;

import com.jay.dove.transport.command.RemotingCommand;

import java.util.concurrent.ExecutorService;

/**
 * <p>
 *  Invoke callback
 * </p>
 *
 * @author Jay
 * @date 2022/01/07 19:55
 */
public interface InvokeCallback {

    /**
     * called on invoke complete
     * @param response {@link RemotingCommand}
     */
    void onComplete(RemotingCommand response);

    /**
     * called on exception
     * @param cause {@link Throwable}
     */
    void exceptionCaught(Throwable cause);

    /**
     * get the callback executor
     * @return {@link ExecutorService}
     */
    ExecutorService getExecutor();
}
