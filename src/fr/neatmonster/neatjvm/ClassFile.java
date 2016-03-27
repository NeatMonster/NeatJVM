package fr.neatmonster.neatjvm;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ExecutionPool.ThreadPriority;
import fr.neatmonster.neatjvm.InstanceData.ArrayInstanceData;
import fr.neatmonster.neatjvm.InstanceData.PrimitiveArrayInstanceData;
import fr.neatmonster.neatjvm.Thread.ThreadState;
import fr.neatmonster.neatjvm.format.AccessFlag;
import fr.neatmonster.neatjvm.format.AttributeInfo;
import fr.neatmonster.neatjvm.format.FieldDescriptor;
import fr.neatmonster.neatjvm.format.FieldInfo;
import fr.neatmonster.neatjvm.format.MethodInfo;

public class ClassFile {
    public static class ArrayClassFile extends ClassFile {
        public final ClassFile arrayClass;

        public ArrayClassFile(final ClassLoader loader, final ClassFile arrayClass, final String name) {
            super(loader, null, name);
            this.arrayClass = arrayClass;
        }

        public int newInstance(final int length) {
            final InstanceData instance = new ArrayInstanceData(this, length);
            return loader.vm.handlePool.addInstance(instance);
        }

        @Override
        public boolean isArray() {
            return true;
        }
    }

    public static class PrimitiveArrayClassFile extends ArrayClassFile {
        public final FieldDescriptor.BaseType arrayType;

        public PrimitiveArrayClassFile(final ClassLoader loader, final FieldDescriptor.BaseType arrayType,
                final String name) {
            super(loader, null, name);
            this.arrayType = arrayType;
        }

        @Override
        public int newInstance(final int length) {
            final InstanceData instance = new PrimitiveArrayInstanceData(this, length);
            return loader.vm.handlePool.addInstance(instance);
        }

        @Override
        public boolean isPrimitiveArray() {
            return true;
        }
    }

    public final ClassLoader loader;
    public final String      name;
    public ClassData         data;

    public int               magic;
    public short             minorVersion;
    public short             majorVersion;
    public ConstantPool      constants;
    public short             accessFlags;
    public short             thisClass;
    public short             superClass;
    public short[]           interfaces;
    public FieldInfo[]       fields;
    public MethodInfo[]      methods;
    public AttributeInfo[]   attributes;

    public ClassFile(final ClassLoader loader, final ByteBuffer buf, final String name) {
        this.loader = loader;
        this.name = name;

        if (buf == null)
            return;

        magic = buf.getInt();
        minorVersion = buf.getShort();
        majorVersion = buf.getShort();

        constants = new ConstantPool(this, buf);

        accessFlags = buf.getShort();
        thisClass = buf.getShort();
        superClass = buf.getShort();

        final short interfacesCount = buf.getShort();
        interfaces = new short[interfacesCount];
        for (int i = 0; i < interfaces.length; ++i)
            interfaces[i] = buf.getShort();

        final short fieldsCount = buf.getShort();
        fields = new FieldInfo[fieldsCount];
        for (int i = 0; i < fields.length; ++i)
            fields[i] = new FieldInfo(this, buf);

        final short methodsCount = buf.getShort();
        methods = new MethodInfo[methodsCount];
        for (int i = 0; i < methods.length; ++i)
            methods[i] = new MethodInfo(this, buf);

        final short attributesCount = buf.getShort();
        attributes = new AttributeInfo[attributesCount];
        for (int i = 0; i < attributes.length; ++i) {
            final short index = buf.getShort();
            final int length = buf.getInt();
            try {
                final String attrName = constants.getUtf8(index);
                final Class<? extends AttributeInfo> attrClass = AttributeInfo.ALL.get(attrName);
                if (attrClass == null) {
                    System.err.println("Unrecognized attribute info w/ name " + attrName);
                    buf.position(buf.position() + length);
                } else
                    attributes[i] = attrClass.getConstructor(ClassFile.class, ByteBuffer.class).newInstance(this, buf);
            } catch (final Exception e) {
                e.printStackTrace(System.err);
                System.exit(0);
            }
        }

        data = new ClassData(this);
    }

    public void initialize() {
        final MethodInfo cinit = getMethod("<cinit>", "()V");
        if (cinit == null)
            return;

        final VirtualMachine vm = loader.vm;
        Thread thread;
        if (vm.currentThread == null)
            thread = vm.runThread(cinit.resolve().code, null, ThreadPriority.NORM_PRIORITY);
        else
            thread = vm.runThread(cinit.resolve().code, vm.currentThread.instance, vm.currentThread.priority);

        while (thread.state != ThreadState.DEAD)
            thread.tick();
    }

    public FieldInfo getField(final String name, final String desc, final AccessFlag... flags) {
        search: for (final FieldInfo field : fields) {
            field.resolve();
            if (!constants.getUtf8(field.nameIndex).equals(name))
                continue;
            if (!constants.getUtf8(field.descriptorIndex).equals(desc))
                continue;
            for (final AccessFlag flag : flags)
                if (!flag.eval(field.accessFlags))
                    continue search;
            return field;
        }
        return null;
    }

    public MethodInfo getMethod(final String name, final String desc, final AccessFlag... flags) {
        search: for (final MethodInfo method : methods) {
            method.resolve();
            if (!constants.getUtf8(method.nameIndex).equals(name))
                continue;
            if (!constants.getUtf8(method.descriptorIndex).equals(desc))
                continue;
            for (final AccessFlag flag : flags)
                if (!flag.eval(method.accessFlags))
                    continue search;
            return method;
        }
        return null;
    }

    public int newInstance() {
        final InstanceData instance = new InstanceData(this);
        return loader.vm.handlePool.addInstance(instance);
    }

    public boolean isArray() {
        return false;
    }

    public boolean isPrimitiveArray() {
        return false;
    }

    public boolean isInterface() {
        return AccessFlag.INTERFACE.eval(accessFlags);
    }

    public boolean extends_(final ClassFile otherClass) {
        if (equals(otherClass))
            return true;

        if (superClass == 0)
            return false;
        return constants.getClass(superClass).extends_(otherClass);
    }

    public boolean implements_(final ClassFile otherClass) {
        if (equals(otherClass))
            return true;

        for (final int interface_ : interfaces)
            if (constants.getClass(interface_).implements_(otherClass))
                return true;
        return false;
    }

    public boolean isInstance(final ClassFile otherClass) {
        if (equals(otherClass))
            return true;

        if (isArray()) {
            if (otherClass.isArray()) {
                if (!isPrimitiveArray() && !otherClass.isPrimitiveArray())
                    return ((ArrayClassFile) this).arrayClass.isInstance(((ArrayClassFile) otherClass).arrayClass);
                else
                    return isPrimitiveArray() && otherClass.isPrimitiveArray();
            } else {
                if (otherClass.isInterface())
                    return otherClass.name.equals("java/lang/Cloneable")
                            || otherClass.name.equals("java/io/Serializable");
                else
                    return otherClass.name.equals("java/lang/Object");
            }
        } else {
            if (isInterface()) {
                if (otherClass.isInterface())
                    return extends_(otherClass);
                else
                    return otherClass.name.equals("java/lang/Object");
            } else {
                if (otherClass.isInterface())
                    return implements_(otherClass);
                else
                    return extends_(otherClass);
            }
        }
    }
}
