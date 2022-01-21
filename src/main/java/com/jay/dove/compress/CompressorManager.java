package com.jay.dove.compress;

/**
 * <p>
 *  This class Manages registered Compressors {@link Compressor}
 * </p>
 *
 * @author Jay
 * @date 2022/01/21 11:35
 */
public class CompressorManager {
    private static final Compressor[] COMPRESSORS = new Compressor[256];

    public static void registerCompressor(Compressor compressor){
        COMPRESSORS[compressor.getCode() + 128] = compressor;
    }

    public static Compressor getCompressor(byte code){
        return COMPRESSORS[code + 128];
    }
}
