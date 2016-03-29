package fr.neatmonster.neatjvm;

import fr.neatmonster.neatjvm.format.FieldInfo;

public abstract class ObjectData {
    private final ClassFile classFile;
    protected int           dataStart;

    public ObjectData(final ClassFile classFile) {
        this.classFile = classFile;
    }

    public ClassFile getClassFile() {
        return classFile;
    }

    public abstract void get(final FieldInfo field, final byte[] value);

    public abstract void put(final FieldInfo field, final byte[] value);
}
