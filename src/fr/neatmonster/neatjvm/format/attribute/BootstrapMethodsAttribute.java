package fr.neatmonster.neatjvm.format.attribute;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile;
import fr.neatmonster.neatjvm.format.AttributeInfo;
import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.format.Resolvable;
import fr.neatmonster.neatjvm.format.constant.MethodHandleConstant;

public class BootstrapMethodsAttribute extends AttributeInfo {
    public class BootstrapMethod implements Resolvable {
        private final short          bootstrapMethodRef;
        private final short[]        bootstrapArguments;

        private MethodHandleConstant method;
        private ConstantInfo[]       arguments;

        private BootstrapMethod(final ByteBuffer buf) {
            bootstrapMethodRef = buf.getShort();

            final short bootstrapArgumentsCount = buf.getShort();
            bootstrapArguments = new short[bootstrapArgumentsCount];
            for (int i = 0; i < bootstrapArgumentsCount; ++i)
                bootstrapArguments[i] = buf.getShort();
        }

        public MethodHandleConstant getMethod() {
            return method;
        }

        public ConstantInfo[] getArguments() {
            return arguments;
        }

        @Override
        public BootstrapMethod resolve() {
            if (method != null)
                return this;

            method = ConstantInfo.getMethodHandle(classFile, bootstrapMethodRef);
            arguments = new ConstantInfo[bootstrapArguments.length];
            for (int i = 0; i < arguments.length; ++i) {
                arguments[i] = classFile.getConstants()[bootstrapArguments[i]];
                arguments[i].resolve();
            }
            return this;
        }
    }

    private final BootstrapMethod[] bootstrapMethods;

    public BootstrapMethodsAttribute(final ClassFile classFile, final ByteBuffer buf) {
        super(classFile);

        final short bootstrapMethodsCount = buf.getShort();
        bootstrapMethods = new BootstrapMethod[bootstrapMethodsCount];
        for (int i = 0; i < bootstrapMethodsCount; ++i)
            bootstrapMethods[i] = new BootstrapMethod(buf);
    }

    public BootstrapMethod[] getBootstrapMethods() {
        return bootstrapMethods;
    }

    @Override
    public BootstrapMethodsAttribute resolve() {
        for (final BootstrapMethod bootstrapMethod : bootstrapMethods)
            bootstrapMethod.resolve();
        return this;
    }
}
