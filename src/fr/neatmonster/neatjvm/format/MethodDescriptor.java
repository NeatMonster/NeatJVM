package fr.neatmonster.neatjvm.format;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import fr.neatmonster.neatjvm.format.FieldDescriptor.FieldType;

public class MethodDescriptor {
    public final FieldType[] parametersType;
    public final FieldType   returnType;

    public MethodDescriptor(final ByteBuffer buf) {
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

        returnType = FieldDescriptor.parseType(buf);
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
