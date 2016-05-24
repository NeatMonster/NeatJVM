package fr.neatmonster.neatjvm;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.neatjvm.ClassFile.ArrayClassFile;
import fr.neatmonster.neatjvm.InstanceData.ArrayInstanceData;
import fr.neatmonster.neatjvm.format.FieldInfo;
import fr.neatmonster.neatjvm.format.MethodInfo;

public class InstancePool {
    private final Map<Integer, InstanceData>   instances;
    private int                                nextReference;

    private final Map<String, InstanceData>    stringToInstance;
    private final Map<InstanceData, String>    instanceToString;

    private final Map<ClassFile, InstanceData> classToJavaClass;
    private final Map<InstanceData, ClassFile> javaClassToClass;

    public InstancePool() {
        instances = new HashMap<>();
        nextReference = 1;

        stringToInstance = new HashMap<>();
        instanceToString = new HashMap<>();

        classToJavaClass = new HashMap<>();
        javaClassToClass = new HashMap<>();
    }

    public int addInstance(final InstanceData instance) {
        final int reference = nextReference++;
        instances.put(reference, instance);

        if (instance.getClassFile().getName().equals("java.lang.String")) {
            final ClassFile stringClass = instance.getClassFile();
            final MemoryPool heapSpace = VirtualMachine.getHeapSpace();

            final FieldInfo valueField = stringClass.getDeclaredField("value", "[C");
            final ByteBuffer buf = ByteBuffer.allocate(4);
            instance.get(valueField, buf.array());

            final int arrayref = buf.getInt();
            if (arrayref > 0) {
                final ArrayInstanceData arrayInstance = (ArrayInstanceData) getInstance(arrayref);

                final char[] value = new char[arrayInstance.getLength()];
                for (int index = 0; index < arrayInstance.getLength(); ++index)
                    value[index] = heapSpace.getChar(arrayInstance.dataStart + index * 2);

                final String string = new String(value);
                stringToInstance.put(string, instance);
                instanceToString.put(instance, string);
            }
        }

        return reference;
    }

    public InstanceData getInstance(final int reference) {
        return instances.get(reference);
    }

    public InstanceData addString(final String string) {
        InstanceData stringInstance = stringToInstance.get(string);
        if (stringInstance != null)
            return stringInstance;

        final ClassLoader classLoader = VirtualMachine.getClassLoader();
        final MemoryPool heapSpace = VirtualMachine.getHeapSpace();

        final ClassFile stringClass = classLoader.loadClass("java.lang.String");
        stringInstance = stringClass.newInstance();

        final ArrayClassFile arrayClass = (ArrayClassFile) classLoader.loadClass("[C");
        final ArrayInstanceData arrayInstance = arrayClass.newInstance(string.length());
        for (int index = 0; index < string.length(); ++index)
            heapSpace.putChar(arrayInstance.dataStart + index * 2, string.charAt(index));

        final FieldInfo valueField = stringClass.getDeclaredField("value", "[C");
        stringInstance.put(valueField, ByteBuffer.allocate(4).putInt(arrayInstance.getReference()).array());

        final FieldInfo offsetField = stringClass.getDeclaredField("offset", "I");
        if (offsetField != null)
            stringInstance.put(offsetField, ByteBuffer.allocate(4).putInt(0).array());

        final FieldInfo countField = stringClass.getDeclaredField("count", "I");
        if (countField != null)
            stringInstance.put(countField, ByteBuffer.allocate(4).putInt(string.length()).array());

        stringToInstance.put(string, stringInstance);
        instanceToString.put(stringInstance, string);
        return stringInstance;
    }

    public String getString(final InstanceData instance) {
        return instanceToString.get(instance);
    }

    public InstanceData addClassFile(final ClassFile classFile) {
        InstanceData javaClass = classToJavaClass.get(classFile);
        if (javaClass != null)
            return javaClass;

        final ClassFile classClass = VirtualMachine.getClassLoader().loadClass("java.lang.Class");
        final MethodInfo initMethod = classClass.getMethod("<init>", "(Ljava.lang.ClassLoader;)V");

        // TODO Support classloaders
        javaClass = classClass.newInstance();
        initMethod.invoke(javaClass, 0);

        classToJavaClass.put(classFile, javaClass);
        javaClassToClass.put(javaClass, classFile);
        return javaClass;
    }

    public ClassFile getClassFile(final InstanceData javaClass) {
        return javaClassToClass.get(javaClass);
    }
}
