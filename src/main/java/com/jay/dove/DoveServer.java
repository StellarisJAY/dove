package com.jay.dove;

import com.jay.dove.common.AbstractLifeCycle;
import com.jay.dove.config.Configs;
import com.jay.dove.transport.BaseRemoting;
import com.jay.dove.transport.HeartBeatHandler;
import com.jay.dove.transport.Url;
import com.jay.dove.transport.callback.InvokeCallback;
import com.jay.dove.transport.callback.InvokeFuture;
import com.jay.dove.transport.codec.Codec;
import com.jay.dove.transport.command.CommandChannelHandler;
import com.jay.dove.transport.command.CommandFactory;
import com.jay.dove.transport.command.DefaultResponseHandler;
import com.jay.dove.transport.command.RemotingCommand;
import com.jay.dove.transport.connection.ConnectEventHandler;
import com.jay.dove.transport.connection.Connection;
import com.jay.dove.transport.connection.ConnectionManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.Attribute;
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

    /**
     * server port
     */
    private final int port;

    /**
     * basic remoting methods
     */
    private final BaseRemoting baseRemoting;

    public DoveServer(Codec codec, int port, CommandFactory commandFactory) {
        this.bootstrap = new ServerBootstrap();
        this.codec = codec;
        this.port = port;
        this.baseRemoting = new BaseRemoting(commandFactory);
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
            protected void initChannel(NioSocketChannel channel) {
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

    /**
     * send a one-way request to target url.
     * This method requires the server connection manager enabled.
     * @param url {@link Url}
     * @param command {@link RemotingCommand}
     */
    public void sendOneway(Url url, RemotingCommand command){
        if(Configs.serverManageConnection()){
            Connection connection = connectionManager.getConnection(url);
            baseRemoting.sendOneway(connection, command);
        }else{
            throw new RuntimeException("server connection manager not available, can't send to target url");
        }
    }

    /**
     * send a one-way request to target channel
     * @param channel {@link Channel}
     * @param command {@link RemotingCommand}
     */
    public void sendOneway(Channel channel, RemotingCommand command){
        Attribute<Connection> attr = channel.attr(Connection.CONNECTION);
        Connection connection;
        if(attr != null && (connection = attr.get()) != null){
            baseRemoting.sendOneway(connection, command);
        }else{
            channel.writeAndFlush(command).addListener((ChannelFutureListener)listener->{
                if(!listener.isSuccess()){
                    log.error("send oneway failed, command: {}, target: {}", command, channel.remoteAddress());
                }
            });
        }
    }

    /**
     * send a request to target channel and await response synchronously.
     * @param channel {@link Channel}
     * @param command {@link RemotingCommand}
     * @param callback {@link InvokeCallback}
     * @return {@link RemotingCommand}
     * @throws InterruptedException await response interrupted
     */
    public RemotingCommand sendSync(Channel channel, RemotingCommand command, InvokeCallback callback) throws InterruptedException {
        InvokeFuture future = sendFuture(channel, command, callback);
        return future.awaitResponse();
    }

    /**
     * send a request to target channel asynchronously with future
     * @param channel {@link Channel}
     * @param command {@link RemotingCommand}
     * @param callback {@link InvokeCallback}
     * @return {@link InvokeCallback}
     */
    public InvokeFuture sendFuture(Channel channel, RemotingCommand command, InvokeCallback callback){
        Attribute<Connection> attr = channel.attr(Connection.CONNECTION);
        Connection connection;
        if(attr != null && (connection = attr.get()) != null){
            return baseRemoting.sendFuture(connection, command, callback);
        }else{
            throw new RuntimeException("This channel is not bind to a Connection instance. Can't send command with future.");
        }
    }

    /**
     * send request asynchronously with callback
     * @param channel {@link Channel}
     * @param command {@link RemotingCommand}
     * @param callback {@link InvokeCallback}
     */
    public void sendAsync(Channel channel, RemotingCommand command, InvokeCallback callback){
        Attribute<Connection> attr = channel.attr(Connection.CONNECTION);
        Connection connection;
        if(attr != null && (connection = attr.get()) != null){
            baseRemoting.sendAsync(connection, command, callback);
        }else{
            throw new RuntimeException("This channel is not bind to a Connection instance. Can't send command asynchronously.");
        }
    }
}
