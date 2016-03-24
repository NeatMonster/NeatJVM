package fr.neatmonster.neatjvm;

import fr.neatmonster.neatjvm.ExecutionPool.ThreadPriority;
import fr.neatmonster.neatjvm.format.MethodInfo;
import fr.neatmonster.neatjvm.format.attribute.CodeAttribute;
import fr.neatmonster.neatjvm.format.constant.ClassConstant;
import fr.neatmonster.neatjvm.format.constant.MethodrefConstant;

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
            case 0x9: // lconst_0
            case 0xa: // lconst_1
            {
                final long l = opcode - 0x9;
                frame.pushLong(l);
                break;
            }
            case 0xb: // fconst_0
            case 0xc: // fconst_1
            case 0xd: // fconst_2
            {
                final float f = opcode - 0xb;
                frame.pushFloat(f);
                break;
            }
            case 0xe: // dconst_0
            case 0xf: // dconst_1
            {
                final double d = opcode - 0xe;
                frame.pushDouble(d);
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
            case 0x15: // iload
            {
                final byte index = code.code[pc++];
                final int value = frame.getInt(index);
                frame.pushInt(value);
                break;
            }
            case 0x16: // lload
            {
                final byte index = code.code[pc++];
                final long value = frame.getLong(index);
                frame.pushLong(value);
                break;
            }
            case 0x17: // fload
            {
                final byte index = code.code[pc++];
                final float value = frame.getFloat(index);
                frame.pushFloat(value);
                break;
            }
            case 0x18: // dload
            {
                final byte index = code.code[pc++];
                final double value = frame.getDouble(index);
                frame.pushDouble(value);
                break;
            }
            case 0x19: // aload
            {
                final byte index = code.code[pc++];
                final int value = frame.getReference(index);
                frame.pushReference(value);
                break;
            }
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
            case 0x1e: // lload_0
            case 0x1f: // lload_1
            case 0x20: // lload_2
            case 0x21: // lload_3
            {
                final int n = opcode - 0x1e;
                final long value = frame.getLong(n);
                frame.pushLong(value);
                break;
            }
            case 0x22: // fload_0
            case 0x23: // fload_1
            case 0x24: // fload_2
            case 0x25: // fload_3
            {
                final int n = opcode - 0x22;
                final float value = frame.getFloat(n);
                frame.pushFloat(value);
                break;
            }
            case 0x26: // dload_0
            case 0x27: // dload_1
            case 0x28: // dload_2
            case 0x29: // dload_3
            {
                final int n = opcode - 0x26;
                final double value = frame.getDouble(n);
                frame.pushDouble(value);
                break;
            }
            case 0x2a: // aload_0
            case 0x2b: // aload_1
            case 0x2c: // aload_2
            case 0x2d: // aload_3
            {
                final int n = opcode - 0x2a;
                final int value = frame.getReference(n);
                frame.pushReference(value);
                break;
            }
            // STORES
            case 0x36: // istore
            {
                final byte index = code.code[pc++];
                final int value = frame.popInt();
                frame.storeInt(index, value);
                break;
            }
            case 0x37: // lstore
            {
                final byte index = code.code[pc++];
                final long value = frame.popLong();
                frame.storeLong(index, value);
                break;
            }
            case 0x38: // fstore
            {
                final byte index = code.code[pc++];
                final float value = frame.popFloat();
                frame.storeFloat(index, value);
                break;
            }
            case 0x39: // dstore
            {
                final byte index = code.code[pc++];
                final double value = frame.popDouble();
                frame.storeDouble(index, value);
                break;
            }
            case 0x3a: // astore
            {
                final byte index = code.code[pc++];
                final int value = frame.popReference();
                frame.storeReference(index, value);
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
            case 0x3f: // lstore_0
            case 0x40: // lstore_1
            case 0x41: // lstore_2
            case 0x42: // lstore_3
            {
                final int n = opcode - 0x3f;
                final long value = frame.popLong();
                frame.storeLong(n, value);
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
            case 0x68: // imul
            {
                final int value2 = frame.popInt();
                final int value1 = frame.popInt();
                frame.pushInt(value1 * value2);
                break;
            }
            case 0x64: // isub
            {
                final int value2 = frame.popInt();
                final int value1 = frame.popInt();
                frame.pushInt(value1 - value2);
            	break ;
            }
            case 0x6c: // idiv
            {
                final int value2 = frame.popInt();
                final int value1 = frame.popInt();
                frame.pushInt(value1 / value2);
            	break ;
            }
            case 0x70: // irem
            {
                final int value2 = frame.popInt();
                final int value1 = frame.popInt();
                frame.pushInt(1 - (value1 / value2) * value2);
            	break ;
            }
            case 0x74: // ineg
            {
                final int value1 = frame.popInt();
                frame.pushInt(-value1);
            	break ;
            }
            // CONVERSIONS
            // COMPARISONS
            // CONTROL
            case 0xb1: // return
                _return();
                break;
            case 0xb7: // invokespecial
            {
                final byte indexbyte1 = code.code[pc++];
                final byte indexbyte2 = code.code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;

                final MethodrefConstant methodInfo = code.classFile.constants.getMethodref(index);
                if (!methodInfo.isResolved())
                    methodInfo.resolve();

                final MethodInfo method = methodInfo.method;
                if (!method.isResolved())
                    method.resolve();
                final int paramsSize = method.descriptor.getParametersSize();

                final int objectref = frame.stack[frame.stackTop - paramsSize - 1];
                final InstanceData instance = vm.handlePool.getInstance(objectref);
                if (instance == null) {
                    // TODO: Throw NullPointerException
                    System.err.println("NullPointerException");
                    System.exit(0);
                }

                final CodeAttribute newCode = method.code;
                final StackFrame newFrame = stack.pushFrame(newCode.maxStack, newCode.maxLocals);
                for (int i = frame.stackTop - paramsSize - 1; i < frame.stackTop; ++i)
                    newFrame.store(i, frame.pop());

                contextSwitchUp(newFrame, newCode);
                break;
            }
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

    public void contextSwitchUp(final StackFrame newFrame, final CodeAttribute newCode) {
        frame.pc = pc;
        frame.code = code;

        pc = 0;
        code = newCode;
        frame = newFrame;
    }

    public void contextSwitchDown(final StackFrame prevFrame) {
        frame = prevFrame;

        pc = prevFrame.pc;
        code = prevFrame.code;
    }
}
