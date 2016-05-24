package fr.neatmonster.neatjvm.format;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.InstanceData;
import fr.neatmonster.neatjvm.VirtualMachine;

public interface FieldType {
    enum BaseType implements FieldType {
        // @formatter:off
        BOOLEAN('Z', 1),
        CHAR(   'C', 2),
        FLOAT(  'F', 4),
        DOUBLE( 'D', 8),
        BYTE(   'B', 1),
        SHORT(  'S', 2),
        INT(    'I', 4),
        LONG(   'J', 8);
        // @formatter:on

        private final char   terminal;
        private final byte[] defaultValue;

        BaseType(final char terminal, final int size) {
            this.terminal = terminal;
            defaultValue = new byte[size];
        }

        public char getTerminal() {
            return terminal;
        }

        @Override
        public int getSize() {
            return defaultValue.length;
        }

        @Override
        public byte[] getDefaultValue() {
            return defaultValue;
        }

        @Override
        public InstanceData getJavaClass() {
            final ClassFile classFile = VirtualMachine.getClassLoader().loadClass(toString());
            return VirtualMachine.getInstancePool().addClassFile(classFile);
        }

        @Override
        public String toString() {
            return Character.toString(terminal);
        }
    }

    class ObjectType implements FieldType {
        private final String className;
        private final byte[] defaultValue;

        public ObjectType(final String className) {
            this.className = className.replace("/", ".");
            defaultValue = new byte[4];
        }

        public String getClassName() {
            return className;
        }

        @Override
        public int getSize() {
            return 4;
        }

        @Override
        public byte[] getDefaultValue() {
            return defaultValue;
        }

        @Override
        public InstanceData getJavaClass() {
            final ClassFile classFile = VirtualMachine.getClassLoader().loadClass(className);
            return VirtualMachine.getInstancePool().addClassFile(classFile);
        }

        @Override
        public String toString() {
            return "L" + className + ";";
        }
    }

    class ArrayType implements FieldType {
        private final FieldType type;
        private final byte[]    defaultBytes;

        public ArrayType(final FieldType type) {
            this.type = type;
            defaultBytes = new byte[4];
        }

        public FieldType getType() {
            return type;
        }

        @Override
        public int getSize() {
            return 4;
        }

        @Override
        public byte[] getDefaultValue() {
            return defaultBytes;
        }

        @Override
        public InstanceData getJavaClass() {
            final ClassFile classFile = VirtualMachine.getClassLoader().loadClass(toString());
            return VirtualMachine.getInstancePool().addClassFile(classFile);
        }

        @Override
        public String toString() {
            return "[" + type.toString();
        }
    }

    int getSize();

    byte[] getDefaultValue();

    InstanceData getJavaClass();

    static FieldType parseType(final ByteBuffer buf) {
        final char typeChar = buf.getChar();

        for (final BaseType type : BaseType.values())
            if (typeChar == type.terminal)
                return type;

        if (typeChar == 'L') {
            String className = "";
            char c = buf.getChar();
            while (c != ';') {
                className += c;
                c = buf.getChar();
            }
            return new ObjectType(className);
        }

        if (typeChar == '[')
            return new ArrayType(parseType(buf));

        return null;
    }
}
