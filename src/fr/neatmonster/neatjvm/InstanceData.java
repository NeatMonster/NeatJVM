package fr.neatmonster.neatjvm;

import java.util.*;

import fr.neatmonster.neatjvm.ClassFile.ArrayClassFile;
import fr.neatmonster.neatjvm.ClassFile.PrimitiveClassFile;
import fr.neatmonster.neatjvm.format.FieldInfo;
import fr.neatmonster.neatjvm.format.Modifier;

public class InstanceData extends ObjectData {
    public static class ArrayInstanceData extends InstanceData {
        private final ClassFile arrayClass;
        private final int       arrayLength;

        public ArrayInstanceData(final ArrayClassFile classFile, final int length) {
            super(classFile);
            arrayClass = classFile.getArrayClass();
            arrayLength = length;

            int typeSize = 4;
            if (arrayClass instanceof PrimitiveClassFile)
                typeSize = ((PrimitiveClassFile) arrayClass).getType().getSize();

            final MemoryPool heap = VirtualMachine.getHeapSpace();
            dataStart = heap.allocate(length * typeSize);
            for (int i = 0; i < length * typeSize; ++i)
                heap.put(dataStart + i, (byte) 0);
        }

        public int getLength() {
            return arrayLength;
        }
    }

    private static final Random           HASH_CODE = new Random();

    private final Map<FieldInfo, Integer> offsets;
    private final int                     reference;
    private final int                     hashCode;

    public InstanceData(final ClassFile classFile) {
        super(classFile);
        hashCode = HASH_CODE.nextInt();

        if (classFile instanceof ArrayClassFile) {
            offsets = null;
            reference = VirtualMachine.getInstancePool().addInstance(this);
            return;
        }

        offsets = new HashMap<>();

        final List<FieldInfo> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(classFile.getFields()));
        final Iterator<FieldInfo> it = fields.iterator();
        while (it.hasNext()) {
            final FieldInfo field = it.next();
            if (Modifier.STATIC.eval(field.getModifiers()))
                it.remove();
        }

        if (!fields.isEmpty()) {
            int totalSize = 0;
            for (final FieldInfo field : fields) {
                offsets.put(field, totalSize);
                totalSize += field.getType().getSize();
            }

            final MemoryPool heap = VirtualMachine.getHeapSpace();
            dataStart = heap.allocate(totalSize);

            int currentAddr = dataStart;
            for (final FieldInfo field : fields) {
                heap.put(currentAddr, field.getType().getDefaultValue());
                currentAddr += field.getType().getSize();
            }
        }

        reference = VirtualMachine.getInstancePool().addInstance(this);
    }

    public int getReference() {
        return reference;
    }

    public int getHashCode() {
        return hashCode;
    }

    @Override
    public void get(final FieldInfo field, final byte[] value) {
        VirtualMachine.getHeapSpace().get(dataStart + offsets.get(field), value, 0, field.getType().getSize());
    }

    @Override
    public void put(final FieldInfo field, final byte[] value) {
        VirtualMachine.getHeapSpace().put(dataStart + offsets.get(field), value, 0, field.getType().getSize());
    }
}
