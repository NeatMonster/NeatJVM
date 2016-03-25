package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class DoubleConstant extends ConstantInfo {
    public final double resolved;

    public DoubleConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        resolved = buf.getDouble();
    }

    @Override
    public Double resolve() {
        return resolved;
    }
}