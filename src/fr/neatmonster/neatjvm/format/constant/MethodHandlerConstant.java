package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.util.StringBuilder;

public class MethodHandlerConstant extends ConstantInfo {
    public final byte  referenceKind;
    public final short referenceIndex;

    public MethodHandlerConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        referenceKind = buf.get();
        referenceIndex = buf.getShort();
    }

    @Override
    public void toString2(final StringBuilder s) {
        s.appendln("referenceKind: " + referenceKind);
        s.appendln("referenceIndex: " + referenceIndex);
    }
}