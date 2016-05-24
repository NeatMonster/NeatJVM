package fr.neatmonster.neatjvm.natives;

import fr.neatmonster.neatjvm.InstanceData;
import fr.neatmonster.neatjvm.InstancePool;
import fr.neatmonster.neatjvm.VirtualMachine;

public class java_lang_String {

    public static InstanceData intern(final InstanceData instance) {
        final InstancePool instancePool = VirtualMachine.getInstancePool();
        return instancePool.addString(instancePool.getString(instance));
    }
}
