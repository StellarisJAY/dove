package com.jay.dove.transport.connection;

import com.jay.dove.config.DoveConfigs;
import com.jay.dove.transport.HeartBeatHandler;
import com.jay.dove.transport.Url;
import com.jay.dove.transport.codec.Codec;
import com.jay.dove.transport.command.DefaultResponseHandler;
import com.jay.dove.transport.protocol.ProtocolCode;
import com.jay.dove.util.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.net.ConnectException;
import java.security.NoSuchAlgorithmException;

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
@Slf4j
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
     * connect event handler
     */
    private final ChannelHandler connectEventHandler;

    /**
     * protocol code of this connection factory
     */
    private final ProtocolCode protocolCode;

    /**
     * SSL Context
     */
    private SSLContext sslContext = null;

    private Bootstrap bootstrap;
    private final EventLoopGroup worker = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() + 1,
            new NamedThreadFactory("dove-client-worker", true));

    public AbstractConnectionFactory(Codec codec, ProtocolCode protocolCode, ChannelHandler connectEventHandler) {
        this.codec = codec;
        this.protocolCode = protocolCode;
        this.heartBeatHandler = new HeartBeatHandler();
        this.connectEventHandler = connectEventHandler;
        if(DoveConfigs.enableSsl()){
            // create ssl context here
        }
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
        bootstrap.option(ChannelOption.TCP_NODELAY, DoveConfigs.tcpNoDelay());

        // register handlers
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel channel) {
                ChannelPipeline pipeline = channel.pipeline();
                // SSL/TLS handlers
                if(DoveConfigs.enableSsl() && sslContext != null){
                    // add SSL Handler here
                }
                // codec decoder and encoder
                pipeline.addLast("decoder", codec.newDecoder());
                pipeline.addLast("encoder", codec.newEncoder());
                // connect event handler
                pipeline.addLast("connect-event-handler", connectEventHandler);

                // heart-beat handler
                if(DoveConfigs.tcpIdleState()){
                    pipeline.addLast("idle-state-handler", new IdleStateHandler(DoveConfigs.tcpIdleTime(), DoveConfigs.tcpIdleTime(), 0));
                    pipeline.addLast("heart-beat-handler", heartBeatHandler);
                }
                // add response handler, handles invoke future
                pipeline.addLast("dove-response-handler", new DefaultResponseHandler());

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
        return new Connection(channel, protocolCode, url);
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

    @Override
    public void shutdown(){
        this.worker.shutdownGracefully();
    }
}
