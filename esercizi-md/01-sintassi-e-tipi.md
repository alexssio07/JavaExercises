# 01 · Sintassi base e tipi

Obiettivo: scrivere Java di tutti i giorni senza pensare in C#. Qui trovi la sintassi a confronto con esempi neutri, poi gli esercizi.

## Sintassi a confronto

```csharp
// C#
namespace Studio;
using System;

class Programma
{
    static void Main(string[] args)
    {
        const int Max = 3;
        var nomi = new[] { "Ada", "Linus", "Grace" };
        foreach (var n in nomi) Console.WriteLine(n);
        for (int i = 0; i < Max; i++) { }
        var esito = Max > 2 ? "alto" : "basso";
    }
}
```

```java
// Java: file Programma.java
package studio;                      // deve combaciare con la cartella

import java.util.Arrays;             // al posto di "using"

public class Programma {
    public static void main(String[] args) {
        final int MAX = 3;           // "const" non esiste in locale: si usa final
        var nomi = new String[] { "Ada", "Linus", "Grace" };
        for (var n : nomi) System.out.println(n);    // for-each con i due punti
        for (int i = 0; i < MAX; i++) { }
        var esito = MAX > 2 ? "alto" : "basso";
        System.out.println(Arrays.toString(nomi));   // stampare un array: serve Arrays.toString
    }
}
```

**Switch classico**: in Java, se dimentichi `break`, l'esecuzione **continua** nel caso successivo (in C# è un errore di compilazione).

```java
switch (giorno) {
    case 1:
        System.out.println("lunedì");
        break;
    case 2:
        System.out.println("martedì");   // senza break cade nel caso successivo
    default:
        System.out.println("altro");
}
```

**Varargs, array e costanti**

```java
static int totale(int... valori) { ... }          // C#: params int[] valori
int[] a = new int[5];                              // a.length, senza parentesi
int[][] griglia = new int[3][4];
static final double IVA = 0.22;                    // C#: const double Iva = 0.22;
```

## Esercizi

Ogni esercizio segue lo stesso schema: **Obiettivo** (cosa impari), **Prima di iniziare** (il concetto in breve), **Cosa fare** (passi da eseguire in ordine), **Cosa osservare** (dove guardare con attenzione), **Verifica** (checklist per dire "ho finito") e **Rifletti** (domande da colloquio). Dove serve c'è anche **Parole da cercare**, per quando vuoi approfondire sulla documentazione.

### Es0101 · Primo programma `[L1]`
**File:** `Es0101_PrimoProgramma.java` (package `studio.base`)

**Obiettivo:** capire com'è fatto un programma Java e come si compila e si esegue, confrontandolo con `dotnet run`.

**Prima di iniziare:** un file `.java` contiene una classe pubblica che si chiama come il file. Il compilatore `javac` la trasforma in un file `.class` (bytecode); il comando `java` avvia la JVM, che esegue il bytecode partendo dal metodo `main`. Maven automatizza i primi due passi e mette i `.class` in `target/classes`.

**Cosa fare:**
1. In `src/main/java` crea le cartelle `studio/base` e dentro il file `Es0101_PrimoProgramma.java`. Come prima riga scrivi `package studio.base;`.
2. Dichiara `public class Es0101_PrimoProgramma` e dentro il metodo `public static void main(String[] args)`.
3. Nel `main` stampa tre righe con `System.out.println`: `Nome: ` seguito dal tuo nome, `Versione Java: ` seguito da `System.getProperty("java.version")`, `Argomenti ricevuti: ` seguito da `args.length`.
4. Esegui con la freccia verde di IntelliJ accanto al `main`.
5. Compila con Maven (pannello Maven → Lifecycle → `compile`, oppure `mvn compile` da terminale) e poi lancia da terminale, dalla cartella del progetto: `java -cp target/classes studio.base.Es0101_PrimoProgramma ciao mondo`.
6. Guasta di proposito il programma, un guasto alla volta, e leggi ogni errore: (a) rinomina la classe senza rinominare il file; (b) rimetti a posto e cambia il `package` in `studio.altro` lasciando il file in `studio/base`; (c) rimetti a posto e togli `static` dal `main`.

