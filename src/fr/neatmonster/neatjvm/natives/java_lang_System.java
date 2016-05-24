package fr.neatmonster.neatjvm.natives;

import java.nio.ByteBuffer;
import java.util.Properties;

import fr.neatmonster.neatjvm.*;
import fr.neatmonster.neatjvm.ClassFile.ArrayClassFile;
import fr.neatmonster.neatjvm.ClassFile.PrimitiveClassFile;
import fr.neatmonster.neatjvm.InstanceData.ArrayInstanceData;
import fr.neatmonster.neatjvm.format.FieldInfo;
import fr.neatmonster.neatjvm.format.MethodInfo;

public class java_lang_System {

    public static void arraycopy(final ClassData instance, final InstanceData src, final int srcPos,
            final InstanceData dest, final int destPos, final int length) {
        if (src == null || dest == null)
            throw new NullPointerException();

        if (!(src instanceof ArrayInstanceData))
            throw new ArrayStoreException();

        if (!(dest instanceof ArrayInstanceData))
            throw new ArrayStoreException();

        final ArrayClassFile srcClass = (ArrayClassFile) src.getClassFile();
        final ClassFile srcArrayClass = srcClass.getArrayClass();

        final ArrayClassFile destClass = (ArrayClassFile) dest.getClassFile();
        final ClassFile destArrayClass = destClass.getArrayClass();

        int elemSize;
        if (srcArrayClass instanceof PrimitiveClassFile) {
            final PrimitiveClassFile srcArrayPrim = (PrimitiveClassFile) srcArrayClass;

            if (destArrayClass instanceof PrimitiveClassFile) {
                final PrimitiveClassFile destArrayPrim = (PrimitiveClassFile) destArrayClass;

                if (srcArrayPrim.getType() == destArrayPrim.getType())
                    elemSize = srcArrayPrim.getType().getSize();
                else
                    throw new ArrayStoreException();
            } else
                throw new ArrayStoreException();
        } else {
            if (destArrayClass instanceof PrimitiveClassFile)
                throw new ArrayStoreException();
            else
                elemSize = 4;
        }

        if (srcPos < 0 || destPos < 0 || length < 0)
            throw new IndexOutOfBoundsException();

        final ArrayInstanceData srcArray = (ArrayInstanceData) src;
        if (srcPos + length > srcArray.getLength())
            throw new IndexOutOfBoundsException();

        final ArrayInstanceData destArray = (ArrayInstanceData) dest;
        if (destPos + length > destArray.getLength())
            throw new IndexOutOfBoundsException();

        final MemoryPool heapSpace = VirtualMachine.getHeapSpace();
        for (int index = 0; index < length; ++index) {
            final byte[] elem = new byte[elemSize];
            heapSpace.get(src.getAddress() + (srcPos + index) * elemSize, elem);
            heapSpace.put(dest.getAddress() + (destPos + index) * elemSize, elem);
        }
    }

    public static long currentTimeMillis(final ClassData instance) {
        return System.currentTimeMillis();
    }

    public static int identityHashCode(final ClassData instance, final InstanceData obj) {
        if (obj == null)
            return 0;
        return obj.getHashCode();
    }

    public static InstanceData initProperties(final ClassData instance, final InstanceData props) {
        final InstancePool instancePool = VirtualMachine.getInstancePool();
        final MethodInfo method = props.getClassFile().getDeclaredMethod("setProperty",
                "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");

        final Properties sysProps = System.getProperties();
        for (final String propertyName : sysProps.stringPropertyNames()) {
            final String propertyValue = sysProps.getProperty(propertyName);
            method.invoke(props, instancePool.addString(propertyName), instancePool.addString(propertyValue));
        }

        return props;
    }

    public static InstanceData mapLibraryName(ClassData instance, InstanceData libname) {
        throw new UnsupportedOperationException();
    }

    public static long nanoTime(final ClassData instance) {
        return System.nanoTime();
    }

    public static void setErr0(final ClassData instance, final InstanceData err) {
        final FieldInfo field = instance.getClassFile().getField("err", "Ljava.io.PrintStream;");
        instance.put(field, ByteBuffer.allocate(4).putInt(err.getReference()).array());
    }

    public static void setIn0(final ClassData instance, final InstanceData in) {
        final FieldInfo field = instance.getClassFile().getField("in", "Ljava.io.InputStream;");
        instance.put(field, ByteBuffer.allocate(4).putInt(in.getReference()).array());
    }

    public static void setOut0(final ClassData instance, final InstanceData out) {
        final FieldInfo field = instance.getClassFile().getField("out", "Ljava.io.PrintStream;");
        instance.put(field, ByteBuffer.allocate(4).putInt(out.getReference()).array());
    }
}
