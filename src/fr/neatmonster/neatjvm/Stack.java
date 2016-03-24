package fr.neatmonster.neatjvm;

import java.util.ArrayList;
import java.util.List;

public class Stack {
    public List<StackFrame> frames = new ArrayList<>();

    public StackFrame addFrame(final short maxStack, final short maxLocals) {
        final StackFrame frame = new StackFrame(maxStack, maxLocals);
        frames.add(frame);
        return frame;
    }
}
