package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class MethodTypeConstant extends ConstantInfo {
    @SuppressWarnings("unused")
    private final short descriptorIndex;

    public MethodTypeConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        descriptorIndex = buf.getShort();
    }

    @Override
    public String resolve() {
        // TODO Resolve this constant type
        return null;
    }
}