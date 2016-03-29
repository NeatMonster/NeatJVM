package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class IntegerConstant extends ConstantInfo {
    private final int constant;

    public IntegerConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        constant = buf.getInt();
    }

    @Override
    public Integer resolve() {
        return constant;
    }
}