**Cosa osservare:** con `ciao mondo` `args.length` vale 2. Nell'errore (c) la JVM dice che non trova il metodo `main`: segnati il messaggio esatto.

**Verifica:**
- [ ] Il programma stampa tre righe sia da IntelliJ sia da terminale.
- [ ] Da terminale `args.length` è 2 (e 0 da IntelliJ, se non hai impostato argomenti).
- [ ] Hai scritto in un commento i tre messaggi d'errore dei tre guasti.

**Rifletti:** cosa corrisponde al `target/classes` in .NET? Perché il nome del `package` deve combaciare con le cartelle?

**Parole da cercare:** `javac`, `classpath`, `java -cp`.

### Es0102 · I limiti dei tipi `[L1]`
**File:** `Es0102_LimitiDeiTipi.java`

**Obiettivo:** conoscere i valori limite dei tipi primitivi e vedere cosa succede quando li superi.

**Prima di iniziare:** ogni tipo primitivo ha una classe "gemella" (`Integer`, `Long`, `Byte`...) con le costanti `MIN_VALUE` e `MAX_VALUE`. In Java l'aritmetica intera in overflow **non lancia eccezioni**: il numero "gira" sul valore minimo (stesso comportamento di C# fuori da un contesto `checked`).

**Cosa fare:**
1. Stampa `MIN_VALUE` e `MAX_VALUE` di `Byte`, `Short`, `Integer`, `Long`, e `MAX_VALUE` di `Float` e `Double`. Per `char` stampa `(int) Character.MAX_VALUE`.
2. Prima di eseguire, scrivi in un commento cosa pensi che stampino queste tre righe, poi eseguile: `Integer.MAX_VALUE + 1`, `Long.MAX_VALUE + 1`, `Byte.MAX_VALUE + 1` (quest'ultima in un `int`, poi prova ad assegnarla a un `byte` e leggi l'errore del compilatore).
3. Prevedi ed esegui: `(long) Integer.MAX_VALUE + 1` e `(long) (Integer.MAX_VALUE + 1)`.
4. Chiama `Math.addExact(Integer.MAX_VALUE, 1)` dentro un `try/catch (ArithmeticException e)` e stampa il messaggio dell'eccezione.
5. Prova anche `Math.multiplyExact(100000, 100000)` e `Math.toIntExact(3_000_000_000L)`.

**Cosa osservare:** la differenza tra i due `long` del punto 3 (conta *dove* avviene il cast). I trattini bassi nei numeri (`3_000_000_000L`) sono ammessi, come in C#.

**Verifica:**
- [ ] Hai le previsioni scritte prima dei risultati.
- [ ] Hai spiegato in un commento perché le due espressioni del punto 3 danno risultati diversi.
- [ ] Hai visto almeno due eccezioni `ArithmeticException` con messaggio.

**Rifletti:** in C# quale costrutto (`checked`) fa quello che fanno i metodi `*Exact`? In quali punti di un backend di prenotazioni (contatori, somme di importi in centesimi) ti servirebbe?

### Es0103 · Divisioni, cast e resti `[L1]`
**File:** `Es0103_DivisioniECast.java`

**Obiettivo:** prevedere correttamente i risultati dell'aritmetica mista tra interi, decimali e caratteri.

**Prima di iniziare:** la divisione tra due interi dà un intero (si perde il decimale). Il cast `(int)` **tronca** verso lo zero. Il resto `%` ha il segno del dividendo. I `double` non sono esatti (`0.1 + 0.2`). Un `char` è un numero: sommare un `int` a un `char` dà un `int`.

