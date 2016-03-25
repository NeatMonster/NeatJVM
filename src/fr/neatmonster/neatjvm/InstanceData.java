package fr.neatmonster.neatjvm;

import java.util.ArrayList;
import java.util.List;

import fr.neatmonster.neatjvm.format.AccessFlag;
import fr.neatmonster.neatjvm.format.FieldInfo;

public class InstanceData {
    public static List<FieldInfo> getFields(final ClassFile classFile, final boolean isSuper) {
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

    public ClassFile classFile;

    public InstanceData(final ClassFile classFile) {
        this.classFile = classFile;

        final List<FieldInfo> fields = getFields(classFile, false);
        if (fields.isEmpty())
            return;

        int totalSize = 0;
        for (final FieldInfo field : fields)
            totalSize += field.descriptor.getSize();

        final HeapManager heap = classFile.loader.vm.javaHeap;
        int addr = heap.allocate(totalSize);
        for (final FieldInfo field : fields) {
            heap.put(addr, field.descriptor.getDefault());
            addr += field.descriptor.getSize();
        }
    }
}
