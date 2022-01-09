package com.jay.dove.transport.connection.strategy;

import com.jay.dove.transport.connection.Connection;
import com.jay.dove.transport.connection.ConnectionSelectStrategy;

import java.util.List;
import java.util.Random;

/**
 * <p>
 *  Select a connection randomly
 * </p>
 *
 * @author Jay
 * @date 2022/01/09 14:26
 */
public class RandomSelectStrategy implements ConnectionSelectStrategy {
    @Override
    public Connection select(List<Connection> connections) {
        // check if the candidate list is null or empty
        if(connections == null || connections.isEmpty()){
            return null;
        }
        // only 1 candidate
        if(connections.size() == 1){
            return connections.get(0);
        }
        // random select
        Random random = new Random(System.nanoTime());
        return connections.get(random.nextInt(connections.size()));
    }
}
