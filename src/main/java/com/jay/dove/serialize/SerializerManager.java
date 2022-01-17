package com.jay.dove.serialize;

/**
 * <p>
 *  Serializer manager
 *  contains all serializers
 * </p>
 *
 * @author Jay
 * @date 2021/12/31 10:22
 */
public class SerializerManager {
    private static final Serializer[] SERIALIZERS = new Serializer[256];

    public static void registerSerializer(byte code, Serializer serializer){
        SERIALIZERS[code + 128] = serializer;
    }

    public static Serializer getSerializer(byte code){
        return SERIALIZERS[code + 128];
    }
}
