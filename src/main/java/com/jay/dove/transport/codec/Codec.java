package com.jay.dove.transport.codec;

import io.netty.channel.ChannelHandler;

/**
 * <p>
 *  An abstract codec
 * </p>
 *
 * @author Jay
 * @date 2021/12/31 14:55
 */
public interface Codec {

    /**
     * get a new decoder for this codec
     * @return Decoder
     */
    ChannelHandler newDecoder();

    /**
     * get a new encoder for this codec
     * @return Encoder
     */
    ChannelHandler newEncoder();

}
