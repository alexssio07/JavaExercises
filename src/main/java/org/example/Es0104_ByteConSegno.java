package org.example;

public class Es0104_ByteConSegno {
    // Da verificare e riflettiamo sulla domanda

    //In Java, un byte ha sempre il segno e spazia da -128 a 127
    static byte a = (byte) 200;  // Occorre inserire il cast perché il numero supera il range del byte
    static byte c = 10;
    static byte d = 127;
    static byte e = (byte) 128;  // 10000000  (8 bit), rientra nel byte
    static byte f = (byte) 255;  // 11111111 (8 bit), rientra nel byte
    static byte b = (byte) 256;  // Occorre inserire il cast perché il numero supera il range del byte
    // livello di bit il numero 256 richiede 9 bit per essere scritto e
    // in binario è composto da un 1 seguito da otto 0 (1 00000000).
    // Mettere 256 nel byte: Significa provare a mettere un liquido in un bicchiere troppo piccolo.
    // Il liquido trabocca, i bit in più vengono persi, e dentro il byte ti ritrovi un valore
    // completamente distorto (in questo caso, zero).


    public static void main(String[] args) {
        System.out.println("toUnsignedInt: " + Byte.toUnsignedInt(a));
        System.out.println("toUnsignedInt: " + Byte.toUnsignedInt(c));
        System.out.println("toUnsignedInt: " + Byte.toUnsignedInt(d));
        System.out.println("toUnsignedInt: " + Byte.toUnsignedInt(e));
        System.out.println("toUnsignedInt: " + Byte.toUnsignedInt(f));
        System.out.println("toUnsignedInt: " + Byte.toUnsignedInt(b));
        System.out.println(a & 0xFF);
        System.out.println(c & 0xFF);
        System.out.println(d & 0xFF);
        System.out.println(e & 0xFF);
        System.out.println(f & 0xFF);
        System.out.println(b & 0xFF);
    }
}
