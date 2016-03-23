package fr.neatmonster.neatjvm;

public enum AccessFlag {
    // @formatter:off
    ACC_PUBLIC(0x0001, true, true, true),
    ACC_PRIVATE(0x0002, false, true, true),
    ACC_PROTECTED(0x0004, false, true, true),
    ACC_STATIC(0x0008, false, true, true),
    ACC_FINAL(0x0010, true, true, true),
    ACC_SUPER(0x0020, true, false, false),
    ACC_SYNCHRONIZED(0x0020, false, false, true),
    ACC_VOLATILE(0x0040, false, true, false),
    ACC_BRIDGE(0x0040, false, false, true),
    ACC_TRANSIENT(0x0080, false, true, false),
    ACC_VARARGS(0x0080, false, false, true),
    ACC_NATIVE(0x0100, false, false, true),
    ACC_INTERFACE(0x0200, true, false, false),
    ACC_ABSTRACT(0x0400, true, false, true),
    ACC_STRICT(0x0800, false, false, true),
    ACC_SYNTHETIC(0x1000, true, true, true),
    ACC_ANNOTATION(0x2000, true, false, false),
    ACC_ENUM(0x4000, true, true, false);
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
