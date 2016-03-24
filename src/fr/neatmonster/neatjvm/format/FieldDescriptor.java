package fr.neatmonster.neatjvm.format;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.util.Utils;

public class FieldDescriptor {
    // @formatter:off
    public static interface FieldType {
        
        public byte[] getDefault();
        
        public int getSize();
    }
    // @formatter:on

    public static enum BaseType implements FieldType {
        // @formatter:off
        BYTE(   'B', 1, new byte[] { 0 }),
        CHAR(   'C', 2, new byte[] { 0, 0}),
        DOUBLE( 'D', 8, Utils.longToBytes(Double.doubleToRawLongBits(0.0))),
        FLOAT(  'F', 4, Utils.intToBytes(Float.floatToRawIntBits(0f))),
        INT(    'I', 4, Utils.intToBytes(0)),
        LONG(   'J', 8, Utils.longToBytes(0)),
        SHORT(  'S', 2, new byte[] { 0, 0 }),
        BOOLEAN('Z', 1, new byte[] { 0 }),
        VOID(   'V', 0, null);
        // @formatter:on

        public final char term;
        public final int size;
        public final byte[] _default;

        private BaseType(final char term, int size, byte[] _default) {
            this.term = term;
            this.size = size;
            this._default = _default;
        }
        
        @Override
        public byte[] getDefault() {
            return _default;
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

        public ObjectType(final String className) {
            this.className = className;
        }
        
        @Override
        public byte[] getDefault() {
            return Utils.intToBytes(0);
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

        public ArrayType(final FieldType type) {
            this.type = type;
        }
        
        @Override
        public byte[] getDefault() {
            return Utils.intToBytes(0);
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

    @Override
    public String toString() {
        return fieldType.toString();
    }
}
