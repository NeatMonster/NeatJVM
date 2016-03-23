package fr.neatmonster.neatjvm.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.ConstantInfo;
import fr.neatmonster.neatjvm.util.StringBuilder;

public class FloatConstant extends ConstantInfo {
    public final float value;

    public FloatConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        value = buf.getFloat();
    }

    @Override
    public void toString2(final StringBuilder s) {
        s.appendln("value: " + value);
    }
}