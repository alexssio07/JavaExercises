package org.example;

public class Es0106_Cicli {

    public static void main(String[] args) {

        // Tabellina del numero 7
        for (int i = 1; i <= 10; i++) {
            System.out.println(i * 7);
        }

        // Somma di se stesso fino a 100 con un ciclo while
        int somma = 0;
        int i = 1;
        while (i <= 100) {
            somma += i;
            i++;
        }
        System.out.println("Risultato con ciclo while: " + somma);

        //  Somma di se stesso fino a 100 con la formula di Gauss
        int n = 100;
        int verifica = n * (n + 1) / 2;
        System.out.println("Risultato con la formula matematica di Gauss: " + verifica);

        // Verifico se i risultati sono identici
        if (verifica == somma) {
            System.out.println("I numeri corrispondono perfettamente.");
        } else {
            System.out.println("Errore nel calcolo della formula");
        }
    }
}
