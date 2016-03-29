package fr.neatmonster.neatjvm;

import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.neatjvm.Thread.ThreadPriority;
import fr.neatmonster.neatjvm.Thread.ThreadState;

public class ExecutionPool {
    private final Map<Thread, ThreadPriority> threads;
    private int                               nextThreadId = 0;

    public ExecutionPool() {
        threads = new HashMap<>();
    }

    public void addThread(final Thread thread, final ThreadPriority priority) {
        threads.put(thread, priority);
        thread.setPriority(priority);
    }

    public Thread getNextThread() {
        // TODO Implement multi-threading
        for (final Thread thread : threads.keySet())
            if (thread.getState() == ThreadState.RUNNABLE)
                return thread;
        return null;
    }

    public int getNextThreadId() {
        return nextThreadId++;
    }
}
