package fr.neatmonster.neatjvm.format.attribute;

import java.nio.ByteBuffer;
import java.util.Arrays;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.AttributeInfo;
import fr.neatmonster.neatjvm.format.constant.Utf8Constant;
import fr.neatmonster.neatjvm.util.StringBuilder;

public class CodeAttribute extends AttributeInfo {
    public static class ExceptionHandler {
        public short startPC;
        public short endPC;
        public short handlerPC;
        public short catchType;

        public ExceptionHandler(final ByteBuffer buf) {
            startPC = buf.getShort();
            endPC = buf.getShort();
            handlerPC = buf.getShort();
            catchType = buf.getShort();
        }

        public void toString(final StringBuilder s) {
            s.openObject(this);

            s.appendln("startPC: " + startPC);
            s.appendln("endPC: " + endPC);
            s.appendln("handlerPC: " + handlerPC);
            s.appendln("catchType: " + catchType);

            s.closeObject();
        }
    }

    public short              maxStack;
    public short              maxLocals;
    public byte[]             code;
    public ExceptionHandler[] exceptions;
    public AttributeInfo[]    attributes;

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
                final String name = classFile.constants.getUtf8(index);
                final Class<? extends AttributeInfo> clazz = AttributeInfo.ALL.get(name);
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

    @Override
    public void toString2(final StringBuilder s) {
        s.appendln("maxStack: " + maxStack);
        s.appendln("maxLocals: " + maxLocals);
        s.appendln("code: " + Arrays.toString(code));

        s.append("exceptions: ");
        s.openArray();
        for (final ExceptionHandler exception : exceptions)
            exception.toString(s);
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
    }
}