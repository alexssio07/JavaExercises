package org.example;

import java.util.Scanner;

public class Es0109_Scanner {

    // Da rivedere

    // Obiettivo: leggere dati dalla console in modo robusto, gestendo gli input errati senza far terminare il programma.
    // Scanner scanner = new Scanner(System.in);

    static void scannerA() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Quanti posti vuoi prenotare? ");
        int numPosti = scanner.nextInt();
        scanner.nextLine();
        // Si utilizza un secondo nextLine per poter saltare l'invio residuo rimasto nel buffer altrimenti leggerebbe stringa vuota
        System.out.print("Come ti chiami? ");
        String nome = scanner.nextLine();
        System.out.println("Numero posti: " + numPosti);
        System.out.println("Nome: " + nome);
    }

    static void scannerB() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Quanti posti vuoi prenotare? ");
        String riga = scanner.nextLine();
        try {
            int numPosti = Integer.parseInt(riga);
            System.out.print(numPosti);
        }
        catch (NumberFormatException e) {
            System.out.println("Non è un numero");
        }
    }

    public static void main(String[] args) {
        //scannerA();
        scannerB();
    }
}
