package com.jay.dove.transport.connection;

import com.jay.dove.config.Configs;
import com.jay.dove.transport.Url;
import io.netty.channel.*;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;
import java.net.SocketAddress;

/**
 * <p>
 *  Connection Event handler.
 *  Handles channel inactive、exception、close.
 *  This handler will call re-connector
 * </p>
 *
 * @author Jay
 * @date 2022/01/10 10:47
 */
@Slf4j
@ChannelHandler.Sharable
public class ConnectEventHandler extends ChannelDuplexHandler {

    private Reconnector reconnector;
    private ConnectionManager connectionManager;


    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        super.connect(ctx, remoteAddress, localAddress, promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        Channel channel = ctx.channel();
        Connection connection = channel.attr(Connection.CONNECTION).get();
        if(connection != null){
            connection.onClose();
        }
        super.close(ctx, promise);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        // get connection bind to this channel
        Attribute<Connection> attr = ctx.channel().attr(Connection.CONNECTION);
        if(attr != null){
            Connection connection = attr.get();
            if(connection != null){
                // remove inactive connection
                connectionManager.remove(connection);
                // fires CLOSE Event
                userEventTriggered(ctx, ConnectEvent.CLOSE);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.userEventTriggered(ctx, ConnectEvent.EXCEPTION);
        ctx.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof ConnectEvent){
            ConnectEvent event = (ConnectEvent)evt;
            Channel channel = ctx.channel();
            if(channel == null){
                return;
            }
            Attribute<Connection> attr = ctx.channel().attr(Connection.CONNECTION);
            Connection connection = attr.get();
            if(connection == null){
                return;
            }
            switch(event){
                case CONNECT: onEvent(connection, event);break;
                case CONNECT_FAIL:
                case EXCEPTION:
                case CLOSE: submitReconnectTask(connection.getUrl());
                            log.warn("connection closed: {}", channel.remoteAddress());
                            onEvent(connection, event);
                            break;
                default:break;
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    /**
     * submit a re-connect task
     * @param address Target address
     */
    private void submitReconnectTask(Url url){
        // check config
        if(Configs.enableReconnect() && reconnector != null){
            // do reconnect
            reconnector.reconnect(url);
        }
    }

    private void onEvent(Connection connection, ConnectEvent event){

    }

    public Reconnector getReconnector() {
        return reconnector;
    }

    public void setReconnector(Reconnector reconnector) {
        this.reconnector = reconnector;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
}
