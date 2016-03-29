package fr.neatmonster.neatjvm.format.attribute;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.AttributeInfo;
import fr.neatmonster.neatjvm.format.ConstantInfo;

public class CodeAttribute extends AttributeInfo {
    public static class ExceptionHandler {
        private final short startPC;
        private final short endPC;
        private final short handlerPC;
        private final short catchType;

        public ExceptionHandler(final ByteBuffer buf) {
            startPC = buf.getShort();
            endPC = buf.getShort();
            handlerPC = buf.getShort();
            catchType = buf.getShort();
        }

        public short getStart() {
            return startPC;
        }

        public short getEnd() {
            return endPC;
        }

        public short getHandler() {
            return handlerPC;
        }

        public short getCatchType() {
            return catchType;
        }
    }

    private final short              maxStack;
    private final short              maxLocals;
    private final byte[]             code;
    private final ExceptionHandler[] exceptions;
    private final AttributeInfo[]    attributes;

    public CodeAttribute(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        maxStack = buf.getShort();
        maxLocals = buf.getShort();

        final int codeLength = buf.getInt();
        code = new byte[codeLength];
        buf.get(code);

        final short exceptionsCount = buf.getShort();
        exceptions = new CodeAttribute.ExceptionHandler[exceptionsCount];
        for (int i = 0; i < exceptionsCount; ++i)
            exceptions[i] = new ExceptionHandler(buf);

        final short attributesCount = buf.getShort();
        attributes = new AttributeInfo[attributesCount];
        for (int i = 0; i < attributes.length; ++i) {
            final short index = buf.getShort();
            final int length = buf.getInt();
            try {
                final String name = ConstantInfo.getUtf8(classFile, index);
                final Class<? extends AttributeInfo> clazz = AttributeInfo.get(name);
                if (clazz == null) {
                    System.err.println("Unrecognized attribute info w/ name " + name);
                    buf.position(buf.position() + length);
                } else
                    attributes[i] = clazz.getConstructor(ClassFile.class, ByteBuffer.class).newInstance(classFile, buf);
            } catch (final Exception e) {
                e.printStackTrace(System.err);
                System.exit(0);
            }
        }
    }

    public short getMaxStack() {
        return maxStack;
    }

    public short getMaxLocals() {
        return maxLocals;
    }

    public byte[] getCode() {
        return code;
    }

    public ExceptionHandler[] getExceptions() {
        return exceptions;
    }

    public AttributeInfo[] getAttributes() {
        return attributes;
    }
}