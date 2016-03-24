package fr.neatmonster.neatjvm;

import java.util.ArrayList;
import java.util.List;

import fr.neatmonster.neatjvm.format.AccessFlag;
import fr.neatmonster.neatjvm.format.FieldInfo;

public class ClassData {
    public ClassFile classFile;

    public ClassData(final ClassFile classFile) {
        this.classFile = classFile;

        final List<FieldInfo> fields = new ArrayList<>();

        int totalSize = 0;
        for (final FieldInfo field : classFile.fields) {
            if (!AccessFlag.STATIC.eval(field.accessFlags))
                continue;
            fields.add(field);

            if (!field.isResolved())
                field.resolve();
            totalSize += field.descriptor.getSize();
        }
        if (fields.isEmpty())
            return;

        final HeapSpace heap = classFile.loader.vm.heapSpace;
        int addr = heap.allocate(totalSize);
        for (final FieldInfo field : fields) {
            heap.put(addr, field.descriptor.getDefault());
            addr += field.descriptor.getSize();
        }
    }
}
