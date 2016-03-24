package fr.neatmonster.neatjvm;

import java.util.HashMap;
import java.util.Map;

public class HandlePool {
    public VirtualMachine             vm;
    public Map<Integer, InstanceData> pool;
    public int                        nextReference = 1;

    public HandlePool(final VirtualMachine vm) {
        this.vm = vm;
        pool = new HashMap<>();
    }

    public int addInstance(final InstanceData instance) {
        final int reference = nextReference++;
        pool.put(reference, instance);
        return reference;
    }

    public InstanceData getInstance(final int reference) {
        return pool.get(reference);
    }
}
