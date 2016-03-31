package fr.neatmonster.neatjvm.natives;

import fr.neatmonster.neatjvm.ClassData;
import fr.neatmonster.neatjvm.InstanceData;

public class java_lang_ClassLoader {

    public static void registerNatives(final ClassData instance) {
    }

    public static InstanceData defineClass0(final InstanceData instance, final InstanceData name, final InstanceData b,
            final int off, final int len, final InstanceData pd) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData defineClass1(final InstanceData instance, final InstanceData name, final InstanceData b,
            final int off, final int len, final InstanceData pd, final InstanceData source) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData defineClass2(final InstanceData instance, final InstanceData name, final InstanceData b,
            final int off, final int len, final InstanceData pd, final InstanceData source) {
        throw new UnsupportedOperationException();
    }

    public static void resolveClass0(final InstanceData instance, final InstanceData c) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData findBootstrapClass(final InstanceData instance, final InstanceData name) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData findLoadedClass0(final InstanceData instance, final InstanceData name) {
        throw new UnsupportedOperationException();
    }

    public static void load(final InstanceData instance, final InstanceData name, final boolean isBuiltin) {
        throw new UnsupportedOperationException();
    }

    public static long find(final InstanceData instance, final InstanceData name) {
        throw new UnsupportedOperationException();
    }

    public static void unload(final InstanceData instance, final InstanceData name, final boolean isBuiltin) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData findBuiltinLib(final ClassData instance, final InstanceData name) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData retrieveDirectives(final ClassData instance) {
        throw new UnsupportedOperationException();
    }
}
