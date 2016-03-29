package fr.neatmonster.neatjvm;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import fr.neatmonster.neatjvm.ClassFile.ArrayClassFile;
import fr.neatmonster.neatjvm.InstanceData.ArrayInstanceData;
import fr.neatmonster.neatjvm.format.ConstantInfo;
import fr.neatmonster.neatjvm.format.FieldInfo;
import fr.neatmonster.neatjvm.format.FieldType.BaseType;
import fr.neatmonster.neatjvm.format.MethodInfo;
import fr.neatmonster.neatjvm.format.attribute.CodeAttribute;

public class Thread {
    public enum ThreadPriority {
        MIN_PRIORITY, NORM_PRIORITY, MAX_PRIORITY
    }

    public enum ThreadState {
        NEW, RUNNABLE, BLOCKED, WAITING, TERMINATED
    }

    private final int        id;
    private final MemoryPool stackSpace;

    private ThreadState      state;
    private ThreadPriority   priority;

    private StackFrame       frame;
    private List<StackFrame> stack;

    private int              pc;
    private byte[]           code;
    private CodeAttribute    codeAttr;
    private ClassFile        classFile;

    public Thread(final int id) {
        this.id = id;
        state = ThreadState.NEW;
        priority = ThreadPriority.NORM_PRIORITY;
        stackSpace = new MemoryPool(VirtualMachine.MAX_STACK_SIZE);
    }

    public int getId() {
        return id;
    }

    public ThreadState getState() {
        return state;
    }

    public ThreadPriority getPriority() {
        return priority;
    }

    public void setPriority(final ThreadPriority priority) {
        this.priority = priority;
    }

    public void start(final CodeAttribute codeAttr) {
        this.codeAttr = codeAttr;
        classFile = codeAttr.getClassFile();
        code = codeAttr.getCode();

        stack = new ArrayList<>();
        frame = pushFrame(codeAttr.getMaxStack(), codeAttr.getMaxLocals());

        state = ThreadState.RUNNABLE;
    }

