package fr.neatmonster.neatjvm.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.ConstantInfo;
import fr.neatmonster.neatjvm.util.StringBuilder;

public class StringConstant extends ConstantInfo {
    public final short stringIndex;

    public StringConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        stringIndex = buf.getShort();
    }

    @Override
    public void toString2(final StringBuilder s) {
        s.appendln("stringIndex: " + stringIndex);
    }
}