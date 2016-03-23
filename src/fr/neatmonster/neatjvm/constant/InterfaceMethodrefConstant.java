package fr.neatmonster.neatjvm.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.ConstantInfo;
import fr.neatmonster.neatjvm.util.StringBuilder;

public class InterfaceMethodrefConstant extends ConstantInfo {
    public final short classIndex;
    public final short nameAndTypeIndex;

    public InterfaceMethodrefConstant(final ClassFile classFile, final ByteBuffer buf) {
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