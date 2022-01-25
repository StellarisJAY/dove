package com.jay.dove;

import com.jay.dove.transport.BaseRemoting;
import com.jay.dove.transport.Url;
import com.jay.dove.transport.callback.InvokeCallback;
import com.jay.dove.transport.callback.InvokeFuture;
import com.jay.dove.transport.command.CommandFactory;
import com.jay.dove.transport.command.RemotingCommand;
import com.jay.dove.transport.connection.Connection;
import com.jay.dove.transport.connection.ConnectionManager;

/**
 * <p>
 *  Dove basic client.
 *  Extends this class to create a custom protocol based client.
 * </p>
 *
 * @author Jay
 * @date 2022/01/09 14:15
 */
public class DoveClient {
    /**
     * client side connection manager
     */
    private final ConnectionManager connectionManager;
    private final CommandFactory commandFactory;
    private final BaseRemoting baseRemoting;

    public DoveClient(ConnectionManager connectionManager, CommandFactory commandFactory) {
        this.connectionManager = connectionManager;
        this.commandFactory = commandFactory;
        this.baseRemoting = new BaseRemoting(commandFactory);
    }

    /**
     * send a one-way request to target url
     * @param url {@link Url}
     * @param command {@link RemotingCommand}
     */
    public void sendOneway(Url url, RemotingCommand command){
        Connection connection = connectionManager.getConnectionAndCreateIfAbsent(url);
        this.baseRemoting.sendOneway(connection, command);
    }

    /**
     * send request and await response synchronously
     * @param url {@link Url}
     * @param command {@link RemotingCommand}
     * @param callback {@link InvokeCallback}
     * @return {@link RemotingCommand}
     * @throws InterruptedException await response interrupted
     */
    public RemotingCommand sendSync(Url url, RemotingCommand command, InvokeCallback callback) throws InterruptedException {
        Connection connection = connectionManager.getConnectionAndCreateIfAbsent(url);
        return this.baseRemoting.sendSync(connection, command, callback);
    }

    /**
     * send request asynchronously with future instance
     * @param url {@link Url}
     * @param command {@link RemotingCommand}
     * @param callback {@link InvokeCallback}
     * @return {@link InvokeFuture}
     */
    public InvokeFuture sendFuture(Url url, RemotingCommand command, InvokeCallback callback){
        Connection connection = connectionManager.getConnectionAndCreateIfAbsent(url);
        return this.baseRemoting.sendFuture(connection, command, callback);
    }

    /**
     * send request asynchronously with callback
     * @param url {@link Url}
     * @param command {@link RemotingCommand}
     * @param callback {@link InvokeCallback}
     */
    public void sendAsync(Url url, RemotingCommand command, InvokeCallback callback){
        Connection connection = connectionManager.getConnectionAndCreateIfAbsent(url);
        this.baseRemoting.sendAsync(connection, command, callback);
    }

    public CommandFactory getCommandFactory(){
        return commandFactory;
    }
}
