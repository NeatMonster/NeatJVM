package fr.neatmonster.neatjvm.natives;

import fr.neatmonster.neatjvm.ClassData;

public class java_lang_Double {

    public static long doubleToRawLongBits(final ClassData instance, final double value) {
        return Double.doubleToRawLongBits(value);
    }

    public static double longBitsToDouble(final ClassData instance, final long bits) {
        return Double.longBitsToDouble(bits);
    }
}
