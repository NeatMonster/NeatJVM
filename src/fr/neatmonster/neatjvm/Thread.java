package fr.neatmonster.neatjvm;

import fr.neatmonster.neatjvm.ExecutionPool.ThreadPriority;
import fr.neatmonster.neatjvm.format.attribute.CodeAttribute;

public class Thread {
    public static enum ThreadState {
        DEAD, RUNNING, WAITING
    }

    public VirtualMachine vm;

    public int            id;
    public ThreadState    state;
    public ThreadPriority priority;

    public int            pc;
    public CodeAttribute  code;
    public Stack          stack;
    public StackFrame     stackFrame;

    public Thread(final VirtualMachine vm, final int id) {
        this.vm = vm;
        this.id = id;
    }

    public void start(final CodeAttribute code) {
        this.code = code;

        stack = new Stack();
        stackFrame = stack.addFrame(code.maxStack, code.maxLocals);
        state = ThreadState.RUNNING;
    }

    public void tick() {
        vm.currentThread = this;

        final byte opcode = code.code[pc++];
        // TODO: Execute the instruction.
        System.out.println("opcode: 0x" + Integer.toHexString(opcode & 0xff));
    }
}
