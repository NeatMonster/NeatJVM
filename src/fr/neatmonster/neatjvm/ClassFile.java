package fr.neatmonster.neatjvm;

import java.nio.ByteBuffer;
import java.util.Arrays;

import fr.neatmonster.neatjvm.utils.StringBuilder;

public class ClassFile {
    public abstract class ConstantInfo {
        protected byte tag;

        public ConstantInfo(final ByteBuffer buf) {
            tag = buf.get();
        }

        public void toString(final StringBuilder s) {
            s.openObject();
            s.appendln("tag: " + tag);
            toString2(s);
            s.closeObject();
        }

        public abstract void toString2(StringBuilder s);
    }

    public class ConstantClassInfo extends ConstantInfo {
        private final short nameIndex;

        public ConstantClassInfo(final ByteBuffer buf) {
            super(buf);

            nameIndex = buf.getShort();
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("nameIndex: " + nameIndex);
        }
    }

    public class ConstantFieldrefInfo extends ConstantInfo {
        private final short classIndex;
        private final short nameAndTypeIndex;

        public ConstantFieldrefInfo(final ByteBuffer buf) {
            super(buf);

            classIndex = buf.getShort();
            nameAndTypeIndex = buf.getShort();
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("classIndex: " + classIndex);
            s.appendln("nameAndTypeIndex: " + nameAndTypeIndex);
        }
    }

    public class ConstantMethodrefInfo extends ConstantInfo {
        private final short classIndex;
        private final short nameAndTypeIndex;

        public ConstantMethodrefInfo(final ByteBuffer buf) {
            super(buf);

            classIndex = buf.getShort();
            nameAndTypeIndex = buf.getShort();
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("classIndex: " + classIndex);
            s.appendln("nameAndTypeIndex: " + nameAndTypeIndex);
        }
    }

    public class ConstantInterfaceMethodrefInfo extends ConstantInfo {
        private final short classIndex;
        private final short nameAndTypeIndex;

        public ConstantInterfaceMethodrefInfo(final ByteBuffer buf) {
            super(buf);

            classIndex = buf.getShort();
            nameAndTypeIndex = buf.getShort();
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("classIndex: " + classIndex);
            s.appendln("nameAndTypeIndex: " + nameAndTypeIndex);
        }
    }

    public class ConstantStringInfo extends ConstantInfo {
        private final short stringIndex;

        public ConstantStringInfo(final ByteBuffer buf) {
            super(buf);

            stringIndex = buf.getShort();
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("stringIndex: " + stringIndex);
        }
    }

    public class ConstantIntegerInfo extends ConstantInfo {
        private final int bytes;

        private final int i;

        public ConstantIntegerInfo(final ByteBuffer buf) {
            super(buf);

            bytes = buf.getInt();

            final ByteBuffer buf2 = ByteBuffer.allocate(4);
            buf2.putInt(bytes);
            i = buf2.getInt(0);
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("bytes: " + bytes);
            s.appendln("i: " + i);
        }
    }

    public class ConstantFloatInfo extends ConstantInfo {
        private final int   bytes;

        private final float f;

        public ConstantFloatInfo(final ByteBuffer buf) {
            super(buf);

            bytes = buf.getInt();

            final ByteBuffer buf2 = ByteBuffer.allocate(4);
            buf2.putInt(bytes);
            f = buf2.getFloat(0);
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("bytes: " + bytes);
            s.appendln("f: " + f);
        }
    }

    public class ConstantLongInfo extends ConstantInfo {
        private final int  highBytes;
        private final int  lowBytes;

        private final long l;

        public ConstantLongInfo(final ByteBuffer buf) {
            super(buf);

            highBytes = buf.getInt();
            lowBytes = buf.getInt();

            final ByteBuffer buf2 = ByteBuffer.allocate(8);
            buf2.putInt(highBytes);
            buf2.putInt(lowBytes);
            l = buf2.getLong(0);
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("highBytes: " + highBytes);
            s.appendln("lowBytes: " + lowBytes);
            s.appendln("l: " + l);
        }
    }

    public class ConstantDoubleInfo extends ConstantInfo {
        private final int    highBytes;
        private final int    lowBytes;

        private final double d;

        public ConstantDoubleInfo(final ByteBuffer buf) {
            super(buf);

            highBytes = buf.getInt();
            lowBytes = buf.getInt();

            final ByteBuffer buf2 = ByteBuffer.allocate(8);
            buf2.putInt(highBytes);
            buf2.putInt(lowBytes);
            d = buf2.getDouble(0);
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("highBytes: " + highBytes);
            s.appendln("lowBytes: " + lowBytes);
            s.appendln("d: " + d);
        }
    }

    public class ConstantNameAndTypeInfo extends ConstantInfo {
        private final short nameIndex;
        private final short descriptorIndex;

        public ConstantNameAndTypeInfo(final ByteBuffer buf) {
            super(buf);

            nameIndex = buf.getShort();
            descriptorIndex = buf.getShort();
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("nameIndex: " + nameIndex);
            s.appendln("descriptorIndex: " + descriptorIndex);
        }
    }

    public class ConstantUtf8Info extends ConstantInfo {
        private final short  length;
        private final byte[] bytes;

        private final String s;

        public ConstantUtf8Info(final ByteBuffer buf) {
            super(buf);

            length = buf.getShort();
            bytes = new byte[length];
            buf.get(bytes);

            s = new String(bytes);
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("length: " + length);
            s.appendln("bytes: " + Arrays.toString(bytes));
            s.appendln("s: " + this.s);
        }
    }

    public class ConstantMethodHandleInfo extends ConstantInfo {
        private final byte  referenceKind;
        private final short referenceIndex;

