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
    public StackFrame     frame;

    public Thread(final VirtualMachine vm, final int id) {
        this.vm = vm;
        this.id = id;
    }

    public void start(final CodeAttribute code) {
        this.code = code;

        stack = new Stack();
        frame = stack.pushFrame(code.maxStack, code.maxLocals);
        state = ThreadState.RUNNING;
    }

    public void tick() {
        vm.currentThread = this;

        final int opcode = code.code[pc++] & 0xff;
        switch (opcode) {
            case 0x3: // iconst_0
            case 0x4: // iconst_1
            case 0x5: // iconst_2
            case 0x6: // iconst_3
            case 0x7: // iconst_4
            case 0x8: // iconst_5
            {
                final int i = opcode - 0x3;
                frame.pushInt(i);
                break;
            }
            case 0x3b: // istore_0
            case 0x3c: // istore_1
            case 0x3d: // istore_2
            case 0x3e: // istore_3
            {
                final int n = opcode - 0x3b;
                final int value = frame.popInt();
                frame.storeInt(n, value);
                break;
            }
            case 0xb1: // return
            {
                doReturn();
                break;
            }
            default:
                System.err.println("Unrecognized opcode 0x" + Integer.toHexString(opcode));
                break;
        }
    }

    private void doReturn() {
        stack.popFrame();

        final StackFrame prevFrame = stack.getTopFrame();
        if (prevFrame == null)
            state = ThreadState.DEAD;
        else
            contextSwitchDown(prevFrame);
    }

    public void contextSwitchUp() {
    }

    public void contextSwitchDown(final StackFrame prevFrame) {
        frame = prevFrame;

        pc = prevFrame.pc;
        code = prevFrame.code;
    }
}
