package fr.neatmonster.neatjvm.format.constant;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.format.FieldInfo;

public class FieldrefConstant extends ConstantInfo {
    public final short classIndex;
    public final short nameAndTypeIndex;

    public FieldInfo   resolved;

    public FieldrefConstant(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        classIndex = buf.getShort();
        nameAndTypeIndex = buf.getShort();
    }

    @Override
    public Object resolve() {
        // TODO Resolve this contant type
        return null;
    }
}