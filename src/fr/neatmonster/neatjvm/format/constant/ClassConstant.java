package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class ClassConstant extends ConstantInfo {
    private final short nameIndex;

    private ClassFile   classFile;

    public ClassConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        nameIndex = buf.getShort();
    }

    @Override
    public ClassFile resolve() {
        if (classFile != null)
            return classFile;

        final String className = ConstantInfo.getUtf8(super.classFile, nameIndex);
        return classFile = super.classFile.getClassLoader().loadClass(className);
    }
}