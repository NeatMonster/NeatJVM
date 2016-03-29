package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class MethodHandlerConstant extends ConstantInfo {
    @SuppressWarnings("unused")
    private final byte  referenceKind;
    @SuppressWarnings("unused")
    private final short referenceIndex;

    public MethodHandlerConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        referenceKind = buf.get();
        referenceIndex = buf.getShort();
    }

    @Override
    public Object resolve() {
        // TODO Resolve this constant type
        return null;
    }
}