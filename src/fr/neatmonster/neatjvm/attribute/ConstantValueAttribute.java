package fr.neatmonster.neatjvm.attribute;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.AttributeInfo;
import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.util.StringBuilder;

public class ConstantValueAttribute extends AttributeInfo {
    public short constantValueIndex;

    public ConstantValueAttribute(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        constantValueIndex = buf.getShort();
    }

    @Override
    public void toString2(final StringBuilder s) {
        s.appendln("constantValueIndex: " + constantValueIndex);
    }
}