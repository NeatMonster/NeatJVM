package fr.neatmonster.neatjvm;

import java.util.*;
import java.util.Map.Entry;

import fr.neatmonster.neatjvm.Thread.ThreadState;
import fr.neatmonster.neatjvm.format.FieldInfo;

public abstract class ObjectData {
    public static class Monitor {
        private final List<Thread> waiting;
        private Thread             owner;
        private int                permits;

        private Monitor() {
            waiting = new ArrayList<>();
        }

        public boolean acquire(final Thread thread) {
            if (owner == null) {
                owner = thread;
                permits = 1;
            } else if (owner == thread)
                ++permits;
            else {
                waiting.add(thread);
                thread.setState(ThreadState.BLOCKED);
            }
            return true;
        }

        public boolean release(final Thread thread) {
            if (owner == thread) {
                if (--permits == 0) {
                    owner = null;
                    if (!waiting.isEmpty()) {
                        final Thread newOwner = waiting.remove(0);
                        owner = newOwner;
                        permits = 1;
                        newOwner.setState(ThreadState.RUNNABLE);
                    }
                }
                return true;
            }
            return false;
        }
    }

    private final ClassFile            classFile;
    protected int                      dataStart;

    private final Monitor              monitor;
    private final Map<Thread, Integer> waiting;

    public ObjectData(final ClassFile classFile) {
        this.classFile = classFile;
        monitor = new Monitor();
        waiting = new LinkedHashMap<>();
    }

    public ClassFile getClassFile() {
        return classFile;
    }

    public int getAddress() {
        return dataStart;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public boolean wait(final Thread thread) {
        if (monitor.owner == thread) {
            waiting.put(thread, monitor.permits);
            monitor.owner = null;
            monitor.permits = 0;

            if (!monitor.waiting.isEmpty()) {
                final Thread newOwner = monitor.waiting.remove(0);
                monitor.owner = newOwner;
                monitor.permits = 1;
                newOwner.setState(ThreadState.RUNNABLE);
            }

            thread.setState(ThreadState.WAITING);
            return true;
        }
        return false;
    }

    public boolean notify(final Thread thread) {
        if (monitor.owner == thread) {
            final Iterator<Entry<Thread, Integer>> it = waiting.entrySet().iterator();

            if (it.hasNext()) {
                final Entry<Thread, Integer> entry = it.next();
                for (int i = 0; i < entry.getValue(); ++i)
                    monitor.acquire(entry.getKey());
                it.remove();
            }
            return true;
        }
        return false;
    }

    public boolean notifyAll(final Thread thread) {
        if (monitor.owner == thread) {
            final Iterator<Entry<Thread, Integer>> it = waiting.entrySet().iterator();

            while (it.hasNext()) {
                final Entry<Thread, Integer> entry = it.next();
                for (int i = 0; i < entry.getValue(); ++i)
                    monitor.acquire(entry.getKey());
                it.remove();
            }
            return true;
        }
        return false;
    }

    public abstract void get(final FieldInfo field, final byte[] value);

    public abstract void put(final FieldInfo field, final byte[] value);
}
