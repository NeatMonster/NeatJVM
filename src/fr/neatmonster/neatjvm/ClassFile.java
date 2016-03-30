package fr.neatmonster.neatjvm;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.InstanceData.ArrayInstanceData;
import fr.neatmonster.neatjvm.Thread.ThreadPriority;
import fr.neatmonster.neatjvm.Thread.ThreadState;
import fr.neatmonster.neatjvm.format.AttributeInfo;
import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.format.FieldInfo;
import fr.neatmonster.neatjvm.format.FieldType.BaseType;
import fr.neatmonster.neatjvm.format.MethodInfo;
import fr.neatmonster.neatjvm.format.Modifier;
import fr.neatmonster.neatjvm.format.constant.DoubleConstant;
import fr.neatmonster.neatjvm.format.constant.LongConstant;

public class ClassFile {
    public static class ArrayClassFile extends ClassFile {
        private final ClassFile arrayClass;

        public ArrayClassFile(final ClassLoader loader, final ClassFile arrayClass) {
            super(loader, "[L" + arrayClass.name + ";");
            this.arrayClass = arrayClass;
        }

        public ClassFile getArrayClass() {
            return arrayClass;
        }

        @Override
        public boolean isArray() {
            return true;
        }

        public int newInstance(final int length) {
            return new ArrayInstanceData(this, length).getReference();
        }
    }

    public static class PrimitiveClassFile extends ClassFile {
        private final BaseType type;

        public PrimitiveClassFile(final ClassLoader loader, final BaseType type) {
            super(loader, type.toString());
            this.type = type;
        }

        public BaseType getType() {
            return type;
        }

        @Override
        public boolean isPrimitive() {
            return true;
        }
    }

    private final ClassLoader     loader;
    private final String          name;
    private final ClassData       instance;

    private final ConstantInfo[]  constants;
    private final short           modifiers;
    private final ClassFile       superclass;
    private final ClassFile[]     interfaces;
    private final FieldInfo[]     fields;
    private final MethodInfo[]    methods;
    private final AttributeInfo[] attributes;

    public ClassFile(final ClassLoader loader, final String name) {
        this(loader, name, null);
    }