**Cosa fare:**
1. Crea una tabella nel commento iniziale con tre colonne: espressione, **tua previsione**, risultato reale (la terza la compili dopo).
2. Aggiungi queste 13 espressioni: `7 / 2`, `7 / 2.0`, `(double) 7 / 2`, `(double) (7 / 2)`, `(int) 3.99`, `(int) -3.99`, `-7 % 3`, `Math.floorMod(-7, 3)`, `0.1 + 0.2`, `'A' + 1`, `(char) ('A' + 1)`, `10.0 / 0`, `0.0 / 0`.
3. Compila **prima** la colonna delle previsioni, senza eseguire nulla.
4. Stampa ogni espressione con `System.out.println("espressione = " + risultato)` ed esegui.
5. Aggiungi per ultimo `10 / 0` dentro un `try/catch (ArithmeticException e)` e confrontalo con `10.0 / 0`.

**Cosa osservare:** quali previsioni hai sbagliato e cosa avevi dimenticato (di solito: divisione intera, segno del resto, `char` + `int`).

**Verifica:**
- [ ] 13 previsioni scritte prima.
- [ ] Per ogni previsione sbagliata hai scritto la regola corretta in una riga.
- [ ] Sai spiegare perché `10 / 0` lancia un'eccezione e `10.0 / 0` no (prova a cercare `Infinity` e `NaN`).

**Rifletti:** quali di questi risultati sarebbero identici in C#? (Controlla soprattutto `-7 % 3` e `0.1 + 0.2`.)

### Es0104 · Il `byte` con segno `[L2]`
**File:** `Es0104_ByteConSegno.java`

**Obiettivo:** non farsi sorprendere dal fatto che il `byte` di Java va da -128 a 127, mentre in C# va da 0 a 255.

**Prima di iniziare:** un byte contiene 8 bit. In Java quegli 8 bit sono interpretati con il segno (complemento a due). Il byte `11001000` vale 200 in C# ma -56 in Java. Per recuperare il valore "da 0 a 255" si usa `Byte.toUnsignedInt(b)` oppure la maschera `b & 0xFF`.

**Cosa fare:**
1. Scrivi `byte b = (byte) 200;` e stampa `b`. Il cast è obbligatorio: scrivi in un commento perché.
2. Recupera il valore senza segno in due modi (`Byte.toUnsignedInt` e `& 0xFF`) e stampa entrambi.
3. Crea `byte[] dati = { 10, 127, (byte) 128, (byte) 200, (byte) 255 };` e con un ciclo stampa per ogni elemento: valore con segno, valore senza segno, e il byte in esadecimale con `String.format("%02X", b)`.
4. Prova `Integer.toBinaryString(b)` con `b = -56` e confrontalo con `Integer.toBinaryString(b & 0xFF)`: prevedi prima la lunghezza delle due stringhe.

**Cosa osservare:** con `-56` la versione senza maschera stampa 32 cifre: il `byte` viene promosso a `int` **mantenendo il segno**.

**Verifica:**
- [ ] Per ogni elemento dell'array i due valori (con e senza segno) differiscono solo quando il bit alto è 1.
- [ ] Hai spiegato la lunghezza delle due stringhe binarie.

**Rifletti:** dove incontrerai i `byte` in un backend (hash SHA-256, file caricati, chiavi)? Perché è facile sbagliare quando li confronti o li stampi?

**Parole da cercare:** complemento a due, `Byte.toUnsignedInt`, *sign extension*.

### Es0105 · Switch con *fallthrough* `[L1]`
**File:** `Es0105_SwitchFallthrough.java`

**Obiettivo:** capire il comportamento dello switch classico di Java (che "cade" nel caso successivo) e imparare la *switch expression* moderna, che lo evita.

**Prima di iniziare:** in C# ogni `case` deve finire con `break`, `return` o simili, altrimenti il compilatore dà errore. In Java **no**: se dimentichi `break`, l'esecuzione continua nelle istruzioni del `case` successivo (e anche del `default`) senza guardare se la condizione combacia. È la sorgente di bug più antica del linguaggio. Dal Java 14 esiste la forma con la freccia (`->`), che non cade mai e può restituire un valore.

