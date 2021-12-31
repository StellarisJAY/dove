package com.jay.dove.transport.protocol;

import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *  Protocol manager.
 *  contains all custom protocols.
 * </p>
 *
 * @author Jay
 * @date 2021/12/31 14:15
 */
public class ProtocolManager {
    private static final ConcurrentHashMap<ProtocolCode, Protocol> PROTOCOLS = new ConcurrentHashMap<>(16);

    /**
     * register a custom protocol to ProtocolManager
     * @param protocolCode {@link ProtocolCode} code
     * @param protocol {@link Protocol} protocol
     */
    public static void registerProtocol(ProtocolCode protocolCode, Protocol protocol){
        PROTOCOLS.putIfAbsent(protocolCode, protocol);
    }

    /**
     * get a registered custom protocol from Manager
     * @param protocolCode {@link ProtocolCode} code
     * @return {@link Protocol}
     */
    public static Protocol getProtocol(ProtocolCode protocolCode){
        return PROTOCOLS.get(protocolCode);
    }
}
