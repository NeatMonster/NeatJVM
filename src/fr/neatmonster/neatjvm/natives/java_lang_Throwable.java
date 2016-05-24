package fr.neatmonster.neatjvm.natives;

import fr.neatmonster.neatjvm.InstanceData;

public class java_lang_Throwable {

    public static InstanceData fillInStackTrace(final InstanceData instance, final int dummy) {
        return instance; // TODO: Implement stacktrace support
    }

    public static int getStackTraceDepth(InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getStackTraceElement(InstanceData instance, int arg0) {
        throw new UnsupportedOperationException();
    }
}