**Cosa fare:**
1. Scrivi un metodo `static void descrivi(int giorno)` con uno switch **classico** (due punti e `break`):
   - caso 1: stampa `lunedì`, poi `break`;
   - caso 2: stampa `martedì` e **non mettere** `break`;
   - caso 3: stampa `mercoledì`, poi `break`;
   - `default`: stampa `altro giorno`.
2. Nel `main` chiama `descrivi(1)`, `descrivi(2)`, `descrivi(3)` e `descrivi(4)`, ciascuna preceduta da una stampa di separazione (`System.out.println("--- 2 ---")`).
3. **Prima di eseguire**, scrivi in un commento quante righe e quali pensi che produca ciascuna chiamata.
4. Esegui e confronta. Annota le chiamate in cui avevi sbagliato.
5. Aggiungi il `break` mancante al caso 2 e riesegui.
6. Scrivi un secondo metodo `static String tipoGiorno(int giorno)` con una **switch expression a frecce** che restituisce: `"feriale"` per i giorni da 1 a 5 (elencali nella stessa riga: `case 1, 2, 3, 4, 5 ->`), `"weekend"` per 6 e 7, e per qualsiasi altro valore lancia `IllegalArgumentException` con un messaggio che include il valore ricevuto.
7. Prova `tipoGiorno(3)`, `tipoGiorno(7)` e `tipoGiorno(9)` (quest'ultimo in un `try/catch`).
8. Togli il ramo `default` e leggi l'errore del compilatore: perché una switch expression su `int` deve essere esaustiva?
9. Riscrivi lo switch classico del punto 1 come switch **statement a frecce** (senza restituire nulla) e verifica che il caso 2 non cada più nel 3.

**Cosa osservare:** nel punto 4 la chiamata con 2 stampa **due** righe. Nel punto 6 non c'è nessun `break` e nessuna ripetizione di `case`.

**Verifica:**
- [ ] Hai scritto la previsione per ciascuna delle 4 chiamate prima di eseguire.
- [ ] Hai visto con i tuoi occhi il fallthrough del caso 2.
- [ ] `tipoGiorno(9)` lancia un'eccezione con il valore nel messaggio.
- [ ] Sai spiegare in una frase perché la versione a frecce non ha bisogno di `break`.

**Rifletti:** cosa succede se la switch expression non copre tutti i valori possibili? Perché con un `enum` completo puoi evitare il `default`? Esiste un caso in cui il fallthrough è utile (cerca "case multipli senza break")?

**Parole da cercare:** *switch expression*, *fall-through*, `yield`.

### Es0106 · Cicli ed etichette `[L1]`
**File:** `Es0106_Cicli.java`

**Obiettivo:** conoscere i quattro cicli di Java e l'uso delle etichette per uscire da cicli annidati (dove in C# useresti un `goto` o una variabile).

**Prima di iniziare:** `for`, `while` e `do-while` sono come in C#. Il `foreach` si scrive con i due punti. Un'**etichetta** (`esterno:`) davanti a un ciclo permette di scrivere `break esterno;` o `continue esterno;` dall'interno di un ciclo annidato.

**Cosa fare:**
1. Con un `for` classico stampa la tabellina del 7 (da `7 x 1 = 7` a `7 x 10 = 70`).
2. Con un `while` somma i numeri da 1 a 100 e stampa il risultato (verificalo con la formula `n * (n + 1) / 2`).
3. Con un `do-while` e uno `Scanner` chiedi un numero all'utente e ripeti finché non inserisce un numero positivo.
4. Con un ciclo `for-each` (`for (int n : array)`) scorri un array `int[]` di 8 numeri e stampa solo quelli pari, usando `continue` per saltare i dispari.
5. Con due `for` annidati (`a` da 1 a 20 e `b` da 1 a 20) cerca la **prima** coppia con `a < b` e `a * b == 42`. Appena la trovi, stampala e **esci da entrambi i cicli** con un'etichetta e `break etichetta;`.
6. Rifai il punto 5 **senza** etichetta, usando una variabile `boolean trovato`, e confronta le due versioni.

**Cosa osservare:** nel punto 5 il programma non deve continuare a ciclare dopo aver trovato la coppia (puoi verificarlo con una stampa a ogni iterazione).

**Verifica:**
- [ ] I punti da 1 a 6 sono tutti nello stesso `main`, ognuno con un titolo stampato.
- [ ] Nel punto 5 stampi una sola coppia.
- [ ] La somma del punto 2 coincide con la formula.

**Rifletti:** quale versione del punto 5 preferisci? Nei colloqui si chiede spesso la differenza tra `break` e `continue`: sai spiegarla con un esempio?

### Es0107 · Array e la classe `Arrays` `[L2]`
**File:** `Es0107_Array.java`

**Obiettivo:** usare gli array di Java e le utility della classe `Arrays`, evitando la trappola del `==` e della stampa diretta.

**Prima di iniziare:** un array è un oggetto di lunghezza fissa. Si legge `array.length` (**senza parentesi**). Stampare un array con `println` stampa il suo "indirizzo" (qualcosa come `[D@1b6d3586`), non il contenuto. `==` confronta i riferimenti, `.equals()` degli array fa lo stesso, e solo `Arrays.equals(a, b)` confronta gli elementi.

**Cosa fare:**
1. Crea `double[] prezzi = { 49.99, 12.5, 30.0, 12.5, 7.25 };`.
2. Stampa `prezzi` con `System.out.println(prezzi)` e commenta cosa vedi. Poi stampa con `Arrays.toString(prezzi)`.
3. Stampa `prezzi.length`, il primo elemento e l'ultimo (senza scrivere il numero 4 a mano).
4. Ordina con `Arrays.sort(prezzi)` e ristampa. Cerca poi come ordinare in senso decrescente (suggerimento: non funziona direttamente con `double[]`; cosa serve?).
5. Crea un secondo array `double[] altri = new double[5]`, riempilo con `Arrays.fill(altri, 9.99)`, e poi crea una copia dei primi 3 elementi di `prezzi` con `Arrays.copyOf`.
6. Crea due array diversi con lo stesso contenuto (`int[] x = {1, 2, 3}; int[] y = {1, 2, 3};`) e stampa i risultati di `x == y`, `x.equals(y)`, `Arrays.equals(x, y)`.
7. Crea una griglia `int[][] griglia = new int[3][4]`, riempila con `riga * 10 + colonna` e stampala con `Arrays.deepToString`.

**Cosa osservare:** il valore dei tre confronti del punto 6 (scrivi prima la previsione).

**Verifica:**
- [ ] Hai spiegato in un commento perché `println(array)` stampa un indirizzo.
- [ ] I tre confronti dell'ultimo punto hanno ciascuno il risultato spiegato.
- [ ] La griglia si stampa su una riga con `deepToString`.

**Rifletti:** quando conviene un array e quando una `List`? In C# `array.Length` è una proprietà: ricordi dove in Java compaiono le parentesi?

**Parole da cercare:** `Arrays.sort`, `Arrays.copyOfRange`, `Arrays.deepToString`.

### Es0108 · Varargs e overload `[L2]`
**File:** `Es0108_Varargs.java`

**Obiettivo:** scrivere metodi con un numero variabile di argomenti e capire come Java sceglie tra overload.

**Prima di iniziare:** `int... numeri` è un parametro *varargs*: dentro il metodo `numeri` è un `int[]`. Può stare solo come **ultimo** parametro. Quando esistono più overload applicabili, Java sceglie quello più specifico e preferisce sempre la versione senza varargs.

**Cosa fare:**
1. Scrivi `static int somma(int... numeri)` che restituisce la somma (0 se non ci sono argomenti).
2. Scrivi anche `static int somma(int a, int b)` che stampa `"versione a due parametri"` e restituisce la somma.
3. Scrivi nel commento quale versione verrà chiamata, **prima di eseguire**, per ciascuna di queste chiamate: `somma()`, `somma(1)`, `somma(1, 2)`, `somma(1, 2, 3)`, `somma(new int[] { 4, 5 })`.
4. Esegui e confronta (usa una stampa dentro la versione varargs per distinguere).
5. Scrivi `static String unisci(String separatore, String... parti)` che unisce le parti con il separatore, senza usare `String.join`.
6. Chiama `unisci("-", "A", "12")`, `unisci("-")`, `unisci("-", "solo")`. Spiega in un commento cosa restituisce ciascuna.
7. Prova a scrivere `static void sbagliato(String... a, int b)`: leggi l'errore.

**Cosa osservare:** la chiamata `somma(1, 2)` sceglie la versione senza varargs.

**Verifica:**
- [ ] Le 5 previsioni sono scritte prima.
- [ ] `unisci("-")` non lancia eccezioni e restituisce una stringa vuota.
- [ ] Hai letto l'errore del punto 7 e sai spiegarlo.

**Rifletti:** in C# `params int[] n` funziona allo stesso modo? Cosa succede se passi `null` come array varargs?

### Es0109 · Input da tastiera `[L2]`
**File:** `Es0109_Scanner.java`

**Obiettivo:** leggere dati dalla console in modo robusto, gestendo gli input errati senza far terminare il programma.

**Prima di iniziare:** `Scanner scanner = new Scanner(System.in);` legge dalla tastiera. Il metodo `nextInt()` legge solo il numero e lascia nel buffer l'**invio**: una successiva `nextLine()` legge una riga vuota. Se l'utente scrive `abc` e tu chiami `nextInt()`, ottieni `InputMismatchException`. In Java non esiste `int.TryParse`: si usa `try/catch` su `NumberFormatException`.

**Cosa fare:**
1. **Versione A.** Chiedi "Quanti posti vuoi prenotare?" con `nextInt()`. Poi chiedi "Come ti chiami?" con `nextLine()` e stampa il nome: noterai che la riga viene saltata. Scrivi in un commento perché.
2. Correggi la versione A (due modi: consumare l'invio residuo, oppure leggere sempre con `nextLine()`).
3. **Versione B.** Leggi sempre una riga intera con `nextLine()` e converti con `Integer.parseInt(riga.strip())` dentro un `try/catch (NumberFormatException e)`.
4. Metti la lettura in un ciclo che ripete la domanda finché il valore non è un intero tra 1 e 10; per ogni errore stampa un messaggio che distingue "non è un numero" da "fuori intervallo".
5. Prova gli input: `abc`, ` 5 ` (con spazi), `-3`, `11`, `4`, e infine un invio vuoto.

**Cosa osservare:** con quali input la versione B accetta e con quali scarta. Cosa fa `Integer.parseInt(" 5 ")` senza `strip()`?

**Verifica:**
- [ ] Digitando `abc`, `-3`, `11`, `4` in quest'ordine il programma accetta solo il `4`.
- [ ] Il programma non termina mai per colpa di un input sbagliato.
- [ ] Hai incapsulato la lettura in un metodo `static int leggiIntero(Scanner s, String domanda, int min, int max)`.

**Rifletti:** quale versione preferisci e perché? Come si chiuderebbe lo `Scanner`? (Attenzione: chiudere `System.in` ha conseguenze.)

### Es0110 · Generatore di codici posto `[L2]` · *progetto*
**File:** `Es0110_CodiciPosto.java`

**Obiettivo:** usare cicli annidati, `char` aritmetico e `StringBuilder` per costruire i codici dei posti di un evento (servirà alla settimana 2).

**Prima di iniziare:** un `char` si può usare come numero: `(char) ('A' + 2)` è `'C'`. Per costruire un testo in un ciclo si usa `StringBuilder` e il suo metodo `append`, non la concatenazione con `+`.

**Cosa fare:**
1. Dichiara due costanti locali: `final int FILE = 5;` e `final int POSTI_PER_FILA = 20;`.
2. Con due `for` annidati stampa tutti i codici da `A1` a `E20`, uno per riga, usando `(char) ('A' + fila)` e il numero del posto (da 1, non da 0).
3. Conta quanti codici hai generato e stampa il totale alla fine.
4. Crea una `List<String>` con tutti i codici (usa `ArrayList`) e stampa il primo, l'ultimo e quello in posizione 42.
5. Con `StringBuilder` costruisci una "mappa": una riga per fila, ogni riga con 20 caselle; la casella è `.` se il numero del posto è pari e `X` se è dispari. Prima di ogni riga scrivi la lettera della fila.
6. Stampa la mappa. Poi cambia `FILE` in 3 e `POSTI_PER_FILA` in 10 e verifica che tutto si adatti senza altre modifiche.

**Cosa osservare:** l'ultimo codice è `E20` e non `E19` o `F1` (errori tipici di "off-by-one").

**Verifica:**
- [ ] Il totale è 100, il primo codice `A1`, l'ultimo `E20`.
- [ ] La mappa ha 5 righe da 20 caselle più la lettera.
- [ ] Cambiando le due costanti il programma funziona senza altri ritocchi.

**Rifletti:** questa logica diventerà la generazione dei posti di un evento (settimana 2, giorno 5): dove la metteresti, in una classe `Evento`, in un service, in un metodo statico?

### Es0111 · Tradurre da C# a Java `[L2]`
**File:** `Es0111_DaCSharp.java`

**Obiettivo:** tradurre a mano un programma C# completo, usando le tabelle di questo corso.

**Prima di iniziare:** apri `00-INDICE-e-tabelle.md` e tieni a vista la tabella della sintassi. Traduci riga per riga, senza cercare "convertitori" in rete.

**Cosa fare:**
1. Copia il codice C# qui sotto in un commento in cima al file Java (ti servirà per il ripasso).
2. Traducilo in Java mantenendo lo stesso comportamento. Trasforma `Main` in `main`, `const` in `static final`, `string` in `String`, `foreach` in `for-each`, e il metodo `Classifica` in `classifica` (convenzione di nome).
3. In C# lo switch usa intervalli (`< Soglia`). In Java 21 questa sintassi non esiste. Scrivi **prima** la versione con `if / else if`.
4. Poi, come sfida, riscrivi lo switch usando un `Integer` (non un `int`) e i *guarded patterns*: `case Integer n when n < SOGLIA ->`. Confronta la leggibilità delle due versioni.
5. Esegui con le tre chiamate del `Main`.

```csharp
using System;

class Test
{
    const int Soglia = 100;

    static string Classifica(int[] valori)
    {
        var somma = 0;
        foreach (var v in valori) somma += v;
        return somma switch
        {
            0 => "vuoto",
            < Soglia => "basso",
            _ => "alto"
        };
    }

    static void Main()
    {
        Console.WriteLine(Classifica(new[] { 10, 20, 30 }));
        Console.WriteLine(Classifica(new[] { 90, 80 }));
        Console.WriteLine(Classifica(new int[0]));
    }
}
```

**Cosa osservare:** la somma dell'array vuoto vale 0; l'ordine dei casi nello switch conta.

**Verifica:**
- [ ] Il programma stampa `basso`, `alto`, `vuoto`, in questo ordine.
- [ ] Il codice C# originale è nel commento in cima.
- [ ] Hai provato entrambe le versioni del punto 3 e 4.

**Rifletti:** quali tra le traduzioni di questo esercizio non hanno un equivalente diretto in Java (`const` locale, `new[]`, `Console`)? Cosa hai dovuto cambiare oltre alla sintassi?


## Cosa devi aver capito

- [ ] Nome file = nome classe pubblica; `package` = cartella.
- [ ] `final` al posto di `const` e `readonly`; `static final` per le costanti di classe.
- [ ] `length`, `length()` e `size()` e quando si usa ciascuno.
- [ ] Il `byte` ha il segno e non esistono i tipi `unsigned`.
- [ ] Lo switch classico cade nel caso successivo senza `break`; le frecce no.
- [ ] Un array non si stampa con `println`.
