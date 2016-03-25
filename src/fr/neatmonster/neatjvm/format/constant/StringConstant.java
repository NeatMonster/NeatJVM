package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class StringConstant extends ConstantInfo {
    public final short stringIndex;

    public String      resolved;

    public StringConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        stringIndex = buf.getShort();
    }

    @Override
    public String resolve() {
        if (resolved != null)
            return resolved;

        return resolved = classFile.constants.getUtf8(stringIndex);
    }
}