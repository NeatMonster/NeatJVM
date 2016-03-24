package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.util.StringBuilder;

public class InvokeDynamicConstant extends ConstantInfo {
    public final short bootstrapMethodAttrIndex;
    public final short nameAndTypeIndex;

    public InvokeDynamicConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        bootstrapMethodAttrIndex = buf.getShort();
        nameAndTypeIndex = buf.getShort();
    }

    @Override
    public void toString2(final StringBuilder s) {
        s.appendln("bootstrapMethodAttrIndex: " + bootstrapMethodAttrIndex);
        s.appendln("nameAndTypeIndex: " + nameAndTypeIndex);
    }
}