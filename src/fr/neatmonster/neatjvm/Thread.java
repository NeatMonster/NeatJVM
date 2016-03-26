package fr.neatmonster.neatjvm;

import java.nio.ByteBuffer;

import fr.neatmonster.neatjvm.ClassFile.PrimitiveArrayClassFile;
import fr.neatmonster.neatjvm.ExecutionPool.ThreadPriority;
import fr.neatmonster.neatjvm.InstanceData.PrimitiveArrayInstanceData;
import fr.neatmonster.neatjvm.format.FieldInfo;
import fr.neatmonster.neatjvm.format.MethodInfo;
import fr.neatmonster.neatjvm.format.attribute.CodeAttribute;

public class Thread {
    public static final int MAX_STACK_SIZE = 10 * 1024;

    public static enum ThreadState {
        DEAD, RUNNING
    }

    public VirtualMachine vm;
    public int            id;
    public ThreadState    state;
    public ThreadPriority priority;
    public HeapManager    stackHeap;

    public int            pc;
    public CodeAttribute  code;
    public Stack          stack;
    public StackFrame     frame;
    public InstanceData   instance;

    public Thread(final VirtualMachine vm, final int id) {
        this.vm = vm;
        this.id = id;

        stackHeap = new HeapManager(MAX_STACK_SIZE);
    }

