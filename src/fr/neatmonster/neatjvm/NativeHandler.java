package fr.neatmonster.neatjvm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.neatjvm.format.FieldType;
import fr.neatmonster.neatjvm.format.FieldType.BaseType;
import fr.neatmonster.neatjvm.format.MethodInfo;
import fr.neatmonster.neatjvm.natives.java_lang_Class;
import fr.neatmonster.neatjvm.natives.java_lang_ClassLoader;
import fr.neatmonster.neatjvm.natives.java_lang_Compiler;
import fr.neatmonster.neatjvm.natives.java_lang_Double;
import fr.neatmonster.neatjvm.natives.java_lang_Float;
import fr.neatmonster.neatjvm.natives.java_lang_NativeLibrary;
import fr.neatmonster.neatjvm.natives.java_lang_Object;
import fr.neatmonster.neatjvm.natives.java_lang_Package;
import fr.neatmonster.neatjvm.natives.java_lang_ProcessEnvironment;
import fr.neatmonster.neatjvm.natives.java_lang_Runtime;
import fr.neatmonster.neatjvm.natives.java_lang_SecurityManager;
import fr.neatmonster.neatjvm.natives.java_lang_Shutdown;
import fr.neatmonster.neatjvm.natives.java_lang_StrictMath;
import fr.neatmonster.neatjvm.natives.java_lang_String;
import fr.neatmonster.neatjvm.natives.java_lang_System;
import fr.neatmonster.neatjvm.natives.java_lang_Thread;
import fr.neatmonster.neatjvm.natives.java_lang_Throwable;
import fr.neatmonster.neatjvm.natives.java_lang_UNIXProcess;
import fr.neatmonster.neatjvm.natives.java_lang_invoke_MethodHandle;
import fr.neatmonster.neatjvm.natives.java_lang_invoke_MethodHandleNatives;
import fr.neatmonster.neatjvm.natives.java_lang_reflect_Array;
import fr.neatmonster.neatjvm.natives.java_lang_reflect_Executable;
import fr.neatmonster.neatjvm.natives.java_lang_reflect_Field;
import fr.neatmonster.neatjvm.natives.java_lang_reflect_Proxy;

public class NativeHandler {
    private final Map<String, Class<?>> NATIVES;

    public NativeHandler() {
        NATIVES = new HashMap<>();
    }

    public void registerNatives() {
        NATIVES.put("java.lang.Class", java_lang_Class.class);
        NATIVES.put("java.lang.NativeLibrary", java_lang_NativeLibrary.class);
        NATIVES.put("java.lang.ClassLoader", java_lang_ClassLoader.class);
        NATIVES.put("java.lang.Compiler", java_lang_Compiler.class);
        NATIVES.put("java.lang.Double", java_lang_Double.class);
        NATIVES.put("java.lang.Float", java_lang_Float.class);
        NATIVES.put("java.lang.invoke.MethodHandle", java_lang_invoke_MethodHandle.class);
        NATIVES.put("java.lang.invoke.MethodHandleNatives", java_lang_invoke_MethodHandleNatives.class);
        NATIVES.put("java.lang.Object", java_lang_Object.class);
        NATIVES.put("java.lang.Package", java_lang_Package.class);
        NATIVES.put("java.lang.ProcessEnvironment", java_lang_ProcessEnvironment.class);
        NATIVES.put("java.lang.reflect.Array", java_lang_reflect_Array.class);
        NATIVES.put("java.lang.reflect.Executable", java_lang_reflect_Executable.class);
        NATIVES.put("java.lang.reflect.Field", java_lang_reflect_Field.class);
        NATIVES.put("java.lang.reflect.Proxy", java_lang_reflect_Proxy.class);
        NATIVES.put("java.lang.Runtime", java_lang_Runtime.class);
        NATIVES.put("java.lang.SecurityManager", java_lang_SecurityManager.class);
        NATIVES.put("java.lang.Shutdown", java_lang_Shutdown.class);
        NATIVES.put("java.lang.StrictMath", java_lang_StrictMath.class);
        NATIVES.put("java.lang.String", java_lang_String.class);
        NATIVES.put("java.lang.System", java_lang_System.class);
        NATIVES.put("java.lang.Thread", java_lang_Thread.class);
        NATIVES.put("java.lang.Throwable", java_lang_Throwable.class);
        NATIVES.put("java.lang.UNIXProcess", java_lang_UNIXProcess.class);
    }

