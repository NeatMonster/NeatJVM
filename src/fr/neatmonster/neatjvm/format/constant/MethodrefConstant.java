package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.util.StringBuilder;

public class MethodrefConstant extends ConstantInfo {
    public final short classIndex;
    public final short nameAndTypeIndex;

    public MethodrefConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        classIndex = buf.getShort();
        nameAndTypeIndex = buf.getShort();
    }

    @Override
    public void toString2(final StringBuilder s) {
        s.appendln("classIndex: " + classIndex);
        s.appendln("nameAndTypeIndex: " + nameAndTypeIndex);
    }
}