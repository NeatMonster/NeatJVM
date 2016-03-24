package fr.neatmonster.neatjvm;

import java.util.ArrayList;
import java.util.List;

import fr.neatmonster.neatjvm.ExecutionPool.ThreadPriority;
import fr.neatmonster.neatjvm.format.AccessFlag;
import fr.neatmonster.neatjvm.format.MethodInfo;
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
    public HandlePool        handlePool;

    public VirtualMachine() {
        heapSpace = new HeapSpace(this, MAX_HEAP_SIZE);
        stackSpace = new HeapSpace(this, MAX_STACK_SIZE);

        classLoaders = new ArrayList<>();
        classLoaders.add(classLoader = new ClassLoader(this, null));

        executionPool = new ExecutionPool(this);
        handlePool = new HandlePool(this);
    }

    public void start(final String className) {
        final ClassFile classFile = classLoader.loadClass(className);
        final MethodInfo main = classFile.getMethod("main", "([Ljava/lang/String;)V", AccessFlag.PUBLIC,
                AccessFlag.STATIC);
        for (final MethodInfo method : classFile.methods)
            if (!method.isResolved())
                method.resolve();
        if (!main.isResolved())
            main.resolve();
        mainThread = runThread(main.code, null, ThreadPriority.NORM_PRIORITY);
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

    public Thread runThread(final CodeAttribute code, final InstanceData instance, final ThreadPriority priority) {
        final Thread thread = new Thread(this, executionPool.getNextThreadId());
        thread.start(code, instance);
        executionPool.addThread(thread, priority);
        return thread;
    }
}