    public void tick() {
        VirtualMachine.setCurrentThread(this);

        final MemoryPool heapSpace = VirtualMachine.getHeapSpace();
        final ClassLoader classLoader = VirtualMachine.getClassLoader();
        final InstancePool instancePool = VirtualMachine.getInstancePool();

        final int opcode = code[pc++] & 0xff;
        switch (opcode) {
            /*
             * CONSTANTS
             */
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
                final byte value = code[pc++];
                frame.pushInt(value);
                break;
            }
            case 0x11: // sipush
            {
                final byte byte1 = code[pc++];
                final byte byte2 = code[pc++];
                final int value = byte1 << 8 | byte2;
                frame.pushInt(value);
                break;
            }
            /*
             * LOADS
             */
            case 0x15: // iload
            {
                final int index = code[pc++] & 0xff;
                final int value = frame.getInt(index);
                frame.pushInt(value);
                break;
            }
            case 0x16: // lload
            {
                final int index = code[pc++] & 0xff;
                final long value = frame.getLong(index);
                frame.pushLong(value);
                break;
            }
            case 0x17: // fload
            {
                final int index = code[pc++] & 0xff;
                final float value = frame.getFloat(index);
                frame.pushFloat(value);
                break;
            }
            case 0x18: // dload
            {
                final int index = code[pc++] & 0xff;
                final double value = frame.getDouble(index);
                frame.pushDouble(value);
                break;
            }
            case 0x19: // aload
            {
                final int index = code[pc++] & 0xff;
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

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                if (index < 0 || index >= instance.getLength()) {
                    // TODO Throw ArrayIndexOutOfBoundsException
                    System.err.println("ArrayIndexOutOfBoundsException");
                    System.exit(0);
                }

                final int value = heapSpace.getInt(instance.dataStart + index * 4);
                frame.pushInt(value);
                break;
            }
            case 0x32: // aaload
            {
                final int index = frame.popInt();

                final int arrayref = frame.popReference();
                if (arrayref == 0) {
                    // TODO Throw NullPointerException
                    System.err.println("NullPointerException");
                    System.exit(0);
                }

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                if (index < 0 || index >= instance.getLength()) {
                    // TODO Throw ArrayIndexOutOfBoundsException
                    System.err.println("ArrayIndexOutOfBoundsException");
                    System.exit(0);
                }

                final int value = heapSpace.getInt(instance.dataStart + index * 4);
                frame.pushReference(value);
                break;
            }
            /*
             * STORES
             */
            case 0x36: // istore
            {
                final int index = code[pc++] & 0xff;
                final int value = frame.popInt();
                frame.storeInt(index, value);
                break;
            }
            case 0x37: // lstore
            {
                final int index = code[pc++] & 0xff;
                final long value = frame.popLong();
                frame.storeLong(index, value);
                break;
            }
            case 0x38: // fstore
            {
                final int index = code[pc++] & 0xff;
                final float value = frame.popFloat();
                frame.storeFloat(index, value);
                break;
            }
            case 0x39: // dstore
            {
                final int index = code[pc++] & 0xff;
                final double value = frame.popDouble();
                frame.storeDouble(index, value);
                break;
            }
            case 0x3a: // astore
            {
                final int index = code[pc++] & 0xff;
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

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                if (index < 0 || index >= instance.getLength()) {
                    // TODO Throw ArrayIndexOutOfBoundsException
                    System.err.println("ArrayIndexOutOfBoundsException");
                    System.exit(0);
                }

                heapSpace.putInt(instance.dataStart + index * 4, value);
                break;
            }
            case 0x53: // aastore
            {
                final int value = frame.popReference();
                final int index = frame.popInt();

                final int arrayref = frame.popReference();
                if (arrayref == 0) {
                    // TODO Throw NullPointerException
                    System.err.println("NullPointerException");
                    System.exit(0);
                }

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                if (index < 0 || index >= instance.getLength()) {
                    // TODO Throw ArrayIndexOutOfBoundsException
                    System.err.println("ArrayIndexOutOfBoundsException");
                    System.exit(0);
                }

                heapSpace.putInt(instance.dataStart, index * 4 + value);
                break;
            }
            /*
             * STACK
             */
            case 0x57: // pop
                frame.popInt();
                break;
            case 0x59: // dup
                frame.dup();
                break;
            /*
             * MATH
             */
            case 0x60: // iadd
            {
                final int value2 = frame.popInt();
                final int value1 = frame.popInt();
                final int result = value1 + value2;
                frame.pushInt(result);
                break;
            }
            case 0x64: // isub
            {
                final int value2 = frame.popInt();
                final int value1 = frame.popInt();
                final int result = value1 - value2;
                frame.pushInt(result);
                break;
            }
            case 0x68: // imul
            {
                final int value2 = frame.popInt();
                final int value1 = frame.popInt();
                final int result = value1 * value2;
                frame.pushInt(result);
                break;
            }
            case 0x6c: // idiv
            {
                final int value2 = frame.popInt();
                final int value1 = frame.popInt();
                if (value2 == 0) {
                    // TODO Throw ArithmeticException
                    System.err.println("ArithmeticException");
                    System.exit(0);
                }
                final int result = value1 / value2;
                frame.pushInt(result);
                break;
            }
            case 0x70: // irem
            {
                final int value2 = frame.popInt();
                final int value1 = frame.popInt();
                if (value2 == 0) {
                    // TODO Throw ArithmeticException
                    System.err.println("ArithmeticException");
                    System.exit(0);
                }
                final int result = value1 % value2;
                frame.pushInt(result);
                break;
            }
            case 0x74: // ineg
            {
                final int value = frame.popInt();
                final int result = -value;
                frame.pushInt(result);
                break;
            }
            case 0x84: // iinc
            {
                final int index = code[pc++] & 0xff;
                final byte const_ = code[pc++];
                final int value = frame.getInt(index);
                frame.storeInt(index, value + const_);
                break;
            }
            /*
             * CONVERSIONS
             */
            /*
             * COMPARISONS
             */
            case 0x99: // ifeq
            case 0x9a: // ifne
            case 0x9b: // iflt
            case 0x9c: // ifge
            case 0x9d: // ifgt
            case 0x9e: // ifle
            {
                final byte branchbyte1 = code[pc++];
                final byte branchbyte2 = code[pc++];
                final int offset = branchbyte1 << 8 | branchbyte2;

                final int value = frame.popInt();

                boolean cond = opcode == 0x99 && value == 0;
                cond |= opcode == 0x9a && value != 0;
                cond |= opcode == 0x9b && value < 0;
                cond |= opcode == 0x9c && value >= 0;
                cond |= opcode == 0x9d && value > 0;
                cond |= opcode == 0x9e && value <= 0;

                if (cond)
                    pc += offset - 3;
                break;
            }
            case 0x9f: // if_icmpeq
            case 0xa0: // if_icmpne
            case 0xa1: // if_icmplt
            case 0xa2: // if_icmpge
            case 0xa3: // if_icmpgt
            case 0xa4: // if_icmple
            {
                final byte branchbyte1 = code[pc++];
                final byte branchbyte2 = code[pc++];
                final int offset = branchbyte1 << 8 | branchbyte2;

                final int value2 = frame.popInt();
                final int value1 = frame.popInt();

                boolean cond = opcode == 0x9f && value1 == value2;
                cond |= opcode == 0xa0 && value1 != value2;
                cond |= opcode == 0xa1 && value1 < value2;
                cond |= opcode == 0xa2 && value1 >= value2;
                cond |= opcode == 0xa3 && value1 > value2;
                cond |= opcode == 0xa4 && value1 <= value2;

                if (cond)
                    pc += offset - 3;
                break;
            }
            /*
             * CONTROL
             */
            case 0xa7: // goto
            {
                final byte branchbyte1 = code[pc++];
                final byte branchbyte2 = code[pc++];
                final int offset = branchbyte1 << 8 | branchbyte2;

                pc += offset - 3;
                break;
            }
            case 0xab: // lookupswitch
            {
                final int instrAddr = pc - 1;
                while (pc % 4 > 0)
                    ++pc;

                int default_ = 0;
                default_ |= code[pc++] << 24;
                default_ |= code[pc++] << 16;
                default_ |= code[pc++] << 8;
                default_ |= code[pc++];

                int npairs = 0;
                npairs |= code[pc++] << 24;
                npairs |= code[pc++] << 16;
                npairs |= code[pc++] << 8;
                npairs |= code[pc++];

                final int key = frame.popInt();
                for (int i = 0; i < npairs; ++i) {
                    int match = 0;
                    match |= code[pc++] << 24;
                    match |= code[pc++] << 16;
                    match |= code[pc++] << 8;
                    match |= code[pc++];

                    int offset = 0;
                    offset |= code[pc++] << 24;
                    offset |= code[pc++] << 16;
                    offset |= code[pc++] << 8;
                    offset |= code[pc++];

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
            /*
             * REFERENCES
             */
            case 0xb2: // getstatic
            {
                final byte indexbyte1 = code[pc++];
                final byte indexbyte2 = code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;

                final FieldInfo field = ConstantInfo.getFieldref(classFile, index);
                getStatic(field.resolve());
                break;
            }
            case 0xb3: // putstatic
            {
                final byte indexbyte1 = code[pc++];
                final byte indexbyte2 = code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;

                final FieldInfo field = ConstantInfo.getFieldref(classFile, index);
                putStatic(field.resolve());
                break;
            }
            case 0xb4: // getfield
            {
                final byte indexbyte1 = code[pc++];
                final byte indexbyte2 = code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;

                final FieldInfo field = ConstantInfo.getFieldref(classFile, index);
                getField(field.resolve());
                break;
            }
            case 0xb5: // putfield
            {
                final byte indexbyte1 = code[pc++];
                final byte indexbyte2 = code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;

                final FieldInfo field = ConstantInfo.getFieldref(classFile, index);
                putField(field.resolve());
                break;
            }
            case 0xb6: // invokevirtual
            {
                final byte indexbyte1 = code[pc++];
                final byte indexbyte2 = code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;

                final MethodInfo method = ConstantInfo.getMethodref(classFile, index);
                invokeVirtual(method.resolve());
                break;
            }
            case 0xb7: // invokespecial
            {
                final byte indexbyte1 = code[pc++];
                final byte indexbyte2 = code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;

                final MethodInfo method = ConstantInfo.getMethodref(classFile, index);
                invokeSpecial(method.resolve());
                break;
            }
            case 0xb8: // invokestatic
            {
                final byte indexbyte1 = code[pc++];
                final byte indexbyte2 = code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;

                final MethodInfo method = ConstantInfo.getMethodref(classFile, index);
                invokeStatic(method.resolve());
                break;
            }
            case 0xb9: // invokeinterface
            {
                final byte indexbyte1 = code[pc++];
                final byte indexbyte2 = code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;
                final int count = code[(pc += 2) - 1] & 0xff;

                invokeInterface(index, count);
                break;
            }
            case 0xbb: // new
            {
                final byte indexbyte1 = code[pc++];
                final byte indexbyte2 = code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;

                final ClassFile resolvedClass = ConstantInfo.getClassFile(classFile, index);
                final int objectref = resolvedClass.newInstance();
                frame.pushReference(objectref);
                break;
            }
            case 0xbc: // newarray
            {
                final byte atype = code[pc++];
                final BaseType type = BaseType.values()[atype - 4];
                final ClassFile resolvedClass = classLoader.loadClass(type.toString());

                final int count = frame.popInt();
                if (count < 0) {
                    // TODO Throw NegativeArraySizeException
                    System.err.println("NegativeArraySizeException");
                    System.exit(0);
                }

                final ArrayClassFile arrayClass = (ArrayClassFile) classLoader.loadClass("[" + resolvedClass.getName());
                final int arrayref = arrayClass.newInstance(count);
                frame.pushReference(arrayref);
                break;
            }
            case 0xbd: // anewarray
            {

                final byte indexbyte1 = code[pc++];
                final byte indexbyte2 = code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;
                final ClassFile resolvedClass = ConstantInfo.getClassFile(classFile, index);

                final int count = frame.popInt();
                if (count < 0) {
                    // TODO Throw NegativeArraySizeException
                    System.err.println("NegativeArraySizeException");
                    System.exit(0);
                }

                final ArrayClassFile arrayClass = (ArrayClassFile) classLoader.loadClass("[" + resolvedClass.getName());
                final int arrayref = arrayClass.newInstance(count);
                frame.pushReference(arrayref);
                break;
            }
            case 0xbe: // arraylength
            {
                final int arrayref = frame.popReference();
                if (arrayref == 0) {
                    // TODO Throw NullPointerException
                    System.err.println("NullPointerException");
                    System.exit(0);
                }

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                final int length = instance.getLength();
                frame.pushInt(length);
                break;
            }
            case 0xc0: // checkcast
            {
                final byte indexbyte1 = code[pc++];
                final byte indexbyte2 = code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;
                final ClassFile resolvedClass = ConstantInfo.getClassFile(classFile, index);

                final int objectref = frame.popReference();
                frame.pushReference(objectref);
                if (objectref == 0)
                    return;
                final ClassFile instanceClass = instancePool.getInstance(objectref).getClassFile();

                if (!instanceClass.isInstance(resolvedClass)) {
                    // TODO Throw ClassCastException
                    System.err.println("ClassCastException");
                    System.exit(0);
                }
                break;
            }
            case 0xc1: // instanceof
            {
                final byte indexbyte1 = code[pc++];
                final byte indexbyte2 = code[pc++];
                final int index = indexbyte1 << 8 | indexbyte2;
                final ClassFile resolvedClass = ConstantInfo.getClassFile(classFile, index);

                final int objectref = frame.popReference();
                if (objectref == 0)
                    frame.pushInt(0);
                final ClassFile instanceClass = instancePool.getInstance(objectref).getClassFile();

                frame.pushInt(instanceClass.isInstance(resolvedClass) ? 1 : 0);
                break;
            }
            /*
             * EXTENDED
             */
            case 0xc7: // ifnonnull
            {
                final byte branchbyte1 = code[pc++];
                final byte branchbyte2 = code[pc++];
                final int offset = branchbyte1 << 8 | branchbyte2;

                final int value = frame.popReference();
                if (value == 0)
                    pc += offset - 3;
                break;
            }
            /*
             * RESERVED
             */
            default:
                System.err.println("Unrecognized opcode 0x" + Integer.toHexString(opcode));
                System.exit(0);
                break;
        }
    }

    private void invokeSpecial(final MethodInfo method) {
        final int paramsSize = MethodInfo.getParametersSize(method);

        final int objectref = frame.peekInt(paramsSize + 1);
        final InstanceData instance = VirtualMachine.getInstancePool().getInstance(objectref);
        if (instance == null) {
            // TODO Throw NullPointerException
            System.err.println("NullPointerException");
            System.exit(0);
        }

        // TODO Throw IllegalAccessError, AbstractMethodError,
        // UnsatisfiedLinkError, IncompatibleClassChangeError if needed

        final CodeAttribute newCode = MethodInfo.getCode(method);
        final StackFrame newFrame = pushFrame(newCode.getMaxStack(), newCode.getMaxLocals());
        for (int i = paramsSize; i >= 0; --i)
            newFrame.storeInt(i, frame.popInt());

        contextSwitchUp(newFrame, newCode);
    }

    private void invokeVirtual(MethodInfo method) {
        final int paramsSize = MethodInfo.getParametersSize(method);

        final int objectref = frame.peekInt(paramsSize + 1);
        final InstanceData instance = VirtualMachine.getInstancePool().getInstance(objectref);
        if (instance == null) {
            // TODO Throw NullPointerException
            System.err.println("NullPointerException");
            System.exit(0);
        }

        int offset = -1;
        for (int i = 0; i < method.getClassFile().getMethods().length; ++i)
            if (method.getClassFile().getMethods()[i] == method) {
                offset = i;
                break;
            }
        method = instance.getClassFile().getMethods()[offset];

        // TODO Throw IllegalAccessError, AbstractMethodError,
        // UnsatisfiedLinkError, IncompatibleClassChangeError if needed

        final CodeAttribute newCode = MethodInfo.getCode(method);
        final StackFrame newFrame = pushFrame(newCode.getMaxStack(), newCode.getMaxLocals());
        for (int i = paramsSize; i >= 0; --i)
            newFrame.storeInt(i, frame.popInt());

        contextSwitchUp(newFrame, newCode);
    }

    private void invokeStatic(final MethodInfo method) {
        final int paramsSize = MethodInfo.getParametersSize(method);

        final CodeAttribute newCode = MethodInfo.getCode(method);
        final StackFrame newFrame = pushFrame(newCode.getMaxStack(), newCode.getMaxLocals());
        for (int i = paramsSize - 1; i >= 0; --i)
            newFrame.storeInt(i, frame.popInt());

        contextSwitchUp(newFrame, newCode);
    }

    private void invokeInterface(final int index, final int paramsSize) {
        final int objectref = frame.peekInt(paramsSize + 1);
        final InstanceData instance = VirtualMachine.getInstancePool().getInstance(objectref);
        if (instance == null) {
            // TODO Throw NullPointerException
            System.err.println("NullPointerException");
            System.exit(0);
        }

        // TODO Throw IllegalAccessError, AbstractMethodError,
        // UnsatisfiedLinkError, IncompatibleClassChangeError if needed

        final MethodInfo method = ConstantInfo.getInterfaceMethodref(classFile, index, instance);

        final CodeAttribute newCode = MethodInfo.getCode(method);
        final StackFrame newFrame = pushFrame(newCode.getMaxStack(), newCode.getMaxLocals());
        for (int i = paramsSize; i >= 0; --i)
            newFrame.storeInt(i, frame.popInt());

        contextSwitchUp(newFrame, newCode);
    }

    private void getStatic(final FieldInfo field) {
        final ClassData instance = field.getClassFile().getInstance();
        final byte[] value = new byte[FieldInfo.getParameterSize(field) * 4];
        instance.get(field, value);

        final ByteBuffer buf = ByteBuffer.wrap(value);
        for (int i = 0; i < FieldInfo.getParameterSize(field); ++i)
            frame.pushInt(buf.getInt());
    }

    private void putStatic(final FieldInfo field) {
        final ByteBuffer buf = ByteBuffer.allocate(FieldInfo.getParameterSize(field) * 4);
        for (int i = 0; i < FieldInfo.getParameterSize(field); ++i)
            buf.putInt(frame.popInt());
        final byte[] value = buf.array();

        final ClassData instance = field.getClassFile().getInstance();
        instance.put(field, value);
    }

    private void getField(final FieldInfo field) {
        final int objectref = frame.popReference();
        final InstanceData instance = VirtualMachine.getInstancePool().getInstance(objectref);
        if (instance == null) {
            // TODO Throw NullPointerException
            System.err.println("NullPointerException");
            System.exit(0);
        }

        final byte[] value = new byte[FieldInfo.getParameterSize(field) * 4];
        instance.get(field, value);

        final ByteBuffer buf = ByteBuffer.wrap(value);
        for (int i = 0; i < FieldInfo.getParameterSize(field); ++i)
            frame.pushInt(buf.getInt());
    }

    private void putField(final FieldInfo field) {
        final ByteBuffer buf = ByteBuffer.allocate(FieldInfo.getParameterSize(field) * 4);
        for (int i = 0; i < FieldInfo.getParameterSize(field); ++i)
            buf.putInt(frame.popInt());
        final byte[] value = buf.array();

        final int objectref = frame.popReference();
        final InstanceData instance = VirtualMachine.getInstancePool().getInstance(objectref);
        if (instance == null) {
            // TODO Throw NullPointerException
            System.err.println("NullPointerException");
            System.exit(0);
        }

        instance.put(field, value);
    }

    private void returnInt() {
        final int returnValue = frame.popInt();

        popFrame();

        final StackFrame prevFrame = getTopFrame();
        if (prevFrame == null)
            state = ThreadState.TERMINATED;
        else {
            prevFrame.pushInt(returnValue);
            contextSwitchDown(prevFrame);
        }
    }

    private void returnVoid() {
        popFrame();

        final StackFrame prevFrame = getTopFrame();
        if (prevFrame == null)
            state = ThreadState.TERMINATED;
        else
            contextSwitchDown(prevFrame);
    }

    private void contextSwitchUp(final StackFrame newFrame, final CodeAttribute newCodeAttr) {
        frame.pc = pc;
        frame.codeAttr = codeAttr;

        frame = newFrame;
        pc = 0;
        codeAttr = newCodeAttr;
        classFile = newCodeAttr.getClassFile();
        code = newCodeAttr.getCode();
    }

    private void contextSwitchDown(final StackFrame prevFrame) {
        frame = prevFrame;
        pc = prevFrame.pc;
        codeAttr = prevFrame.codeAttr;
        classFile = prevFrame.codeAttr.getClassFile();
        code = prevFrame.codeAttr.getCode();
    }

    private StackFrame getTopFrame() {
        if (stack.isEmpty())
            return null;
        return stack.get(stack.size() - 1);
    }

    private StackFrame pushFrame(final short maxStack, final short maxLocals) {
        final StackFrame frame = new StackFrame(stackSpace, maxStack, maxLocals);
        stack.add(frame);
        return frame;
    }

    private void popFrame() {
        stack.remove(stack.size() - 1);
    }
}
