package fr.neatmonster.neatjvm.format;

import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.attribute.CodeAttribute;

public abstract class AttributeInfo implements Resolvable {
    // @formatter:off
    @SuppressWarnings("serial")
    private static final Map<String, Class<? extends AttributeInfo>> INTERNAL = new HashMap<String, Class<? extends AttributeInfo>>() {{
        put("Code", CodeAttribute.class);
    }};
    // @formatter:on

    public static Class<? extends AttributeInfo> get(final String name) {
        return INTERNAL.get(name);
    }

    protected final ClassFile classFile;

    protected AttributeInfo(final ClassFile classFile) {
        this.classFile = classFile;
    }

    public ClassFile getClassFile() {
        return classFile;
    }
}
