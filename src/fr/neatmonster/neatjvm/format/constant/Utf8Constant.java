package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.util.StringBuilder;

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
    public void toString2(final StringBuilder s) {
        s.appendln("value: \"" + value + "\"");
    }
}