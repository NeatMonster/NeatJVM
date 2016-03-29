package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class FloatConstant extends ConstantInfo {
    private final float constant;

    public FloatConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        constant = buf.getFloat();
    }

    @Override
    public Float resolve() {
        return constant;
    }
}