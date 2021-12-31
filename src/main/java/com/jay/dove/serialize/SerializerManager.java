package com.jay.dove.serialize;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  Serializer manager
 *  contains all serializers =
 * </p>
 *
 * @author Jay
 * @date 2021/12/31 10:22
 */
public class SerializerManager {
    private static final Map<String, Serializer> SERIALIZER_MAP = new HashMap<>(16);

    public void registerSerializer(String name, Serializer serializer){
        SERIALIZER_MAP.putIfAbsent(name, serializer);
    }

    public Serializer getSerializer(String name){
        return SERIALIZER_MAP.get(name);
    }
}
