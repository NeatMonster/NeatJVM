package fr.neatmonster.neatjvm;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ExecutionPool.ThreadPriority;
import fr.neatmonster.neatjvm.Thread.ThreadState;
import fr.neatmonster.neatjvm.format.AccessFlag;
import fr.neatmonster.neatjvm.format.AttributeInfo;
import fr.neatmonster.neatjvm.format.FieldInfo;
import fr.neatmonster.neatjvm.format.MethodInfo;

public class ClassFile {
    public final ClassLoader     loader;
    public final ClassData       data;

    public final int             magic;
    public final short           minorVersion;
    public final short           majorVersion;
    public final ConstantPool    constants;
    public final short           accessFlags;
    public final short           thisClass;
    public final short           superClass;
    public final short[]         interfaces;
    public final FieldInfo[]     fields;
    public final MethodInfo[]    methods;
    public final AttributeInfo[] attributes;

    public ClassFile(final ClassLoader loader, final ByteBuffer buf) {
        this.loader = loader;

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
                final String name = constants.getUtf8(index);
                final Class<? extends AttributeInfo> clazz = AttributeInfo.ALL.get(name);
                if (clazz == null) {
                    System.err.println("Unrecognized attribute info w/ name " + name);
                    buf.position(buf.position() + length);
                } else
                    attributes[i] = clazz.getConstructor(ClassFile.class, ByteBuffer.class).newInstance(this, buf);
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
}
