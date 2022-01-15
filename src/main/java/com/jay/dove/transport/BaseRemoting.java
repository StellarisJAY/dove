package com.jay.dove.transport;

import com.jay.dove.transport.callback.DefaultInvokeFuture;
import com.jay.dove.transport.callback.InvokeCallback;
import com.jay.dove.transport.callback.InvokeFuture;
import com.jay.dove.transport.command.CommandFactory;
import com.jay.dove.transport.command.RemotingCommand;
import com.jay.dove.transport.connection.Connection;
import com.jay.dove.util.TimerHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.HashedWheelTimer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2022/01/13 14:18
 */
@Slf4j
public class BaseRemoting implements Remoting{

    private final CommandFactory commandFactory;

    public BaseRemoting(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    @Override
    public void sendOneway(Connection connection, RemotingCommand command) {
        // check if command already timeout, client fail-fast
        long timeout = command.getTimeoutMillis();
        if(System.currentTimeMillis() >= timeout){
            log.warn("oneway request timeout before sending, {}", command);
            return;
        }
        // get channel from connection
        Channel channel = connection.getChannel();
        // send
        channel.writeAndFlush(command).addListener((ChannelFutureListener)future->{
            if(!future.isSuccess()){
                log.warn("oneway request failed, command: {}, connection: {}", command, connection);
            }else{
                log.info("oneway request sent, command: {}", command);
            }
        });
    }

    @Override
    public RemotingCommand sendSync(Connection connection, RemotingCommand command, InvokeCallback callback) throws InterruptedException {
        InvokeFuture future = sendFuture(connection, command, callback);
        // await response sync
        return future.awaitResponse();
    }

    @Override
    public InvokeFuture sendFuture(Connection connection, RemotingCommand command, InvokeCallback callback) {
        int commandId = command.getId();
        // create invoke future
        DefaultInvokeFuture future = new DefaultInvokeFuture(callback);

        // check if command already timeout, client fail-fast
        long timeout = command.getTimeoutMillis();
        if(System.currentTimeMillis() >= timeout){
            throw new RuntimeException("request timeout before sending, command: " + command);
        }
        // submit a new timeout task
        HashedWheelTimer timer = TimerHolder.getTimer();
        timer.newTimeout(time->{
            // remove timeout future
            InvokeFuture timeoutFuture = connection.removeInvokeFuture(commandId);
            if(timeoutFuture != null){
                // put timeout response
                future.putResponse(commandFactory.createTimeoutResponse(commandId, "await response timeout, request id: " + commandId));
            }
        }, (timeout - System.currentTimeMillis()), TimeUnit.MILLISECONDS);

        // send request and listen send result
        connection.getChannel().writeAndFlush(command).addListener((ChannelFutureListener) listener->{
            if(!listener.isSuccess()){
                // failed to send
                log.warn("sync send failed, command: {}, target: {}", command, connection);
                // put exception response
                future.putResponse(commandFactory.createExceptionResponse(commandId, "failed to send request to target address"));
            }else{
                // send success, save future
                connection.addInvokeFuture(commandId, future);
            }
        });
        return future;
    }

    @Override
    public void sendAsync(Connection connection, RemotingCommand command, InvokeCallback callback) {

    }
}