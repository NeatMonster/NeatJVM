package fr.neatmonster.neatjvm.format;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.InstanceData;
import fr.neatmonster.neatjvm.ObjectData;
import fr.neatmonster.neatjvm.VirtualMachine;

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

    private ByteBuffer getHelper(final ObjectData instance) {
        final ByteBuffer buf = ByteBuffer.allocate(type.getSize());
        instance.get(this, buf.array());
        return buf;
    }

    public InstanceData get(final ObjectData instance) {
        final int objectref = getHelper(instance).getInt();
        return VirtualMachine.getInstancePool().getInstance(objectref);
    }

    public boolean getBoolean(final ObjectData instance) {
        return getHelper(instance).get() != 0;
    }

    public byte getByte(final ObjectData instance) {
        return getHelper(instance).get();
    }

    public char getChar(final ObjectData instance) {
        return getHelper(instance).getChar();
    }

    public double getDouble(final ObjectData instance) {
        return getHelper(instance).getDouble();
    }

    public float getFloat(final ObjectData instance) {
        return getHelper(instance).getFloat();
    }

    public int getInt(final ObjectData instance) {
        return getHelper(instance).getInt();
    }

    public long getLong(final ObjectData instance) {
        return getHelper(instance).getLong();
    }

    public short getShort(final ObjectData instance) {
        return getHelper(instance).getShort();
    }

    public void set(final ObjectData instance, final InstanceData value) {
        setHelper(instance, ByteBuffer.allocate(4).putInt(value.getReference()));
    }

    public void setBoolean(final ObjectData instance, final boolean value) {
        setHelper(instance, ByteBuffer.allocate(1).put(value ? (byte) 1 : (byte) 0));
    }

    public void setByte(final ObjectData instance, final byte value) {
        setHelper(instance, ByteBuffer.allocate(1).put(value));
    }

    public void setChar(final ObjectData instance, final char value) {
        setHelper(instance, ByteBuffer.allocate(2).putChar(value));
    }

    public void setDouble(final ObjectData instance, final double value) {
        setHelper(instance, ByteBuffer.allocate(8).putDouble(value));
    }

    public void setFloat(final ObjectData instance, final float value) {
        setHelper(instance, ByteBuffer.allocate(4).putFloat(value));
    }

    public void setInt(final ObjectData instance, final int value) {
        setHelper(instance, ByteBuffer.allocate(4).putInt(value));
    }

    public void setLong(final ObjectData instance, final long value) {
        setHelper(instance, ByteBuffer.allocate(8).putLong(value));
    }

    public void setShort(final ObjectData instance, final short value) {
        setHelper(instance, ByteBuffer.allocate(2).putShort(value));
    }

    private void setHelper(final ObjectData instance, final ByteBuffer buf) {
        instance.put(this, buf.array());
    }

    public static int getParameterSize(final FieldInfo field) {
        return field.getType().getSize() > 4 ? 2 : 1;
    }
}
