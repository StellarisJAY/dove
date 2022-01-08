package com.jay.dove.transport;

import com.jay.dove.transport.callback.InvokeCallback;
import com.jay.dove.transport.callback.InvokeFuture;
import com.jay.dove.transport.command.RemotingCommand;

import java.net.InetSocketAddress;

/**
 * <p>
 *  This interface provides 4 basic remoting methods.
 *  One-way: one-way request, won't receive any response.
 *  Sync: sync request, block sender thread until response received.
 *  Future Async: async request, this will return a future.
 *  Callback Async: async with callback. Callback method will be called after receiving response.
 * </p>
 *
 * @author Jay
 * @date 2022/01/08 15:33
 */
public interface BaseRemoting {

    /**
     * send a one-way remoting command
     * @param address {@link InetSocketAddress}
     * @param command {@link RemotingCommand}
     */
    void sendOneway(InetSocketAddress address, RemotingCommand command);

    /**
     * send synchrony, this method will block sender thread
     * @param address {@link InetSocketAddress}
     * @param command {@link RemotingCommand}
     * @return {@link RemotingCommand} the response command
     */
    RemotingCommand sendSync(InetSocketAddress address, RemotingCommand command);

    /**
     * send an async command with future
     * @param address {@link InetSocketAddress}
     * @param command {@link RemotingCommand}
     * @return {@link InvokeFuture} the future contains response
     */
    InvokeFuture sendFuture(InetSocketAddress address, RemotingCommand command);

    /**
     * send a fully async command with callback
     * @param address {@link InetSocketAddress}
     * @param command {@link RemotingCommand}
     * @param callback {@link InvokeCallback} callback
     */
    void sendAsync(InetSocketAddress address, RemotingCommand command, InvokeCallback callback);


}