    public void start(final CodeAttribute code, final InstanceData instance) {
        this.code = code;
        this.instance = instance;

        stack = new Stack(this);
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
                final int b1 = code.code[pc++] & 0xff;
                final int b2 = code.code[pc++] & 0xff;
                frame.pushInt(b1 << 8 | b2);
                break;
            }
            // LOADS
            case 0x15: // iload
            {
                final int index = code.code[pc++] & 0xff;
                final int value = frame.getInt(index);
                frame.pushInt(value);
                break;
            }
            case 0x16: // lload
            {
                final int index = code.code[pc++] & 0xff;
                final long value = frame.getLong(index);
                frame.pushLong(value);
                break;
            }
            case 0x17: // fload
            {
                final int index = code.code[pc++] & 0xff;
                final float value = frame.getFloat(index);
                frame.pushFloat(value);
                break;
            }
            case 0x18: // dload
            {
                final int index = code.code[pc++] & 0xff;
                final double value = frame.getDouble(index);
                frame.pushDouble(value);
                break;
            }
            case 0x19: // aload
            {
                final int index = code.code[pc++] & 0xff;
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
            case 0x2e: // iaload
            {
                final int index = frame.popInt();

                final int arrayref = frame.popReference();
                if (arrayref == 0) {
                    // TODO Throw NullPointerException
                    System.err.println("NullPointerException");
                    System.exit(0);
                }

                final PrimitiveArrayInstanceData instance = (PrimitiveArrayInstanceData) vm.handlePool
                        .getInstance(arrayref);
                if (index < 0 || index >= instance.arrayLength) {
                    // TODO Throw ArrayIndexOutOfBoundsException
                    System.err.println("ArrayIndexOutOfBoundsException");
                    System.exit(0);
                }

                final int value = vm.javaHeap.getInt(instance.dataStart + index * 4);
                frame.pushInt(value);
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
                final int index = code.code[pc++] & 0xff;
                final long value = frame.popLong();
                frame.storeLong(index, value);
                break;
            }
            case 0x38: // fstore
            {
                final int index = code.code[pc++] & 0xff;
                final float value = frame.popFloat();
                frame.storeFloat(index, value);
                break;
            }
            case 0x39: // dstore
            {
                final int index = code.code[pc++] & 0xff;
                final double value = frame.popDouble();
                frame.storeDouble(index, value);
                break;
            }
            case 0x3a: // astore
            {
                final int index = code.code[pc++] & 0xff;
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
            case 0x4b: // astore_0
            case 0x4c: // astore_1
            case 0x4d: // astore_2
            case 0x4e: // astore_3
            {
                final int n = opcode - 0x4b;
                final int objectref = frame.popReference();
                frame.storeReference(n, objectref);
                break;
            }
            case 0x4f: // iastore
            {
                final int value = frame.popInt();
                final int index = frame.popInt();

                final int arrayref = frame.popReference();
                if (arrayref == 0) {
                    // TODO Throw NullPointerException
                    System.err.println("NullPointerException");
                    System.exit(0);
                }

                final PrimitiveArrayInstanceData instance = (PrimitiveArrayInstanceData) vm.handlePool
                        .getInstance(arrayref);
                if (index < 0 || index >= instance.arrayLength) {
                    // TODO Throw ArrayIndexOutOfBoundsException
                    System.err.println("ArrayIndexOutOfBoundsException");
                    System.exit(0);
                }

                vm.javaHeap.putInt(instance.dataStart + index * 4, value);
                break;
            }
            // STACK
            case 0x57: // pop
                frame.popInt();
                break;
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
            case 0x64: // isub
            {
                final int value2 = frame.popInt();
                final int value1 = frame.popInt();
                frame.pushInt(value1 - value2);
                break;
            }
            case 0x68: // imul
            {
                final int value2 = frame.popInt();
                final int value1 = frame.popInt();
                frame.pushInt(value1 * value2);
                break;
            }
            case 0x6c: // idiv
            {
                final int value2 = frame.popInt();
                if (value2 == 0) {
                    // TODO Throw ArithmeticException
                    System.err.println("ArithmeticException");
                    System.exit(0);
                }
                final int value1 = frame.popInt();
                frame.pushInt(value1 / value2);
                break;
            }
            case 0x70: // irem
            {
                final int value2 = frame.popInt();
                if (value2 == 0) {
                    // TODO Throw ArithmeticException
                    System.err.println("ArithmeticException");
                    System.exit(0);
                }
                final int value1 = frame.popInt();
                frame.pushInt(value1 % value2);
                break;
            }
            case 0x74: // ineg
            {
                final int value1 = frame.popInt();
                frame.pushInt(-value1);
                break;
            }
            case 0x84: // iinc
            {
                final int index = code.code[pc++] & 0xff;
                final byte const_ = code.code[pc++];
                final int value = frame.getInt(index);
                frame.storeInt(index, value + const_);
                break;
            }
            // CONVERSIONS
            // COMPARISONS
            case 0x99: // ifeq
            case 0x9a: // ifne
            case 0x9b: // iflt
            case 0x9c: // ifge
            case 0x9d: // ifgt
            case 0x9e: // ifle
            {
                final byte branchbyte1 = code.code[pc++];
                final byte branchbyte2 = code.code[pc++];
                final int offset = branchbyte1 << 8 | branchbyte2;

                final int value = frame.popInt();

                boolean cond = false;
                cond |= opcode == 0x99 && value == 0;
                cond |= opcode == 0x9a && value != 0;
                cond |= opcode == 0x9b && value < 0;
                cond |= opcode == 0x9c && value >= 0;
                cond |= opcode == 0x9d && value > 0;
                cond |= opcode == 0x9e && value <= 0;

                if (cond)
                    pc += offset - 3;
                break;
            }
            // CONTROL
            case 0xab: // lookupswitch
            {
                final int instrAddr = pc - 1;
                while (pc % 4 > 0)
                    ++pc;

                int default_ = 0;
                default_ |= code.code[pc++] << 24;
                default_ |= code.code[pc++] << 16;
                default_ |= code.code[pc++] << 8;
                default_ |= code.code[pc++];

                int npairs = 0;
                npairs |= code.code[pc++] << 24;
                npairs |= code.code[pc++] << 16;
                npairs |= code.code[pc++] << 8;
                npairs |= code.code[pc++];

                final int key = frame.popInt();
                for (int i = 0; i < npairs; ++i) {
                    int match = 0;
                    match |= code.code[pc++] << 24;
                    match |= code.code[pc++] << 16;
                    match |= code.code[pc++] << 8;
                    match |= code.code[pc++];

                    int offset = 0;
                    offset |= code.code[pc++] << 24;
                    offset |= code.code[pc++] << 16;
                    offset |= code.code[pc++] << 8;
                    offset |= code.code[pc++];

                    if (key == match) {
                        pc = instrAddr + offset;
                        return;
                    }
                }

                pc = instrAddr + default_;
                break;
            }
            case 0xac: // ireturn
                returnInt();
                break;
            case 0xb1: // return
                returnVoid();
                break;
            // REFERENCES
            case 0xb2: // getstatic
            {
                final byte indexbyte1 = code.code[pc++];
                final byte indexbyte2 = code.code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;

                final FieldInfo field = code.classFile.constants.getFieldref(index);
                getStatic(field.resolve());
                break;
            }
            case 0xb3: // putstatic
            {
                final byte indexbyte1 = code.code[pc++];
                final byte indexbyte2 = code.code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;

                final FieldInfo field = code.classFile.constants.getFieldref(index);
                putStatic(field.resolve());
                break;
            }
            case 0xb4: // getfield
            {
                final byte indexbyte1 = code.code[pc++];
                final byte indexbyte2 = code.code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;

                final FieldInfo field = code.classFile.constants.getFieldref(index);
                getField(field.resolve());
                break;
            }
            case 0xb5: // putfield
            {
                final byte indexbyte1 = code.code[pc++];
                final byte indexbyte2 = code.code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;

                final FieldInfo field = code.classFile.constants.getFieldref(index);
                putField(field.resolve());
                break;
            }
            case 0xb6: // invokevirtual
            {
                final byte indexbyte1 = code.code[pc++];
                final byte indexbyte2 = code.code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;

                final MethodInfo method = code.classFile.constants.getMethodref(index);
                invokeVirtual(method.resolve());
                break;
            }
            case 0xb7: // invokespecial
            {
                final byte indexbyte1 = code.code[pc++];
                final byte indexbyte2 = code.code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;

                final MethodInfo method = code.classFile.constants.getMethodref(index);
                invokeSpecial(method.resolve());
                break;
            }
            case 0xb8: // invokestatic
            {
                final byte indexbyte1 = code.code[pc++];
                final byte indexbyte2 = code.code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;

                final MethodInfo method = code.classFile.constants.getMethodref(index);
                invokeStatic(method.resolve());
                break;
            }
            case 0xbb: // new
            {
                final byte indexbyte1 = code.code[pc++];
                final byte indexbyte2 = code.code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;

                final ClassFile classFile = code.classFile.constants.getClass(index);
                final int objectref = classFile.newInstance();
                frame.pushReference(objectref);
                break;
            }
            case 0xbc: // newarray
            {
                final int count = frame.popInt();
                if (count < 0) {
                    // TODO Throw NegativeArraySizeException
                    System.err.println("NegativeArraySizeException");
                    System.exit(0);
                }
                final byte atype = code.code[pc++];

                final PrimitiveArrayClassFile arrayClass = vm.classLoader.definePrimitiveArrayClass(atype);
                final int arrayref = arrayClass.newInstance(count);
                frame.pushReference(arrayref);
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

    public void invokeSpecial(final MethodInfo method) {
        final int paramsSize = method.descriptor.getIntSize();

        final int objectref = stackHeap.getInt(frame.stack + (frame.stackTop - paramsSize - 1) * 4);
        final InstanceData instance = vm.handlePool.getInstance(objectref);
        if (instance == null) {
            // TODO Throw NullPointerException
            System.err.println("NullPointerException");
            System.exit(0);
        }

        // TODO Throw IllegalAccessError, AbstractMethodError,
        // UnsatisfiedLinkError, IncompatibleClassChangeError if needed

        final CodeAttribute newCode = method.code;
        final StackFrame newFrame = stack.pushFrame(newCode.maxStack, newCode.maxLocals);
        for (int i = paramsSize; i >= 0; --i)
            newFrame.storeInt(i, frame.popInt());

        contextSwitchUp(newFrame, newCode);
    }

    public void invokeVirtual(MethodInfo method) {
        final int paramsSize = method.descriptor.getIntSize();

        final int objectref = stackHeap.getInt(frame.stack + (frame.stackTop - paramsSize - 1) * 4);
        final InstanceData instance = vm.handlePool.getInstance(objectref);
        if (instance == null) {
            // TODO Throw NullPointerException
            System.err.println("NullPointerException");
            System.exit(0);
        }

        // TODO Throw IllegalAccessError, AbstractMethodError,
        // UnsatisfiedLinkError, IncompatibleClassChangeError if needed

        int offset = -1;
        for (int i = 0; i < method.classFile.methods.length; ++i)
            if (method.classFile.methods[i] == method) {
                offset = i;
                break;
            }
        method = instance.classFile.methods[offset];

        final CodeAttribute newCode = method.code;
        final StackFrame newFrame = stack.pushFrame(newCode.maxStack, newCode.maxLocals);
        for (int i = paramsSize; i >= 0; --i)
            newFrame.storeInt(i, frame.popInt());

        contextSwitchUp(newFrame, newCode);
    }

    public void invokeStatic(final MethodInfo method) {
        final int paramsSize = method.descriptor.getIntSize();

        final CodeAttribute newCode = method.code;
        final StackFrame newFrame = stack.pushFrame(newCode.maxStack, newCode.maxLocals);
        for (int i = paramsSize - 1; i >= 0; --i)
            newFrame.storeInt(i, frame.popInt());

        contextSwitchUp(newFrame, newCode);
    }

    public void getStatic(final FieldInfo field) {
        final ClassData common = field.classFile.data;
        final byte[] value = new byte[field.descriptor.getIntSize() * 4];
        vm.javaHeap.get(common.dataStart + common.offsets.get(field), value, 0, field.descriptor.getSize());

        final ByteBuffer buf = ByteBuffer.wrap(value);
        for (int i = 0; i < field.descriptor.getIntSize(); ++i)
            frame.pushInt(buf.getInt());
    }

    public void putStatic(final FieldInfo field) {
        final ByteBuffer buf = ByteBuffer.allocate(field.descriptor.getIntSize() * 4);
        for (int i = 0; i < field.descriptor.getIntSize(); ++i)
            buf.putInt(frame.popInt());
        final byte[] value = buf.array();

        final ClassData common = field.classFile.data;
        vm.javaHeap.put(common.dataStart + common.offsets.get(field), value, 0, field.descriptor.getSize());
    }

    public void getField(final FieldInfo field) {
        final int objectref = frame.popReference();
        final InstanceData instance = vm.handlePool.getInstance(objectref);
        if (instance == null) {
            // TODO Throw NullPointerException
            System.err.println("NullPointerException");
            System.exit(0);
        }

        final byte[] value = new byte[field.descriptor.getIntSize() * 4];
        vm.javaHeap.get(instance.dataStart + instance.offsets.get(field), value, 0, field.descriptor.getSize());

        final ByteBuffer buf = ByteBuffer.wrap(value);
        for (int i = 0; i < field.descriptor.getIntSize(); ++i)
            frame.pushInt(buf.getInt());
    }

    public void putField(final FieldInfo field) {
        final ByteBuffer buf = ByteBuffer.allocate(field.descriptor.getIntSize() * 4);
        for (int i = 0; i < field.descriptor.getIntSize(); ++i)
            buf.putInt(frame.popInt());
        final byte[] value = buf.array();

        final int objectref = frame.popReference();
        final InstanceData instance = vm.handlePool.getInstance(objectref);
        if (instance == null) {
            // TODO Throw NullPointerException
            System.err.println("NullPointerException");
            System.exit(0);
        }

        vm.javaHeap.put(instance.dataStart + instance.offsets.get(field), value, 0, field.descriptor.getSize());
    }

    public void returnInt() {
        final int returnValue = frame.popInt();

        stack.popFrame();

        final StackFrame prevFrame = stack.getTopFrame();
        if (prevFrame == null)
            state = ThreadState.DEAD;
        else {
            prevFrame.pushInt(returnValue);
            contextSwitchDown(prevFrame);
        }
    }

    public void returnVoid() {
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

        frame = newFrame;
        pc = 0;
        code = newCode;
    }

    public void contextSwitchDown(final StackFrame prevFrame) {
        frame = prevFrame;
        pc = prevFrame.pc;
        code = prevFrame.code;
    }
}
