package com.jay.dove.transport.connection;

import com.jay.dove.transport.Url;
import com.jay.dove.transport.callback.InvokeFuture;
import com.jay.dove.transport.command.RemotingCommand;
import com.jay.dove.transport.protocol.Protocol;
import com.jay.dove.transport.protocol.ProtocolCode;
import com.jay.dove.transport.protocol.ProtocolManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;
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
    /**
     * connection's channel
     */
    private final Channel channel;

    /**
     * future map.
     * This holds futures from async send
     */
    private final ConcurrentHashMap<Integer, InvokeFuture> invokeFutureMap = new ConcurrentHashMap<>(256);

    public static final AttributeKey<Connection> CONNECTION = AttributeKey.valueOf("connection");

    public static final AttributeKey<ProtocolCode> PROTOCOL = AttributeKey.valueOf("protocol");


    private final String poolKey;

    private final Url url;
    /**
     * Connection status
     */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public Connection (Channel channel, Url url){
        this.channel = channel;
        this.url = url;
        this.poolKey = url.getPoolKey();
        channel.attr(CONNECTION).set(this);
    }

    public Connection(Channel channel, ProtocolCode protocolCode, Url url) {
        this.channel = channel;
        this.poolKey = url.getPoolKey();
        this.url = url;
        // associate channel with this connection
        channel.attr(CONNECTION).set(this);
        channel.attr(PROTOCOL).set(protocolCode);
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

    /**
     * Connection closed callback
     */
    public void onClose(){
        Iterator<Map.Entry<Integer, InvokeFuture>> iterator = invokeFutureMap.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<Integer, InvokeFuture> entry = iterator.next();
            iterator.remove();
            InvokeFuture future = entry.getValue();
            if(future != null){
                ProtocolCode code = channel.attr(PROTOCOL).get();
                Protocol protocol = ProtocolManager.getProtocol(code);
                // put connection closed response
                future.putResponse(protocol.getCommandFactory().createExceptionResponse(entry.getKey(), "connection closed"));
            }
        }
        this.closed.set(true);
    }

    public void putResponse(RemotingCommand response){
        InvokeFuture invokeFuture = invokeFutureMap.get(response.getId());
        if(invokeFuture != null){
            invokeFuture.putResponse(response);
        }
    }

    public Channel getChannel(){
        return channel;
    }

    public void addInvokeFuture(int id, InvokeFuture future){
        invokeFutureMap.put(id, future);
    }

    public InvokeFuture removeInvokeFuture(Integer id){
        return invokeFutureMap.remove(id);
    }

    public String getPoolKey() {
        return poolKey;
    }

    public Url getUrl(){return url;}
}
