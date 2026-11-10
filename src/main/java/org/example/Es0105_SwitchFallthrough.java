package org.example;

public class Es0105_SwitchFallthrough {

    // Da verificare e riflettiamo sulla domanda

    static void descrivi(int giorno) {
//        switch (giorno) {
//            case 1:
//                System.out.println("Lunedì");
//                break;
//            case 2:
//                System.out.println("Martedì");
//            case 3:
//                System.out.println("Mercoledì");
//                break;
//            case 4:
//                System.out.println("Giovedì");
//                break;
//            case 5:
//                System.out.println("Venerdì");
//                break;
//            case 6:
//                System.out.println("Sabato");
//                break;
//            case 7:
//                System.out.println("Domenica");
//                break;
//            default:
//                System.out.println("Nessuna giorno invalida");
//                break;
//        }
        switch (giorno) {
            case 1 -> System.out.println("--- " + giorno + " --- Lunedì");
            case 2 -> System.out.println("--- " + giorno + " --- Martedì");
            case 3 -> System.out.println("--- " + giorno + " --- Mercoledì");
            case 4 -> System.out.println("--- " + giorno + " --- Giovedì");
            case 5 -> System.out.println("--- " + giorno + " --- Venerdì");
            case 6 -> System.out.println("--- " + giorno + " --- Sabato");
            case 7 -> System.out.println("--- " + giorno + " --- Domenica");
            default -> System.out.println("Nessun giorno invalida");
        }
    }

    public static String tipoGiorno(int giorno) {
        switch (giorno) {
            case 1, 2, 3, 4, 5 -> {
                return "Feriale";
            }
            case 6, 7 -> {
                return "Weekend";
            }
            default -> throw new IllegalArgumentException("Ricevuto valore errato: " + giorno);
        }
    }

    public static void main(String[] args) {
        descrivi(1); // Lunedì
        descrivi(2); // Martedì
        descrivi(3); // Mercoledì
        System.out.println(tipoGiorno(3)); // Feriale
        System.out.println(tipoGiorno(7)); // Weekend
        try {
            System.out.println(tipoGiorno(9)); // Errore, non esiste quel valore nello switch
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
