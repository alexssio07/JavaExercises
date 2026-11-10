package org.example;

import java.math.BigDecimal;

public class PrimoApproccio {
    public enum Stato {LIBERO, IN_ATTESA, VENDUTO}

    public enum Fascia {
        BASE(new BigDecimal("1.0")),
        VIP(new BigDecimal("2.5")),
        PLATEA(new BigDecimal("1.5"));

        private final BigDecimal prezzo;

        Fascia(BigDecimal prezzo) {
            this.prezzo = prezzo;
        }
    }

    public static BigDecimal calcolaPrezzo(BigDecimal base, Fascia fascia) {
        return base.multiply(fascia.prezzo);
    }

    public record Posto(String codice, Stato stato, BigDecimal prezzo) {
    }

    public static String descrivi(Posto p) throws IllegalStateException {
        return switch (p.stato()) {
            case LIBERO -> "%s: libero a %.2f euro".formatted(p.codice(), p.prezzo());
            case IN_ATTESA -> "%s: in attesa".formatted(p.codice());
            case VENDUTO -> "%s: venduto".formatted(p.codice());
            default -> throw new IllegalStateException();
        };
    }

    public static void main(String[] args) {
        Posto p = new Posto("A12", Stato.LIBERO, new BigDecimal("50"));
        System.out.println("Costo biglietto BASE " + calcolaPrezzo(new BigDecimal("10.9"), Fascia.BASE));
        System.out.println("Costo biglietto VIP " + calcolaPrezzo(new BigDecimal("10.9"), Fascia.VIP));
        System.out.println("Costo biglietto PLATEA " + calcolaPrezzo(new BigDecimal("10.9"), Fascia.PLATEA));
        System.out.println(descrivi(p));
    }
}
