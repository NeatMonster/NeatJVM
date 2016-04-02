package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.AttributeInfo;
import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.format.attribute.BootstrapMethodsAttribute;
import fr.neatmonster.neatjvm.format.attribute.BootstrapMethodsAttribute.BootstrapMethod;

public class InvokeDynamicConstant extends ConstantInfo {
    private final short         bootstrapMethodAttrIndex;
    private final short         nameAndTypeIndex;

    private BootstrapMethod     bootstrapMethod;
    private NameAndTypeConstant nameAndType;

    public InvokeDynamicConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        bootstrapMethodAttrIndex = buf.getShort();
        nameAndTypeIndex = buf.getShort();
    }

    public BootstrapMethod getBootstrapMethod() {
        return bootstrapMethod;
    }

    public NameAndTypeConstant getNameAndType() {
        return nameAndType;
    }

    @Override
    public InvokeDynamicConstant resolve() {
        if (bootstrapMethod != null)
            return this;

        final BootstrapMethodsAttribute attr = AttributeInfo.getBootstrapMethods(classFile).resolve();
        bootstrapMethod = attr.getBootstrapMethods()[bootstrapMethodAttrIndex];
        nameAndType = ConstantInfo.getNameAndType(classFile, nameAndTypeIndex);
        return this;
    }
}