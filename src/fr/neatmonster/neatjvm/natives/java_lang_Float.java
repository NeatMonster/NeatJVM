package fr.neatmonster.neatjvm.natives;

import fr.neatmonster.neatjvm.ClassData;

public class java_lang_Float {

    public static int floatToRawIntBits(final ClassData instance, final float value) {
        return Float.floatToRawIntBits(value);
    }

    public static float intBitsToFloat(final ClassData instance, final int bits) {
        return Float.intBitsToFloat(bits);
    }
}
