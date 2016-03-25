package fr.neatmonster.neatjvm.format;

import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.attribute.CodeAttribute;
import fr.neatmonster.neatjvm.format.attribute.ConstantValueAttribute;
import fr.neatmonster.neatjvm.format.attribute.ExceptionsAttribute;

public abstract class AttributeInfo {
    // @formatter:off
    @SuppressWarnings("serial")
    public static Map<String, Class<? extends AttributeInfo>> ALL = new HashMap<String, Class<? extends AttributeInfo>>() {{
        put("ConstantValue", ConstantValueAttribute.class);
        put("Code", CodeAttribute.class);
        put("Exceptions", ExceptionsAttribute.class);
    }};
    // @formatter:on

    public final ClassFile                                    classFile;

    public AttributeInfo(final ClassFile classFile) {
        this.classFile = classFile;
    }
}
