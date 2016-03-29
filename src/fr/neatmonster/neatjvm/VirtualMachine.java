package fr.neatmonster.neatjvm;

import java.util.ArrayList;
import java.util.List;

import fr.neatmonster.neatjvm.Thread.ThreadPriority;
import fr.neatmonster.neatjvm.format.MethodInfo;
import fr.neatmonster.neatjvm.format.attribute.CodeAttribute;

public class VirtualMachine {
    public static final int       MAX_HEAP_SIZE  = 1024 * 1024;
    public static final int       MAX_STACK_SIZE = 10 * 1024;

    private static VirtualMachine instance;

    public static VirtualMachine getInstance() {
        return instance;
    }

    public static MemoryPool getHeapSpace() {
        return instance.heapSpace;
    }

    public static ClassLoader getClassLoader() {
        return instance.classLoader;
    }

    public static List<ClassLoader> getClassLoaders() {
        return instance.classLoaders;
    }

    public static HandlePool getHandlePool() {
        return instance.handlePool;
    }

    public static ExecutionPool getExecutionPool() {
        return instance.executionPool;
    }

    public static Thread getCurrentThread() {
        return instance.currentThread;
    }

    public static void setCurrentThread(final Thread thread) {
        instance.currentThread = thread;
    }

    private final MemoryPool        heapSpace;

    private final ClassLoader       classLoader;
    private final List<ClassLoader> classLoaders;

    private final HandlePool        handlePool;
    private final ExecutionPool     executionPool;

    private Thread                  currentThread;

    public VirtualMachine() {
        instance = this;

        heapSpace = new MemoryPool(MAX_HEAP_SIZE);

        classLoader = new ClassLoader(null);
        classLoaders = new ArrayList<>();
        classLoaders.add(classLoader);

        handlePool = new HandlePool();
        executionPool = new ExecutionPool();

        currentThread = null;
    }

    public void start(final String className) {
        final ClassFile mainClass = instance.classLoader.loadClass(className);
        final MethodInfo mainMethod = mainClass.getMethod("main", "([Ljava/lang/String;)V");
        startThread(MethodInfo.getCode(mainMethod.resolve()), ThreadPriority.NORM_PRIORITY);
        while (true) {
            final Thread thread = instance.executionPool.getNextThread();
            if (thread == null)
                break;
            thread.tick();
        }
    }

    public Thread startThread(final CodeAttribute code, final ThreadPriority priority) {
        final Thread thread = new Thread(instance.executionPool.getNextThreadId());
        thread.start(code);
        instance.executionPool.addThread(thread, priority);
        return thread;
    }
}
