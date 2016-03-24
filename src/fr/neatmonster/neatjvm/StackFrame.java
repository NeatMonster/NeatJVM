package fr.neatmonster.neatjvm;

public class StackFrame {
    public final byte[] stack;
    public final byte[] locals;

    public StackFrame(final short maxStack, final short maxLocals) {
        stack = new byte[maxStack];
        locals = new byte[maxLocals];
    }
}
