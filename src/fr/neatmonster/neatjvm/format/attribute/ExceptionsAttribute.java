package fr.neatmonster.neatjvm.format.attribute;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.AttributeInfo;

public class ExceptionsAttribute extends AttributeInfo {
    public short[] exceptionIndexes;

    public ExceptionsAttribute(ClassFile classFile, ByteBuffer buf) {
        super(classFile);

        short exceptionIndexesCount = buf.getShort();
        exceptionIndexes = new short[exceptionIndexesCount];
        for (int i = 0; i < exceptionIndexes.length; ++i)
            exceptionIndexes[i] = buf.getShort();
    }
}
