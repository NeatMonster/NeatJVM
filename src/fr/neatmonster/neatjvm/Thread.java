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
import fr.neatmonster.neatjvm.format.Modifier;
import fr.neatmonster.neatjvm.format.attribute.CodeAttribute;
import fr.neatmonster.neatjvm.format.attribute.CodeAttribute.ExceptionHandler;
import fr.neatmonster.neatjvm.format.constant.ClassConstant;
import fr.neatmonster.neatjvm.format.constant.DoubleConstant;
import fr.neatmonster.neatjvm.format.constant.FloatConstant;
import fr.neatmonster.neatjvm.format.constant.IntegerConstant;
import fr.neatmonster.neatjvm.format.constant.LongConstant;
import fr.neatmonster.neatjvm.format.constant.StringConstant;

public class Thread {
    public enum ThreadState {
        NEW, RUNNABLE, BLOCKED, WAITING, TERMINATED
    }

    public enum ThreadPriority {
        MIN_PRIORITY, NORM_PRIORITY, MAX_PRIORITY
    }

    private final int        id;
    private final MemoryPool stackSpace;

    private ThreadState      state;
    private ThreadPriority   priority;

    private StackFrame       frame;
    private List<StackFrame> stack;

    private int              pc;
    private CodeAttribute    code;
    private InstanceData     exception;

    private byte[]           codeBytes;
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

    public void setState(final ThreadState state) {
        this.state = state;
    }

    public ThreadPriority getPriority() {
        return priority;
    }

    public void setPriority(final ThreadPriority priority) {
        this.priority = priority;
    }

    public void start(final CodeAttribute code) {
        this.code = code;
        classFile = code.getClassFile();
        codeBytes = code.getCode();

        stack = new ArrayList<>();
        frame = pushFrame(code.getMaxStack(), code.getMaxLocals());

        state = ThreadState.RUNNABLE;
    }