    public ClassFile(final ClassLoader loader, final String name, final ByteBuffer buf) {
        this.loader = loader;
        this.name = name;

        if (isPrimitive()) {
            instance = null;
            constants = null;
            modifiers = 0x0;
            superclass = null;
            interfaces = null;
            fields = null;
            methods = null;
            attributes = null;
            return;
        }

        if (isArray()) {
            instance = null;
            constants = new ConstantInfo[0];
            modifiers = 0x411;
            superclass = loader.loadClass("java/lang/Object");
            interfaces = new ClassFile[] { loader.loadClass("java/lang/Cloneable"),
                    loader.loadClass("java/io/Serializable") };
            fields = new FieldInfo[0];
            methods = new MethodInfo[0];
            attributes = new AttributeInfo[0];
            return;
        }

        buf.getInt();
        buf.getShort();
        buf.getShort();

        final short constantsCount = buf.getShort();
        constants = new ConstantInfo[constantsCount - 1];
        for (int i = 0; i < constants.length; ++i) {
            final int tag = buf.get();
            try {
                final Class<? extends ConstantInfo> clazz = ConstantInfo.get(tag);
                if (clazz == null)
                    System.err.println("Unrecognized constant info w/ tag " + tag);
                else {
                    constants[i] = clazz.getConstructor(ClassFile.class, ByteBuffer.class).newInstance(this, buf);
                    i = constants[i] instanceof LongConstant || constants[i] instanceof DoubleConstant ? ++i : i;
                }
            } catch (final Exception e) {
                e.printStackTrace(System.err);
                System.exit(0);
            }
        }

        modifiers = buf.getShort();

        buf.getShort();

        final short superclassIndex = buf.getShort();
        if (superclassIndex == 0)
            superclass = null;
        else
            superclass = ConstantInfo.getClassFile(this, superclassIndex);

        final short interfacesCount = buf.getShort();
        interfaces = new ClassFile[interfacesCount];
        for (int i = 0; i < interfaces.length; ++i)
            interfaces[i] = ConstantInfo.getClassFile(this, buf.getShort());

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
                final String name_ = ConstantInfo.getUtf8(this, index);
                final Class<? extends AttributeInfo> clazz = AttributeInfo.get(name_);
                if (clazz == null) {
                    System.err.println("Unrecognized attribute info w/ name " + name_);
                    buf.position(buf.position() + length);
                } else
                    attributes[i] = clazz.getConstructor(ClassFile.class, ByteBuffer.class).newInstance(this, buf);
            } catch (final Exception e) {
                e.printStackTrace(System.err);
                System.exit(0);
            }
        }

        instance = new ClassData(this);
    }

    public void initialize() {
        final MethodInfo cinit = getMethod("<clinit>", "()V");
        if (cinit == null)
            return;
        cinit.resolve();

        Thread thread = VirtualMachine.getCurrentThread();
        if (thread == null)
            thread = VirtualMachine.getInstance().startThread(MethodInfo.getCode(cinit), ThreadPriority.NORM_PRIORITY);
        else
            thread = VirtualMachine.getInstance().startThread(MethodInfo.getCode(cinit), thread.getPriority());

        while (thread.getState() != ThreadState.TERMINATED)
            thread.tick();
    }

    public AttributeInfo[] getAttributes() {
        return attributes;
    }

    public ClassLoader getClassLoader() {
        return loader;
    }

    public ConstantInfo[] getConstants() {
        return constants;
    }

    public FieldInfo getField(final String name, final String descriptor) {
        for (final FieldInfo field : fields) {
            field.resolve();
            if (!field.getName().equals(name))
                continue;
            if (!field.getType().toString().equals(descriptor))
                continue;
            return field;
        }

        for (final ClassFile interfaceClass : interfaces) {
            final FieldInfo field = interfaceClass.getField(name, descriptor);
            if (field != null)
                return field;
        }

        if (superclass != null)
            return superclass.getField(name, descriptor);
        return null;
    }

    public FieldInfo[] getFields() {
        return fields;
    }

    public ClassFile[] getInterfaces() {
        return interfaces;
    }

    public MethodInfo getMethod(final String name, final String descriptor) {
        for (final MethodInfo method : methods) {
            method.resolve();
            if (!method.getName().equals(name))
                continue;
            if (!MethodInfo.getDescriptor(method).equals(descriptor))
                continue;
            return method;
        }

        if (superclass != null)
            return superclass.getMethod(name, descriptor);
        return null;
    }

    public MethodInfo[] getMethods() {
        return methods;
    }

    public short getModifiers() {
        return modifiers;
    }

    public String getName() {
        return name;
    }

    public ClassFile getSuperclass() {
        return superclass;
    }

    public boolean isAnnotation() {
        return Modifier.ANNOTATION.eval(modifiers);
    }

    public boolean isArray() {
        return false;
    }

    public boolean isEnum() {
        return Modifier.ENUM.eval(modifiers);
    }

    public boolean isInterface() {
        return Modifier.INTERFACE.eval(modifiers);
    }

    public boolean isPrimitive() {
        return false;
    }

    public boolean isSynthetic() {
        return Modifier.SYNTHETIC.eval(modifiers);
    }

    public int newInstance() {
        return new InstanceData(this).getReference();
    }

    public ClassData getInstance() {
        return instance;
    }

    public boolean extendsClass(final ClassFile classFile) {
        if (equals(classFile))
            return true;
        return superclass != null && superclass.extendsClass(classFile);
    }

    public boolean implementsClass(final ClassFile classFile) {
        if (equals(classFile))
            return true;

        for (final ClassFile interfaceClass : interfaces)
            if (interfaceClass.implementsClass(classFile))
                return true;
        return false;
    }

    public boolean isInstance(final ClassFile classFile) {
        if (equals(classFile))
            return true;

        if (isArray()) {
            if (classFile.isArray()) {
                final boolean thisPrimArr = ((ArrayClassFile) this).arrayClass.isPrimitive();
                final boolean classPrimArr = ((ArrayClassFile) classFile).arrayClass.isPrimitive();
                if (!thisPrimArr && !classPrimArr)
                    return ((ArrayClassFile) this).arrayClass.isInstance(((ArrayClassFile) classFile).arrayClass);
                else
                    return thisPrimArr && classPrimArr;
            } else {
                if (classFile.isInterface())
                    return classFile.name.equals("java/lang/Cloneable")
                            || classFile.name.equals("java/io/Serializable");
                else
                    return classFile.name.equals("java/lang/Object");
            }
        } else {
            if (isInterface()) {
                if (classFile.isInterface())
                    return extendsClass(classFile);
                else
                    return classFile.name.equals("java/lang/Object");
            } else {
                if (classFile.isInterface())
                    return implementsClass(classFile);
                else
                    return extendsClass(classFile);
            }
        }
    }
}
