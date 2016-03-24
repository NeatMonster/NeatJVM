package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.util.StringBuilder;

public class ClassConstant extends ConstantInfo {
    public final short nameIndex;
    
    public ClassFile resolvedClass;

    public ClassConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        nameIndex = buf.getShort();
    }
    
    public void resolve() {
        String resolvedName = classFile.constants.getUtf8(nameIndex);
        
        resolvedClass = classFile.loader.getClass(resolvedName);
        if (resolvedClass != null)
            return;
        
        resolvedClass = classFile.loader.loadClass(resolvedName);
    }
    
    public boolean isResolved() {
        return resolvedClass != null;
    }

    @Override
    public void toString2(final StringBuilder s) {
        s.appendln("nameIndex: " + nameIndex);
    }
}