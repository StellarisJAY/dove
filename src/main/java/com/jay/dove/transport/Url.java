package com.jay.dove.transport;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

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
    private int protocol;
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

    public static Url parseString(String originalUrl){
        Url url = new Url(originalUrl);
        int queryStart = originalUrl.indexOf(QUERY);
        // parse ip & port
        String address = originalUrl.substring(0, queryStart);
        url.parseAddress(address);

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
        this.protocol = Integer.parseInt(properties.get("protocol"));
        this.expectedConnectionCount = Integer.parseInt(properties.get("conn"));
    }

    private void parsePoolKey(){
        this.poolKey = ip + ":" + port + protocol;
    }
}
