package fr.neatmonster.neatjvm.natives;

import fr.neatmonster.neatjvm.ClassData;
import fr.neatmonster.neatjvm.InstanceData;

public class java_lang_Runtime {

    public static int availableProcessors(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static long freeMemory(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static long totalMemory(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static long maxMemory(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static void gc(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static void runFinalization0(final ClassData instance) {
        throw new UnsupportedOperationException();
    }

    public static void traceInstructions(final InstanceData instance, final boolean on) {
        throw new UnsupportedOperationException();
    }

    public static void traceMethodCalls(final InstanceData instance, final boolean on) {
        throw new UnsupportedOperationException();
    }
}