    public void tick() {
        VirtualMachine.setCurrentThread(this);

        final MemoryPool heapSpace = VirtualMachine.getHeapSpace();
        final ClassLoader classLoader = VirtualMachine.getClassLoader();
        final InstancePool instancePool = VirtualMachine.getInstancePool();

        if (exception != null) {
            for (final ExceptionHandler handler : code.getExceptions()) {
                if (frame.pc < handler.getStart() || frame.pc >= handler.getEnd())
                    continue;

                final ClassFile catchClass = handler.getCatchType();
                if (catchClass == null || exception.getClassFile().extendsClass(catchClass)) {
                    frame.pushReference(exception.getReference());
                    pc = handler.getHandler();
                    exception = null;
                    return;
                }
            }

            popFrame();
            final StackFrame prevFrame = getTopFrame();
            if (prevFrame == null)
                state = ThreadState.TERMINATED;
            else
                contextSwitchDown(prevFrame);
            return;
        }

        frame.pc = pc;
        final int opcode = codeBytes[pc++] & 0xff;
        execution: switch (opcode) {
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
                final byte value = codeBytes[pc++];
                frame.pushInt(value);
                break;
            }
            case 0x11: // sipush
            {
                final int byte1 = codeBytes[pc++] & 0xff;
                final int byte2 = codeBytes[pc++] & 0xff;
                final short value = (short) (byte1 << 8 | byte2);
                frame.pushInt(value);
                break;
            }
            case 0x12: // ldc
            {
                final int index = codeBytes[pc++] & 0xff;
                final ConstantInfo constant = classFile.getConstants()[index - 1];

                if (constant instanceof IntegerConstant) {
                    final int value = ((IntegerConstant) constant).resolve();
                    frame.pushInt(value);
                }

                else if (constant instanceof FloatConstant) {
                    final float value = ((FloatConstant) constant).resolve();
                    frame.pushFloat(value);
                }

                else if (constant instanceof StringConstant) {
                    final int value = ((StringConstant) constant).resolve();
                    frame.pushReference(value);
                }

                else if (constant instanceof ClassConstant) {
                    final int value = instancePool.getJavaClass(((ClassConstant) constant).resolve()).getReference();
                    frame.pushReference(value);
                }

                else {
                    System.err.println(
                            "Unrecognized constant w/ " + constant.getClass().getSimpleName() + " when loading!");
                    System.exit(0);
                }

                // TODO Add support for MethodTypes and MethodHandles
                break;
            }
            case 0x13: // ldc_w
            {
                final int indexbyte1 = codeBytes[pc++] & 0xff;
                final int indexbyte2 = codeBytes[pc++] & 0xff;
                final int index = indexbyte1 << 8 | indexbyte2;
                final ConstantInfo constant = classFile.getConstants()[index - 1];

                if (constant instanceof IntegerConstant) {
                    final int value = ((IntegerConstant) constant).resolve();
                    frame.pushInt(value);
                }

                else if (constant instanceof FloatConstant) {
                    final float value = ((FloatConstant) constant).resolve();
                    frame.pushFloat(value);
                }

                else if (constant instanceof StringConstant) {
                    final int value = ((StringConstant) constant).resolve();
                    frame.pushReference(value);
                }

                else if (constant instanceof ClassConstant) {
                    final int value = instancePool.getJavaClass(((ClassConstant) constant).resolve()).getReference();
                    frame.pushReference(value);
                }

                else {
                    System.err.println(
                            "Unrecognized constant w/ " + constant.getClass().getSimpleName() + " when loading!");
                    System.exit(0);
                }

                // TODO Add support for MethodTypes and MethodHandles
                break;
            }
            case 0x14: // ldc2_w
            {
                final int indexbyte1 = codeBytes[pc++] & 0xff;
                final int indexbyte2 = codeBytes[pc++] & 0xff;
                final int index = indexbyte1 << 8 | indexbyte2;
                final ConstantInfo constant = classFile.getConstants()[index - 1];

                if (constant instanceof LongConstant) {
                    final long value = ((LongConstant) constant).resolve();
                    frame.pushLong(value);
                }

                else if (constant instanceof DoubleConstant) {
                    final double value = ((DoubleConstant) constant).resolve();
                    frame.pushDouble(value);
                }

                break;
            }
            /*
             * LOADS
             */
            case 0x15: // iload
            {
                final int index = codeBytes[pc++] & 0xff;
                final int value = frame.getInt(index);
                frame.pushInt(value);
                break;
            }
            case 0x16: // lload
            {
                final int index = codeBytes[pc++] & 0xff;
                final long value = frame.getLong(index);
                frame.pushLong(value);
                break;
            }
            case 0x17: // fload
            {
                final int index = codeBytes[pc++] & 0xff;
                final float value = frame.getFloat(index);
                frame.pushFloat(value);
                break;
            }
            case 0x18: // dload
            {
                final int index = codeBytes[pc++] & 0xff;
                final double value = frame.getDouble(index);
                frame.pushDouble(value);
                break;
            }
            case 0x19: // aload
            {
                final int index = codeBytes[pc++] & 0xff;
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
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                if (index < 0 || index >= instance.getLength()) {
                    throwException(classLoader.loadClass("java/lang/ArrayIndexOutOfBoundsException"));
                    break;
                }

                final int value = heapSpace.getInt(instance.dataStart + index * 4);
                frame.pushInt(value);
                break;
            }
            case 0x2f: // laload
            {
                final int index = frame.popInt();

                final int arrayref = frame.popReference();
                if (arrayref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                if (index < 0 || index >= instance.getLength()) {
                    throwException(classLoader.loadClass("java/lang/ArrayIndexOutOfBoundsException"));
                    break;
                }

                final long value = heapSpace.getLong(instance.dataStart + index * 8);
                frame.pushLong(value);
                break;
            }
            case 0x30: // faload
            {
                final int index = frame.popInt();

                final int arrayref = frame.popReference();
                if (arrayref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                if (index < 0 || index >= instance.getLength()) {
                    throwException(classLoader.loadClass("java/lang/ArrayIndexOutOfBoundsException"));
                    break;
                }

                final float value = heapSpace.getFloat(instance.dataStart + index * 4);
                frame.pushFloat(value);
                break;
            }
            case 0x31: // daload
            {
                final int index = frame.popInt();

                final int arrayref = frame.popReference();
                if (arrayref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                if (index < 0 || index >= instance.getLength()) {
                    throwException(classLoader.loadClass("java/lang/ArrayIndexOutOfBoundsException"));
                    break;
                }

                final double value = heapSpace.getDouble(instance.dataStart + index * 8);
                frame.pushDouble(value);
                break;
            }
            case 0x32: // aaload
            {
                final int index = frame.popInt();

                final int arrayref = frame.popReference();
                if (arrayref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                if (index < 0 || index >= instance.getLength()) {
                    throwException(classLoader.loadClass("java/lang/ArrayIndexOutOfBoundsException"));
                    break;
                }

                final int value = heapSpace.getReference(instance.dataStart + index * 4);
                frame.pushReference(value);
                break;
            }
            case 0x33: // baload
            {
                final int index = frame.popInt();

                final int arrayref = frame.popReference();
                if (arrayref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                if (index < 0 || index >= instance.getLength()) {
                    throwException(classLoader.loadClass("java/lang/ArrayIndexOutOfBoundsException"));
                    break;
                }

                final byte value = heapSpace.get(instance.dataStart + index);
                frame.pushInt(value);
                break;
            }
            case 0x34: // caload
            {
                final int index = frame.popInt();

                final int arrayref = frame.popReference();
                if (arrayref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                if (index < 0 || index >= instance.getLength()) {
                    throwException(classLoader.loadClass("java/lang/ArrayIndexOutOfBoundsException"));
                    break;
                }

                final char value = heapSpace.getChar(instance.dataStart + index * 2);
                frame.pushInt(value);
                break;
            }
            case 0x35: // saload
            {
                final int index = frame.popInt();

                final int arrayref = frame.popReference();
                if (arrayref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                if (index < 0 || index >= instance.getLength()) {
                    throwException(classLoader.loadClass("java/lang/ArrayIndexOutOfBoundsException"));
                    break;
                }

                final short value = heapSpace.getShort(instance.dataStart + index * 2);
                frame.pushInt(value);
                break;
            }
            /*
             * STORES
             */
            case 0x36: // istore
            {
                final int index = codeBytes[pc++] & 0xff;
                final int value = frame.popInt();
                frame.storeInt(index, value);
                break;
            }
            case 0x37: // lstore
            {
                final int index = codeBytes[pc++] & 0xff;
                final long value = frame.popLong();
                frame.storeLong(index, value);
                break;
            }
            case 0x38: // fstore
            {
                final int index = codeBytes[pc++] & 0xff;
                final float value = frame.popFloat();
                frame.storeFloat(index, value);
                break;
            }
            case 0x39: // dstore
            {
                final int index = codeBytes[pc++] & 0xff;
                final double value = frame.popDouble();
                frame.storeDouble(index, value);
                break;
            }
            case 0x3a: // astore
            {
                final int index = codeBytes[pc++] & 0xff;
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
            case 0x43: // fstore_0
            case 0x44: // fstore_1
            case 0x45: // fstore_2
            case 0x46: // fstore_3
            {
                final int n = opcode - 0x3f;
                final float value = frame.popFloat();
                frame.storeFloat(n, value);
                break;
            }
            case 0x47: // dstore_0
            case 0x48: // dstore_1
            case 0x49: // dstore_2
            case 0x4a: // dstore_3
            {
                final int n = opcode - 0x3f;
                final double value = frame.popDouble();
                frame.storeDouble(n, value);
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
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                if (index < 0 || index >= instance.getLength()) {
                    throwException(classLoader.loadClass("java/lang/ArrayIndexOutOfBoundsException"));
                    break;
                }

                heapSpace.putInt(instance.dataStart + index * 4, value);
                break;
            }
            case 0x50: // lastore
            {
                final long value = frame.popLong();
                final int index = frame.popInt();

                final int arrayref = frame.popReference();
                if (arrayref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                if (index < 0 || index >= instance.getLength()) {
                    throwException(classLoader.loadClass("java/lang/ArrayIndexOutOfBoundsException"));
                    break;
                }

                heapSpace.putLong(instance.dataStart + index * 8, value);
                break;
            }
            case 0x51: // fastore
            {
                final float value = frame.popFloat();
                final int index = frame.popInt();

                final int arrayref = frame.popReference();
                if (arrayref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                if (index < 0 || index >= instance.getLength()) {
                    throwException(classLoader.loadClass("java/lang/ArrayIndexOutOfBoundsException"));
                    break;
                }

                heapSpace.putFloat(instance.dataStart + index * 4, value);
                break;
            }
            case 0x52: // dastore
            {
                final double value = frame.popDouble();
                final int index = frame.popInt();

                final int arrayref = frame.popReference();
                if (arrayref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                if (index < 0 || index >= instance.getLength()) {
                    throwException(classLoader.loadClass("java/lang/ArrayIndexOutOfBoundsException"));
                    break;
                }

                heapSpace.putDouble(instance.dataStart + index * 8, value);
                break;
            }
            case 0x53: // aastore
            {
                final int value = frame.popReference();
                final int index = frame.popInt();

                final int arrayref = frame.popReference();
                if (arrayref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                if (index < 0 || index >= instance.getLength()) {
                    throwException(classLoader.loadClass("java/lang/ArrayIndexOutOfBoundsException"));
                    break;
                }

                heapSpace.putReference(instance.dataStart + index * 4, value);
                break;
            }
            case 0x54: // bastore
            {
                final byte value = (byte) frame.popInt();
                final int index = frame.popInt();

                final int arrayref = frame.popReference();
                if (arrayref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                if (index < 0 || index >= instance.getLength()) {
                    throwException(classLoader.loadClass("java/lang/ArrayIndexOutOfBoundsException"));
                    break;
                }

                heapSpace.put(instance.dataStart + index, value);
                break;
            }
            case 0x55: // castore
            {
                final char value = (char) frame.popInt();
                final int index = frame.popInt();

                final int arrayref = frame.popReference();
                if (arrayref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                if (index < 0 || index >= instance.getLength()) {
                    throwException(classLoader.loadClass("java/lang/ArrayIndexOutOfBoundsException"));
                    break;
                }

                heapSpace.putChar(instance.dataStart + index * 2, value);
                break;
            }
            case 0x56: // sastore
            {
                final short value = (short) frame.popInt();
                final int index = frame.popInt();

                final int arrayref = frame.popReference();
                if (arrayref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                if (index < 0 || index >= instance.getLength()) {
                    throwException(classLoader.loadClass("java/lang/ArrayIndexOutOfBoundsException"));
                    break;
                }

                heapSpace.putShort(instance.dataStart + index * 2, value);
                break;
            }
            /*
             * STACK
             */
            case 0x57: // pop
                frame.popInt();
                break;
            case 0x58: // pop2
                frame.popInt();
                frame.popInt();
                break;
            case 0x59: // dup
            {
                final int value = frame.popInt();
                frame.pushInt(value);
                frame.pushInt(value);
                break;
            }
            case 0x5a: // dup_x1
            {
                final int value1 = frame.popInt();
                final int value2 = frame.popInt();
                frame.pushInt(value1);
                frame.pushInt(value2);
                frame.pushInt(value1);
                break;
            }
            case 0x5b: // dup_x2
            {
                final int value1 = frame.popInt();
                final long value2 = frame.popLong();
                frame.pushInt(value1);
                frame.pushLong(value2);
                frame.pushInt(value1);
                break;
            }
            case 0x5c: // dup2
            {
                final long value = frame.popLong();
                frame.pushLong(value);
                frame.pushLong(value);
                break;
            }
            case 0x5d: // dup2_x1
            {

                final long value1 = frame.popLong();
                final int value2 = frame.popInt();
                frame.pushLong(value1);
                frame.pushInt(value2);
                frame.pushLong(value1);
                break;
            }
            case 0x5e: // dup2_x2
            {
                final long value1 = frame.popLong();
                final long value2 = frame.popLong();
                frame.pushLong(value1);
                frame.pushLong(value2);
                frame.pushLong(value1);
                break;
            }
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
            case 0x61: // ladd
            {
                final long value2 = frame.popLong();
                final long value1 = frame.popLong();
                final long result = value1 + value2;
                frame.pushLong(result);
                break;
            }
            case 0x62: // fadd
            {
                final float value2 = frame.popFloat();
                final float value1 = frame.popFloat();
                final float result = value1 + value2;
                frame.pushFloat(result);
                break;
            }
            case 0x63: // dadd
            {
                final double value2 = frame.popDouble();
                final double value1 = frame.popDouble();
                final double result = value1 + value2;
                frame.pushDouble(result);
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
            case 0x65: // lsub
            {
                final long value2 = frame.popLong();
                final long value1 = frame.popLong();
                final long result = value1 - value2;
                frame.pushLong(result);
                break;
            }
            case 0x66: // fsub
            {
                final float value2 = frame.popFloat();
                final float value1 = frame.popFloat();
                final float result = value1 - value2;
                frame.pushFloat(result);
                break;
            }
            case 0x67: // dsub
            {
                final double value2 = frame.popDouble();
                final double value1 = frame.popDouble();
                final double result = value1 - value2;
                frame.pushDouble(result);
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
            case 0x69: // lmul
            {
                final long value2 = frame.popLong();
                final long value1 = frame.popLong();
                final long result = value1 * value2;
                frame.pushLong(result);
                break;
            }
            case 0x6a: // fmul
            {
                final float value2 = frame.popFloat();
                final float value1 = frame.popFloat();
                final float result = value1 * value2;
                frame.pushFloat(result);
                break;
            }
            case 0x6b: // dmul
            {
                final double value2 = frame.popDouble();
                final double value1 = frame.popDouble();
                final double result = value1 * value2;
                frame.pushDouble(result);
                break;
            }
            case 0x6c: // idiv
            {
                final int value2 = frame.popInt();
                final int value1 = frame.popInt();
                if (value2 == 0) {
                    throwException(classLoader.loadClass("java/lang/ArithmeticException"));
                    break;
                }
                final int result = value1 / value2;
                frame.pushInt(result);
                break;
            }
            case 0x6d: // ldiv
            {
                final long value2 = frame.popLong();
                final long value1 = frame.popLong();
                if (value2 == 0L) {
                    throwException(classLoader.loadClass("java/lang/ArithmeticException"));
                    break;
                }
                final long result = value1 / value2;
                frame.pushLong(result);
                break;
            }
            case 0x6e: // fdiv
            {
                final float value2 = frame.popFloat();
                final float value1 = frame.popFloat();
                if (value2 == 0f) {
                    throwException(classLoader.loadClass("java/lang/ArithmeticException"));
                    break;
                }
                final float result = value1 / value2;
                frame.pushFloat(result);
                break;
            }
            case 0x6f: // ddiv
            {
                final double value2 = frame.popDouble();
                final double value1 = frame.popDouble();
                if (value2 == 0.0) {
                    throwException(classLoader.loadClass("java/lang/ArithmeticException"));
                    break;
                }
                final double result = value1 / value2;
                frame.pushDouble(result);
                break;
            }
            case 0x70: // irem
            {
                final int value2 = frame.popInt();
                final int value1 = frame.popInt();
                if (value2 == 0) {
                    throwException(classLoader.loadClass("java/lang/ArithmeticException"));
                    break;
                }
                final int result = value1 % value2;
                frame.pushInt(result);
                break;
            }
            case 0x71: // lrem
            {
                final long value2 = frame.popLong();
                final long value1 = frame.popLong();
                if (value2 == 0L) {
                    throwException(classLoader.loadClass("java/lang/ArithmeticException"));
                    break;
                }
                final long result = value1 % value2;
                frame.pushLong(result);
                break;
            }
            case 0x72: // frem
            {
                final float value2 = frame.popFloat();
                final float value1 = frame.popFloat();
                if (value2 == 0f) {
                    throwException(classLoader.loadClass("java/lang/ArithmeticException"));
                    break;
                }
                final float result = value1 % value2;
                frame.pushFloat(result);
                break;
            }
            case 0x73: // drem
            {
                final double value2 = frame.popDouble();
                final double value1 = frame.popDouble();
                if (value2 == 0.0) {
                    throwException(classLoader.loadClass("java/lang/ArithmeticException"));
                    break;
                }
                final double result = value1 % value2;
                frame.pushDouble(result);
                break;
            }
            case 0x74: // ineg
            {
                final int value = frame.popInt();
                final int result = -value;
                frame.pushInt(result);
                break;
            }
            case 0x75: // lneg
            {
                final long value = frame.popLong();
                final long result = -value;
                frame.pushLong(result);
                break;
            }
            case 0x76: // fneg
            {
                final float value = frame.popFloat();
                final float result = -value;
                frame.pushFloat(result);
                break;
            }
            case 0x77: // dneg
            {
                final double value = frame.popDouble();
                final double result = -value;
                frame.pushDouble(result);
                break;
            }
            case 0x78: // ishl
            {
                final int value2 = frame.popInt();
                final int value1 = frame.popInt();
                final int s = value2 & 0x1f;
                final int result = value1 << s;
                frame.pushInt(result);
                break;
            }
            case 0x79: // lshl
            {
                final int value2 = frame.popInt();
                final long value1 = frame.popLong();
                final int s = value2 & 0x1f;
                final long result = value1 << s;
                frame.pushLong(result);
                break;
            }
            case 0x7a: // ishr
            {
                final int value2 = frame.popInt();
                final int value1 = frame.popInt();
                final int s = value2 & 0x1f;
                final int result = value1 >> s;
                frame.pushInt(result);
                break;
            }
            case 0x7b: // lshr
            {
                final int value2 = frame.popInt();
                final long value1 = frame.popLong();
                final int s = value2 & 0x1f;
                final long result = value1 >> s;
                frame.pushLong(result);
                break;
            }
            case 0x7c: // iushr
            {
                final int value2 = frame.popInt();
                final int value1 = frame.popInt();
                final int s = value2 & 0x1f;
                final int result = value1 >>> s;
                frame.pushInt(result);
                break;
            }
            case 0x7d: // lushr
            {
                final int value2 = frame.popInt();
                final long value1 = frame.popLong();
                final int s = value2 & 0x1f;
                final long result = value1 >>> s;
                frame.pushLong(result);
                break;
            }
            case 0x7e: // iand
            {
                final int value2 = frame.popInt();
                final int value1 = frame.popInt();
                final int result = value1 & value2;
                frame.pushInt(result);
                break;
            }
            case 0x7f: // land
            {
                final long value2 = frame.popLong();
                final long value1 = frame.popLong();
                final long result = value1 & value2;
                frame.pushLong(result);
                break;
            }
            case 0x80: // ior
            {
                final int value2 = frame.popInt();
                final int value1 = frame.popInt();
                final int result = value1 | value2;
                frame.pushInt(result);
                break;
            }
            case 0x81: // lor
            {
                final long value2 = frame.popLong();
                final long value1 = frame.popLong();
                final long result = value1 | value2;
                frame.pushLong(result);
                break;
            }
            case 0x82: // ixor
            {
                final int value2 = frame.popInt();
                final int value1 = frame.popInt();
                final int result = value1 ^ value2;
                frame.pushInt(result);
                break;
            }
            case 0x83: // lxor
            {
                final long value2 = frame.popLong();
                final long value1 = frame.popLong();
                final long result = value1 ^ value2;
                frame.pushLong(result);
                break;
            }
            case 0x84: // iinc
            {
                final int index = codeBytes[pc++] & 0xff;
                final byte const_ = codeBytes[pc++];
                frame.storeInt(index, frame.getInt(index) + const_);
                break;
            }
            /*
             * CONVERSIONS
             */
            case 0x85: // i2l
            {
                final int value = frame.popInt();
                final long result = value;
                frame.pushLong(result);
                break;
            }
            case 0x86: // i2f
            {
                final int value = frame.popInt();
                final float result = value;
                frame.pushFloat(result);
                break;
            }
            case 0x87: // i2d
            {
                final int value = frame.popInt();
                final double result = value;
                frame.pushDouble(result);
                break;
            }
            case 0x88: // l2i
            {
                final long value = frame.popLong();
                final int result = (int) value;
                frame.pushInt(result);
                break;
            }
            case 0x89: // l2f
            {
                final long value = frame.popLong();
                final float result = value;
                frame.pushFloat(result);
                break;
            }
            case 0x8a: // l2d
            {
                final long value = frame.popLong();
                final double result = value;
                frame.pushDouble(result);
                break;
            }
            case 0x8b: // f2i
            {
                final float value = frame.popFloat();
                final int result = (int) value;
                frame.pushInt(result);
                break;
            }
            case 0x8c: // f2l
            {
                final float value = frame.popFloat();
                final long result = (long) value;
                frame.pushLong(result);
                break;
            }
            case 0x8d: // f2d
            {
                final float value = frame.popFloat();
                final double result = value;
                frame.pushDouble(result);
                break;
            }
            case 0x8e: // d2i
            {
                final double value = frame.popDouble();
                final int result = (int) value;
                frame.pushInt(result);
                break;
            }
            case 0x8f: // d2l
            {
                final double value = frame.popDouble();
                final long result = (long) value;
                frame.pushLong(result);
                break;
            }
            case 0x90: // d2f
            {
                final double value = frame.popDouble();
                final float result = (float) value;
                frame.pushFloat(result);
                break;
            }
            case 0x91: // i2b
            {
                final int value = frame.popInt();
                final byte result = (byte) value;
                frame.pushInt(result);
                break;
            }
            case 0x92: // i2c
            {
                final int value = frame.popInt();
                final char result = (char) value;
                frame.pushInt(result);
                break;
            }
            case 0x93: // i2s
            {
                final int value = frame.popInt();
                final short result = (short) value;
                frame.pushInt(result);
                break;
            }
            /*
             * COMPARISONS
             */
            case 0x94: // lcmp
            {
                final long value2 = frame.popLong();
                final long value1 = frame.popLong();
                if (value1 > value2)
                    frame.pushInt(1);
                else if (value1 == value2)
                    frame.pushInt(0);
                else if (value1 < value2)
                    frame.pushInt(-1);
                break;
            }
            case 0x95: // fcmpl
            case 0x96: // fcmpg
            {
                final float value2 = frame.popFloat();
                final float value1 = frame.popFloat();
                if (value1 > value2)
                    frame.pushInt(1);
                else if (value1 == value2)
                    frame.pushInt(0);
                else if (value1 < value2)
                    frame.pushInt(-1);
                else
                    frame.pushInt(opcode == 0x96 ? 1 : -1);
                break;
            }
            case 0x97: // dcmpl
            case 0x98: // dcmpg
            {
                final double value2 = frame.popDouble();
                final double value1 = frame.popDouble();
                if (value1 > value2)
                    frame.pushInt(1);
                else if (value1 == value2)
                    frame.pushInt(0);
                else if (value1 < value2)
                    frame.pushInt(-1);
                else
                    frame.pushInt(opcode == 0x96 ? 1 : -1);
                break;
            }
            case 0x99: // ifeq
            case 0x9a: // ifne
            case 0x9b: // iflt
            case 0x9c: // ifge
            case 0x9d: // ifgt
            case 0x9e: // ifle
            {
                final byte branchbyte1 = codeBytes[pc++];
                final byte branchbyte2 = codeBytes[pc++];
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
                final byte branchbyte1 = codeBytes[pc++];
                final byte branchbyte2 = codeBytes[pc++];
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
            case 0xa5: // if_acmpeq
            case 0xa6: // if_acmpne
            {
                final byte branchbyte1 = codeBytes[pc++];
                final byte branchbyte2 = codeBytes[pc++];
                final int offset = branchbyte1 << 8 | branchbyte2;

                final int value2 = frame.popReference();
                final int value1 = frame.popReference();

                boolean cond = opcode == 0xa5 && value1 == value2;
                cond |= opcode == 0xa6 && value1 != value2;

                if (cond)
                    pc += offset - 3;
                break;
            }
            /*
             * CONTROL
             */
            case 0xa7: // goto
            {
                final byte branchbyte1 = codeBytes[pc++];
                final byte branchbyte2 = codeBytes[pc++];
                final int offset = branchbyte1 << 8 | branchbyte2;

                pc += offset - 3;
                break;
            }
            case 0xa8: // jsr
            {
                final byte branchbyte1 = codeBytes[pc++];
                final byte branchbyte2 = codeBytes[pc++];
                final int offset = branchbyte1 << 8 | branchbyte2;

                frame.pushInt(pc);
                pc += offset - 3;
                break;
            }
            case 0xa9: // ret
            {
                final int index = codeBytes[pc++] & 0xff;
                pc = frame.getInt(index);
                break;
            }
            case 0xaa: // tableswitch
            {
                final int instrAddr = pc - 1;
                while (pc % 4 > 0)
                    ++pc;

                int default_ = 0;
                default_ |= codeBytes[pc++] << 24;
                default_ |= codeBytes[pc++] << 16;
                default_ |= codeBytes[pc++] << 8;
                default_ |= codeBytes[pc++];

                int low = 0;
                low |= codeBytes[pc++] << 24;
                low |= codeBytes[pc++] << 16;
                low |= codeBytes[pc++] << 8;
                low |= codeBytes[pc++];

                int high = 0;
                high |= codeBytes[pc++] << 24;
                high |= codeBytes[pc++] << 16;
                high |= codeBytes[pc++] << 8;
                high |= codeBytes[pc++];

                final int tableAddr = pc;

                int index = frame.popInt();
                if (index < low || index > high)
                    pc = instrAddr + default_;
                else {
                    index = tableAddr + (index - low) * 4;

                    int offset = 0;
                    offset |= codeBytes[index] << 24;
                    offset |= codeBytes[index + 1] << 16;
                    offset |= codeBytes[index + 2] << 8;
                    offset |= codeBytes[index + 3];

                    pc = instrAddr + offset;
                }

                break;
            }
            case 0xab: // lookupswitch
            {
                final int instrAddr = pc - 1;
                while (pc % 4 > 0)
                    ++pc;

                int default_ = 0;
                default_ |= codeBytes[pc++] << 24;
                default_ |= codeBytes[pc++] << 16;
                default_ |= codeBytes[pc++] << 8;
                default_ |= codeBytes[pc++];

                int npairs = 0;
                npairs |= codeBytes[pc++] << 24;
                npairs |= codeBytes[pc++] << 16;
                npairs |= codeBytes[pc++] << 8;
                npairs |= codeBytes[pc++];

                final int key = frame.popInt();
                for (int i = 0; i < npairs; ++i) {
                    int match = 0;
                    match |= codeBytes[pc++] << 24;
                    match |= codeBytes[pc++] << 16;
                    match |= codeBytes[pc++] << 8;
                    match |= codeBytes[pc++];

                    int offset = 0;
                    offset |= codeBytes[pc++] << 24;
                    offset |= codeBytes[pc++] << 16;
                    offset |= codeBytes[pc++] << 8;
                    offset |= codeBytes[pc++];

                    if (key == match) {
                        pc = instrAddr + offset;
                        break execution;
                    }
                }

                pc = instrAddr + default_;
                break;
            }
            case 0xac: // ireturn
            {
                releaseMonitor();

                final int value = frame.popInt();

                popFrame();

                final StackFrame prevFrame = getTopFrame();
                if (prevFrame == null)
                    state = ThreadState.TERMINATED;
                else {
                    prevFrame.pushInt(value);
                    contextSwitchDown(prevFrame);
                }
                break;
            }
            case 0xad: // lreturn
            {
                releaseMonitor();

                final long value = frame.popLong();

                popFrame();

                final StackFrame prevFrame = getTopFrame();
                if (prevFrame == null)
                    state = ThreadState.TERMINATED;
                else {
                    prevFrame.pushLong(value);
                    contextSwitchDown(prevFrame);
                }
                break;
            }
            case 0xae: // freturn
            {
                releaseMonitor();

                final float value = frame.popFloat();

                popFrame();

                final StackFrame prevFrame = getTopFrame();
                if (prevFrame == null)
                    state = ThreadState.TERMINATED;
                else {
                    prevFrame.pushFloat(value);
                    contextSwitchDown(prevFrame);
                }
                break;
            }
            case 0xaf: // dreturn
            {
                releaseMonitor();

                final double value = frame.popDouble();

                popFrame();

                final StackFrame prevFrame = getTopFrame();
                if (prevFrame == null)
                    state = ThreadState.TERMINATED;
                else {
                    prevFrame.pushDouble(value);
                    contextSwitchDown(prevFrame);
                }
                break;
            }
            case 0xb0: // areturn
            {
                releaseMonitor();

                final int value = frame.popReference();

                popFrame();

                final StackFrame prevFrame = getTopFrame();
                if (prevFrame == null)
                    state = ThreadState.TERMINATED;
                else {
                    prevFrame.pushReference(value);
                    contextSwitchDown(prevFrame);
                }
                break;
            }
            case 0xb1: // return
            {
                releaseMonitor();

                popFrame();

                final StackFrame prevFrame = getTopFrame();
                if (prevFrame == null)
                    state = ThreadState.TERMINATED;
                else
                    contextSwitchDown(prevFrame);
                break;
            }
            /*
             * REFERENCES
             */
            case 0xb2: // getstatic
            {
                final int indexbyte1 = codeBytes[pc++] & 0xff;
                final int indexbyte2 = codeBytes[pc++] & 0xff;
                final int index = indexbyte1 << 8 | indexbyte2;
                final FieldInfo field = ConstantInfo.getFieldref(classFile, index);

                final ClassData instance = field.getClassFile().getInstance();
                final byte[] value = new byte[FieldInfo.getParameterSize(field) * 4];
                instance.get(field, value);

                final ByteBuffer buf = ByteBuffer.wrap(value);
                for (int i = 0; i < FieldInfo.getParameterSize(field); ++i)
                    frame.pushInt(buf.getInt());
                break;
            }
            case 0xb3: // putstatic
            {
                final int indexbyte1 = codeBytes[pc++] & 0xff;
                final int indexbyte2 = codeBytes[pc++] & 0xff;
                final int index = indexbyte1 << 8 | indexbyte2;
                final FieldInfo field = ConstantInfo.getFieldref(classFile, index);

                final ByteBuffer buf = ByteBuffer.allocate(FieldInfo.getParameterSize(field) * 4);
                for (int i = 0; i < FieldInfo.getParameterSize(field); ++i)
                    buf.putInt(frame.popInt());
                final byte[] value = buf.array();

                final ClassData instance = field.getClassFile().getInstance();
                instance.put(field, value);
                break;
            }
            case 0xb4: // getfield
            {
                final int indexbyte1 = codeBytes[pc++] & 0xff;
                final int indexbyte2 = codeBytes[pc++] & 0xff;
                final int index = indexbyte1 << 8 | indexbyte2;
                final FieldInfo field = ConstantInfo.getFieldref(classFile, index);

                final int objectref = frame.popReference();
                if (objectref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }
                final InstanceData instance = VirtualMachine.getInstancePool().getInstance(objectref);

                final byte[] value = new byte[FieldInfo.getParameterSize(field) * 4];
                instance.get(field, value);

                final ByteBuffer buf = ByteBuffer.wrap(value);
                for (int i = 0; i < FieldInfo.getParameterSize(field); ++i)
                    frame.pushInt(buf.getInt());
                break;
            }
            case 0xb5: // putfield
            {
                final int indexbyte1 = codeBytes[pc++] & 0xff;
                final int indexbyte2 = codeBytes[pc++] & 0xff;
                final int index = indexbyte1 << 8 | indexbyte2;
                final FieldInfo field = ConstantInfo.getFieldref(classFile, index);

                final ByteBuffer buf = ByteBuffer.allocate(FieldInfo.getParameterSize(field) * 4);
                for (int i = 0; i < FieldInfo.getParameterSize(field); ++i)
                    buf.putInt(frame.popInt());
                final byte[] value = buf.array();

                final int objectref = frame.popReference();
                if (objectref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }
                final InstanceData instance = VirtualMachine.getInstancePool().getInstance(objectref);

                instance.put(field, value);
                break;
            }
            case 0xb6: // invokevirtual
            {
                final int indexbyte1 = codeBytes[pc++] & 0xff;
                final int indexbyte2 = codeBytes[pc++] & 0xff;
                final int index = indexbyte1 << 8 | indexbyte2;
                MethodInfo method = ConstantInfo.getMethodref(classFile, index);
                final int paramsSize = MethodInfo.getParametersSize(method);

                final int objectref = frame.peekInt(paramsSize + 1);
                if (objectref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }
                final InstanceData instance = VirtualMachine.getInstancePool().getInstance(objectref);

                method = instance.getClassFile().getMethod(method.getName(), MethodInfo.getDescriptor(method));

                // TODO Throw IllegalAccessError, AbstractMethodError,
                // UnsatisfiedLinkError, IncompatibleClassChangeError if needed

                if (Modifier.SYNCHRONIZED.eval(method.getModifiers())) {
                    frame.monitor = instance.getMonitor();
                    frame.monitor.acquire(this);
                } else
                    frame.monitor = null;

                if (Modifier.NATIVE.eval(method.getModifiers())) {
                    final ClassFile methodClass = method.getClassFile();

                    if (methodClass.getName().equals("java/lang/Object"))
                        if (method.getName().equals("getClass")) {
                            frame.pushReference(instancePool.getJavaClass(instance.getClassFile()).getReference());
                            break;
                        }

                    if (methodClass.getName().equals("java/lang/Class"))
                        if (method.getName().equals("getComponentType")) {
                            final ClassFile objectClass = instancePool.getClassFile(instance);
                            final ClassFile arrayClass = ((ArrayClassFile) objectClass).getArrayClass();
                            frame.pushReference(instancePool.getJavaClass(arrayClass).getReference());
                            break;
                        }

                    System.err.println("Native virtual method " + method.getName() + " of class "
                            + method.getClassFile().getName() + " is not implemented!");
                    System.exit(0);
                }

                final CodeAttribute newCode = MethodInfo.getCode(method);
                final StackFrame newFrame = pushFrame(newCode.getMaxStack(), newCode.getMaxLocals());
                for (int i = paramsSize; i >= 0; --i)
                    newFrame.storeInt(i, frame.popInt());

                contextSwitchUp(newFrame, newCode);
                break;
            }
            case 0xb7: // invokespecial
            {
                final int indexbyte1 = codeBytes[pc++] & 0xff;
                final int indexbyte2 = codeBytes[pc++] & 0xff;
                final int index = indexbyte1 << 8 | indexbyte2;
                final MethodInfo method = ConstantInfo.getMethodref(classFile, index);
                final int paramsSize = MethodInfo.getParametersSize(method);

                final int objectref = frame.peekInt(paramsSize + 1);
                if (objectref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }
                final InstanceData instance = instancePool.getInstance(objectref);

                // TODO Throw IllegalAccessError, AbstractMethodError,
                // UnsatisfiedLinkError, IncompatibleClassChangeError if needed

                if (Modifier.SYNCHRONIZED.eval(method.getModifiers())) {
                    frame.monitor = instance.getMonitor();
                    frame.monitor.acquire(this);
                } else
                    frame.monitor = null;

                if (Modifier.NATIVE.eval(method.getModifiers())) {
                    System.err.println("Native special method " + method.getName() + " of class "
                            + method.getClassFile().getName() + " is not implemented!");
                    System.exit(0);
                }

                final CodeAttribute newCode = MethodInfo.getCode(method);
                final StackFrame newFrame = pushFrame(newCode.getMaxStack(), newCode.getMaxLocals());
                for (int i = paramsSize; i >= 0; --i)
                    newFrame.storeInt(i, frame.popInt());

                contextSwitchUp(newFrame, newCode);
                break;
            }
            case 0xb8: // invokestatic
            {
                final int indexbyte1 = codeBytes[pc++] & 0xff;
                final int indexbyte2 = codeBytes[pc++] & 0xff;
                final int index = indexbyte1 << 8 | indexbyte2;
                final MethodInfo method = ConstantInfo.getMethodref(classFile, index);
                final int paramsSize = MethodInfo.getParametersSize(method);

                if (Modifier.NATIVE.eval(method.getModifiers())) {
                    final ClassFile methodClass = method.getClassFile();

                    if (methodClass.getName().equals("java/lang/Object"))
                        if (method.getName().equals("registerNatives"))
                            break;

                    if (methodClass.getName().equals("java/lang/Class")) {
                        if (method.getName().equals("registerNatives"))
                            break;

                        if (method.getName().equals("getPrimitiveClass")) {
                            final int objectref = frame.popReference();
                            final InstanceData instance = instancePool.getInstance(objectref);
                            final ClassFile classFile = instance.getClassFile();

                            final ByteBuffer buf = ByteBuffer.allocate(4);
                            instance.get(classFile.getField("value", "[C"), buf.array());
                            final int arrayref = buf.getInt();
                            final ArrayInstanceData arrayInstance = (ArrayInstanceData) instancePool
                                    .getInstance(arrayref);

                            final char[] value = new char[arrayInstance.getLength()];
                            for (int i = 0; i < value.length; ++i)
                                value[i] = heapSpace.getChar(arrayInstance.dataStart + i * 2);
                            final String name = new String(value);

                            switch (name) {
                                case "boolean":
                                    frame.pushReference(
                                            instancePool.getJavaClass(classLoader.loadClass("Z")).getReference());
                                    break;
                                case "char":
                                    frame.pushReference(
                                            instancePool.getJavaClass(classLoader.loadClass("C")).getReference());
                                    break;
                                case "float":
                                    frame.pushReference(
                                            instancePool.getJavaClass(classLoader.loadClass("F")).getReference());
                                    break;
                                case "double":
                                    frame.pushReference(
                                            instancePool.getJavaClass(classLoader.loadClass("D")).getReference());
                                    break;
                                case "byte":
                                    frame.pushReference(
                                            instancePool.getJavaClass(classLoader.loadClass("B")).getReference());
                                    break;
                                case "short":
                                    frame.pushReference(
                                            instancePool.getJavaClass(classLoader.loadClass("s")).getReference());
                                    break;
                                case "int":
                                    frame.pushReference(
                                            instancePool.getJavaClass(classLoader.loadClass("I")).getReference());
                                    break;
                                case "long":
                                    frame.pushReference(
                                            instancePool.getJavaClass(classLoader.loadClass("J")).getReference());
                                    break;
                                default:
                                    System.err.println("Unknown primitive name " + name);
                                    System.exit(0);
                            }

                            break;
                        }

                        if (method.getName().equals("desiredAssertionStatus0")) {
                            frame.pushInt(0);
                            break;
                        }
                    }

                    if (methodClass.getName().equals("java/lang/reflect/Array"))
                        if (method.getName().equals("newArray")) {
                            final int arrayLength = frame.popInt();
                            final int componentType = frame.popReference();
                            final ClassFile arrayClass = instancePool
                                    .getClassFile(instancePool.getInstance(componentType));
                            frame.pushReference(((ArrayClassFile) classLoader.loadClass("[" + arrayClass.getName()))
                                    .newInstance(arrayLength));
                            break;
                        }

                    System.err.println("Native static method " + method.getName() + " of class "
                            + method.getClassFile().getName() + " is not implemented!");
                    System.exit(0);
                }

                final CodeAttribute newCode = MethodInfo.getCode(method);
                final StackFrame newFrame = pushFrame(newCode.getMaxStack(), newCode.getMaxLocals());
                for (int i = paramsSize - 1; i >= 0; --i)
                    newFrame.storeInt(i, frame.popInt());

                contextSwitchUp(newFrame, newCode);
                break;
            }
            case 0xb9: // invokeinterface
            {
                final int indexbyte1 = codeBytes[pc++] & 0xff;
                final int indexbyte2 = codeBytes[pc++] & 0xff;
                final int index = indexbyte1 << 8 | indexbyte2;
                final int paramsSize = (codeBytes[(pc += 2) - 2] & 0xff) - 1;

                final int objectref = frame.peekInt(paramsSize + 1);
                if (objectref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }
                final InstanceData instance = VirtualMachine.getInstancePool().getInstance(objectref);

                final MethodInfo method = ConstantInfo.getInterfaceMethodref(classFile, index, instance);

                // TODO Throw IllegalAccessError, AbstractMethodError,
                // UnsatisfiedLinkError, IncompatibleClassChangeError if needed

                if (Modifier.SYNCHRONIZED.eval(method.getModifiers())) {
                    frame.monitor = method.getClassFile().getInstance().getMonitor();
                    frame.monitor.acquire(this);
                } else
                    frame.monitor = null;

                if (Modifier.NATIVE.eval(method.getModifiers())) {
                    System.err.println("Native interface method " + method.getName() + " of class "
                            + method.getClassFile().getName() + " is not implemented!");
                    System.exit(0);
                }

                final CodeAttribute newCode = MethodInfo.getCode(method);
                final StackFrame newFrame = pushFrame(newCode.getMaxStack(), newCode.getMaxLocals());
                for (int i = paramsSize; i >= 0; --i)
                    newFrame.storeInt(i, frame.popInt());

                contextSwitchUp(newFrame, newCode);
                break;
            }
            case 0xbb: // new
            {
                final int indexbyte1 = codeBytes[pc++] & 0xff;
                final int indexbyte2 = codeBytes[pc++] & 0xff;
                final int index = indexbyte1 << 8 | indexbyte2;

                final ClassFile resolvedClass = ConstantInfo.getClassFile(classFile, index);
                final int objectref = resolvedClass.newInstance();
                frame.pushReference(objectref);
                break;
            }
            case 0xbc: // newarray
            {
                final byte atype = codeBytes[pc++];
                final BaseType type = BaseType.values()[atype - 4];
                final ClassFile resolvedClass = classLoader.loadClass(type.toString());

                final int count = frame.popInt();
                if (count < 0) {
                    throwException(classLoader.loadClass("java/lang/NegativeArraySizeException"));
                    break;
                }

                final ArrayClassFile arrayClass = (ArrayClassFile) classLoader.loadClass("[" + resolvedClass.getName());
                final int arrayref = arrayClass.newInstance(count);
                frame.pushReference(arrayref);
                break;
            }
            case 0xbd: // anewarray
            {

                final int indexbyte1 = codeBytes[pc++] & 0xff;
                final int indexbyte2 = codeBytes[pc++] & 0xff;
                final int index = indexbyte1 << 8 | indexbyte2;
                final ClassFile resolvedClass = ConstantInfo.getClassFile(classFile, index);

                final int count = frame.popInt();
                if (count < 0) {
                    throwException(classLoader.loadClass("java/lang/NegativeArraySizeException"));
                    break;
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
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }

                final ArrayInstanceData instance = (ArrayInstanceData) instancePool.getInstance(arrayref);
                final int length = instance.getLength();
                frame.pushInt(length);
                break;
            }
            case 0xbf: // athrow
            {
                final int objectref = frame.popReference();
                if (objectref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }

                throwException(instancePool.getInstance(objectref));
                break;
            }
            case 0xc0: // checkcast
            {
                final int indexbyte1 = codeBytes[pc++] & 0xff;
                final int indexbyte2 = codeBytes[pc++] & 0xff;
                final int index = indexbyte1 << 8 | indexbyte2;
                final ClassFile resolvedClass = ConstantInfo.getClassFile(classFile, index);

                final int objectref = frame.popReference();
                frame.pushReference(objectref);
                if (objectref == 0)
                    break;
                final ClassFile instanceClass = instancePool.getInstance(objectref).getClassFile();

                if (!instanceClass.isInstance(resolvedClass))
                    throwException(classLoader.loadClass("java/lang/ClassCastException"));
                break;
            }
            case 0xc1: // instanceof
            {
                final int indexbyte1 = codeBytes[pc++] & 0xff;
                final int indexbyte2 = codeBytes[pc++] & 0xff;
                final int index = indexbyte1 << 8 | indexbyte2;
                final ClassFile resolvedClass = ConstantInfo.getClassFile(classFile, index);

                final int objectref = frame.popReference();
                if (objectref == 0)
                    frame.pushInt(0);
                final ClassFile instanceClass = instancePool.getInstance(objectref).getClassFile();

                frame.pushInt(instanceClass.isInstance(resolvedClass) ? 1 : 0);
                break;
            }
            case 0xc2: // monitorenter
            {
                final int objectref = frame.popReference();
                if (objectref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }

                final InstanceData instance = instancePool.getInstance(objectref);
                instance.getMonitor().acquire(this);
                break;
            }
            case 0xc3: // monitorexit
            {
                final int objectref = frame.popReference();
                if (objectref == 0) {
                    throwException(classLoader.loadClass("java/lang/NullPointerException"));
                    break;
                }

                final InstanceData instance = instancePool.getInstance(objectref);
                if (!instance.getMonitor().release(this))
                    throwException(classLoader.loadClass("java/lang/IllegalMonitorStateException"));
                break;
            }
            /*
             * EXTENDED
             */
            case 0xc6: // ifnull
            {
                final byte branchbyte1 = codeBytes[pc++];
                final byte branchbyte2 = codeBytes[pc++];
                final int offset = branchbyte1 << 8 | branchbyte2;

                final int value = frame.popReference();
                if (value == 0)
                    pc += offset - 3;
                break;
            }
            case 0xc7: // ifnonnull
            {
                final byte branchbyte1 = codeBytes[pc++];
                final byte branchbyte2 = codeBytes[pc++];
                final int offset = branchbyte1 << 8 | branchbyte2;

                final int value = frame.popReference();
                if (value != 0)
                    pc += offset - 3;
                break;
            }
            case 0xc8: // goto_w
            {
                int offset = 0;
                offset |= codeBytes[pc++] << 24;
                offset |= codeBytes[pc++] << 16;
                offset |= codeBytes[pc++] << 8;
                offset |= codeBytes[pc++];

                pc += offset - 5;
                break;
            }
            case 0xc9: // jsr_w
            {
                int offset = 0;
                offset |= codeBytes[pc++] << 24;
                offset |= codeBytes[pc++] << 16;
                offset |= codeBytes[pc++] << 8;
                offset |= codeBytes[pc++];

                frame.pushInt(pc);
                pc += offset - 5;
                break;
            }
            default:
                System.err.println("Unrecognized opcode 0x" + Integer.toHexString(opcode));
                System.exit(0);
                break;
        }
    }

    private void contextSwitchUp(final StackFrame newFrame, final CodeAttribute newCode) {
        frame.pc = pc;
        frame.code = code;

        frame = newFrame;
        pc = 0;
        code = newCode;
        codeBytes = newCode.getCode();
        classFile = newCode.getClassFile();
    }

    private void contextSwitchDown(final StackFrame prevFrame) {
        frame = prevFrame;
        pc = prevFrame.pc;
        code = prevFrame.code;
        codeBytes = prevFrame.code.getCode();
        classFile = prevFrame.code.getClassFile();
    }

    private void releaseMonitor() {
        if (frame.monitor != null) {
            if (!frame.monitor.release(this))
                throwException(VirtualMachine.getClassLoader().loadClass("java/lang/IllegalMonitorStateException"));
            else
                frame.monitor = null;
        }
    }

    private void throwException(final ClassFile classFile) {
        final int objectref = classFile.newInstance();
        final InstanceData instance = VirtualMachine.getInstancePool().getInstance(objectref);
        throwException(instance);
    }

    private void throwException(final InstanceData instance) {
        exception = instance;
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
