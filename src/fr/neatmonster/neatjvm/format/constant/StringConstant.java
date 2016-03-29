package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class StringConstant extends ConstantInfo {
    private final short stringIndex;

    private String      string;

    public StringConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        stringIndex = buf.getShort();
    }

    @Override
    public String resolve() {
        if (string != null)
            return string;

        return string = ConstantInfo.getUtf8(classFile, stringIndex);
    }
}