package fr.neatmonster.neatjvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.neatmonster.neatjvm.format.FieldInfo;
import fr.neatmonster.neatjvm.format.Modifier;

public class ClassData extends ObjectData {
    private final Map<FieldInfo, Integer> offsets;

    public ClassData(final ClassFile classFile) {
        super(classFile);
        offsets = new HashMap<>();

        int totalSize = 0;
        final List<FieldInfo> fields = new ArrayList<>();
        for (final FieldInfo field : classFile.getFields()) {
            if (!Modifier.STATIC.eval(field.getModifiers()))
                continue;
            fields.add(field);

            offsets.put(field, totalSize);
            totalSize += field.resolve().getType().getSize();
        }

        if (fields.isEmpty())
            return;

        final MemoryPool heap = VirtualMachine.getHeapSpace();
        dataStart = heap.allocate(totalSize);

        int currentAddr = dataStart;
        for (final FieldInfo field : fields) {
            heap.put(currentAddr, field.getType().getDefaultValue());
            currentAddr += field.getType().getSize();
        }
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