    public int[] executeMethod(final StackFrame frame, final MethodInfo method, final InstanceData instance)
            throws InvocationTargetException {
        if (method.getName().equals("registerNatives"))
            return new int[0];

        final InstancePool instancePool = VirtualMachine.getInstancePool();

        final String className = method.getClassFile().getName();
        final Class<?> nativeClass = NATIVES.get(className);
        if (nativeClass == null)
            return null;

        final FieldType[] types = method.getParameterTypes();
        final Object[] params = new Object[types.length + 1];
        final Class<?>[] paramTypes = new Class<?>[types.length + 1];

        if (instance == null) {
            params[0] = method.getClassFile().getInstance();
            paramTypes[0] = ClassData.class;
        } else {
            params[0] = instance;
            paramTypes[0] = InstanceData.class;
        }

        for (int i = 1; i < paramTypes.length; ++i)
            if (types[i - 1] == BaseType.BOOLEAN) {
                params[i] = frame.popInt() != 0;
                paramTypes[i] = boolean.class;
            } else if (types[i - 1] == BaseType.CHAR) {
                params[i] = (char) frame.popInt();
                paramTypes[i] = char.class;
            } else if (types[i - 1] == BaseType.FLOAT) {
                params[i] = frame.popFloat();
                paramTypes[i] = float.class;
            } else if (types[i - 1] == BaseType.DOUBLE) {
                params[i] = frame.popDouble();
                paramTypes[i] = double.class;
            } else if (types[i - 1] == BaseType.BYTE) {
                params[i] = (byte) frame.popInt();
                paramTypes[i] = byte.class;
            } else if (types[i - 1] == BaseType.SHORT) {
                params[i] = (short) frame.popInt();
                paramTypes[i] = short.class;
            } else if (types[i - 1] == BaseType.INT) {
                params[i] = frame.popInt();
                paramTypes[i] = int.class;
            } else if (types[i - 1] == BaseType.LONG) {
                params[i] = frame.popLong();
                paramTypes[i] = long.class;
            } else {
                params[i] = instancePool.getInstance(frame.popReference());
                paramTypes[i] = InstanceData.class;
            }

        try {
            final Method nativeMethod = nativeClass.getMethod(method.getName(), paramTypes);
            final Object nativeReturn = nativeMethod.invoke(null, params);

            if (nativeReturn == null)
                return new int[0];
            else if (nativeReturn instanceof Boolean)
                return new int[] { (boolean) nativeReturn ? 1 : 0 };
            else if (nativeReturn instanceof Character)
                return new int[] { (char) nativeReturn };
            else if (nativeReturn instanceof Float)
                return new int[] { Float.floatToIntBits((float) nativeReturn) };
            else if (nativeReturn instanceof Double) {
                final ByteBuffer buf = ByteBuffer.allocate(8).putDouble((double) nativeReturn);
                return new int[] { buf.getInt(), buf.getInt() };
            } else if (nativeReturn instanceof Byte)
                return new int[] { (byte) nativeReturn };
            else if (nativeReturn instanceof Short)
                return new int[] { (short) nativeReturn };
            else if (nativeReturn instanceof Integer)
                return new int[] { (int) nativeReturn };
            else if (nativeReturn instanceof Long) {
                final ByteBuffer buf = ByteBuffer.allocate(8).putLong((long) nativeReturn);
                return new int[] { buf.getInt(), buf.getInt() };
            } else
                return new int[] { ((InstanceData) nativeReturn).getReference() };
        } catch (final InvocationTargetException e) {
            e.printStackTrace();
            throw e;
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
