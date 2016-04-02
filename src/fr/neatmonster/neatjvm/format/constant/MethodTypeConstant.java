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

    public String getType() {
        return descriptor;
    }

    @Override
    public MethodTypeConstant resolve() {
        if (descriptor != null)
            return this;

        descriptor = ConstantInfo.getUtf8(classFile, descriptorIndex);
        return this;
    }
}