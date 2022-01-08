package com.jay.dove.util;

import com.jay.dove.transport.connection.Connection;
import io.netty.channel.Channel;
import io.netty.util.Attribute;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2022/01/07 20:38
 */
public class ConnectionUtil {
    public static Connection getConnectionFromChannel(Channel channel){
        if(channel != null){
            Attribute<Connection> attr = channel.attr(Connection.CONNECTION);
            if(attr != null){
                return attr.get();
            }
        }
        return null;
    }
}
