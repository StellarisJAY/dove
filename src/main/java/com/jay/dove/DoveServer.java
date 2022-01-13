package com.jay.dove;

import com.jay.dove.common.AbstractLifeCycle;
import com.jay.dove.config.Configs;
import com.jay.dove.transport.HeartBeatHandler;
import com.jay.dove.transport.Url;
import com.jay.dove.transport.codec.Codec;
import com.jay.dove.transport.command.CommandChannelHandler;
import com.jay.dove.transport.command.DefaultResponseHandler;
import com.jay.dove.transport.connection.ConnectEventHandler;
import com.jay.dove.transport.connection.Connection;
import com.jay.dove.transport.connection.ConnectionManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  Dove Server.
 * </p>
 *
 * @author Jay
 * @date 2022/01/13 10:49
 */
@Slf4j
public class DoveServer extends AbstractLifeCycle {
    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private final ServerBootstrap bootstrap;
    /**
     * server codec
     */
    private final Codec codec;
    /**
     * server side connection manager
     */
    private ConnectionManager connectionManager;
    /**
     * connect event handler
     */
    private ConnectEventHandler connectEventHandler;

    private final int port;

    public DoveServer(Codec codec, int port) {
        this.bootstrap = new ServerBootstrap();
        this.codec = codec;
        this.port = port;
    }

    public void doInit(){
        this.boss = new NioEventLoopGroup(1);
        this.worker = new NioEventLoopGroup();

        if(Configs.serverManageConnection()){
            this.connectionManager = new ConnectionManager();
            this.connectEventHandler = new ConnectEventHandler();
            this.connectEventHandler.setConnectionManager(connectionManager);
        }else{
            this.connectEventHandler = new ConnectEventHandler();
        }
        // group and channel type
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class);

        // options here


        // init channel
        bootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                // protocol encoder and decoder
                pipeline.addLast("decoder", codec.newDecoder());
                pipeline.addLast("encoder", codec.newEncoder());

                // connect event handler
                pipeline.addLast("connect-event-handler", DoveServer.this.connectEventHandler);

                // add heart-beat handlers
                if(Configs.tcpIdleState()){
                    pipeline.addLast("idle-state-handler", new IdleStateHandler(Configs.tcpIdleTime(), Configs.tcpIdleTime(), 0, TimeUnit.MILLISECONDS));
                    pipeline.addLast("heart-beat-handler", new HeartBeatHandler());
                }
                // invoke future response handler
                pipeline.addLast("response-handler", new DefaultResponseHandler());
                // command handler
                pipeline.addLast("command-handler", new CommandChannelHandler());

                // create connection instance and bind it with channel.
                createConnection(channel);
            }
        });
    }

    private void createConnection(Channel channel){
        // parse url from SocketAddress
        Url url = Url.fromAddress((InetSocketAddress) channel.remoteAddress());
        if(Configs.serverManageConnection()){
            // add connection to connection Manager
            this.connectionManager.add(new Connection(channel, url));
        }else{
            // bind the connection instance with channel
            new Connection(channel, url);
        }
    }

    @Override
    public void startup() {
        super.startup();
        long start = System.currentTimeMillis();
        // init server
        doInit();
        try{
            // bind port and sync start
            ChannelFuture future = bootstrap.bind(port).sync();
            if(future.isSuccess()){
                log.info("server started, time used : {}ms", (System.currentTimeMillis() - start));
            }
        }catch (Exception e){
            log.error("server start failed, ", e);
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        this.boss.shutdownGracefully();
        this.worker.shutdownGracefully();
    }
}
