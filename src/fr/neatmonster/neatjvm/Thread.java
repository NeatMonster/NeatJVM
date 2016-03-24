package fr.neatmonster.neatjvm;

import fr.neatmonster.neatjvm.ExecutionPool.ThreadPriority;
import fr.neatmonster.neatjvm.format.attribute.CodeAttribute;
import fr.neatmonster.neatjvm.format.constant.ClassConstant;

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
    public InstanceData   instance;

    public Thread(final VirtualMachine vm, final int id) {
        this.vm = vm;
        this.id = id;
    }

    public void start(final CodeAttribute code, final InstanceData instance) {
        this.code = code;
        this.instance = instance;

        stack = new Stack();
        frame = stack.pushFrame(code.maxStack, code.maxLocals);
        state = ThreadState.RUNNING;
    }

    public void tick() {
        vm.currentThread = this;

        final int opcode = code.code[pc++] & 0xff;
        switch (opcode) {
            // CONSTANTS
            case 0x0: // nop
                break;
            case 0x1: // aconst_null
                frame.pushReference(0);
                break;
            case 0x2: // iconst_m1
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
            case 0x10: // bipush
            {
                final byte b = code.code[pc++];
                frame.pushInt(b);
                break;
            }
            case 0x11: // sipush
            {
                final byte b1 = code.code[pc++];
                final byte b2 = code.code[pc++];
                frame.pushInt(b1 << 8 | b2);
                break;
            }
            // LOADS
            case 0x1a: // iload_0
            case 0x1b: // iload_1
            case 0x1c: // iload_2
            case 0x1d: // iload_3
            {
                final int n = opcode - 0x1a;
                final int value = frame.getInt(n);
                frame.pushInt(value);
                break;
            }
            // STORES
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
            // STACK
            case 0x59: // dup
                frame.dup();
                break;
            // MATH
            case 0x60: // iadd
            {
                final int value2 = frame.popInt();
                final int value1 = frame.popInt();
                frame.pushInt(value1 + value2);
                break;
            }
            // CONVERSIONS
            // COMPARISONS
            // CONTROL
            case 0xb1: // return
                _return();
                break;
            // REFERENCES
            case 0xbb: // new
            {
                final byte indexbyte1 = code.code[pc++];
                final byte indexbyte2 = code.code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;
                final ClassConstant classInfo = code.classFile.constants.getClass(index);
                if (!classInfo.isResolved())
                    classInfo.resolve();
                final int objectref = classInfo.resolvedClass.newInstance();
                frame.pushReference(objectref);
                break;
            }
            // EXTENDED
            // RESERVED
            default:
                System.err.println("Unrecognized opcode 0x" + Integer.toHexString(opcode));
                System.exit(0);
                break;
        }
    }

    private void _return() {
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
