package fr.neatmonster.neatjvm.format;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;

public class FieldInfo implements Resolvable {
    public final ClassFile       classFile;
    public final short           accessFlags;
    public final short           nameIndex;
    public final short           descriptorIndex;
    public final AttributeInfo[] attributes;

    public String                name;
    public FieldDescriptor       descriptor;

    public FieldInfo(final ClassFile classFile, final ByteBuffer buf) {
        this.classFile = classFile;

        accessFlags = buf.getShort();
        nameIndex = buf.getShort();
        descriptorIndex = buf.getShort();

        final short attributesCount = buf.getShort();
        attributes = new AttributeInfo[attributesCount];
        for (int i = 0; i < attributes.length; ++i) {
            final short index = buf.getShort();
            final int length = buf.getInt();
            try {
                final String name = classFile.constants.getUtf8(index);
                final Class<? extends AttributeInfo> clazz = AttributeInfo.ALL.get(name);
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

    public FieldInfo resolve() {
        if (name != null)
            return this;

        name = classFile.constants.getUtf8(nameIndex);
        String descriptorStr = classFile.constants.getUtf8(descriptorIndex);
        try {
            descriptor = new FieldDescriptor(descriptorStr);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(0);
        }
        return this;
    }
}
