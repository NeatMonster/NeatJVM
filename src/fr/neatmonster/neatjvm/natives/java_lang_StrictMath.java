package fr.neatmonster.neatjvm.natives;

import fr.neatmonster.neatjvm.ClassData;

public class java_lang_StrictMath {

    public static double acos(final ClassData instance, final double a) {
        return StrictMath.acos(a);
    }

    public static double asin(final ClassData instance, final double a) {
        return StrictMath.asin(a);
    }

    public static double atan(final ClassData instance, final double a) {
        return StrictMath.atan(a);
    }

    public static double atan2(final ClassData instance, final double y, final double x) {
        return StrictMath.atan2(y, x);
    }

    public static double cbrt(final ClassData instance, final double a) {
        return StrictMath.cbrt(a);
    }

    public static double cos(final ClassData instance, final double a) {
        return StrictMath.cos(a);
    }

    public static double cosh(final ClassData instance, final double x) {
        return StrictMath.cosh(x);
    }

    public static double exp(final ClassData instance, final double a) {
        return StrictMath.exp(a);
    }

    public static double expm1(final ClassData instance, final double x) {
        return StrictMath.expm1(x);
    }

    public static double hypot(final ClassData instance, final double x, final double y) {
        return StrictMath.hypot(x, y);
    }

    public static double IEEEremainder(final ClassData instance, final double f1, final double f2) {
        return StrictMath.IEEEremainder(f1, f2);
    }

    public static double log(final ClassData instance, final double a) {
        return StrictMath.log(a);
    }

    public static double log10(final ClassData instance, final double a) {
        return StrictMath.log10(a);
    }

    public static double log1p(final ClassData instance, final double x) {
        return StrictMath.log1p(x);
    }

    public static double pow(final ClassData instance, final double a, final double b) {
        return StrictMath.pow(a, b);
    }

    public static double sin(final ClassData instance, final double a) {
        return StrictMath.sin(a);
    }

    public static double sinh(final ClassData instance, final double x) {
        return StrictMath.sinh(x);
    }

    public static double sqrt(final ClassData instance, final double a) {
        return StrictMath.sqrt(a);
    }

    public static double tan(final ClassData instance, final double a) {
        return StrictMath.tan(a);
    }

    public static double tanh(final ClassData instance, final double x) {
        return StrictMath.tanh(x);
    }
}
