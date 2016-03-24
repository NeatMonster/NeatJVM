package fr.neatmonster.neatjvm;

public class HeapSpace {
    public VirtualMachine vm;

    public HeapSpace(final VirtualMachine vm, final int size) {
        this.vm = vm;
    }
}
