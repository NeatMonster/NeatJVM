package fr.neatmonster.neatjvm;

import java.util.HashMap;
import java.util.Map;

public class InstancePool {
    private final Map<Integer, InstanceData> instances;
    private int                              nextReference;

    public InstancePool() {
        instances = new HashMap<>();
        nextReference = 1;
    }

    public int addInstance(final InstanceData instance) {
        final int reference = nextReference++;
        instances.put(reference, instance);
        return reference;
    }

    public InstanceData getInstance(final int reference) {
        return instances.get(reference);
    }
}
