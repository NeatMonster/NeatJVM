package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.util.StringBuilder;

public class MethodeTypeConstant extends ConstantInfo {
    public final short descriptorIndex;

    public MethodeTypeConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        descriptorIndex = buf.getShort();
    }

    @Override
    public void toString2(final StringBuilder s) {
        s.appendln("descriptorIndex: " + descriptorIndex);
    }
}