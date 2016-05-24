package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.VirtualMachine;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class StringConstant extends ConstantInfo {
    private final short stringIndex;

    private int         stringref;

    public StringConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        stringIndex = buf.getShort();
    }

    @Override
    public Integer resolve() {
        if (stringref != 0)
            return stringref;

        final String string = ConstantInfo.getUtf8(classFile, stringIndex);
        return stringref = VirtualMachine.getInstancePool().addString(string).getReference();
    }
}