package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.util.StringBuilder;

public class LongConstant extends ConstantInfo {
    public final long value;

    public LongConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        final ByteBuffer bufLoc = ByteBuffer.allocate(8);
        bufLoc.putInt(buf.getInt());
        bufLoc.putInt(buf.getInt());
        value = bufLoc.getLong(0);
    }

    @Override
    public void toString2(final StringBuilder s) {
        s.appendln("value: " + value);
    }
}