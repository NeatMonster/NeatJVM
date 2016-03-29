package fr.neatmonster.neatjvm;

import java.nio.ByteBuffer;

public class MemoryPool {
    private final ByteBuffer heap;
    private int              heapTop;

    public MemoryPool(final int size) {
        heap = ByteBuffer.allocate(size);
    }

    public int allocate(final int size) {
        if (heapTop + size >= heap.capacity()) {
            // TODO Throw OutOfMemoryError
            System.err.println("OutOfMemoryError");
            System.exit(0);
        }
        heapTop += size;
        return heapTop - size;
    }

    public byte get(final int addr) {
        return heap.get(addr);
    }

    public void get(final int addr, final byte[] value) {
        heap.position(addr);
        heap.get(value);
    }

    public void get(final int addr, final byte[] dst, final int offset, final int length) {
        heap.position(addr);
        heap.get(dst, offset, length);
    }

    public int getInt(final int addr) {
        return heap.getInt(addr);
    }

    public long getLong(final int addr) {
        return heap.getLong(addr);
    }

    public float getFloat(final int addr) {
        return heap.getFloat(addr);
    }

    public double getDouble(final int addr) {
        return heap.getDouble(addr);
    }

    public int getReference(final int addr) {
        return heap.getInt(addr);
    }

    public char getChar(final int addr) {
        return heap.getChar(addr);
    }

    public short getShort(final int addr) {
        return heap.getShort(addr);
    }

    public void put(final int addr, final byte value) {
        heap.put(addr, value);
    }

    public void put(final int addr, final byte[] value) {
        heap.position(addr);
        heap.put(value);
    }

    public void put(final int addr, final byte[] value, final int offset, final int length) {
        heap.position(addr);
        heap.put(value, offset, length);
    }

    public void putInt(final int addr, final int value) {
        heap.putInt(addr, value);
    }

    public void putLong(final int addr, final long value) {
        heap.putLong(addr, value);
    }

    public void putFloat(final int addr, final float value) {
        heap.putFloat(addr, value);
    }

    public void putDouble(final int addr, final double value) {
        heap.putDouble(addr, value);
    }

    public void putReference(final int addr, final int value) {
        heap.putInt(addr, value);
    }

    public void putChar(final int addr, final char value) {
        heap.putChar(addr, value);
    }

    public void putShort(final int addr, final short value) {
        heap.putShort(addr, value);
    }
}
