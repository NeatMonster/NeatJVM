package fr.neatmonster.neatjvm;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.neatmonster.neatjvm.format.AccessFlag;
import fr.neatmonster.neatjvm.format.AttributeInfo;
import fr.neatmonster.neatjvm.format.FieldInfo;
import fr.neatmonster.neatjvm.format.MethodInfo;
import fr.neatmonster.neatjvm.format.attribute.CodeAttribute;
import fr.neatmonster.neatjvm.util.StringBuilder;

public class ClassFile {
    public final int             magic;
    public final short           minorVersion;
    public final short           majorVersion;
    public final ConstantPool    constants;
    public final short           accessFlags;
    public final short           thisClass;
    public final short           superClass;
    public final short[]         interfaces;
    public final FieldInfo[]     fields;
    public final MethodInfo[]    methods;
    public final AttributeInfo[] attributes;

    public ClassFile(final ClassLoader classLoader, final ByteBuffer buf) {
        magic = buf.getInt();
        minorVersion = buf.getShort();
        majorVersion = buf.getShort();

        constants = new ConstantPool(this, buf);

        accessFlags = buf.getShort();
        thisClass = buf.getShort();
        superClass = buf.getShort();

        final short interfacesCount = buf.getShort();
        interfaces = new short[interfacesCount];
        for (int i = 0; i < interfaces.length; ++i)
            interfaces[i] = buf.getShort();

        final short fieldsCount = buf.getShort();
        fields = new FieldInfo[fieldsCount];
        for (int i = 0; i < fields.length; ++i)
            fields[i] = new FieldInfo(this, buf);

        final short methodsCount = buf.getShort();
        methods = new MethodInfo[methodsCount];
        for (int i = 0; i < methods.length; ++i)
            methods[i] = new MethodInfo(this, buf);

        final short attributesCount = buf.getShort();
        attributes = new AttributeInfo[attributesCount];
        for (int i = 0; i < attributes.length; ++i) {
            final short index = buf.getShort();
            final int length = buf.getInt();
            try {
                final String name = constants.getUtf8(index);
                final Class<? extends AttributeInfo> clazz = AttributeInfo.ALL.get(name);
                if (clazz == null) {
                    System.err.println("Unrecognized attribute info w/ name " + name);
                    buf.position(buf.position() + length);
                } else
                    attributes[i] = clazz.getConstructor(ClassFile.class, ByteBuffer.class).newInstance(this, buf);
            } catch (final Exception e) {
                e.printStackTrace(System.err);
                System.exit(0);
            }
        }
    }

    public CodeAttribute getMainMethod() {
        for (final MethodInfo method : methods)
            if (constants.getUtf8(method.nameIndex).equals("main"))
                for (final AttributeInfo attribute : method.attributes)
                    if (attribute instanceof CodeAttribute)
                        return (CodeAttribute) attribute;
        return null;
    }

    public void toString(final StringBuilder s) {
        s.openObject(this);

        s.appendln("magic: 0x" + Integer.toHexString(magic));
        s.appendln("version: " + majorVersion + "." + minorVersion);

        constants.toString(s);

        final List<String> flags = new ArrayList<>();
        for (final AccessFlag flag : AccessFlag.values())
            if (flag.clazz && flag.eval(accessFlags))
                flags.add(flag.name());
        s.appendln("accessFlags: " + Arrays.asList(flags.toArray()));

        s.appendln("thisClass: " + thisClass);
        s.appendln("superClass: " + superClass);

        s.appendln("interfaces: " + Arrays.toString(interfaces));

        s.append("fields: ");
        s.openArray();
        for (final FieldInfo field : fields)
            field.toString(s);
        s.closeArray();

        s.append("methods: ");
        s.openArray();
        for (final MethodInfo method : methods)
            method.toString(s);
        s.closeArray();

        s.append("attributes: ");
        s.openArray();
        for (final AttributeInfo attribute : attributes) {
            if (attribute == null)
                s.appendln("null");
            else
                attribute.toString(s);
        }
        s.closeArray();

        s.closeObject();
    }
}
