package fr.neatmonster.neatjvm.natives;

import fr.neatmonster.neatjvm.ClassData;
import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.ClassFile.ArrayClassFile;
import fr.neatmonster.neatjvm.ClassFile.PrimitiveClassFile;
import fr.neatmonster.neatjvm.InstanceData;
import fr.neatmonster.neatjvm.InstanceData.ArrayInstanceData;
import fr.neatmonster.neatjvm.MemoryPool;
import fr.neatmonster.neatjvm.VirtualMachine;

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
        return obj.getHashCode();
    }

    public static InstanceData initProperties(final ClassData instance, final InstanceData props) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData mapLibraryName(final ClassData instance, final InstanceData libname) {
        throw new UnsupportedOperationException();
    }

    public static long nanoTime(final ClassData instance) {
        return System.nanoTime();
    }

    public static void setErr0(final ClassData instance, final InstanceData err) {
        throw new UnsupportedOperationException();
    }

    public static void setIn0(final ClassData instance, final InstanceData in) {
        throw new UnsupportedOperationException();
    }

    public static void setOut0(final ClassData instance, final InstanceData out) {
        throw new UnsupportedOperationException();
    }
}
