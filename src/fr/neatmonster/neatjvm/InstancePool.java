package fr.neatmonster.neatjvm;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.neatjvm.ClassFile.ArrayClassFile;
import fr.neatmonster.neatjvm.InstanceData.ArrayInstanceData;
import fr.neatmonster.neatjvm.format.FieldInfo;

public class InstancePool {
    private final Map<Integer, InstanceData>   instances;
    private int                                nextReference;

    private final Map<String, InstanceData>    strings;

    private final Map<ClassFile, InstanceData> classToJavaClass;
    private final Map<InstanceData, ClassFile> javaClassToClass;

    public InstancePool() {
        instances = new HashMap<>();
        nextReference = 1;

        strings = new HashMap<>();

        classToJavaClass = new HashMap<>();
        javaClassToClass = new HashMap<>();
    }

    public int addInstance(final InstanceData instance) {
        final int reference = nextReference++;
        instances.put(reference, instance);
        return reference;
    }

    public InstanceData getInstance(final int reference) {
        return instances.get(reference);
    }

    public int addString(final String string) {
        InstanceData stringInstance = strings.get(string);
        if (stringInstance != null)
            return stringInstance.getReference();

        final ClassLoader classLoader = VirtualMachine.getClassLoader();
        final MemoryPool heapSpace = VirtualMachine.getHeapSpace();

        final ClassFile stringClass = classLoader.loadClass("java.lang.String");
        final int stringref = stringClass.newInstance();
        stringInstance = getInstance(stringref);

        final ArrayClassFile arrayClass = (ArrayClassFile) classLoader.loadClass("[B");
        final int arrayref = arrayClass.newInstance(string.length());
        final ArrayInstanceData arrayInstance = (ArrayInstanceData) getInstance(arrayref);
        for (int index = 0; index < string.length(); ++index)
            heapSpace.putChar(arrayInstance.dataStart + index * 2, string.charAt(index));

        final FieldInfo valueField = stringClass.getField("value", "[C");
        stringInstance.put(valueField, ByteBuffer.allocate(4).putInt(arrayref).array());

        final FieldInfo offsetField = stringClass.getField("offset", "I");
        if (offsetField != null)
            stringInstance.put(offsetField, ByteBuffer.allocate(4).putInt(0).array());

        final FieldInfo countField = stringClass.getField("count", "I");
        if (countField != null)
            stringInstance.put(countField, ByteBuffer.allocate(4).putInt(string.length()).array());

        strings.put(string, stringInstance);
        return stringref;
    }

    public InstanceData getJavaClass(final ClassFile classFile) {
        InstanceData javaClass = classToJavaClass.get(classFile);
        if (javaClass != null)
            return javaClass;

        javaClass = getInstance(VirtualMachine.getClassLoader().loadClass("java.lang.Class").newInstance());
        classToJavaClass.put(classFile, javaClass);
        javaClassToClass.put(javaClass, classFile);
        return javaClass;
    }

    public ClassFile getClassFile(final InstanceData javaClass) {
        return javaClassToClass.get(javaClass);
    }
}
