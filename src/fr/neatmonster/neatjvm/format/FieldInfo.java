package fr.neatmonster.neatjvm.format;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.util.StringBuilder;

public class FieldInfo {
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

    public void resolve() {
        name = classFile.constants.getUtf8(nameIndex);
        String descriptorStr = classFile.constants.getUtf8(descriptorIndex);
        try {
            descriptor = new FieldDescriptor(descriptorStr);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(0);
        }
    }
    
    public boolean isResolved() {
        return name != null;
    }

    public void toString(final StringBuilder s) {
        s.openObject(this);

        final List<String> flags = new ArrayList<>();
        for (final AccessFlag flag : AccessFlag.values())
            if (flag.field && (accessFlags & flag.value) > 0)
                flags.add(flag.name());
        s.appendln("accessFlags: " + Arrays.asList(flags.toArray()));

        s.appendln("nameIndex: " + nameIndex);
        s.appendln("descriptorIndex: " + descriptorIndex);

        s.append("attributes: ");
        s.openArray();
        for (final AttributeInfo attribute : attributes) {
            if (attribute == null)
                s.appendln("null");
            else
                attribute.toString(s);
        }
        s.closeArray();

        s.closeObject();
    }
}
