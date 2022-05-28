package com.jay.dove.transport.command;

import com.jay.dove.transport.connection.Connection;
import com.jay.dove.transport.protocol.Protocol;
import com.jay.dove.transport.protocol.ProtocolCode;
import com.jay.dove.transport.protocol.ProtocolManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;

/**
 * <p>
 *  Command Channel Handler。
 *  This handler gets this channel's protocol and protocol's commandHandler.
 * </p>
 *
 * @author Jay
 * @date 2022/01/08 14:53
 */
public class CommandChannelHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // get the protocol of this channel
        Attribute<ProtocolCode> attr = ctx.channel().attr(Connection.PROTOCOL);
        if(attr.get() != null){
            Protocol protocol = ProtocolManager.getProtocol(attr.get());
            // call protocol's command handler
            protocol.getCommandHandler().handleCommand(ctx, msg);
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // get the protocol of this channel
        Attribute<ProtocolCode> attr = ctx.channel().attr(Connection.PROTOCOL);
        if(attr.get() != null){
            Protocol protocol = ProtocolManager.getProtocol(attr.get());
            // call protocol's command handler
            protocol.getCommandHandler().channelInactive(ctx);
        }
    }
}
