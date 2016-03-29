package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.InstanceData;
import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.format.MethodInfo;

public class InterfaceMethodrefConstant extends ConstantInfo {
    @SuppressWarnings("unused")
    private final short classIndex;
    private final short nameAndTypeIndex;

    public InterfaceMethodrefConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        classIndex = buf.getShort();
        nameAndTypeIndex = buf.getShort();
    }

    @Override
    public MethodInfo resolve() {
        return null;
    }

    public MethodInfo resolveOn(final InstanceData instance) {
        final String[] nameAndType = ConstantInfo.getNameAndType(classFile, nameAndTypeIndex);
        return instance.getClassFile().getMethod(nameAndType[0], nameAndType[1]);
    }
}