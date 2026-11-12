package org.example;

public class Es0108_Varargs {

    // Da verificare e rivedere i risultati del metodo unisci


    //  E' un parametro varargs: dentro il metodo numeri è un int[].
    //  Può stare solo come ultimo parametro.
    //  Quando esistono più overload applicabili, Java sceglie quello più specifico e preferisce sempre la versione senza varargs.

    static int somma(int... numeri) {
        System.out.println("Somma varargs");
        int somma = 0;
        for (int i = 0; i < numeri.length; i++) {
            somma = somma + numeri[i];
        }
        return somma;
    }

    static int somma(int a, int b) {
        return a + b;
    }

    static String unisci(String separatore, String... parti) {
        String resp = "";
        for (int i = 0; i < parti.length; i++) {
            resp = separatore + parti[i];
        }
        return resp;
    }
    //    IMPOSSIBILE DA DICHIARARE IN QUANTO IL PARAMETRO DI TIPO VARARGS VA DICHIARATO COME ULTIMO ELEMENTO NELLA FIRMA DEL METODO
    //    static void sbagliato(String... a, int b) {
    //
    //    }

    public static void main(String[] args) {
        System.out.println(somma()); // 1° Somma con Varargs
        System.out.println();
        System.out.println(somma(1)); // 2° Somma con Varargs
        System.out.println();
        System.out.println(somma(1, 2)); // 3° Somma a 2 parametri
        System.out.println();
        System.out.println(somma(1, 2, 3)); // 4° Somma con Varargs
        System.out.println();
        System.out.println(somma(new int[]{4, 5})); // 5° Somma con Varargs perché non sono 2 elementi ma è un unico elemento array
        System.out.println();
        System.out.println(unisci("-", "A", "12")); // Perché restituisce soltanto -12 invece che A-12 ??
        System.out.println(unisci("-")); // Non restituisce nulla perché l'array ...parti è vuoto
        System.out.println(unisci("-", "solo")); // Restituisce -solo
    }

}
