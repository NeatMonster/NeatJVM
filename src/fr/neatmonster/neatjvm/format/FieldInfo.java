package fr.neatmonster.neatjvm.format;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;

public class FieldInfo implements Resolvable {
    private final ClassFile       classFile;
    private final short           modifiers;
    private final short           nameIndex;
    private final short           descriptorIndex;
    private final AttributeInfo[] attributes;

    private String                name;
    private FieldType             type;

    public FieldInfo(final ClassFile classFile, final ByteBuffer buf) {
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

    public FieldType getType() {
        return type;
    }

    @Override
    public FieldInfo resolve() {
        if (name != null)
            return this;

        name = ConstantInfo.getUtf8(classFile, nameIndex);

        final String descriptor = ConstantInfo.getUtf8(classFile, descriptorIndex);
        try {
            type = FieldType.parseType(ByteBuffer.wrap(descriptor.getBytes("UTF-16BE")));
        } catch (final Exception e) {
            e.printStackTrace(System.err);
            System.exit(0);
        }

        return this;
    }

    public static int getParameterSize(final FieldInfo field) {
        return field.getType().getSize() > 4 ? 2 : 1;
    }
}
