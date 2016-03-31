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
        pushInt((int) (value >> 16));
    }

    public void pushFloat(final float value) {
        pushInt(Float.floatToIntBits(value));
    }

    public void pushDouble(final double value) {
        final long bits = Double.doubleToRawLongBits(value);
        pushInt((int) bits);
        pushInt((int) (bits >> 16));
    }

    public void pushReference(final int value) {
        pushInt(value);
    }

    public int popInt() {
        return stackSpace.getInt(stack + --stackTop * 4);
    }

    public long popLong() {
        final int value = popInt() << 16;
        return value | popInt();
    }

    public float popFloat() {
        return Float.intBitsToFloat(popInt());
    }

    public double popDouble() {
        final int value = popInt() << 16;
        return Double.longBitsToDouble(value | popInt());
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
        storeInt(index + 1, (int) (value >> 16));
    }

    public void storeFloat(final int index, final float value) {
        storeInt(index, Float.floatToIntBits(value));
    }

    public void storeDouble(final int index, final double value) {
        final long bits = Double.doubleToRawLongBits(value);
        storeInt(index, (int) bits);
        storeInt(index + 1, (int) (bits >> 16));
    }

    public void storeReference(final int index, final int value) {
        storeInt(index, value);
    }

    public int getInt(final int index) {
        return stackSpace.getInt(locals + index * 4);
    }

    public long getLong(final int index) {
        final int value = getInt(index + 1) << 16;
        return value | getInt(index);
    }

    public float getFloat(final int index) {
        return Float.intBitsToFloat(getInt(index));
    }

    public double getDouble(final int index) {
        final int value = getInt(index + 1) << 16;
        return Double.longBitsToDouble(value | getInt(index));
    }

    public int getReference(final int index) {
        return getInt(index);
    }
}
