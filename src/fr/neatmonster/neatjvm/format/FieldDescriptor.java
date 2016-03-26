package fr.neatmonster.neatjvm.format;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class FieldDescriptor {
    // @formatter:off
    public static interface FieldType {
        
        public byte[] getDefault();
        
        public int getSize();
    }
    // @formatter:on

    public static enum BaseType implements FieldType {
        // @formatter:off
        BOOLEAN('Z', 1),
        CHAR(   'C', 2),
        FLOAT(  'F', 4),
        DOUBLE( 'D', 8),
        BYTE(   'B', 1),
        SHORT(  'S', 2),
        INT(    'I', 4),
        LONG(   'J', 8),
        VOID(   'V', 0);
        // @formatter:on

        public final char   term;
        public final int    size;
        public final byte[] defVal;

        private BaseType(final char term, int size) {
            this.term = term;
            this.size = size;
            defVal = new byte[size];
        }

        @Override
        public byte[] getDefault() {
            return defVal;
        }

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String toString() {
            return "" + term;
        }
    }

    public static class ObjectType implements FieldType {
        public final String className;
        public final byte[] defVal;

        public ObjectType(final String className) {
            this.className = className;
            defVal = new byte[4];
        }

        @Override
        public byte[] getDefault() {
            return defVal;
        }

        @Override
        public int getSize() {
            return 4;
        }

        @Override
        public String toString() {
            return "L" + className + ";";
        }
    }

    public static class ArrayType implements FieldType {
        public final FieldType type;
        public final byte[]    defVal;

        public ArrayType(final FieldType type) {
            this.type = type;
            defVal = new byte[4];
        }

        @Override
        public byte[] getDefault() {
            return defVal;
        }

        @Override
        public int getSize() {
            return 4;
        }

        @Override
        public String toString() {
            return "[" + type.toString();
        }
    }

    public static FieldType parseType(final ByteBuffer buf) {
        final char typeChar = buf.getChar();

        for (final BaseType type : BaseType.values())
            if (typeChar == type.term)
                return type;

        if (typeChar == 'L') {
            String className = "";
            char classNameChar = buf.getChar();
            while (classNameChar != ';') {
                className += classNameChar;
                classNameChar = buf.getChar();
            }
            return new ObjectType(className);
        }

        if (typeChar == '[')
            return new ArrayType(parseType(buf));

        return null;
    }

    public final FieldType fieldType;

    public FieldDescriptor(final String str) throws UnsupportedEncodingException {
        fieldType = parseType(ByteBuffer.wrap(str.getBytes("UTF-16BE")));
    }

    public byte[] getDefault() {
        return fieldType.getDefault();
    }

    public int getSize() {
        return fieldType.getSize();
    }
    
    public int getIntSize() {
        return fieldType.getSize() > 4 ? 2 : 1;
    }

    @Override
    public String toString() {
        return fieldType.toString();
    }
}
