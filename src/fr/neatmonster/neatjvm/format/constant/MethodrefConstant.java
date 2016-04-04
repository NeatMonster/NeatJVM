package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.format.MethodInfo;

public class MethodrefConstant extends ConstantInfo {
    private final short classIndex;
    private final short nameAndTypeIndex;

    private MethodInfo  method;

    public MethodrefConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        classIndex = buf.getShort();
        nameAndTypeIndex = buf.getShort();
    }

    @Override
    public MethodInfo resolve() {
        if (method != null)
            return method;

        final ClassFile classFile = ConstantInfo.getClassFile(this.classFile, classIndex);
        final NameAndTypeConstant nameAndType = ConstantInfo.getNameAndType(this.classFile, nameAndTypeIndex);
        return method = classFile.getDeclaredMethod(nameAndType.getName(), nameAndType.getType());
    }
}