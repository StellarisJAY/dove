package com.jay.dove.transport.connection;

import com.jay.dove.config.Configs;
import com.jay.dove.transport.HeartBeatHandler;
import com.jay.dove.transport.Url;
import com.jay.dove.transport.codec.Codec;
import com.jay.dove.transport.protocol.ProtocolCode;
import com.jay.dove.util.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.StringUtil;

import java.net.ConnectException;

/**
 * <p>
 *  Abstract implementation of ConnectionFactory.
 *  This provides default initialization and create methods.
 *  To create a new Protocol Connection Factory,
 *  Extends this class and provide protocolCodec {@link Codec}, main and heart-beat handler, protocolCode
 * </p>
 *
 * @author Jay
 * @date 2022/01/09 11:25
 */
public abstract class AbstractConnectionFactory implements ConnectionFactory{

    /**
     * Codec of this connection factory
     */
    private final Codec codec;

    /**
     * heart-beat handler
     */
    private final ChannelHandler heartBeatHandler;

    /**
     * protocol code of this connection factory
     */
    private final ProtocolCode protocolCode;

    private Bootstrap bootstrap;
    private final EventLoopGroup worker = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() + 1,
            new NamedThreadFactory("dove-client-worker", true));

    public AbstractConnectionFactory(Codec codec, ProtocolCode protocolCode, ChannelHandler heartBeatHandler) {
        this.codec = codec;
        this.protocolCode = protocolCode;
        this.heartBeatHandler = new HeartBeatHandler();
    }

    /**
     * init client bootstrap
     */
    @Override
    public void init(){
        bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class)
                .group(worker);

        // options
        bootstrap.option(ChannelOption.TCP_NODELAY, Configs.tcpNoDelay());

        // register handlers
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel channel) {
                ChannelPipeline pipeline = channel.pipeline();
                // codec decoder and encoder
                pipeline.addLast("decoder", codec.newDecoder());
                // connect event handler
                pipeline.addLast("connect-event-handler", new ConnectEventHandler());

                // heart-beat handler
                if(Configs.tcpIdleState()){
                    pipeline.addLast("idle-state-handler", new IdleStateHandler(Configs.tcpIdleTime(), Configs.tcpIdleTime(), 0));
                    pipeline.addLast("heart-beat-handler", heartBeatHandler);
                }
                pipeline.addLast("encoder", codec.newEncoder());
            }
        });
    }


    @Override
    public Connection create(Url url, int timeout) throws Exception {
        // check arguments
        if(StringUtil.isNullOrEmpty(url.getIp()) || url.getPort() <= 0){
            throw new IllegalArgumentException("invalid socket address");
        }
        if(timeout <= 0){
            throw new IllegalArgumentException("connect timeout must be positive");
        }
        Channel channel = doCreateConnection(url.getIp(), url.getPort(), timeout);
        return new Connection(channel, protocolCode, url.getPoolKey());
    }

    /**
     * establish connection and returns the target channel
     * @param ip ip
     * @param port port
     * @param timeout timeout ms
     * @return {@link Channel}
     * @throws Exception exceptions {@link ConnectException}
     */
    private Channel doCreateConnection(String ip, int port, int timeout) throws Exception{
        // set connect timeout
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout);
        ChannelFuture future = bootstrap.connect(ip, port);

        // wait for result
        future.awaitUninterruptibly();

        if(!future.isDone()){
            // connect timeout
            throw new ConnectException("connect timeout, target address: " + ip + ":" + port);
        }
        if(future.isCancelled()){
            // connect task cancelled
            throw new ConnectException("connect cancelled");
        }
        if(!future.isSuccess()){
            // error
            throw new ConnectException("connect error");
        }

        return future.channel();
    }
}
