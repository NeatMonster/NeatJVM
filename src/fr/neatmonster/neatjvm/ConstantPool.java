package fr.neatmonster.neatjvm;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.format.constant.ClassConstant;
import fr.neatmonster.neatjvm.format.constant.Utf8Constant;
import fr.neatmonster.neatjvm.util.StringBuilder;

public class ConstantPool {
    public ClassFile      classFile;
    public ConstantInfo[] constants;

    public ConstantPool(final ClassFile classFile, final ByteBuffer buf) {
        final short constantsCount = buf.getShort();
        constants = new ConstantInfo[constantsCount - 1];
        for (int i = 0; i < constants.length; ++i) {
            final int tag = buf.get();
            try {
                final Class<? extends ConstantInfo> clazz = ConstantInfo.ALL.get(tag);
                if (clazz == null)
                    System.err.println("Unrecognized constant info w/ tag " + tag);
                else
                    constants[i] = clazz.getConstructor(ClassFile.class, ByteBuffer.class).newInstance(classFile, buf);
            } catch (final Exception e) {
                e.printStackTrace(System.err);
                System.exit(0);
            }
        }
    }

    public ClassConstant getClass(final int index) {
        return (ClassConstant) constants[index - 1];
    }

    public String getUtf8(final int index) {
        return ((Utf8Constant) constants[index - 1]).value;
    }

    public void toString(final StringBuilder s) {
        s.append("constants: ");
        s.openArray();
        for (final ConstantInfo constant : constants) {
            if (constant == null)
                s.appendln("null");
            else
                constant.toString(s);
        }
        s.closeArray();
    }
}
