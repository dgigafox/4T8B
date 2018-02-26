package com.a4t8bfarm.a4t8b.Interfaces;

import com.a4t8bfarm.a4t8b.MainActivity;

import java.text.DecimalFormat;

/**
 * Created by Darren Gegantino on 10/30/2017.
 */

public class DecimalToFraction {

    private double d;


    public DecimalToFraction(double d) {
        // TODO Auto-generated constructor stub
        this.d = d;
    }

    public String getFraction() {
        int d1 = 1;
        String result = "0";
        String text = Double.toString(Math.abs(d));
        double modulo = d % 1;
        String moduloString = Double.toString(modulo);
        switch (moduloString){
            case "0.0":
                DecimalFormat decimalFormat = new DecimalFormat("0");
                result = decimalFormat.format(d);
                break;
            default:
                int integerPlaces = text.indexOf('.');
                int decimalPlaces = text.length() - integerPlaces - 1;

                int i = (int) (d*ipower(10, decimalPlaces));
                int i1 = (int) (d1*ipower(10, decimalPlaces));

                int commonfactor = commonFactor(i, i1);

                int num = i/commonfactor;
                int denom = i1/commonfactor;

                if (num < denom){
                    result = i/commonfactor + "/" + i1/commonfactor;
                }
                else {
                    result = mixedFraction(num, denom);
                }
                break;
        }

        return result;
    }

    static int ipower(int base, int exp) {
        int result = 1;
        for (int i = 1; i <= exp; i++) {
            result *= base;
        }

        return result;
    }

    private static int commonFactor(int num, int divisor) {
        if (divisor == 0) {
            return num;
        }
        return commonFactor(divisor, num % divisor);
    }

    private static String mixedFraction(int num, int denom){
        int wholeNumber = num / denom;
        int mixedFractNum = num % denom;
        String result = wholeNumber + " " + mixedFractNum + "/" + denom;
        return result;
    }
}
