package fr.neatmonster.neatjvm.format;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import fr.neatmonster.neatjvm.format.FieldDescriptor.FieldType;

public class MethodDescriptor {
    public static interface ReturnType {}

    public final FieldType[] parametersType;
    public final ReturnType  returnType;

    public MethodDescriptor(final String str) throws UnsupportedEncodingException {
        ByteBuffer buf = ByteBuffer.wrap(str.getBytes("UTF-16BE"));

        buf.getChar();

        final List<FieldType> types = new ArrayList<>();
        buf.mark();
        FieldType type = FieldDescriptor.parseType(buf);
        while (type != null) {
            types.add(type);
            buf.mark();
            type = FieldDescriptor.parseType(buf);
        }
        buf.reset();
        parametersType = new FieldType[types.size()];
        types.toArray(parametersType);

        buf.getChar();

        if (buf.getChar() == 'V')
            returnType = null;
        else {        
            buf.position(buf.position() - 2);
            returnType = FieldDescriptor.parseType(buf);
        }
    }

    public int getSize() {
        int size = 0;
        for (FieldType type : parametersType)
            size += type.getSize();
        return size;
    }

    public int getIntSize() {
        int size = 0;
        for (FieldType type : parametersType)
            size += type.getSize() > 4 ? 2 : 1;
        return size;
    }

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        s.append("(");
        for (final FieldType parameterType : parametersType)
            s.append(parameterType.toString());
        s.append(")");
        s.append(returnType.toString());
        return s.toString();
    }
}
