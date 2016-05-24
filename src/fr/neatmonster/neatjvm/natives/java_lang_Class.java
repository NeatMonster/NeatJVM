package fr.neatmonster.neatjvm.natives;

import java.util.ArrayList;
import java.util.List;

import fr.neatmonster.neatjvm.*;
import fr.neatmonster.neatjvm.ClassFile.ArrayClassFile;
import fr.neatmonster.neatjvm.ClassLoader;
import fr.neatmonster.neatjvm.InstanceData.ArrayInstanceData;
import fr.neatmonster.neatjvm.format.FieldInfo;
import fr.neatmonster.neatjvm.format.MethodInfo;
import fr.neatmonster.neatjvm.format.Modifier;

public class java_lang_Class {

    public static boolean desiredAssertionStatus0(final ClassData instance, final InstanceData clazz) {
        return false;
    }

    public static InstanceData forName0(ClassData instance, String name, boolean initialize, InstanceData classLoader) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getComponentType(InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getConstantPool(InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getDeclaredClasses0(InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getDeclaredConstructors0(InstanceData instance, boolean publicOnly) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getDeclaredFields0(final InstanceData instance, final boolean publicOnly) {
        final ClassLoader loader = VirtualMachine.getClassLoader();
        final MemoryPool heapSpace = VirtualMachine.getHeapSpace();
        final InstancePool instancePool = VirtualMachine.getInstancePool();

        final ClassFile classFile = instancePool.getClassFile(instance);
        final ClassFile fieldClass = loader.loadClass("java.lang.reflect.Field");
        final MethodInfo fieldInit = fieldClass.getMethod("<init>", "*");

        final List<InstanceData> fieldInstances = new ArrayList<>();
        for (final FieldInfo field : classFile.getDeclaredFields()) {
            if (publicOnly && !Modifier.PUBLIC.eval(field.getModifiers()))
                continue;
            final InstanceData fieldInstance = fieldClass.newInstance();
            final InstanceData declaringClass = instancePool.addClassFile(classFile);
            final InstanceData name = instancePool.addString(field.getName());
            final InstanceData type = field.getType().getJavaClass();
            final int modifiers = field.getModifiers();
            fieldInit.invoke(fieldInstance, declaringClass, name, type, modifiers, 0, null, null);
            fieldInstances.add(fieldInstance);
        }

        final ArrayClassFile fieldsClass = (ArrayClassFile) loader.loadClass("[Ljava.lang.reflect.Field;");
        final ArrayInstanceData fields = fieldsClass.newInstance(fieldInstances.size());
        for (int index = 0; index < fieldInstances.size(); ++index)
            heapSpace.putReference(fields.getAddress() + index * 4, fieldInstances.get(index).getReference());
        return fields;
    }

    public static InstanceData getDeclaredMethods0(InstanceData instance, boolean publicOnly) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getDeclaringClass0(InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getEnclosingMethod0(InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getGenericSignature0(InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getInterfaces0(InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static int getModifiers(final InstanceData instance) {
        return getClassFile(instance).getModifiers();
    }

    public static String getName0(final InstanceData instance) {
        return getClassFile(instance).getName();
    }

    public static InstanceData getPrimitiveClass(final ClassData instance, final String name) {
        final ClassLoader classLoader = VirtualMachine.getClassLoader();
        switch (name) {
            case "boolean":
                return getJavaClass(classLoader.loadClass("Z"));
            case "char":
                return getJavaClass(classLoader.loadClass("C"));
            case "float":
                return getJavaClass(classLoader.loadClass("F"));
            case "double":
                return getJavaClass(classLoader.loadClass("D"));
            case "byte":
                return getJavaClass(classLoader.loadClass("B"));
            case "short":
                return getJavaClass(classLoader.loadClass("S"));
            case "int":
                return getJavaClass(classLoader.loadClass("I"));
            case "long":
                return getJavaClass(classLoader.loadClass("J"));
        }
        return null;
    }

    public static InstanceData getProtectionDomain0(InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getRawAnnotations(InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getRawTypeAnnotations(InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getSigners(InstanceData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getSuperclass(final InstanceData instance) {
        return getJavaClass(getClassFile(instance).getSuperclass());
    }

    public static boolean isArray(final InstanceData instance) {
        return getClassFile(instance).isArray();
    }

    public static boolean isAssignableFrom(final InstanceData instance, final InstanceData cls) {
        return getClassFile(cls).isInstance(getClassFile(instance));
    }

    public static boolean isInstance(final InstanceData instance, final InstanceData obj) {
        return obj.getClassFile().isInstance(getClassFile(instance));
    }

    public static boolean isInterface(final InstanceData instance) {
        return getClassFile(instance).isInterface();
    }

    public static boolean isPrimitive(final InstanceData instance) {
        return getClassFile(instance).isPrimitive();
    }

    public static void setSigners(InstanceData instance, InstanceData signers) {
        throw new UnsupportedOperationException();
    }

    private static ClassFile getClassFile(final InstanceData instance) {
        return VirtualMachine.getInstancePool().getClassFile(instance);
    }

    private static InstanceData getJavaClass(final ClassFile classFile) {
        return VirtualMachine.getInstancePool().addClassFile(classFile);
    }
}
