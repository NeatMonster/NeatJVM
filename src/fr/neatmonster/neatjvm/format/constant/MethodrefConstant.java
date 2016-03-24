package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.format.MethodInfo;
import fr.neatmonster.neatjvm.util.StringBuilder;

public class MethodrefConstant extends ConstantInfo {
    public final short classIndex;
    public final short nameAndTypeIndex;

    public MethodInfo  method;

    public MethodrefConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        classIndex = buf.getShort();
        nameAndTypeIndex = buf.getShort();
    }

    public void resolve() {
        ClassConstant classInfo = classFile.constants.getClass(classIndex);
        if (!classInfo.isResolved())
            classInfo.resolve();

        NameAndTypeConstant nameAndTypeInfo = classFile.constants.getNameAndType(nameAndTypeIndex);
        if (!nameAndTypeInfo.isResolved())
            nameAndTypeInfo.resolve();
        
        method = classInfo.resolvedClass.getMethod(nameAndTypeInfo.name, nameAndTypeInfo.descriptor);
    }

    public boolean isResolved() {
        return method != null;
    }

    @Override
    public void toString2(final StringBuilder s) {
        s.appendln("classIndex: " + classIndex);
        s.appendln("nameAndTypeIndex: " + nameAndTypeIndex);
    }
}