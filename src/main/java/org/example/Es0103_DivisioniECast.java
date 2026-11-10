package org.example;

public class Es0103_DivisioniECast {

    // Da rivedere e ricontrollare
    public static void main(String[] args) {
        System.out.println(7 / 2); // 3 (OK)
        System.out.println(7 / 2.0); // 3.5 (OK)
        System.out.println((double) 7 / 2); // 3.5 (OK)
        System.out.println((int) 3.99); // 3 (OK)
        System.out.println((int) -3.99); // -3 (OK)
        System.out.println(-7 % 3); // 1  (KO) -> -1 (OK)
        // Math.floorMod(x,y) restituisce il resto della divisione (il modulo) tra due numeri
        System.out.println(Math.floorMod(-7, 3)); //2.33 (KO) -> 2 (OK)
        System.out.println(0.1 + 0.2); // 0.30 (OK)
        System.out.println('A' + 1); // 'A1' (KO) -> 66 (OK)
        System.out.println((char) ('A' + 1)); // 'A1' KO -> 'B' (OK)
        try {
        System.out.println(10 / 0); // 0 o errore
        System.out.println(10.0 / 0); // 0 o errore

        }
        catch (ArithmeticException e) {}

    }
}
