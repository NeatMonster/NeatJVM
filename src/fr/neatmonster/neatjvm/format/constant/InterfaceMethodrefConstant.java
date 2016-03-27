package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.InstanceData;
import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.format.MethodInfo;

public class InterfaceMethodrefConstant extends ConstantInfo {
    public final short classIndex;
    public final short nameAndTypeIndex;

    public InterfaceMethodrefConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        classIndex = buf.getShort();
        nameAndTypeIndex = buf.getShort();
    }

    @Override
    public Object resolve() {
        return null;
    }
    
    public MethodInfo resolveOn(InstanceData instance) {
        String[] nameAndType = classFile.constants.getNameAndType(nameAndTypeIndex);
        return instance.classFile.getMethod(nameAndType[0], nameAndType[1]);
    }
}