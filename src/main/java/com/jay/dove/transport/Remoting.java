package com.jay.dove.transport;

import com.jay.dove.transport.callback.InvokeCallback;
import com.jay.dove.transport.callback.InvokeFuture;
import com.jay.dove.transport.command.RemotingCommand;
import com.jay.dove.transport.connection.Connection;

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
public interface Remoting {

    /**
     * send a one-way remoting command
     * @param connection {@link Connection}
     * @param command {@link RemotingCommand}
     */
    void sendOneway(Connection connection, RemotingCommand command);

    /**
     * send synchrony, this method will block sender thread
     * @param connection {@link Connection}
     * @param command {@link RemotingCommand}
     * @param callback {@link InvokeCallback} callback
     * @return {@link RemotingCommand} the response command
     */
    RemotingCommand sendSync(Connection connection, RemotingCommand command, InvokeCallback callback) throws InterruptedException;

    /**
     * send an async command with future
     * @param connection {@link Connection}
     * @param command {@link RemotingCommand}
     * @param callback {@link InvokeCallback} callback
     * @return {@link InvokeFuture} the future contains response
     */
    InvokeFuture sendFuture(Connection connection, RemotingCommand command, InvokeCallback callback);

    /**
     * send a fully async command with callback
     * @param connection {@link Connection}
     * @param command {@link RemotingCommand}
     * @param callback {@link InvokeCallback} callback
     */
    void sendAsync(Connection connection, RemotingCommand command, InvokeCallback callback);


}
