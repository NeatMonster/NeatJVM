package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class NameAndTypeConstant extends ConstantInfo {
    private final short nameIndex;
    private final short descriptorIndex;

    private String      name;
    private String      descriptor;

    public NameAndTypeConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        nameIndex = buf.getShort();
        descriptorIndex = buf.getShort();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return descriptor;
    }

    @Override
    public NameAndTypeConstant resolve() {
        if (name != null)
            return this;

        name = ConstantInfo.getUtf8(classFile, nameIndex);
        descriptor = ConstantInfo.getUtf8(classFile, descriptorIndex);
        return this;
    }
}