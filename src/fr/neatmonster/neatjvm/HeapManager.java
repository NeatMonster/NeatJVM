package fr.neatmonster.neatjvm;

import java.nio.ByteBuffer;

public class HeapManager {
    public ByteBuffer heap;
    public int        heapTop;

    public HeapManager(final int size) {
        heap = ByteBuffer.allocate(size);
    }

    public int allocate(final int size) {
        if (heapTop + size >= heap.capacity()) {
            System.err.println("OutOfMemoryError");
            System.exit(0);
        }
        heapTop += size;
        return heapTop - size;
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
}
