package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class LongConstant extends ConstantInfo {
    private final long constant;

    public LongConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        constant = buf.getLong();
    }

    @Override
    public Long resolve() {
        return constant;
    }
}