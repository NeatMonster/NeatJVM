package fr.neatmonster.neatjvm.format;

import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.attribute.BootstrapMethodsAttribute;
import fr.neatmonster.neatjvm.format.attribute.CodeAttribute;

public abstract class AttributeInfo implements Resolvable {
    // @formatter:off
    @SuppressWarnings("serial")
    private static final Map<String, Class<? extends AttributeInfo>> INTERNAL = new HashMap<String, Class<? extends AttributeInfo>>() {{
        put("Code", CodeAttribute.class);
        put("BootstrapMethods", BootstrapMethodsAttribute.class);
    }};
    // @formatter:on

    public static Class<? extends AttributeInfo> get(final String name) {
        return INTERNAL.get(name);
    }

    public static BootstrapMethodsAttribute getBootstrapMethods(final ClassFile classFile) {
        for (final AttributeInfo attribute : classFile.getAttributes())
            if (attribute instanceof BootstrapMethodsAttribute)
                return (BootstrapMethodsAttribute) attribute;
        return null;
    }

    protected final ClassFile classFile;

    protected AttributeInfo(final ClassFile classFile) {
        this.classFile = classFile;
    }

    public ClassFile getClassFile() {
        return classFile;
    }
}
