package fr.neatmonster.neatjvm;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.neatmonster.neatjvm.utils.StringBuilder;

public class ClassFile {
    public class AttributeInfo {
        private final short attributeNameIndex;
        private final int   attributeLength;

        public AttributeInfo(final ByteBuffer buf) {
            attributeNameIndex = buf.getShort();
            attributeLength = buf.getInt();

            // TODO: Read specific attributes
            buf.position(buf.position() + attributeLength);
        }

        public void toString(final StringBuilder s) {
            s.openObject(this);
            s.appendln("attributeNameIndex: " + attributeNameIndex);
            s.appendln("attributeLength: " + attributeLength);
            s.closeObject();
        }
    }

    public class FieldInfo {
        private final short           accessFlags;
        private final short           nameIndex;
        private final short           descriptorIndex;
        private final AttributeInfo[] attributes;

        public FieldInfo(final ByteBuffer buf) {
            accessFlags = buf.getShort();
            nameIndex = buf.getShort();
            descriptorIndex = buf.getShort();

            final short attributesCount = buf.getShort();
            attributes = new AttributeInfo[attributesCount];
            for (int i = 0; i < attributes.length; ++i)
                attributes[i] = new AttributeInfo(buf);
        }

        public void toString(final StringBuilder s) {
            s.openObject(this);

            final List<String> flags = new ArrayList<>();
            for (final AccessFlag flag : AccessFlag.values())
                if (flag.field && (accessFlags & flag.value) > 0)
                    flags.add(flag.name());
            s.appendln("accessFlags: " + Arrays.asList(flags.toArray()));

            s.appendln("nameIndex: " + nameIndex);
            s.appendln("descriptorIndex: " + descriptorIndex);

            s.append("attributes: ");
            s.openArray();
            for (final AttributeInfo attribute : attributes)
                attribute.toString(s);
            s.closeArray();
            s.closeObject();
        }
    }

    public class MethodInfo {
        private final short           accessFlags;
        private final short           nameIndex;
        private final short           descriptorIndex;
        private final AttributeInfo[] attributes;

        public MethodInfo(final ByteBuffer buf) {
            accessFlags = buf.getShort();
            nameIndex = buf.getShort();
            descriptorIndex = buf.getShort();

            final short attributesCount = buf.getShort();
            attributes = new AttributeInfo[attributesCount];
            for (int i = 0; i < attributes.length; ++i)
                attributes[i] = new AttributeInfo(buf);
        }

        public void toString(final StringBuilder s) {
            s.openObject(this);

            final List<String> flags = new ArrayList<>();
            for (final AccessFlag flag : AccessFlag.values())
                if (flag.method && (accessFlags & flag.value) > 0)
                    flags.add(flag.name());
            s.appendln("accessFlags: " + Arrays.asList(flags.toArray()));

            s.appendln("nameIndex: " + nameIndex);
            s.appendln("descriptorIndex: " + descriptorIndex);

            s.append("attributes: ");
            s.openArray();
            for (final AttributeInfo attribute : attributes)
                attribute.toString(s);
            s.closeArray();
            s.closeObject();
        }
    }

    private final int             magic;
    private final short           minorVersion;
    private final short           majorVersion;
    private final short           constantPoolCount;
    private final ConstantInfo[]  constantPool;
    private final short           accessFlags;
    private final short           thisClass;
    private final short           superClass;
    private final short           interfacesCount;
    private final short[]         interfaces;
    private final short           fieldsCount;
    private final FieldInfo[]     fields;
    private final short           methodsCount;
    private final MethodInfo[]    methods;
    private final short           attributesCount;
    private final AttributeInfo[] attributes;

    public ClassFile(final ByteBuffer buf) {
        magic = buf.getInt();
        minorVersion = buf.getShort();
        majorVersion = buf.getShort();

        constantPoolCount = buf.getShort();
        constantPool = new ConstantInfo[constantPoolCount - 1];
        for (int i = 0; i < constantPool.length; ++i) {
            final byte tag = buf.get();
            try {
                final Class<?> clazz = ConstantInfo.LOOKUP_TABLE[tag - 1];
                final Constructor<?> construct = clazz.getConstructor(ByteBuffer.class);
                constantPool[i] = (ConstantInfo) construct.newInstance(buf);
            } catch (final Exception e) {
                e.printStackTrace(System.err);
                System.exit(0);
            }
        }

        accessFlags = buf.getShort();
        thisClass = buf.getShort();
        superClass = buf.getShort();

        interfacesCount = buf.getShort();
        interfaces = new short[interfacesCount];
        for (int i = 0; i < interfaces.length; ++i)
            interfaces[i] = buf.getShort();

        fieldsCount = buf.getShort();
        fields = new FieldInfo[fieldsCount];
        for (int i = 0; i < fields.length; ++i)
            fields[i] = new FieldInfo(buf);

        methodsCount = buf.getShort();
        methods = new MethodInfo[methodsCount];
        for (int i = 0; i < methods.length; ++i)
            methods[i] = new MethodInfo(buf);

        attributesCount = buf.getShort();
        attributes = new AttributeInfo[attributesCount];
        for (int i = 0; i < attributes.length; ++i)
            attributes[i] = new AttributeInfo(buf);
    }

    public void toString(final StringBuilder s) {
        s.openObject(this);
        s.appendln("magic: 0x" + Integer.toHexString(magic));
        s.appendln("version: " + majorVersion + "." + minorVersion);

        s.append("constantPool: ");
        s.openArray();
        for (final ConstantInfo element : constantPool) {
            if (element == null)
                s.appendln("null");
            else
                element.toString(s);
        }
        s.closeArray();

        final List<String> flags = new ArrayList<>();
        for (final AccessFlag flag : AccessFlag.values())
            if (flag.clazz && (accessFlags & flag.value) > 0)
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
        for (final AttributeInfo attribute : attributes)
            attribute.toString(s);
        s.closeArray();

        s.closeObject();
    }
}
