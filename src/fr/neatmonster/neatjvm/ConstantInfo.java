package fr.neatmonster.neatjvm;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.utils.StringBuilder;

public abstract class ConstantInfo {
    public static class ConstantClassInfo extends ConstantInfo {
        private final short nameIndex;

        public ConstantClassInfo(final ByteBuffer buf) {
            nameIndex = buf.getShort();
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("nameIndex: " + nameIndex);
        }
    }

    public static class ConstantFieldrefInfo extends ConstantInfo {
        private final short classIndex;
        private final short nameAndTypeIndex;

        public ConstantFieldrefInfo(final ByteBuffer buf) {
            classIndex = buf.getShort();
            nameAndTypeIndex = buf.getShort();
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("classIndex: " + classIndex);
            s.appendln("nameAndTypeIndex: " + nameAndTypeIndex);
        }
    }

    public static class ConstantMethodrefInfo extends ConstantInfo {
        private final short classIndex;
        private final short nameAndTypeIndex;

        public ConstantMethodrefInfo(final ByteBuffer buf) {
            classIndex = buf.getShort();
            nameAndTypeIndex = buf.getShort();
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("classIndex: " + classIndex);
            s.appendln("nameAndTypeIndex: " + nameAndTypeIndex);
        }
    }

    public static class ConstantInterfaceMethodrefInfo extends ConstantInfo {
        private final short classIndex;
        private final short nameAndTypeIndex;

        public ConstantInterfaceMethodrefInfo(final ByteBuffer buf) {
            classIndex = buf.getShort();
            nameAndTypeIndex = buf.getShort();
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("classIndex: " + classIndex);
            s.appendln("nameAndTypeIndex: " + nameAndTypeIndex);
        }
    }

    public static class ConstantStringInfo extends ConstantInfo {
        private final short stringIndex;

        public ConstantStringInfo(final ByteBuffer buf) {
            stringIndex = buf.getShort();
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("stringIndex: " + stringIndex);
        }
    }

    public static class ConstantIntegerInfo extends ConstantInfo {
        private final int value;

        public ConstantIntegerInfo(final ByteBuffer buf) {
            value = buf.getInt();
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("value: " + value);
        }
    }

    public static class ConstantFloatInfo extends ConstantInfo {
        private final float value;

        public ConstantFloatInfo(final ByteBuffer buf) {
            value = buf.getFloat();
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("value: " + value);
        }
    }

    public static class ConstantLongInfo extends ConstantInfo {
        private final long value;

        public ConstantLongInfo(final ByteBuffer buf) {
            final ByteBuffer bufLoc = ByteBuffer.allocate(8);
            bufLoc.putInt(buf.getInt());
            bufLoc.putInt(buf.getInt());
            value = bufLoc.getLong(0);
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("value: " + value);
        }
    }

    public static class ConstantDoubleInfo extends ConstantInfo {
        private final double value;

        public ConstantDoubleInfo(final ByteBuffer buf) {
            final ByteBuffer bufLoc = ByteBuffer.allocate(8);
            bufLoc.putInt(buf.getInt());
            bufLoc.putInt(buf.getInt());
            value = bufLoc.getDouble(0);
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("value: " + value);
        }
    }

    public static class ConstantNameAndTypeInfo extends ConstantInfo {
        private final short nameIndex;
        private final short descriptorIndex;

        public ConstantNameAndTypeInfo(final ByteBuffer buf) {
            nameIndex = buf.getShort();
            descriptorIndex = buf.getShort();
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("nameIndex: " + nameIndex);
            s.appendln("descriptorIndex: " + descriptorIndex);
        }
    }

    public static class ConstantUtf8Info extends ConstantInfo {
        private final String value;

        public ConstantUtf8Info(final ByteBuffer buf) {
            final short length = buf.getShort();
            final byte[] bytes = new byte[length];
            buf.get(bytes);
            value = new String(bytes);
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("value: \"" + value + "\"");
        }
    }

    public static class ConstantMethodHandleInfo extends ConstantInfo {
        private final byte  referenceKind;
        private final short referenceIndex;

        public ConstantMethodHandleInfo(final ByteBuffer buf) {
            referenceKind = buf.get();
            referenceIndex = buf.getShort();
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("referenceKind: " + referenceKind);
            s.appendln("referenceIndex: " + referenceIndex);
        }
    }

    public static class ConstantMethodTypeInfo extends ConstantInfo {
        private final short descriptorIndex;

        public ConstantMethodTypeInfo(final ByteBuffer buf) {
            descriptorIndex = buf.getShort();
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("descriptorIndex: " + descriptorIndex);
        }
    }

    public static class ConstantInvokeDynamicInfo extends ConstantInfo {
        private final short bootstrapMethodAttrIndex;
        private final short nameAndTypeIndex;

        public ConstantInvokeDynamicInfo(final ByteBuffer buf) {
            bootstrapMethodAttrIndex = buf.getShort();
            nameAndTypeIndex = buf.getShort();
        }

        @Override
        public void toString2(final StringBuilder s) {
            s.appendln("bootstrapMethodAttrIndex: " + bootstrapMethodAttrIndex);
            s.appendln("nameAndTypeIndex: " + nameAndTypeIndex);
        }
    }

    public static Class<?>[] LOOKUP_TABLE = new Class<?>[] {
        // @formatter:off
        ConstantUtf8Info.class,
        null,
        ConstantIntegerInfo.class,
        ConstantFloatInfo.class,
        ConstantLongInfo.class,
        ConstantDoubleInfo.class,
        ConstantClassInfo.class,
        ConstantStringInfo.class,
        ConstantFieldrefInfo.class,
        ConstantMethodrefInfo.class,
        ConstantInterfaceMethodrefInfo.class,
        ConstantNameAndTypeInfo.class,
        null,
        null,
        ConstantMethodHandleInfo.class,
        ConstantMethodTypeInfo.class,
        null,
        ConstantInvokeDynamicInfo.class
        // @formatter:on
    };

    public void toString(final StringBuilder s) {
        s.openObject(this);
        toString2(s);
        s.closeObject();
    }

    public abstract void toString2(StringBuilder s);
}
