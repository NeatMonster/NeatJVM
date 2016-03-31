package fr.neatmonster.neatjvm.natives;

import fr.neatmonster.neatjvm.ClassData;
import fr.neatmonster.neatjvm.InstanceData;

public class java_lang_Thread {

    public static void registerNatives(final ClassData instance) {
    }

    public static InstanceData currentThread(final ClassData instance) {
        throw new UnsupportedOperationException();
    }

    public static void yield(final ClassData instance) {
        throw new UnsupportedOperationException();
    }

    public static void sleep(final ClassData instance, final long millis) {
        throw new UnsupportedOperationException();
    }

    public static void start0(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static boolean isInterrupted(final InstanceData instance, final boolean ClearInterrupted) {
        throw new UnsupportedOperationException();
    }

    public static boolean isAlive(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static int countStackFrames(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static boolean holdsLock(final ClassData instance, final InstanceData obj) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData dumpThreads(final ClassData instance, final InstanceData threads) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getThreads(final ClassData instance) {
        throw new UnsupportedOperationException();
    }

    public static void setPriority0(final InstanceData instance, final int newPriority) {
        throw new UnsupportedOperationException();
    }

    public static void stop0(final InstanceData instance, final InstanceData o) {
        throw new UnsupportedOperationException();
    }

    public static void suspend0(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static void resume0(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static void interrupt0(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static void setNativeName(final InstanceData instance, final InstanceData name) {
        throw new UnsupportedOperationException();
    }
}
