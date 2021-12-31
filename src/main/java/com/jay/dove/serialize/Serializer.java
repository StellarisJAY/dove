package com.jay.dove.serialize;

/**
 * <p>
 *  Serializer interface
 *  implement this class to add a new serializer
 * </p>
 *
 * @author Jay
 * @date 2021/12/31 10:19
 */
public interface Serializer {
    /**
     * serialize
     * @param object object to serialize
     * @param clazz object class
     * @param <T> Type
     * @return byte[]
     */
    <T> byte[] serialize(T object, Class<T> clazz);

    /**
     * deserialize
     * @param serialized serialized object, byte[]
     * @param clazz target class
     * @param <T> Type
     * @return T
     */
    <T> T deserialize(byte[] serialized, Class<T> clazz);
}
