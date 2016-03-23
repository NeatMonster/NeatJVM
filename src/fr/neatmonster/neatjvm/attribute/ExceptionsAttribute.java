package fr.neatmonster.neatjvm.attribute;

import java.nio.ByteBuffer;
import java.util.Arrays;

import fr.neatmonster.neatjvm.AttributeInfo;
import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.util.StringBuilder;

public class ExceptionsAttribute extends AttributeInfo {
    public short[] exceptionIndexes;

    public ExceptionsAttribute(ClassFile classFile, ByteBuffer buf) {
        super(classFile);

        short exceptionIndexesCount = buf.getShort();
        exceptionIndexes = new short[exceptionIndexesCount];
        for (int i = 0; i < exceptionIndexes.length; ++i)
            exceptionIndexes[i] = buf.getShort();
    }

    @Override
    public void toString2(StringBuilder s) {
        s.appendln("exceptionIndexes: " + Arrays.toString(exceptionIndexes));
    }
}
