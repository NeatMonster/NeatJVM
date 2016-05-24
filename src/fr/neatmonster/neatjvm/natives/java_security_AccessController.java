package fr.neatmonster.neatjvm.natives;

import fr.neatmonster.neatjvm.ClassData;
import fr.neatmonster.neatjvm.InstanceData;

public class java_security_AccessController {

    public static InstanceData doPrivileged(final ClassData instance, final InstanceData action) {
        return (InstanceData) action.getClassFile().getMethod("run", "*").invoke(action);
    }

    public static InstanceData doPrivileged(final ClassData instance, final InstanceData action,
            final InstanceData context) {
        return (InstanceData) action.getClassFile().getMethod("run", "*").invoke(action);
    }

    public static InstanceData getInheritedAccessControlContext(ClassData instance) {
        throw new UnsupportedOperationException();
    }

    public static InstanceData getStackAccessControlContext(ClassData instance) {
        throw new UnsupportedOperationException();
    }
}
