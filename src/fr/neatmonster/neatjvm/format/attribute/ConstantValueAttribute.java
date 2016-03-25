package fr.neatmonster.neatjvm.format.attribute;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.AttributeInfo;

public class ConstantValueAttribute extends AttributeInfo {
    public short constantValueIndex;

    public ConstantValueAttribute(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        constantValueIndex = buf.getShort();
    }
}