package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.format.FieldInfo;

public class FieldrefConstant extends ConstantInfo {
    private final short classIndex;
    private final short nameAndTypeIndex;

    private FieldInfo   field;

    public FieldrefConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        classIndex = buf.getShort();
        nameAndTypeIndex = buf.getShort();
    }

    @Override
    public FieldInfo resolve() {
        if (field != null)
            return field;

        final ClassFile classFile = ConstantInfo.getClassFile(this.classFile, classIndex);
        final NameAndTypeConstant nameAndType = ConstantInfo.getNameAndType(classFile, nameAndTypeIndex);
        return field = classFile.getField(nameAndType.getName(), nameAndType.getType());
    }
}