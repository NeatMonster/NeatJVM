package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class MethodHandleConstant extends ConstantInfo {
    // @formatter:off
    public static enum ReferenceKind {
        GET_FIELD,
        GET_STATIC,
        PUT_FIELD,
        PUT_STATIC,
        INVOKE_VIRTUAL,
        INVOKE_STATIC,
        INVOKE_SPECIAL,
        NEW_INVOKE_SPECIAL,
        INVOKE_INTERFACE
    }
    // @formatter:on

    private final ReferenceKind referenceKind;
    private final short         referenceIndex;

    private ConstantInfo        reference;

    public MethodHandleConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        final int kind = buf.get() - 1;
        referenceKind = ReferenceKind.values()[kind];
        referenceIndex = buf.getShort();
    }

    public ReferenceKind getKind() {
        return referenceKind;
    }

    public ConstantInfo getReference() {
        return reference;
    }

    @Override
    public MethodHandleConstant resolve() {
        if (reference != null)
            return this;

        reference = classFile.getConstants()[referenceIndex];
        reference.resolve();
        return this;
    }
}