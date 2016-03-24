package fr.neatmonster.neatjvm;

import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.neatjvm.Thread.ThreadState;

public class ExecutionPool {
    public static enum ThreadPriority {
        MIN_PRIORITY, NORM_PRIORITY, MAX_PRIORITY;
    }

    public VirtualMachine              vm;
    public Map<Thread, ThreadPriority> threads;
    public int                         nextThreadId = 0;

    public ExecutionPool(final VirtualMachine vm) {
        this.vm = vm;
        threads = new HashMap<>();
    }

    public void addThread(final Thread thread, final ThreadPriority priority) {
        threads.put(thread, priority);
        thread.priority = priority;
    }

    public Thread getNextThread() {
        // TODO: Implement multi-threading
        for (final Thread thread : threads.keySet())
            if (thread.state == ThreadState.RUNNING)
                return thread;
        return null;
    }

    public int getNextThreadId() {
        return nextThreadId++;
    }
}
