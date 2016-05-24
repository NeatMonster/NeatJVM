package fr.neatmonster.neatjvm.format;

import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.InstanceData;
import fr.neatmonster.neatjvm.format.constant.*;

public abstract class ConstantInfo implements Resolvable {
    // @formatter:off
    @SuppressWarnings("serial")
    private static final Map<Integer, Class<? extends ConstantInfo>> INTERNAL = new HashMap<Integer, Class<? extends ConstantInfo>>() {{
        put(1, Utf8Constant.class);
        put(3, IntegerConstant.class);
        put(4, FloatConstant.class);
        put(5, LongConstant.class);
        put(6, DoubleConstant.class);
        put(7, ClassConstant.class);
        put(8, StringConstant.class);
        put(9, FieldrefConstant.class);
        put(10, MethodrefConstant.class);
        put(11, InterfaceMethodrefConstant.class);
        put(12, NameAndTypeConstant.class);
        put(15, MethodHandleConstant.class);
        put(16, MethodTypeConstant.class);
        put(18, InvokeDynamicConstant.class);
    }};
    // @formatter:on

    public static Class<? extends ConstantInfo> get(final int type) {
        return INTERNAL.get(type);
    }

    public static String getUtf8(final ClassFile classFile, final int index) {
        return ((Utf8Constant) classFile.getConstants()[index - 1]).resolve();
    }

    public static ClassFile getClassFile(final ClassFile classFile, final int index) {
        return ((ClassConstant) classFile.getConstants()[index - 1]).resolve();
    }

    public static FieldInfo getFieldref(final ClassFile classFile, final int index) {
        return ((FieldrefConstant) classFile.getConstants()[index - 1]).resolve();
    }

    public static MethodInfo getMethodref(final ClassFile classFile, final int index) {
        return ((MethodrefConstant) classFile.getConstants()[index - 1]).resolve();
    }

    public static MethodInfo getInterfaceMethodref(final ClassFile classFile, final int index,
            final InstanceData instance) {
        return ((InterfaceMethodrefConstant) classFile.getConstants()[index - 1]).resolveOn(instance);
    }

    public static NameAndTypeConstant getNameAndType(final ClassFile classFile, final int index) {
        return ((NameAndTypeConstant) classFile.getConstants()[index - 1]).resolve();
    }

    public static MethodHandleConstant getMethodHandle(final ClassFile classFile, final int index) {
        return ((MethodHandleConstant) classFile.getConstants()[index - 1]).resolve();
    }

    public static MethodTypeConstant getMethodType(final ClassFile classFile, final int index) {
        return ((MethodTypeConstant) classFile.getConstants()[index - 1]).resolve();
    }

    public static InvokeDynamicConstant getInvokeDynamic(final ClassFile classFile, final int index) {
        return ((InvokeDynamicConstant) classFile.getConstants()[index - 1]).resolve();
    }

    protected final ClassFile classFile;

    protected ConstantInfo(final ClassFile classFile) {
        this.classFile = classFile;
    }
}
