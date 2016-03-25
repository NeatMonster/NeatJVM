package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class ClassConstant extends ConstantInfo {
    public final short nameIndex;

    public ClassFile   resolved;

    public ClassConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        nameIndex = buf.getShort();
    }

    @Override
    public ClassFile resolve() {
        if (resolved != null)
            return resolved;

        String resolvedName = classFile.constants.getUtf8(nameIndex);
        resolved = classFile.loader.getClass(resolvedName);
        if (resolved == null)
            resolved = classFile.loader.loadClass(resolvedName);
        return resolved;
    }
}