package fr.neatmonster.neatjvm;

import java.util.ArrayList;
import java.util.List;

public class Stack {
    public List<StackFrame> frames = new ArrayList<>();

    public StackFrame getTopFrame() {
        if (frames.isEmpty())
            return null;
        return frames.get(frames.size() - 1);
    }

    public StackFrame pushFrame(final short maxStack, final short maxLocals) {
        final StackFrame frame = new StackFrame(maxStack, maxLocals);
        frames.add(frame);
        return frame;
    }

    public void popFrame() {
        frames.remove(frames.size() - 1);
    }
}
