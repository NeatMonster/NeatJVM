package fr.neatmonster.neatjvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final Map<FieldInfo, Integer> offsets;
    private final int                     reference;

    public InstanceData(final ClassFile classFile) {
        super(classFile);
        reference = VirtualMachine.getInstancePool().addInstance(this);

        if (classFile instanceof ArrayClassFile) {
            offsets = null;
            return;
        }

        offsets = new HashMap<>();

        final List<FieldInfo> fields = getFields(classFile, false);
        if (fields.isEmpty())
            return;

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

    public int getReference() {
        return reference;
    }

    @Override
    public void get(final FieldInfo field, final byte[] value) {
        VirtualMachine.getHeapSpace().get(dataStart + offsets.get(field), value, 0, field.getType().getSize());
    }

    @Override
    public void put(final FieldInfo field, final byte[] value) {
        VirtualMachine.getHeapSpace().put(dataStart + offsets.get(field), value, 0, field.getType().getSize());
    }

    private static List<FieldInfo> getFields(final ClassFile classFile, final boolean isSuper) {
        final List<FieldInfo> fields = new ArrayList<>();

        if (classFile.getSuperclass() != null)
            fields.addAll(getFields(classFile.getSuperclass(), true));

        for (final FieldInfo field : classFile.getFields()) {
            field.resolve();
            if (Modifier.STATIC.eval(field.getModifiers()))
                continue;
            if (isSuper && Modifier.PRIVATE.eval(field.getModifiers()))
                continue;
            fields.add(field);
        }

        return fields;
    }
}
