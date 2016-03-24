package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.util.StringBuilder;

public class IntegerConstant extends ConstantInfo {
    public final int value;

    public IntegerConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        value = buf.getInt();
    }

    @Override
    public void toString2(final StringBuilder s) {
        s.appendln("value: " + value);
    }
}