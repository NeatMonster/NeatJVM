package fr.neatmonster.neatjvm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.neatmonster.neatjvm.format.FieldType;
import fr.neatmonster.neatjvm.format.FieldType.BaseType;
import fr.neatmonster.neatjvm.format.FieldType.ObjectType;
import fr.neatmonster.neatjvm.format.MethodInfo;
import fr.neatmonster.neatjvm.natives.*;

public class NativeHandler {
    private final Map<String, Class<?>> NATIVES;

    public NativeHandler() {
        NATIVES = new HashMap<>();
    }

    public void registerNatives() {
        NATIVES.put("java.io.Console", java_io_Console.class);
        NATIVES.put("java.io.FileDescriptor", java_io_FileDescriptor.class);
        NATIVES.put("java.io.FileInputStream", java_io_FileInputStream.class);
        NATIVES.put("java.io.FileOutputStream", java_io_FileOutputStream.class);
        NATIVES.put("java.io.ObjectInputStream", java_io_ObjectInputStream.class);
        NATIVES.put("java.io.ObjectOutputStream", java_io_ObjectOutputStream.class);
        NATIVES.put("java.io.ObjectStreamClass", java_io_ObjectStreamClass.class);
        NATIVES.put("java.io.RandomAccessFile", java_io_RandomAccessFile.class);
        NATIVES.put("java.io.UnixFileSystem", java_io_UnixFileSystem.class);
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
        NATIVES.put("java.net.DatagramPacket", java_net_DatagramPacket.class);
        NATIVES.put("java.net.Inet4Address", java_net_Inet4Address.class);
        NATIVES.put("java.net.Inet4AddressImpl", java_net_Inet4AddressImpl.class);
        NATIVES.put("java.net.Inet6Address", java_net_Inet6Address.class);
        NATIVES.put("java.net.Inet6AddressImpl", java_net_Inet6AddressImpl.class);
        NATIVES.put("java.net.InetAddress", java_net_InetAddress.class);
        NATIVES.put("java.net.InetAddressImplFactory", java_net_InetAddressImplFactory.class);
        NATIVES.put("java.net.NetworkInterface", java_net_NetworkInterface.class);
        NATIVES.put("java.net.PlainDatagramSocketImpl", java_net_PlainDatagramSocketImpl.class);
        NATIVES.put("java.net.PlainSocketImpl", java_net_PlainSocketImpl.class);
        NATIVES.put("java.net.SocketInputStream", java_net_SocketInputStream.class);
        NATIVES.put("java.net.SocketOutputStream", java_net_SocketOutputStream.class);
        NATIVES.put("java.nio.Bits", java_nio_Bits.class);
        NATIVES.put("java.nio.MappedByteBuffer", java_nio_MappedByteBuffer.class);
        NATIVES.put("java.security.AccessController", java_security_AccessController.class);
        NATIVES.put("java.util.concurrent.atomic.AtomicLong", java_util_concurrent_atomic_AtomicLong.class);
        NATIVES.put("java.util.jar.JarFile", java_util_jar_JarFile.class);
        NATIVES.put("java.util.logging.FileHandler", java_util_logging_FileHandler.class);
        NATIVES.put("java.util.prefs.FileSystemPreferences", java_util_prefs_FileSystemPreferences.class);
        NATIVES.put("java.util.prefs.MacOSXPreferencesFile", java_util_prefs_MacOSXPreferencesFile.class);
        NATIVES.put("java.util.TimeZone", java_util_TimeZone.class);
        NATIVES.put("java.util.zip.Adler32", java_util_zip_Adler32.class);
        NATIVES.put("java.util.zip.CRC32", java_util_zip_CRC32.class);
        NATIVES.put("java.util.zip.Deflater", java_util_zip_Deflater.class);
        NATIVES.put("java.util.zip.Inflater", java_util_zip_Inflater.class);
        NATIVES.put("java.util.zip.ZipFile", java_util_zip_ZipFile.class);
    }

    public int[] executeMethod(final Thread thread, final MethodInfo method, final InstanceData instance)
            throws InvocationTargetException {
        if (method.getName().equals("registerNatives"))
            return new int[0];

        final InstancePool instancePool = VirtualMachine.getInstancePool();

        if (method.getClassFile().getName().equals("sun.misc.Unsafe")) {
            if (method.getName().equals("addressSize"))
                return new int[] { 0 };
            if (method.getName().equals("arrayBaseOffset"))
                return new int[] { 0 };
            if (method.getName().equals("arrayIndexScale"))
                return new int[] { 0 };
        }
        if (method.getClassFile().getName().equals("sun.misc.VM")) {
            if (method.getName().equals("initialize"))
                return new int[0];
        }
        if (method.getClassFile().getName().equals("sun.reflect.Reflection")) {
            if (method.getName().equals("getCallerClass")) {
                final List<StackFrame> stack = thread.getFrames();
                return new int[] {
                        instancePool.addClassFile(stack.get(stack.size() - 2).code.getClassFile()).getReference() };
            }
            if (method.getName().equals("getClassAccessFlags"))
                return new int[] { thread.getFrame().code.getClassFile().getModifiers() };
        }

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

        final StackFrame frame = thread.getFrame();
        for (int i = paramTypes.length - 1; i >= 1; --i) {
            final FieldType type = types[i - 1];
            if (type == BaseType.BOOLEAN) {
                params[i] = frame.popInt() != 0;
                paramTypes[i] = boolean.class;
            } else if (type == BaseType.CHAR) {
                params[i] = (char) frame.popInt();
                paramTypes[i] = char.class;
            } else if (type == BaseType.FLOAT) {
                params[i] = frame.popFloat();
                paramTypes[i] = float.class;
            } else if (type == BaseType.DOUBLE) {
                params[i] = frame.popDouble();
                paramTypes[i] = double.class;
            } else if (type == BaseType.BYTE) {
                params[i] = (byte) frame.popInt();
                paramTypes[i] = byte.class;
            } else if (type == BaseType.SHORT) {
                params[i] = (short) frame.popInt();
                paramTypes[i] = short.class;
            } else if (type == BaseType.INT) {
                params[i] = frame.popInt();
                paramTypes[i] = int.class;
            } else if (type == BaseType.LONG) {
                params[i] = frame.popLong();
                paramTypes[i] = long.class;
            } else if (type instanceof ObjectType) {
                final ObjectType objType = (ObjectType) type;
                if (objType.getClassName().equals("java.lang.String")) {
                    params[i] = instancePool.getString(instancePool.getInstance(frame.popReference()));
                    paramTypes[i] = String.class;
                } else {
                    params[i] = instancePool.getInstance(frame.popReference());
                    paramTypes[i] = InstanceData.class;
                }
            } else
                throw new IllegalArgumentException("Invalid type " + type);
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
                final ByteBuffer buf = ByteBuffer.allocate(8).putDouble(0, (double) nativeReturn);
                return new int[] { buf.getInt(), buf.getInt() };
            } else if (nativeReturn instanceof Byte)
                return new int[] { (byte) nativeReturn };
            else if (nativeReturn instanceof Short)
                return new int[] { (short) nativeReturn };
            else if (nativeReturn instanceof Integer)
                return new int[] { (int) nativeReturn };
            else if (nativeReturn instanceof Long) {
                final ByteBuffer buf = ByteBuffer.allocate(8).putLong(0, (long) nativeReturn);
                return new int[] { buf.getInt(), buf.getInt() };
            } else if (nativeReturn instanceof String)
                return new int[] { instancePool.addString((String) nativeReturn).getReference() };
            else
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
