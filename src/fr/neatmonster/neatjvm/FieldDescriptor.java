package fr.neatmonster.neatjvm;

import java.nio.ByteBuffer;

public class FieldDescriptor {
    // @formatter:off
    public static interface FieldType {}
    // @formatter:on

    public static enum BaseType implements FieldType {
        // @formatter:off
        BYTE('B'),
        CHAR('C'),
        DOUBLE('D'),
        FLOAT('F'),
        INT('I'),
        LONG('J'),
        SHORT('S'),
        BOOLEAN('Z'),
        VOID('V');
        // @formatter:on

        public final char term;

        private BaseType(final char term) {
            this.term = term;
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
        public String toString() {
            return "L" + className;
        }
    }

    public static class ArrayType implements FieldType {
        public final FieldType type;

        public ArrayType(final FieldType type) {
            this.type = type;
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

    public FieldDescriptor(final ByteBuffer buf) {
        fieldType = parseType(buf);
    }

    @Override
    public String toString() {
        return fieldType.toString();
    }
}
