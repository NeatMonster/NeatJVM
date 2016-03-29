package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class MethodTypeConstant extends ConstantInfo {
    private final short descriptorIndex;

    private String      descriptor;

    public MethodTypeConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        descriptorIndex = buf.getShort();
    }

    @Override
    public String resolve() {
        if (descriptor != null)
            return descriptor;

        return descriptor = ConstantInfo.getUtf8(classFile, descriptorIndex);
    }
}