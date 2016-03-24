package fr.neatmonster.neatjvm;

import java.nio.ByteBuffer;

public class HeapSpace {
    public VirtualMachine vm;
    public ByteBuffer     heap;
    public int            heapTop;

    public HeapSpace(final VirtualMachine vm, final int size) {
        this.vm = vm;
        heap = ByteBuffer.allocate(size);
    }

    public int allocate(final int size) {
        if (heapTop + size >= heap.capacity())
            return 0;
        heapTop += size;
        return heapTop - size;
    }

    public void put(final int addr, final byte b) {
        heap.put(addr, b);
    }

    public void put(final int addr, final byte[] bs) {
        heap.position(addr);
        heap.put(bs);
    }

    public byte get(final int addr) {
        return heap.get(addr);
    }

    public void get(final int addr, final byte[] bs) {
        heap.position(addr);
        heap.get(bs);
    }
}
