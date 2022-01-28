package com.jay.dove.transport.command;

import com.jay.dove.transport.callback.InvokeFuture;
import com.jay.dove.transport.connection.Connection;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  This processor put response into async invoke future
 * </p>
 *
 * @author Jay
 * @date 2022/01/13 10:39
 */
@Slf4j
public class DefaultResponseHandler extends ChannelInboundHandlerAdapter {
    @SuppressWarnings("rawtypes")
    @Override
    public void channelRead(ChannelHandlerContext context, Object msg) {
        if(msg instanceof RemotingCommand){
            processOne(context, msg);
        }
        else if(msg instanceof List){
            List list = (ArrayList)msg;
            for(Object obj : list){
                processOne(context, obj);
            }
        }
        context.fireChannelRead(msg);
    }

    private void processOne(ChannelHandlerContext context, Object msg){
        RemotingCommand cmd = (RemotingCommand) msg;
        // get connection instance
        Connection connection = context.channel().attr(Connection.CONNECTION).get();
        if(connection != null){
            // remove invoke future
            InvokeFuture invokeFuture = connection.removeInvokeFuture(cmd.getId());
            if(invokeFuture != null){
                // put response
                invokeFuture.putResponse(cmd);
                // execute callback
                try{
                    invokeFuture.executeCallback();
                }catch (Exception e){
                    log.error("callback execution error ", e);
                }
            }else{
                log.warn("missing invoke future for id: {}, conn: {}", cmd.getId(), connection);
            }
        }else{
            log.warn("missing connection instance");
        }
    }
}
