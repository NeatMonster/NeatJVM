package fr.neatmonster.neatjvm;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.neatjvm.ClassFile.ArrayClassFile;
import fr.neatmonster.neatjvm.ClassFile.PrimitiveClassFile;
import fr.neatmonster.neatjvm.format.FieldType.BaseType;

public class ClassLoader {
    private final ClassLoader            parent;
    private final Map<String, ClassFile> namespace;

    public ClassLoader() {
        this(null);
    }

    public ClassLoader(final ClassLoader parent) {
        this.parent = parent;
        namespace = new HashMap<>();

        if (parent == null) {
            for (final BaseType type : BaseType.values()) {
                final ClassFile classFile = new PrimitiveClassFile(this, type);
                namespace.put(classFile.getName(), classFile);
            }
        }
    }

    public ClassLoader getParent() {
        return parent;
    }

    public ClassFile findClass(final String className) {
        return namespace.get(className);
    }

    public ClassFile loadClass(final String className) {
        if (parent != null)
            return parent.loadClass(className);

        ClassFile classFile = findClass(className);
        if (classFile != null)
            return classFile;

        if (className.startsWith("[")) {
            classFile = new ArrayClassFile(this, loadClass(className.substring(1)));
            namespace.put(className, classFile);
            return classFile;
        }

        try {
            final byte[] classBytes = Files.readAllBytes(new File(className + ".class").toPath());
            return defineClass(className, ByteBuffer.wrap(classBytes));
        } catch (final IOException e) {
            e.printStackTrace(System.err);
            System.exit(0);
        }
        return null;
    }

    public ClassFile defineClass(final String className, final ByteBuffer buf) {
        final ClassFile classFile = new ClassFile(this, className, buf);
        namespace.put(className, classFile);
        classFile.initialize();
        return classFile;
    }
}
