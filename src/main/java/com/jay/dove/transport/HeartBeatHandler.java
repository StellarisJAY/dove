package com.jay.dove.transport;

import com.jay.dove.transport.connection.Connection;
import com.jay.dove.transport.protocol.Protocol;
import com.jay.dove.transport.protocol.ProtocolCode;
import com.jay.dove.transport.protocol.ProtocolManager;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;

/**
 * <p>
 *  Heart Beat Event Handler
 * </p>
 *
 * @author Jay
 * @date 2022/01/11 13:29
 */
@ChannelHandler.Sharable
public class HeartBeatHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            // get protocol code
            Attribute<ProtocolCode> attr = ctx.channel().attr(Connection.PROTOCOL);
            if(attr != null){
                ProtocolCode protocolCode = attr.get();
                if(protocolCode != null){
                    // get protocol from protocol manager
                    Protocol protocol = ProtocolManager.getProtocol(protocolCode);
                    // call protocol's heart-beat trigger
                    HeartBeatTrigger heartBeatTrigger = protocol.getHeartBeatTrigger();
                    if(heartBeatTrigger != null){
                        heartBeatTrigger.heartBeatTriggered(ctx);
                    }
                }
            }
        }
    }
}
