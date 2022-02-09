package com.jay.dove.transport;

import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 *  Basic url in dove network
 *  The query strings of url are now connection options.
 * </p>
 *
 * @author Jay
 * @date 2022/01/10 15:44
 */
@Setter
@Getter
public class Url {
    private String originalUrl;

    private String ip;

    private int port;

    /**
     * protocol
     */
    private short protocol;
    /**
     * expected connection pool size
     */
    private int expectedConnectionCount;

    /**
     * connection pool id key
     */
    private String poolKey;

    private final Map<String, String> properties = new HashMap<>(16);

    public static final String QUERY = "?";
    public static final String EQUALS = "=";
    public static final String AND = "&";
    public static final String PORT_SEPARATOR = ":";
    public static final int DEFAULT_PORT = 9009;

    public Url(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public void addProperty(String name, String value){
        properties.putIfAbsent(name, value);
    }

    public static Url fromAddress(InetSocketAddress address){
        String host = address.getHostString();
        int port = address.getPort();
        return parseString(host + ":" + port);
    }

    public static Url parseString(String originalUrl){
        Url url = new Url(originalUrl);
        int queryStart = originalUrl.indexOf(QUERY);
        if(queryStart == -1){
            url.parseAddress(originalUrl);
        }else{
            // parse ip & port
            String address = originalUrl.substring(0, queryStart);
            url.parseAddress(address);
        }

        // parse query properties
        String[] queries = originalUrl.substring(queryStart + 1).split(AND);
        url.parseQueries(queries);
        url.parseArguments();
        url.parsePoolKey();
        return url;
    }

    private void parseAddress(String address){
        int portOffset = address.indexOf(PORT_SEPARATOR);
        // check if port is present
        if(portOffset == -1){
            this.ip = address;
            this.port = DEFAULT_PORT;
        }else{
            this.ip = address.substring(0, portOffset);
            this.port = Integer.parseInt(address.substring(portOffset + 1));
        }
    }

    private void parseQueries(String[] queries){
        for(String query : queries){
            int equalsOffset = query.indexOf(EQUALS);
            if(equalsOffset != -1){
                String name = query.substring(0, equalsOffset);
                String value = query.substring(equalsOffset + 1);
                this.addProperty(name, value);
            }
        }
    }

    private void parseArguments(){
        this.protocol = properties.get("protocol") == null ? 22 : Short.parseShort(properties.get("protocol"));
        this.expectedConnectionCount = properties.get("conn") == null ? 1 :  Integer.parseInt(properties.get("conn"));
    }

    private void parsePoolKey(){
        this.poolKey = ip + ":" + port + protocol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Url url = (Url) o;
        return port == url.port && Objects.equals(ip, url.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }
}
