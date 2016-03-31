package fr.neatmonster.neatjvm;

import java.util.PriorityQueue;
import java.util.Queue;

import fr.neatmonster.neatjvm.Thread.ThreadState;

public class ThreadPool {
    private static class ThreadEntry implements Comparable<ThreadEntry> {
        private static long  nextTicket = 0L;
        private final Thread thread;
        private long         ticket;

        private ThreadEntry(final Thread thread) {
            this.thread = thread;
            nextTicket();
        }

        private Thread getThread() {
            return thread;
        }

        private void nextTicket() {
            ticket = nextTicket++;
        }

        @Override
        public int compareTo(final ThreadEntry o) {
            int res = thread.getPriority().ordinal() - o.getThread().getPriority().ordinal();
            if (res == 0 && thread != o.thread)
                res = ticket < o.ticket ? -1 : 1;
            return res;
        }
    }

    private final Queue<ThreadEntry> threads;
    private int                      nextThreadId = 0;

    public ThreadPool() {
        threads = new PriorityQueue<>();
    }

    public void addThread(final Thread thread) {
        threads.offer(new ThreadEntry(thread));
    }

    public Thread getNextThread() {
        final ThreadEntry entry = threads.poll();
        if (entry == null)
            return null;
        final Thread thread = entry.getThread();
        if (thread.getState() == ThreadState.RUNNABLE) {
            entry.nextTicket();
            threads.offer(entry);
            return thread;
        }
        return getNextThread();
    }

    public int getNextThreadId() {
        return nextThreadId++;
    }
}
