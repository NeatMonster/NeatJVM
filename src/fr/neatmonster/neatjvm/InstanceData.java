package fr.neatmonster.neatjvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.neatmonster.neatjvm.format.AccessFlag;
import fr.neatmonster.neatjvm.format.FieldInfo;

public class InstanceData extends ObjectData {
    public Map<FieldInfo, Integer> offsets;

    public InstanceData(final ClassFile classFile) {
        super(classFile);
        offsets = new HashMap<>();

        final List<FieldInfo> fields = getFields(classFile, false);
        if (fields.isEmpty())
            return;

        int totalSize = 0;
        for (final FieldInfo field : fields) {
            offsets.put(field, totalSize);
            totalSize += field.descriptor.getSize();
        }

        final HeapManager heap = classFile.loader.vm.javaHeap;
        dataStart = heap.allocate(totalSize);

        int currentAddr = dataStart;
        for (final FieldInfo field : fields) {
            heap.put(currentAddr, field.descriptor.getDefault());
            currentAddr += field.descriptor.getSize();
        }
    }

    public List<FieldInfo> getFields(final ClassFile classFile, final boolean isSuper) {
        final List<FieldInfo> fields = new ArrayList<>();

        if (classFile.superClass != 0) {
            final ClassFile superClass = classFile.constants.getClass(classFile.superClass);
            fields.addAll(getFields(superClass, true));
        }

        for (final FieldInfo field : classFile.fields) {
            if (AccessFlag.STATIC.eval(field.accessFlags))
                continue;
            if (isSuper && AccessFlag.PRIVATE.eval(field.accessFlags))
                continue;
            fields.add(field.resolve());
        }

        return fields;
    }
}
