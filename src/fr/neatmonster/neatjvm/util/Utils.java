package fr.neatmonster.neatjvm.util;

import java.nio.ByteBuffer;

public class Utils {
    public static byte[] intToBytes(int value) {
        return ByteBuffer.allocate(Integer.SIZE / Byte.SIZE).putInt(value).array();
    }
    
    public static byte[] longToBytes(long value) {
        return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(value).array();
    }
}
