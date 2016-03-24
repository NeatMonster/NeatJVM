package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.util.StringBuilder;

public class NameAndTypeConstant extends ConstantInfo {
    public final short nameIndex;
    public final short descriptorIndex;
    
    public String name;
    public String descriptor;

    public NameAndTypeConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        nameIndex = buf.getShort();
        descriptorIndex = buf.getShort();
    }
    
    public void resolve() {
        name = classFile.constants.getUtf8(nameIndex);
        descriptor = classFile.constants.getUtf8(descriptorIndex);
    }
    
    public boolean isResolved() {
        return name != null;
    }

    @Override
    public void toString2(final StringBuilder s) {
        s.appendln("nameIndex: " + nameIndex);
        s.appendln("descriptorIndex: " + descriptorIndex);
    }
}