package fr.neatmonster.neatjvm.format;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import fr.neatmonster.neatjvm.ClassFile;
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
                if (clazz == null) {
                    System.err.println("Unrecognized attribute info w/ name " + name);
                    buf.position(buf.position() + length);
                } else
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

    public static CodeAttribute getCode(final MethodInfo method) {
        for (final AttributeInfo attribute : method.attributes)
            if (attribute instanceof CodeAttribute)
                return (CodeAttribute) attribute;
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
