package fr.neatmonster.neatjvm.format;

import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.attribute.CodeAttribute;

public abstract class AttributeInfo {
    // @formatter:off
    @SuppressWarnings("serial")
    private static Map<String, Class<? extends AttributeInfo>> INTERNAL = new HashMap<String, Class<? extends AttributeInfo>>() {{
        put("Code", CodeAttribute.class);
    }};
    // @formatter:on

    public static Class<? extends AttributeInfo> get(final String name) {
        return INTERNAL.get(name);
    }

    private final ClassFile classFile;

    public AttributeInfo(final ClassFile classFile) {
        this.classFile = classFile;
    }

    public ClassFile getClassFile() {
        return classFile;
    }
}