        public ConstantMethodHandleInfo(final ByteBuffer buf) {
            super(buf);

            referenceKind = buf.get();
            referenceIndex = buf.getShort();
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("referenceKind: " + referenceKind);
            s.appendln("referenceIndex: " + referenceIndex);
        }
    }

    public class ConstantMethodTypeInfo extends ConstantInfo {
        private final short descriptorIndex;

        public ConstantMethodTypeInfo(final ByteBuffer buf) {
            super(buf);

            descriptorIndex = buf.getShort();
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("descriptorIndex: " + descriptorIndex);
        }
    }

    public class ConstantInvokeDynamicInfo extends ConstantInfo {
        private final short bootstrapMethodAttrIndex;
        private final short nameAndTypeIndex;

        public ConstantInvokeDynamicInfo(final ByteBuffer buf) {
            super(buf);

            bootstrapMethodAttrIndex = buf.getShort();
            nameAndTypeIndex = buf.getShort();
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("bootstrapMethodAttrIndex: " + bootstrapMethodAttrIndex);
            s.appendln("nameAndTypeIndex: " + nameAndTypeIndex);
        }
    }

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
            s.openObject();
            s.appendln("attributeNameIndex: " + attributeNameIndex);
            s.appendln("attributeLength: " + attributeLength);
            s.closeObject();
        }
    }

    public class FieldInfo {
        private final short           accessFlags;
        private final short           nameIndex;
        private final short           descriptorIndex;
        private final short           attributesCount;
        private final AttributeInfo[] attributes;

        public FieldInfo(final ByteBuffer buf) {
            accessFlags = buf.getShort();
            nameIndex = buf.getShort();
            descriptorIndex = buf.getShort();

            attributesCount = buf.getShort();
            attributes = new AttributeInfo[attributesCount];
            for (int i = 0; i < attributes.length; ++i)
                attributes[i] = new AttributeInfo(buf);
        }

        public void toString(final StringBuilder s) {
            s.openObject();
            s.appendln("accessFlags: " + accessFlags);
            s.appendln("nameIndex: " + nameIndex);
            s.appendln("descriptorIndex: " + descriptorIndex);
            s.appendln("attributesCount: " + attributesCount);
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
        private final short           attributesCount;
        private final AttributeInfo[] attributes;

        public MethodInfo(final ByteBuffer buf) {
            accessFlags = buf.getShort();
            nameIndex = buf.getShort();
            descriptorIndex = buf.getShort();

            attributesCount = buf.getShort();
            attributes = new AttributeInfo[attributesCount];
            for (int i = 0; i < attributes.length; ++i)
                attributes[i] = new AttributeInfo(buf);
        }

        public void toString(final StringBuilder s) {
            s.openObject();
            s.appendln("accessFlags: " + accessFlags);
            s.appendln("nameIndex: " + nameIndex);
            s.appendln("descriptorIndex: " + descriptorIndex);
            s.appendln("attributesCount: " + attributesCount);
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
            ConstantInfo constant = null;
            final byte tag = buf.get();
            buf.position(buf.position() - 1);
            switch (tag) {
            case 7:
                constant = new ConstantClassInfo(buf);
                break;
            case 9:
                constant = new ConstantFieldrefInfo(buf);
                break;
            case 10:
                constant = new ConstantMethodrefInfo(buf);
                break;
            case 11:
                constant = new ConstantInterfaceMethodrefInfo(buf);
                break;
            case 8:
                constant = new ConstantStringInfo(buf);
                break;
            case 3:
                constant = new ConstantIntegerInfo(buf);
                break;
            case 4:
                constant = new ConstantFloatInfo(buf);
                break;
            case 5:
                constant = new ConstantLongInfo(buf);
                break;
            case 6:
                constant = new ConstantDoubleInfo(buf);
                break;
            case 12:
                constant = new ConstantNameAndTypeInfo(buf);
                break;
            case 1:
                constant = new ConstantUtf8Info(buf);
                break;
            case 15:
                constant = new ConstantMethodHandleInfo(buf);
                break;
            case 16:
                constant = new ConstantMethodTypeInfo(buf);
                break;
            case 18:
                constant = new ConstantInvokeDynamicInfo(buf);
                break;
            default:
                System.err.println("Unrecongnized constant pool entry w/ tag " + tag);
                break;
            }
            constantPool[i] = constant;
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
        s.openObject();
        s.appendln("magic: 0x" + Integer.toHexString(magic));
        s.appendln("minorVersion: " + minorVersion);
        s.appendln("majorVersion: " + majorVersion);

        s.appendln("constantPoolCount: " + constantPoolCount);
        s.append("constantPool: ");
        s.openArray();
        for (final ConstantInfo element : constantPool) {
            if (element == null)
                s.appendln("null");
            else
                element.toString(s);
        }
        s.closeArray();

        s.appendln("accessFlags: " + accessFlags);
        s.appendln("thisClass: " + thisClass);
        s.appendln("superClass: " + superClass);

        s.appendln("interfacesCount: " + interfacesCount);
        s.appendln("interfaces: " + Arrays.toString(interfaces));

        s.appendln("fieldsCount: " + fieldsCount);
        s.append("fields: ");
        s.openArray();
        for (final FieldInfo field : fields)
            field.toString(s);
        s.closeArray();

        s.appendln("methodsCount: " + methodsCount);
        s.append("methods: ");
        s.openArray();
        for (final MethodInfo method : methods)
            method.toString(s);
        s.closeArray();

        s.appendln("attributesCount: " + attributesCount);
        s.append("attributes: ");
        s.openArray();
        for (final AttributeInfo attribute : attributes)
            attribute.toString(s);
        s.closeArray();

        s.closeObject();
    }
}
