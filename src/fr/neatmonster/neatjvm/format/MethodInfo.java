package fr.neatmonster.neatjvm.format;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.InstanceData;
import fr.neatmonster.neatjvm.ObjectData;
import fr.neatmonster.neatjvm.StackFrame;
import fr.neatmonster.neatjvm.Thread;
import fr.neatmonster.neatjvm.Thread.ThreadState;
import fr.neatmonster.neatjvm.format.FieldType.ArrayType;
import fr.neatmonster.neatjvm.format.FieldType.BaseType;
import fr.neatmonster.neatjvm.format.FieldType.ObjectType;
import fr.neatmonster.neatjvm.format.attribute.CodeAttribute;

public class MethodInfo implements Resolvable {
    private final ClassFile       classFile;
    private final short           modifiers;
    private final short           nameIndex;
    private final short           descriptorIndex;
    private final AttributeInfo[] attributes;

    private String                name;
    private FieldType[]           paramsTypes;
    private FieldType             returnType;

    public MethodInfo(final ClassFile classFile, final ByteBuffer buf) {
        this.classFile = classFile;

        modifiers = buf.getShort();
        nameIndex = buf.getShort();
        descriptorIndex = buf.getShort();

        final short attributesCount = buf.getShort();
        attributes = new AttributeInfo[attributesCount];
        for (int i = 0; i < attributes.length; ++i) {
            final short index = buf.getShort();
            final int length = buf.getInt();
            try {
                final String name = ConstantInfo.getUtf8(classFile, index);
                final Class<? extends AttributeInfo> clazz = AttributeInfo.get(name);
                if (clazz == null)
                    buf.position(buf.position() + length);
                else
                    attributes[i] = clazz.getConstructor(ClassFile.class, ByteBuffer.class).newInstance(classFile, buf);
            } catch (final Exception e) {
                e.printStackTrace(System.err);
                System.exit(0);
            }
        }
    }

    public ClassFile getClassFile() {
        return classFile;
    }

    public AttributeInfo[] getAttributes() {
        return attributes;
    }

    public short getModifiers() {
        return modifiers;
    }

    public String getName() {
        return name;
    }

    public FieldType[] getParameterTypes() {
        return paramsTypes;
    }

    public FieldType getReturnType() {
        return returnType;
    }

    @Override
    public MethodInfo resolve() {
        if (name != null)
            return this;

        name = ConstantInfo.getUtf8(classFile, nameIndex);

        final String descriptor = ConstantInfo.getUtf8(classFile, descriptorIndex);
        try {
            final ByteBuffer buf = ByteBuffer.wrap(descriptor.getBytes("UTF-16BE"));

            buf.getChar();

            final List<FieldType> types = new ArrayList<>();
            while (true) {
                buf.mark();
                final FieldType type = FieldType.parseType(buf);
                if (type == null)
                    break;
                types.add(type);
            }
            buf.reset();
            paramsTypes = new FieldType[types.size()];
            types.toArray(paramsTypes);

            buf.getChar();

            returnType = FieldType.parseType(buf);
        } catch (final Exception e) {
            e.printStackTrace(System.err);
            System.exit(0);
        }

        return this;
    }

    public Object invoke(final ObjectData instance, final Object... args) {
        final Thread thread = new Thread(0);
        thread.start(MethodInfo.getCode(this));
        final StackFrame frame = thread.getFrame();

        int index = 0;
        if (instance instanceof InstanceData)
            frame.storeReference(index++, ((InstanceData) instance).getReference());
        for (final Object arg : args) {
            if (arg instanceof Integer)
                frame.storeInt(index++, (int) arg);
            else if (arg instanceof Long)
                frame.storeLong((index += 2) - 2, (long) arg);
            else if (arg instanceof Float)
                frame.storeFloat(index++, (float) arg);
            else if (arg instanceof Double)
                frame.storeDouble((index += 2) - 2, (double) arg);
            else {
                if (arg == null)
                    frame.storeReference(index++, 0);
                else
                    frame.storeReference(index++, ((InstanceData) arg).getReference());
            }
        }

        Object returnValue = null;
        while (thread.getState() != ThreadState.TERMINATED)
            returnValue = thread.tick();

        if (returnType instanceof BaseType) {
            switch ((BaseType) returnType) {
                case BOOLEAN:
                    return (int) returnValue > 0;
                case CHAR:
                    return (char) (int) returnValue;
                case FLOAT:
                    return (float) returnValue;
                case DOUBLE:
                    return (double) returnValue;
                case BYTE:
                    return (byte) (int) returnValue;
                case SHORT:
                    return (short) (int) returnValue;
                case INT:
                    return (int) returnValue;
                case LONG:
                    return (long) returnValue;
            }
        } else if (returnType instanceof ObjectType || returnType instanceof ArrayType)
            return returnValue;
        return null;
    }

    public static CodeAttribute getCode(final MethodInfo method) {
        for (final AttributeInfo attribute : method.attributes) {
            if (attribute == null)
                continue;
            if (attribute instanceof CodeAttribute)
                return (CodeAttribute) attribute.resolve();
        }
        return null;
    }

    public static String getDescriptor(final MethodInfo method) {
        String descriptor = "(";
        for (final FieldType type : method.paramsTypes)
            descriptor += type.toString();
        descriptor += ")";
        if (method.returnType == null)
            descriptor += "V";
        else
            descriptor += method.returnType.toString();
        return descriptor;
    }

    public static int getParametersSize(final MethodInfo method) {
        int paramsSize = 0;
        for (final FieldType type : method.paramsTypes)
            paramsSize += type.getSize() > 4 ? 2 : 1;
        return paramsSize;
    }
}
