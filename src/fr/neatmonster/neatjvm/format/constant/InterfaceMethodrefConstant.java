package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class InterfaceMethodrefConstant extends ConstantInfo {
    public final short classIndex;
    public final short nameAndTypeIndex;

    public InterfaceMethodrefConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        classIndex = buf.getShort();
        nameAndTypeIndex = buf.getShort();
    }

    @Override
    public Object resolve() {
        // TODO Resolve this constant type
        return null;
    }
}