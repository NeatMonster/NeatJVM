package fr.neatmonster.neatjvm.natives;

import fr.neatmonster.neatjvm.InstanceData;
import fr.neatmonster.neatjvm.VirtualMachine;

public class java_lang_Object {

    public static InstanceData clone(final InstanceData instance) throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public static InstanceData getClass(final InstanceData instance) {
        return VirtualMachine.getInstancePool().getJavaClass(instance.getClassFile());
    }

    public static int hashCode(final InstanceData instance) {
        return instance.getHashCode();
    }

    public static void notify(final InstanceData instance) {
        instance.notify(VirtualMachine.getCurrentThread());
    }

    public static void notifyAll(final InstanceData instance) {
        instance.notifyAll(VirtualMachine.getCurrentThread());
    }

    public static void wait(final InstanceData instance, final long timeout) {
        instance.wait(VirtualMachine.getCurrentThread());
    }
}
