package com.jay.dove.transport.connection;

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
            // check if connection manager present, server-side may be absent
            if(connection != null){
                connectionManager.removeConnectionPool(connection.getPoolKey());
                connection.onClose();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(log.isDebugEnabled()){
            log.debug("channel error: ", cause);
        }
        this.userEventTriggered(ctx, ConnectEvent.EXCEPTION);
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
                case CONNECT:
                case EXCEPTION:
                default:break;
            }
        }
        super.userEventTriggered(ctx, evt);
    }


    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
}
