package fr.neatmonster.neatjvm;

import fr.neatmonster.neatjvm.format.attribute.CodeAttribute;

public class StackFrame {
    public final int[]   stack;
    public int           stackTop;

    public final int[]   locals;
    public int           localsTop;

    public int           pc;
    public CodeAttribute code;

    public StackFrame(final short maxStack, final short maxLocals) {
        stack = new int[maxStack];
        locals = new int[maxLocals];
    }

    public void pushInt(final int value) {
        stack[stackTop++] = value;
    }

    public void pushLong(final long value) {
        stack[stackTop++] = (int) (value & 0xffffffff);
        stack[stackTop++] = (int) (value >> 16 & 0xffffffff);
    }

    public void pushFloat(final float value) {
        stack[stackTop++] = Float.floatToIntBits(value);
    }

    public void pushDouble(final double value) {
        final long bits = Double.doubleToRawLongBits(value);
        stack[stackTop++] = (int) (bits & 0xffffffff);
        stack[stackTop++] = (int) (bits >> 16 & 0xffffffff);
    }

    public void pushReference(final int value) {
        stack[stackTop++] = value;
    }

    public void pushReturnType(final int value) {
        stack[stackTop++] = value;
    }

    public int popInt() {
        return stack[--stackTop];
    }

    public long popLong() {
        final int value = stack[--stackTop] << 16;
        return value | stack[--stackTop];
    }

    public float popFloat() {
        return Float.intBitsToFloat(stack[--stackTop]);
    }

    public double popDouble() {
        final int value = stack[--stackTop] << 16;
        return Double.longBitsToDouble(value | stack[--stackTop]);
    }

    public int popReference() {
        return stack[--stackTop];
    }

    public int popReturnType() {
        return stack[--stackTop];
    }

    public void storeInt(final int index, final int value) {
        locals[index] = value;
    }

    public void storeLong(final int index, final long value) {
        locals[index] = (int) (value & 0xffffffff);
        locals[index + 1] = (int) (value >> 16 & 0xffffffff);
    }

    public void storeFloat(final int index, final float value) {
        locals[index] = Float.floatToIntBits(value);
    }

    public void storeDouble(final int index, final double value) {
        final long bits = Double.doubleToRawLongBits(value);
        locals[index] = (int) (bits & 0xffffffff);
        locals[index + 1] = (int) (bits >> 16 & 0xffffffff);
    }

    public void storeReference(final int index, final int value) {
        locals[index] = value;
    }

    public void storeReturnType(final int index, final int value) {
        locals[index] = value;
    }

    public int getInt(final int index) {
        return locals[index];
    }

    public long getLong(final int index) {
        final int value = locals[index + 1] << 16;
        return value | locals[index];
    }

    public float getFloat(final int index) {
        return Float.intBitsToFloat(locals[index]);
    }

    public double getDouble(final int index) {
        final int value = locals[index + 1] << 16;
        return Double.longBitsToDouble(value | locals[index]);
    }

    public int getReference(final int index) {
        return locals[index];
    }

    public int getReturnType(final int index) {
        return locals[index];
    }

    public void dup() {
        final int value = stack[stackTop - 1];
        stack[stackTop++] = value;
    }
}
