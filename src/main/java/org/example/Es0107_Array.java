package org.example;

import java.util.Arrays;

public class Es0107_Array {

    public static void main(String[] args) {
        double[] prezzi = { 49.99, 12.5, 30.0, 12.5, 7.25 };

        for (int i = 0; i < prezzi.length; i++) {
            System.out.println(prezzi[i]);
        }
        System.out.println(Arrays.toString(prezzi));
        System.out.println(prezzi.length);
        System.out.println("Primo elemento: " + prezzi[0] + "-- Ultimo elemento: " + prezzi[prezzi.length - 1]);
        Arrays.sort(prezzi);
        System.out.println(Arrays.toString(prezzi));
    }
}
