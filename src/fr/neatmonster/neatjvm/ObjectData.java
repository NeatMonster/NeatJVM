package fr.neatmonster.neatjvm;

import java.util.ArrayList;
import java.util.List;

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

    private final ClassFile classFile;
    private final Monitor   monitor;
    protected int           dataStart;

    public ObjectData(final ClassFile classFile) {
        this.classFile = classFile;
        monitor = new Monitor();
    }

    public ClassFile getClassFile() {
        return classFile;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public abstract void get(final FieldInfo field, final byte[] value);

    public abstract void put(final FieldInfo field, final byte[] value);
}
