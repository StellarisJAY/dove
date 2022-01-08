package com.jay.dove.transport;

import com.jay.dove.common.LifeCycle;
import com.jay.dove.transport.codec.ProtocolCodeBasedDecoder;
import com.jay.dove.transport.codec.ProtocolCodeBasedEncoder;
import com.jay.dove.transport.command.CommandChannelHandler;
import com.jay.dove.transport.protocol.ProtocolCode;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2022/01/07 15:49
 */
public class BaseServer implements LifeCycle {

    /**
     * boss group
     */
    private final EventLoopGroup boss = new NioEventLoopGroup(1);

    /**
     * worker group
     */
    private final EventLoopGroup worker = new NioEventLoopGroup();

    /**
     * user defined custom handlers
     */
    private final Map<String, ChannelHandler> handlers = new HashMap<>(16);

    /**
     * server bootstrap
     */
    private final ServerBootstrap bootstrap;

    private final int port;

    public BaseServer(int port) {
        this.port = port;
        this.bootstrap = new ServerBootstrap();
    }

    @Override
    public void init() {
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        // protocol decoder
                        pipeline.addLast(new ProtocolCodeBasedDecoder());
                        //
                        pipeline.addLast(new CommandChannelHandler());
                        // protocol encoder
                        pipeline.addLast(new ProtocolCodeBasedEncoder(ProtocolCode.fromBytes("rpc".getBytes(StandardCharsets.UTF_8))));
                        // user defined custom handlers
                        for (Map.Entry<String, ChannelHandler> entry : handlers.entrySet()) {
                            pipeline.addLast(entry.getKey(), entry.getValue());
                        }
                    }
                });

    }

    @Override
    public void start() {
        try{
            init();
            ChannelFuture future = bootstrap.bind(port).sync();
        }catch (Throwable e){
            exceptionCaught(e);
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }



    @Override
    public void shutdown() {

    }

    @Override
    public void exceptionCaught(Throwable e) {

    }

    public void addHandler(String name, ChannelHandler handler){
        handlers.putIfAbsent(name, handler);
    }
}
