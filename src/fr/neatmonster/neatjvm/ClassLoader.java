package fr.neatmonster.neatjvm;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.neatjvm.ClassFile.ArrayClassFile;
import fr.neatmonster.neatjvm.ClassFile.PrimitiveClassFile;
import fr.neatmonster.neatjvm.format.FieldDescriptor.BaseType;

public class ClassLoader {
    public VirtualMachine         vm;
    public ClassLoader            parent;
    public Map<String, ClassFile> namespace;

    public ClassLoader(final VirtualMachine vm, final ClassLoader parent) {
        this.vm = vm;
        this.parent = parent;
        namespace = new HashMap<>();

        if (parent == null) {
            for (final BaseType type : BaseType.values()) {
                final ClassFile primitiveClass = new PrimitiveClassFile(this, type);
                namespace.put(primitiveClass.name, primitiveClass);
            }
        }
    }

    public ClassFile getClass(final String className) {
        return namespace.get(className);
    }

    public ClassFile loadClass(final String className) {
        if (parent == null) {
            byte[] bytes;
            try {
                bytes = Files.readAllBytes(new File(className + ".class").toPath());
                return defineClass(className, ByteBuffer.wrap(bytes));
            } catch (final IOException e) {
                e.printStackTrace(System.err);
                System.exit(0);
            }
            return null;
        } else
            return parent.loadClass(className);
    }

    public ClassFile defineClass(final String className, final ByteBuffer buf) {
        final ClassFile classFile = new ClassFile(this, buf, className);
        namespace.put(className, classFile);

        if (!className.equals("java/lang/Object")) {
            if (classFile.superClass != 0)
                classFile.constants.getClass(classFile.superClass);

            for (final short index : classFile.interfaces)
                classFile.constants.getClass(index);
        }

        classFile.initialize();
        return classFile;
    }

    public ArrayClassFile defineArrayClass(final ClassFile arrayClass) {
        final String className = "[" + arrayClass.name;

        ArrayClassFile classFile = (ArrayClassFile) getClass(className);
        if (classFile != null)
            return classFile;

        classFile = new ArrayClassFile(this, arrayClass);
        namespace.put(className, classFile);
        return classFile;
    }
}
