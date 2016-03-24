package fr.neatmonster.neatjvm.format;

import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.constant.ClassConstant;
import fr.neatmonster.neatjvm.format.constant.DoubleConstant;
import fr.neatmonster.neatjvm.format.constant.FieldrefConstant;
import fr.neatmonster.neatjvm.format.constant.FloatConstant;
import fr.neatmonster.neatjvm.format.constant.IntegerConstant;
import fr.neatmonster.neatjvm.format.constant.InterfaceMethodrefConstant;
import fr.neatmonster.neatjvm.format.constant.InvokeDynamicConstant;
import fr.neatmonster.neatjvm.format.constant.LongConstant;
import fr.neatmonster.neatjvm.format.constant.MethodHandlerConstant;
import fr.neatmonster.neatjvm.format.constant.MethodeTypeConstant;
import fr.neatmonster.neatjvm.format.constant.MethodrefConstant;
import fr.neatmonster.neatjvm.format.constant.NameAndTypeConstant;
import fr.neatmonster.neatjvm.format.constant.StringConstant;
import fr.neatmonster.neatjvm.format.constant.Utf8Constant;
import fr.neatmonster.neatjvm.util.StringBuilder;

public abstract class ConstantInfo {
    // @formatter:off
    @SuppressWarnings("serial")
    public static Map<Integer, Class<? extends ConstantInfo>> ALL = new HashMap<Integer, Class<? extends ConstantInfo>>() {{
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
        put(15, MethodHandlerConstant.class);
        put(16, MethodeTypeConstant.class);
        put(18, InvokeDynamicConstant.class);
    }};
    // @formatter:on

    public final ClassFile                                    classFile;

    public ConstantInfo(final ClassFile classFile) {
        this.classFile = classFile;
    }

    public void toString(final StringBuilder s) {
        s.openObject(this);
        toString2(s);
        s.closeObject();
    }

    public abstract void toString2(StringBuilder s);
}
