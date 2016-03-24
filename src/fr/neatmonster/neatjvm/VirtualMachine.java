package fr.neatmonster.neatjvm;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import fr.neatmonster.neatjvm.ExecutionPool.ThreadPriority;
import fr.neatmonster.neatjvm.format.attribute.CodeAttribute;

public class VirtualMachine {
    public static final int  MAX_HEAP_SIZE  = 1024 * 1024;
    public static final int  MAX_STACK_SIZE = 10 * 1024;

    public HeapSpace         heapSpace;
    public HeapSpace         stackSpace;
    public ClassLoader       classLoader;
    public List<ClassLoader> classLoaders;
    public Thread            mainThread;
    public Thread            currentThread;
    public ExecutionPool     executionPool;

    public VirtualMachine() {
        heapSpace = new HeapSpace(this, MAX_HEAP_SIZE);
        stackSpace = new HeapSpace(this, MAX_STACK_SIZE);

        classLoaders = new ArrayList<>();
        classLoaders.add(classLoader = new ClassLoader(this));

        executionPool = new ExecutionPool(this);
        mainThread = currentThread = null;
    }

    public void start(final File file) throws IOException {
        final byte[] bytes = Files.readAllBytes(file.toPath());
        final ClassFile clazz = new ClassFile(classLoader, ByteBuffer.wrap(bytes));
        mainThread = runThread(clazz.getMainMethod(), ThreadPriority.MIN_PRIORITY);
        run();
    }

    public void run() {
        while (true) {
            final Thread nextThread = executionPool.getNextThread();
            if (nextThread == null)
                break;
            nextThread.tick();
        }
    }

    public Thread runThread(final CodeAttribute code, final ThreadPriority priority) {
        final Thread thread = new Thread(this, executionPool.getNextThreadId());
        thread.start(code);
        executionPool.addThread(thread, priority);
        return thread;
    }
}
