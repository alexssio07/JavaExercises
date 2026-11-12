package org.example;

import java.util.ArrayList;

public class Es0110_CodiciPosto {

    // Esercizio OK

    static final int FILE = 5;
    static final int POSTI_PER_FILA = 20;

    public static void costruisciPosto(int file, int posti) {
        StringBuilder sb = new StringBuilder();
        String[][] sala = new String[file][posti];
        ArrayList<String> listaPosti = new ArrayList<String>();
        int count = 0;
        for (int i = 0; i < file; i++) {
            char letteraFila = (char) ('A' + i);
            sb.append(letteraFila);
            sb.append(" ");
            for (int j = 0; j < posti; j++) {
                int numeroPosto = j + 1;
                sala[i][j] = letteraFila + "" + numeroPosto;
                listaPosti.add(sala[i][j]);
                if (numeroPosto % 2 == 0) {
                    sb.append(".");
                }
                else {
                    sb.append("X");
                }

                sb.append(" ");
                count++;
                System.out.print(letteraFila + "" + numeroPosto + " ");
            }
            sb.append("\n\n");
            System.out.println();
        }

//        System.out.println("Sala : " + Arrays.deepToString(sala));
        System.out.println();
        System.out.println("Totale codici: " + count);
        System.out.println("Primo elemento listaPosti: " + listaPosti.getFirst());
        System.out.println("Primo elemento listaPosti: " + listaPosti.getLast());
        //System.out.println("42esimo elemento listaPosti: " + listaPosti.get(42 - 1));
        System.out.println();
        System.out.println(sb.toString());

    }

    public static void main(String[] args) {
        costruisciPosto(5, 20);
        costruisciPosto(3, 10);
    }
}
