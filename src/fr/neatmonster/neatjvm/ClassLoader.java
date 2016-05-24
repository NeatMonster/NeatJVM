package fr.neatmonster.neatjvm;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import fr.neatmonster.neatjvm.ClassFile.ArrayClassFile;
import fr.neatmonster.neatjvm.ClassFile.PrimitiveClassFile;
import fr.neatmonster.neatjvm.format.FieldType.BaseType;

public abstract class ClassLoader {
    public static class BootstrapClassLoader extends ClassLoader {

        public BootstrapClassLoader() {
            super(null);
        }

        public void initialize() {
            for (final BaseType type : BaseType.values()) {
                final ClassFile classFile = new PrimitiveClassFile(this, type);
                namespace.put(classFile.getName(), classFile);
            }

            findClass("java.lang.System").getMethod("initializeSystemClass", "()V").invoke(null);
        }

        @Override
        protected ClassFile findClass(final String className) {
            if (className.startsWith("[")) {
                String arrayClassName = className.substring(1);
                if (arrayClassName.startsWith("L"))
                    arrayClassName = arrayClassName.substring(1, arrayClassName.length() - 1);
                final ClassFile classFile = new ArrayClassFile(this, loadClass(arrayClassName));
                namespace.put(className, classFile);
                return classFile;
            }

            try {
                final ZipFile zipFile = new ZipFile("rt.jar");
                final Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    final ZipEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    if (!entryName.endsWith(".class"))
                        continue;
                    entryName = entryName.substring(0, entryName.length() - 6);
                    entryName = entryName.replaceAll("/", ".");
                    if (entryName.equals(className)) {
                        final InputStream is = zipFile.getInputStream(entry);
                        final byte[] bytes = new byte[(int) entry.getSize()];
                        final DataInputStream data = new DataInputStream(is);
                        data.readFully(bytes);
                        data.close();
                        return defineClass(className, ByteBuffer.wrap(bytes));
                    }
                }
                zipFile.close();
            } catch (final IOException e) {
                e.printStackTrace(System.err);
            }

            try {
                final File file = new File(className.replaceAll("\\.", "/") + ".class");
                final byte[] classBytes = Files.readAllBytes(file.toPath());
                return defineClass(className, ByteBuffer.wrap(classBytes));
            } catch (final IOException e) {
                e.printStackTrace(System.err);
            }
            return null;
        }
    }

    protected final ClassLoader            parent;
    protected final Map<String, ClassFile> namespace;

    public ClassLoader(final ClassLoader parent) {
        this.parent = parent;
        namespace = new HashMap<>();
    }

    public ClassLoader getParent() {
        return parent;
    }

    public ClassFile loadClass(String className) {
        className = className.replaceAll("/", ".");

        ClassFile classFile = namespace.get(className);

        if (classFile == null && parent != null)
            classFile = parent.loadClass(className);

        if (classFile == null)
            classFile = findClass(className);

        return classFile;
    }

    protected final ClassFile defineClass(final String className, final ByteBuffer buf) {
        final ClassFile classFile = new ClassFile(this, className, buf);
        namespace.put(className, classFile);
        classFile.initialize();
        return classFile;
    }

    protected abstract ClassFile findClass(final String className);
}
