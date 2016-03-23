package fr.neatmonster.neatjvm;

public enum AccessFlag {
    // @formatter:off
    PUBLIC(      0x0001,  true,  true,  true),
    PRIVATE(     0x0002, false,  true,  true),
    PROTECTED(   0x0004, false,  true,  true),
    STATIC(      0x0008, false,  true,  true),
    FINAL(       0x0010,  true,  true,  true),
    SUPER(       0x0020,  true, false, false),
    SYNCHRONIZED(0x0020, false, false,  true),
    VOLATILE(    0x0040, false,  true, false),
    BRIDGE(      0x0040, false, false,  true),
    TRANSIENT(   0x0080, false,  true, false),
    VARARGS(     0x0080, false, false,  true),
    NATIVE(      0x0100, false, false,  true),
    INTERFACE(   0x0200,  true, false, false),
    ABSTRACT(    0x0400,  true, false,  true),
    STRICT(      0x0800, false, false, true),
    SYNTHETIC(   0x1000,  true,  true,  true),
    ANNOTATION(  0x2000,  true, false, false),
    ENUM(        0x4000,  true,  true, false);
    // @formatter:on

    public short   value;
    public boolean clazz;
    public boolean field;
    public boolean method;

    private AccessFlag(final int value, final boolean clazz, final boolean field, final boolean method) {
        this.value = (short) value;
        this.clazz = clazz;
        this.field = field;
        this.method = method;
    }
}
