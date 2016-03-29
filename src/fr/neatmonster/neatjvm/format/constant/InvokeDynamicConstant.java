package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class InvokeDynamicConstant extends ConstantInfo {
    @SuppressWarnings("unused")
    private final short bootstrapMethodAttrIndex;
    @SuppressWarnings("unused")
    private final short nameAndTypeIndex;

    public InvokeDynamicConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        bootstrapMethodAttrIndex = buf.getShort();
        nameAndTypeIndex = buf.getShort();
    }

    @Override
    public Object resolve() {
        // TODO Resolve this constant type
        return null;
    }
}