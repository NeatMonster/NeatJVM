package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class NameAndTypeConstant extends ConstantInfo {
    private final short nameIndex;
    private final short descriptorIndex;

    private String[]    nameAndType;

    public NameAndTypeConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        nameIndex = buf.getShort();
        descriptorIndex = buf.getShort();
    }

    @Override
    public String[] resolve() {
        if (nameAndType != null)
            return nameAndType;

        return nameAndType = new String[] { ConstantInfo.getUtf8(classFile, nameIndex),
                ConstantInfo.getUtf8(classFile, descriptorIndex) };
    }
}