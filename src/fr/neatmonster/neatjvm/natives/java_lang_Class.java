package fr.neatmonster.neatjvm.natives;

import fr.neatmonster.neatjvm.ClassData;
import fr.neatmonster.neatjvm.InstanceData;

public class java_lang_Class {

    public static void registerNatives(final ClassData instance) {
    }

    public static InstanceData forName0(final ClassData instance, final InstanceData name, final boolean initialize,
            final InstanceData loader) {
        throw new UnsupportedOperationException();
    }

    public static boolean isInstance(final InstanceData instance, final InstanceData obj) {
        throw new UnsupportedOperationException();
    }

    public static boolean isAssignableFrom(final InstanceData instance, final InstanceData cls) {
        throw new UnsupportedOperationException();
    }

    public static boolean isInterface(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static boolean isArray(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static boolean isPrimitive(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getName0(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getClassLoader0(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getSuperclass(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getInterfaces0(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getComponentType(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static int getModifiers(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getSigners(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static void setSigners(final InstanceData instance, final InstanceData signers) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getEnclosingMethod0(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getDeclaringClass0(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getProtectionDomain0(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getPrimitiveClass(final ClassData instance, final InstanceData name) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getGenericSignature0(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getRawAnnotations(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getRawTypeAnnotations(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getConstantPool(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getDeclaredFields0(final InstanceData instance, final boolean publicOnly) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getDeclaredMethods0(final InstanceData instance, final boolean publicOnly) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getDeclaredConstructors0(final InstanceData instance, final boolean publicOnly) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getDeclaredClasses0(final InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static boolean desiredAssertionStatus0(final ClassData instance, final InstanceData clazz) {
        return false;
    }
}
