package fr.neatmonster.neatjvm;

public class ObjectData {
    public ClassFile classFile;
    public int       dataStart;

    public ObjectData(final ClassFile classFile) {
        this.classFile = classFile;
    }
}
