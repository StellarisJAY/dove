package com.jay.dove.transport.callback;

import com.jay.dove.transport.command.RemotingCommand;

import java.util.concurrent.ExecutorService;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2022/01/07 19:55
 */
public interface InvokeCallback {

    void onComplete(RemotingCommand response);

    void exceptionCaught(Throwable cause);

    ExecutorService getExecutor();
}
