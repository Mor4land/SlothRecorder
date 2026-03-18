package space.kaelus.slothrecorder.util;

public class SlothMath {
    private static final double MINIMUM_DIVISOR = Math.pow(0.2f, 3) * 8 * 0.15 - 1e-3;

    public static double getMinimumDivisor() {
        return MINIMUM_DIVISOR;
    }

    public static double gcd(double aInput, double bInput) {
        if (aInput == 0.0) return 0.0;

        double a = aInput;
        double b = bInput;
        
        if (a < b) {
            double temp = a;
            a = b;
            b = temp;
        }

        while (b > MINIMUM_DIVISOR) {
            double temp = a - (Math.floor(a / b) * b);
            a = b;
            b = temp;
        }

        return a;
    }
}
