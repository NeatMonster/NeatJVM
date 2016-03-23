package fr.neatmonster.neatjvm;

import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.neatjvm.attribute.CodeAttribute;
import fr.neatmonster.neatjvm.attribute.ConstantValueAttribute;
import fr.neatmonster.neatjvm.attribute.ExceptionsAttribute;
import fr.neatmonster.neatjvm.util.StringBuilder;

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

    public void toString(final StringBuilder s) {
        s.openObject(this);
        toString2(s);
        s.closeObject();
    }

    public abstract void toString2(StringBuilder s);
}
