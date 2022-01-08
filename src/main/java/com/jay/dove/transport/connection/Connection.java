package com.jay.dove.transport.connection;

import com.jay.dove.transport.callback.InvokeFuture;
import com.jay.dove.transport.command.RemotingCommand;
import com.jay.dove.transport.protocol.Protocol;
import com.jay.dove.transport.protocol.ProtocolCode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2022/01/07 19:19
 */
@Slf4j
public class Connection {
    private final Channel channel;

    private final ConcurrentHashMap<Integer, InvokeFuture> invokeFutureMap = new ConcurrentHashMap<>(256);

    public static final AttributeKey<Connection> CONNECTION = AttributeKey.valueOf("connection");

    public static final AttributeKey<ProtocolCode> PROTOCOL = AttributeKey.valueOf("protocol");

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public Connection(Channel channel) {
        this.channel = channel;
        // associate channel with this connection
        channel.attr(CONNECTION).set(this);
    }

    public boolean isClosed(){
        return closed.get();
    }

    /**
     * close this connection
     */
    public void close(){
        // set status
        if(closed.compareAndSet(false, true)){
            // close channel
            if(channel != null){
                channel.close().addListener((ChannelFutureListener) future->{
                    log.info("connection closed, status {}, error: {}", future.isSuccess(), future.cause());
                });
            }
        }
    }

    public void putResponse(RemotingCommand response){
        InvokeFuture invokeFuture = invokeFutureMap.get(response.getId());
        if(invokeFuture != null){
            invokeFuture.putResponse(response);
        }
    }
}
