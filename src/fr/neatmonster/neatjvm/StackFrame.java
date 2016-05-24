package fr.neatmonster.neatjvm;

import fr.neatmonster.neatjvm.ObjectData.Monitor;
import fr.neatmonster.neatjvm.format.attribute.CodeAttribute;

public class StackFrame {
    private final MemoryPool stackSpace;

    private final int        stack;
    private int              stackTop;
    private final int        locals;

    int                      pc;
    CodeAttribute            code;
    Monitor                  monitor;

    public StackFrame(final MemoryPool stackSpace, final short maxStack, final short maxLocals) {
        this.stackSpace = stackSpace;

        stack = stackSpace.allocate(maxStack * 4);
        locals = stackSpace.allocate(maxLocals * 4);
    }

    public void pushInt(final int value) {
        stackSpace.putInt(stack + stackTop++ * 4, value);
    }

    public void pushLong(final long value) {
        pushInt((int) value);
        pushInt((int) (value >> 32));
    }

    public void pushFloat(final float value) {
        pushInt(Float.floatToRawIntBits(value));
    }

    public void pushDouble(final double value) {
        pushLong(Double.doubleToRawLongBits(value));
    }

    public void pushReference(final int value) {
        pushInt(value);
    }

    public int popInt() {
        return stackSpace.getInt(stack + --stackTop * 4);
    }

    public long popLong() {
        return (long) popInt() << 32 | popInt() & 0xffffffffL;
    }

    public float popFloat() {
        return Float.intBitsToFloat(popInt());
    }

    public double popDouble() {
        return Double.longBitsToDouble(popLong());
    }

    public int popReference() {
        return popInt();
    }

    public int peekInt(final int offset) {
        return stackSpace.getInt(stack + (stackTop - offset) * 4);
    }

    public void storeInt(final int index, final int value) {
        stackSpace.putInt(locals + index * 4, value);
    }

    public void storeLong(final int index, final long value) {
        storeInt(index, (int) value);
        storeInt(index + 1, (int) (value >> 32));
    }

    public void storeFloat(final int index, final float value) {
        storeInt(index, Float.floatToRawIntBits(value));
    }

    public void storeDouble(final int index, final double value) {
        storeLong(index, Double.doubleToRawLongBits(value));
    }

    public void storeReference(final int index, final int value) {
        storeInt(index, value);
    }

    public int getInt(final int index) {
        return stackSpace.getInt(locals + index * 4);
    }

    public long getLong(final int index) {
        return (long) getInt(index + 1) << 32 | getInt(index) & 0xffffffffL;
    }

    public float getFloat(final int index) {
        return Float.intBitsToFloat(getInt(index));
    }

    public double getDouble(final int index) {
        return Double.longBitsToDouble(getLong(index));
    }

    public int getReference(final int index) {
        return getInt(index);
    }
}
