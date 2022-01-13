package com.jay.dove;

import com.jay.dove.transport.BaseRemoting;
import com.jay.dove.transport.Url;
import com.jay.dove.transport.callback.InvokeCallback;
import com.jay.dove.transport.callback.InvokeFuture;
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
    private final BaseRemoting baseRemoting = new BaseRemoting();

    public DoveClient(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void sendOneway(Url url, RemotingCommand command){
        Connection connection = connectionManager.getConnectionAndCreateIfAbsent(url);
        this.baseRemoting.sendOneway(connection, command);
    }

    public RemotingCommand sendSync(Url url, RemotingCommand command, InvokeCallback callback) throws InterruptedException {
        Connection connection = connectionManager.getConnectionAndCreateIfAbsent(url);
        return this.baseRemoting.sendSync(connection, command, callback);
    }

    public InvokeFuture sendFuture(Url url, RemotingCommand command, InvokeCallback callback){
        Connection connection = connectionManager.getConnectionAndCreateIfAbsent(url);
        return this.baseRemoting.sendFuture(connection, command, callback);
    }
}
