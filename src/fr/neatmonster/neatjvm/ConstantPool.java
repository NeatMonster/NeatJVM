package fr.neatmonster.neatjvm;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.format.FieldInfo;
import fr.neatmonster.neatjvm.format.MethodInfo;
import fr.neatmonster.neatjvm.format.constant.ClassConstant;
import fr.neatmonster.neatjvm.format.constant.DoubleConstant;
import fr.neatmonster.neatjvm.format.constant.FieldrefConstant;
import fr.neatmonster.neatjvm.format.constant.FloatConstant;
import fr.neatmonster.neatjvm.format.constant.IntegerConstant;
import fr.neatmonster.neatjvm.format.constant.LongConstant;
import fr.neatmonster.neatjvm.format.constant.MethodTypeConstant;
import fr.neatmonster.neatjvm.format.constant.MethodrefConstant;
import fr.neatmonster.neatjvm.format.constant.NameAndTypeConstant;
import fr.neatmonster.neatjvm.format.constant.StringConstant;
import fr.neatmonster.neatjvm.format.constant.Utf8Constant;

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

    public String getUtf8(final int index) {
        return ((Utf8Constant) constants[index - 1]).resolve();
    }

    public int getInteger(final int index) {
        return ((IntegerConstant) constants[index - 1]).resolve();
    }

    public float getFloat(final int index) {
        return ((FloatConstant) constants[index - 1]).resolve();
    }

    public long getLong(final int index) {
        return ((LongConstant) constants[index - 1]).resolve();
    }

    public double getDouble(final int index) {
        return ((DoubleConstant) constants[index - 1]).resolve();
    }

    public ClassFile getClass(final int index) {
        return ((ClassConstant) constants[index - 1]).resolve();
    }

    public String getString(final int index) {
        return ((StringConstant) constants[index - 1]).resolve();
    }

    public FieldInfo getFieldref(final int index) {
        return ((FieldrefConstant) constants[index - 1]).resolve();
    }

    public MethodInfo getMethodref(final int index) {
        return ((MethodrefConstant) constants[index - 1]).resolve();
    }

    public String[] getNameAndType(final int index) {
        return ((NameAndTypeConstant) constants[index - 1]).resolve();
    }

    public String getMethodType(final int index) {
        return ((MethodTypeConstant) constants[index - 1]).resolve();
    }
}
