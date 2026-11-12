package org.example;

import java.util.Arrays;
import java.util.Collections;

public class Es0107_Array {

    // Da verificare e riflettiamo sul confronto degli Array

    public static void main(String[] args) {
        Double[] prezzi = {49.99, 12.5, 30.0, 12.5, 7.25};
        // Convertito da double e Double per permettere di fare il sort inverso in quanto il metodo sort accetta tipi generici nella firma

        for (int i = 0; i < prezzi.length; i++) {
            System.out.println(prezzi[i]);
        }
        System.out.println();
        System.out.println(prezzi); // Stampa il riferimento o indirizzo in memoria?
        System.out.println(Arrays.toString(prezzi)); // OK
        System.out.println(prezzi.length); // OK
        System.out.println("Primo elemento: " + prezzi[0] + " -- Ultimo elemento: " + prezzi[prezzi.length - 1]);
        Arrays.sort(prezzi); // Ordinali in modo crescente
        System.out.println("Array in ordine crescente: " + Arrays.toString(prezzi));
        Arrays.sort(prezzi, Collections.reverseOrder()); // Tipo di comparazione nel secondo parametro del metodo Arrays.sort
        System.out.println("Array in ordine decrescente: " + Arrays.toString(prezzi));
        System.out.println();

        double[] altri = new double[5];
        Arrays.fill(altri, 9.99);
        Double[] prezzi3 = Arrays.copyOf(prezzi, 3); // Copia dei primi 3 elemento dell'array prezzi
        System.out.println("Primi 3 elementi array prezzi: " + Arrays.toString(prezzi3));
        System.out.println();

        int[] x = {1, 2, 3};
        int[] y = {1, 2, 3};
        boolean confrSimbolo = x == y; // Fallirà sempre
        boolean confrEquals = x.equals(y); // Fallirà sempre
        boolean confrArraysEquals = Arrays.equals(x, y); // OK Perché è il metodo corretto per poter confrontare degli Array
        System.out.println("ConfrSimbolo: " + confrSimbolo);
        System.out.println("ConfrEquals: " + confrEquals);
        System.out.println("ConfrArraysEquals: " + confrArraysEquals);
        System.out.println();

        int[][] griglia = new int[3][4];
        for (int i = 0; i < griglia.length; i++) {
            for (int j = 0; j < griglia[i].length; j++) {
                griglia[i][j] = i * 10 + j;
            }
        }
        System.out.println("Griglia: " + Arrays.deepToString(griglia));
    }
}
