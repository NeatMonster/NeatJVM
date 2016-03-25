package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class NameAndTypeConstant extends ConstantInfo {
    public final short nameIndex;
    public final short descriptorIndex;

    public String[]    resolved;

    public NameAndTypeConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        nameIndex = buf.getShort();
        descriptorIndex = buf.getShort();
    }

    @Override
    public String[] resolve() {
        if (resolved != null)
            return resolved;

        return resolved = new String[] { classFile.constants.getUtf8(nameIndex),
                classFile.constants.getUtf8(descriptorIndex) };
    }
}