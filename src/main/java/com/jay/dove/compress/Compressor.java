package com.jay.dove.compress;

/**
 * <p>
 *  standard compressor interface.
 *  Implements this class to create a new compressor.
 * </p>
 *
 * @author Jay
 * @date 2022/01/21 11:35
 */
public interface Compressor {
    /**
     * compress the src byte array
     * @param src byte[]
     * @return byte[] compressed bytes
     */
    byte[] compress(byte[] src);

    /**
     * decompress the src byte array
     * @param src byte[]
     * @return byte[] decompressed bytes
     */
    byte[] decompress(byte[] src);

    /**
     * get the unique id code for this compressor
     * @return byte
     */
    byte getCode();
}
