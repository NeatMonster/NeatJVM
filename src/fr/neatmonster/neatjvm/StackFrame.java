package fr.neatmonster.neatjvm;

import fr.neatmonster.neatjvm.format.attribute.CodeAttribute;

public class StackFrame {
    public HeapManager   stackHeap;

    public final int     stack;
    public int           stackTop;
    public final int     locals;

    public int           pc;
    public CodeAttribute code;

    public StackFrame(final Stack frameStack, final short maxStack, final short maxLocals) {
        stackHeap = frameStack.thread.stackHeap;

        stack = stackHeap.allocate(maxStack * 4);
        locals = stackHeap.allocate(maxLocals * 4);
    }

    public void pushInt(final int value) {
        stackHeap.putInt(stack + stackTop++ * 4, value);
    }

    public void pushLong(final long value) {
        pushInt((int) (value & 0xffffffff));
        pushInt((int) (value >> 16 & 0xffffffff));
    }

    public void pushFloat(final float value) {
        pushInt(Float.floatToIntBits(value));
    }

    public void pushDouble(final double value) {
        final long bits = Double.doubleToRawLongBits(value);
        pushInt((int) (bits & 0xffffffff));
        pushInt((int) (bits >> 16 & 0xffffffff));
    }

    public void pushReference(final int value) {
        pushInt(value);
    }

    public void pushReturnType(final int value) {
        pushInt(value);
    }

    public int popInt() {
        return stackHeap.getInt(stack + --stackTop * 4);
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

    public int popReturnType() {
        return popInt();
    }

    public void storeInt(final int index, final int value) {
        stackHeap.putInt(locals + index * 4, value);
    }

    public void storeLong(final int index, final long value) {
        storeInt(index, (int) (value & 0xffffffff));
        storeInt(index + 1, (int) (value >> 16 & 0xffffffff));
    }

    public void storeFloat(final int index, final float value) {
        storeInt(index, Float.floatToIntBits(value));
    }

    public void storeDouble(final int index, final double value) {
        final long bits = Double.doubleToRawLongBits(value);
        storeInt(index, (int) (bits & 0xffffffff));
        storeInt(index + 1, (int) (bits >> 16 & 0xffffffff));
    }

    public void storeReference(final int index, final int value) {
        storeInt(index, value);
    }

    public void storeReturnType(final int index, final int value) {
        storeInt(index, value);
    }

    public int getInt(final int index) {
        return stackHeap.getInt(locals + index * 4);
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

    public int getReturnType(final int index) {
        return getInt(index);
    }

    public void dup() {
        final int value = stackHeap.getInt(stack + (stackTop - 1) * 4);
        stackHeap.putInt(stack + stackTop++ * 4, value);
    }
}
