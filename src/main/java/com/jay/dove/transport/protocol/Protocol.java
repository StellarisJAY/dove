package com.jay.dove.transport.protocol;

/**
 * <p>
 *  Protocol interface.
 *  implement this class to create a custom protocol
 * </p>
 *
 * @author Jay
 * @date 2021/12/31 14:15
 */
public interface Protocol {
    /**
     * get the encoder for this protocol
     * @return {@link ProtocolEncoder}
     */
    ProtocolEncoder getEncoder();

    /**
     * get the decoder for this protocol
     * @return {@link ProtocolDecoder}
     */
    ProtocolDecoder getDecoder();


}
