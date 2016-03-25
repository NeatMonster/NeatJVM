package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class MethodTypeConstant extends ConstantInfo {
    public final short descriptorIndex;
    
    public String resolved;

    public MethodTypeConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        descriptorIndex = buf.getShort();
    }

    @Override
    public String resolve() {
        if (resolved != null)
            return resolved;
        
        return resolved = classFile.constants.getUtf8(descriptorIndex);
    }
}