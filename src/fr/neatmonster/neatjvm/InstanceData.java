package fr.neatmonster.neatjvm;

import java.util.ArrayList;
import java.util.List;

import fr.neatmonster.neatjvm.format.AccessFlag;
import fr.neatmonster.neatjvm.format.FieldInfo;
import fr.neatmonster.neatjvm.format.constant.ClassConstant;

public class InstanceData {
    public static List<FieldInfo> getFields(final ClassFile classFile, final boolean isSuper) {
        final List<FieldInfo> fields = new ArrayList<>();

        if (classFile.superClass != 0) {
            final ClassConstant superInfo = classFile.constants.getClass(classFile.superClass);
            // TODO: Implement native support
            if (superInfo.resolvedClass != null)
                fields.addAll(getFields(superInfo.resolvedClass, true));
        }

        for (final FieldInfo field : classFile.fields) {
            if (AccessFlag.STATIC.eval(field.accessFlags))
                continue;
            if (isSuper && AccessFlag.PRIVATE.eval(field.accessFlags))
                continue;
            fields.add(field);

            if (!field.isResolved())
                field.resolve();
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
