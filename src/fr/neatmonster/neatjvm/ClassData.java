package fr.neatmonster.neatjvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.neatmonster.neatjvm.format.AccessFlag;
import fr.neatmonster.neatjvm.format.FieldInfo;

public class ClassData extends ObjectData {
    public Map<FieldInfo, Integer> offsets;

    public ClassData(final ClassFile classFile) {
        super(classFile);
        offsets = new HashMap<>();

        int totalSize = 0;
        final List<FieldInfo> fields = new ArrayList<>();
        for (final FieldInfo field : classFile.fields) {
            if (!AccessFlag.STATIC.eval(field.accessFlags))
                continue;
            fields.add(field);

            offsets.put(field, totalSize);
            totalSize += field.resolve().descriptor.getSize();
        }

        if (fields.isEmpty())
            return;

        final HeapManager heap = classFile.loader.vm.javaHeap;
        dataStart = heap.allocate(totalSize);

        int currentAddr = dataStart;
        for (final FieldInfo field : fields) {
            heap.put(currentAddr, field.descriptor.getDefault());
            currentAddr += field.descriptor.getSize();
        }
    }
}
