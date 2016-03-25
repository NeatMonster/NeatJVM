package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.format.MethodInfo;

public class MethodrefConstant extends ConstantInfo {
    public final short classIndex;
    public final short nameAndTypeIndex;

    public MethodInfo  resolved;

    public MethodrefConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        classIndex = buf.getShort();
        nameAndTypeIndex = buf.getShort();
    }

    @Override
    public MethodInfo resolve() {
        if (resolved != null)
            return resolved;

        ClassFile classFile = this.classFile.constants.getClass(classIndex);
        String[] nameAndType = this.classFile.constants.getNameAndType(nameAndTypeIndex);
        return resolved = classFile.getMethod(nameAndType[0], nameAndType[1]);
    }
}