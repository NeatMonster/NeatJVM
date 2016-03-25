package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class Utf8Constant extends ConstantInfo {
    public final String value;

    public Utf8Constant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        final short length = buf.getShort();
        final byte[] bytes = new byte[length];
        buf.get(bytes);
        value = new String(bytes);
    }

    @Override
    public String resolve() {
        return value;
    }
}