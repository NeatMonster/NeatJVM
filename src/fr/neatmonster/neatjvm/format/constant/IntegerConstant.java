package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class IntegerConstant extends ConstantInfo {
    public final int resolved;

    public IntegerConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        resolved = buf.getInt();
    }

    @Override
    public Integer resolve() {
        return resolved;
    }
}