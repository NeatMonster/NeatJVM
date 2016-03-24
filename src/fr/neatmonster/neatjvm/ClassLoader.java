package fr.neatmonster.neatjvm;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.neatjvm.format.constant.ClassConstant;

public class ClassLoader {
    public VirtualMachine         vm;
    public ClassLoader            parent;
    public Map<String, ClassFile> namespace;

    public ClassLoader(final VirtualMachine vm, final ClassLoader parent) {
        this.vm = vm;
        this.parent = parent;
        namespace = new HashMap<>();
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
        final ClassFile classFile = new ClassFile(this, buf);
        namespace.put(className, classFile);

        if (!className.equals("java/lang/Object")) {
            if (classFile.superClass != 0) {
                final ClassConstant classInfo = classFile.constants.getClass(classFile.superClass);
                if (!classInfo.isResolved())
                    classInfo.resolve();
            }

            for (final short index : classFile.interfaces) {
                final ClassConstant classInfo = classFile.constants.getClass(index);
                if (!classInfo.isResolved())
                    classInfo.resolve();
            }
        }

        classFile.initialize();
        return classFile;
    }
}
