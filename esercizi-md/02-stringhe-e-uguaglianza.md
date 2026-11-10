# 02 · Stringhe, uguaglianza e formattazione

Obiettivo: non cadere mai nella trappola di `==` sugli oggetti, e saper manipolare e formattare le stringhe come fai in C#.

## Sintassi a confronto

| C# | Java | Nota |
| --- | --- | --- |
| `s.Length` | `s.length()` | Con le parentesi |
| `s[0]` | `s.charAt(0)` | Niente indicizzatore |
| `s.Substring(1, 3)` | `s.substring(1, 4)` | Il secondo parametro è l'**indice finale escluso**, non la lunghezza |
| `s.IndexOf("x")` | `s.indexOf("x")` | Restituisce `-1` se manca |
| `s.Contains("x")` | `s.contains("x")` | |
| `s.StartsWith("x")` | `s.startsWith("x")` | |
| `s.Replace("a", "b")` | `s.replace("a", "b")` | `replaceAll` usa le regex |
| `s.Split(',')` | `s.split(",")` | **Il parametro è una regex** |
| `s.Trim()` | `s.strip()` (o `trim()`) | `strip` gestisce meglio gli spazi Unicode |
| `s.ToUpper()` | `s.toUpperCase()` | |
| `string.IsNullOrEmpty(s)` | `s == null \|\| s.isEmpty()` | |
| `string.IsNullOrWhiteSpace(s)` | `s == null \|\| s.isBlank()` | |
| `string.Join(", ", xs)` | `String.join(", ", xs)` | |
| `a == b` | `a.equals(b)` | `==` confronta i riferimenti |
| `a.Equals(b, OrdinalIgnoreCase)` | `a.equalsIgnoreCase(b)` | |
| `a.CompareTo(b)` | `a.compareTo(b)` | |
| `$"Ciao {nome}"` | `"Ciao %s".formatted(nome)` | Non esiste l'interpolazione |
| `new string('-', 20)` | `"-".repeat(20)` | |

**Segnaposto di `formatted` / `String.format`**

| Segnaposto | Significa | Esempio |
| --- | --- | --- |
| `%s` | testo (o qualsiasi oggetto) | `"%s".formatted("A12")` |
| `%d` | intero | `"%d".formatted(20)` |
| `%.2f` | decimale con 2 cifre | `"%.2f".formatted(2.5)` |
| `%10s`, `%-10s` | larghezza 10, allineato a destra / a sinistra | |
| `%08.3f` | larghezza 8, riempito di zeri, 3 decimali | |
| `%,d` | separatore delle migliaia | |
| `%b`, `%c`, `%x` | booleano, carattere, esadecimale | |
| `%n`, `%%` | a capo, simbolo `%` | |
| `%1$s` | usa l'argomento numero 1 (anche più volte) | |

## Esercizi

Ogni esercizio segue lo stesso schema: **Obiettivo** (cosa impari), **Prima di iniziare** (il concetto in breve), **Cosa fare** (passi da eseguire in ordine), **Cosa osservare** (dove guardare con attenzione), **Verifica** (checklist per dire "ho finito") e **Rifletti** (domande da colloquio). Dove serve c'è anche **Parole da cercare**.

### Es0201 · Lo string pool, variazione `[L2]`
**File:** `Es0201_StringPool.java`

**Obiettivo:** capire perché `==` tra stringhe a volte dà `true` e a volte `false`, e quando il compilatore le unisce in anticipo.

**Prima di iniziare:** i letterali di stringa (`"ab"`) vengono messi in un'area di memoria condivisa, lo **string pool**: due letterali uguali sono lo stesso oggetto. Una stringa costruita **a runtime** (con `new String(...)` o concatenando variabili) è un oggetto nuovo, fuori dal pool. Il metodo `intern()` restituisce la versione del pool. Se i due operandi di `+` sono **costanti di compilazione** (letterali o variabili `final` inizializzate con un letterale), il compilatore fa la concatenazione prima ancora dell'esecuzione.

**Cosa fare:**
1. Scrivi questo blocco nel `main`:
   ```java
   String x = "ab";
   String y = "a";
   String z = y + "b";
   final String w = "a";
   String k = w + "b";
   ```
2. In un commento scrivi la previsione (true/false) di queste quattro righe, **prima di eseguire**: `x == z`, `x == k`, `x.equals(z)`, `x == z.intern()`.
3. Stampa le quattro righe e confronta.
4. Per ogni previsione sbagliata scrivi una riga di spiegazione.
5. Aggiungi tre varianti tue (per esempio: `new String("ab") == "ab"`, `"a" + "b" == "ab"`, una stringa letta da `Scanner` confrontata con un letterale) e prevedi anche queste.

**Cosa osservare:** la differenza tra `y` (non `final`) e `w` (`final`) cambia il risultato.

**Verifica:**
- [ ] Previsioni scritte prima (almeno 7 in tutto).
- [ ] Spiegazione di perché `final` cambia il risultato.
- [ ] Sai dire quando usare `intern()` (quasi mai) e cosa usare al suo posto (`equals`).

**Rifletti:** cos'è una "espressione costante" per il compilatore? In C# esiste un meccanismo analogo (*string interning*)?

**Parole da cercare:** *string pool*, `String.intern`, *constant expression*.

### Es0202 · La cache degli `Integer` `[L2]`
**File:** `Es0202_IntegerCache.java`

**Obiettivo:** riconoscere e prevenire il bug "funziona con i numeri piccoli e si rompe con quelli grandi".

**Prima di iniziare:** `Integer` è un oggetto. `Integer a = 100;` chiama in realtà `Integer.valueOf(100)`, che per i valori da -128 a 127 restituisce sempre lo stesso oggetto dalla cache; per gli altri ne crea uno nuovo. Quindi `==` dà `true` per 127 e `false` per 128. Un `Integer` e un `Long` con lo stesso valore **non** sono mai `equals`.

**Cosa fare:**
1. Crea due `Integer` con valore 127 e confrontali con `==`; poi due con valore 128. Scrivi prima le previsioni.
2. Riscrivi i due confronti con `.equals()`.
3. Confronta `int p = 128; Integer q = 128;` con `p == q`: prevedi e spiega (suggerimento: quale operando viene "scartato"?).
4. Prevedi ed esegui `Integer.valueOf(1).equals(1L)` e `Long.valueOf(1).equals(1L)`.
5. Importa `java.util.Objects` e confronta con `Objects.equals`: due `Long` nulli, un `Long` nullo con uno valorizzato, due `Long` da 5000 con lo stesso valore.
6. Crea una `List<Long>` con tre id (`1000L`, `2000L`, `3000L`) e scrivi un metodo `static boolean contiene(List<Long> ids, Long cercato)` che usa `==` dentro un ciclo; verifica che sbaglia. Poi correggilo.

**Cosa osservare:** nel punto 4 il compilatore non protesta: `equals` accetta qualsiasi `Object`.

**Verifica:**
- [ ] Previsioni scritte prima.
- [ ] Hai trovato il valore minimo per cui `==` smette di funzionare.
- [ ] Il metodo `contiene` corretto non usa `==` sugli oggetti.

**Rifletti:** un id di entity JPA è un `Long`. Dove rischi di confrontarlo con `==`? Perché `Objects.equals` è comodo con valori che possono essere nulli?

### Es0203 · Immutabilità delle stringhe `[L1]`
**File:** `Es0203_StringImmutabili.java`

**Obiettivo:** capire che i metodi delle stringhe restituiscono una **nuova** stringa e che concatenare in un ciclo è costoso.

**Prima di iniziare:** una `String` non cambia mai. `s.toUpperCase();` calcola una nuova stringa, che va assegnata (o usata) da qualche parte. Ogni `+=` in un ciclo crea una nuova stringa e copia la vecchia. `StringBuilder` è modificabile e quindi più efficiente in questi casi.

**Cosa fare:**
1. Scrivi `String s = "java"; s.toUpperCase(); System.out.println(s);` e commenta il risultato.
2. Correggi in modo che il risultato sia visibile.
3. Con un ciclo da 1 a 20000 concatena una lettera a una stringa con `+=`. Misura il tempo con `long inizio = System.nanoTime();` e `System.nanoTime() - inizio`, e stampalo in millisecondi.
4. Ripeti con `StringBuilder` e `append`. Stampa i due tempi affiancati.
5. Aumenta a 100000 iterazioni e osserva come cresce il primo tempo rispetto al secondo.
6. Stampa `sb.length()` e `sb.reverse()` su un piccolo esempio (`StringBuilder` ha metodi che `String` non ha).

**Cosa osservare:** il tempo della versione con `+=` cresce molto più che linearmente.

**Verifica:**
- [ ] Hai due tempi stampati e un commento che li interpreta.
- [ ] Hai spiegato perché il punto 1 "non fa niente".

**Rifletti:** un singolo `a + b + c` fuori da un ciclo è un problema? (Cerca come lo traduce il compilatore.)

### Es0204 · Manipolare codici posto `[L2]`
**File:** `Es0204_ManipolareStringhe.java`

**Obiettivo:** tradurre le operazioni sulle stringhe da C# a Java e scoprire due trappole di `split` e `substring`.

**Prima di iniziare:** `Substring(inizio, lunghezza)` di C# diventa `substring(inizio, fineEsclusa)`: il secondo parametro è un **indice**, non una lunghezza. `split` prende un'**espressione regolare**: il punto `.` significa "qualsiasi carattere".

**Cosa fare:**
1. Traduci in Java questo codice C#, riga per riga, stampando ogni variabile:
   ```csharp
   var code = "  a12 ";
   var clean = code.Trim().ToUpper();
   var row = clean[0];
   var number = int.Parse(clean.Substring(1));
   var ok = !string.IsNullOrWhiteSpace(code) && clean.StartsWith("A");
   var parts = "A1,A2,B7".Split(',');
   var joined = string.Join(" | ", parts);
   ```
2. Controlla i tipi: `row` è un `char` in Java, `parts` è un `String[]` (stampalo con `Arrays.toString`).
3. Aggiungi `var pezzi = "1.5".split(".");` e stampa `pezzi.length` e il contenuto. Spiega perché è diverso da quello che ti aspettavi.
4. Correggi con la forma giusta per cercare un punto letterale (escape della regex).
5. Aggiungi `"A12".substring(1, 3)` e `"A12".substring(1, 2)` e verifica che hai capito il secondo parametro.
6. Prova con codice `"B"` (nessun numero): cosa fa `Integer.parseInt("")`? Gestisci il caso senza far crollare il programma.

**Cosa osservare:** il risultato del punto 3 è un array vuoto.

**Verifica:**
- [ ] `row` vale `'A'`, `number` vale `12`, `joined` vale `A1 | A2 | B7`.
- [ ] Hai scritto in un commento il motivo per cui `split(".")` non va.
- [ ] Il punto 6 lancia (e gestisce) un'eccezione.

**Rifletti:** quali metodi di C# non hanno un nome simile in Java? (Guarda `IsNullOrWhiteSpace` e `Join`.)

### Es0205 · Tabella formattata `[L2]`
**File:** `Es0205_Formattazione.java`

**Obiettivo:** formattare numeri e testi con `String.format` e `formatted`, controllando larghezza, decimali e lingua.

**Prima di iniziare:** un segnaposto ha la forma `%[flag][larghezza][.precisione]tipo`. Esempi: `%-8s` testo allineato a sinistra in 8 caratteri, `%8.2f` decimale in 8 caratteri con 2 cifre dopo la virgola, `%5d` intero in 5 caratteri. Il separatore decimale dipende dalla `Locale` del computer, a meno che tu non la specifichi.

**Cosa fare:**
1. Prepara tre array paralleli con 4 posti: codici (`"A1"`, `"B12"`, `"E20"`, `"C7"`), prezzi (`10.5`, `120.0`, `7.25`, `1999.99`) e quantità (`1`, `12`, `100`, `3`).
2. Stampa un'intestazione e le 4 righe in colonne allineate con `"%-8s %10.2f %5d"`. Cambia le larghezze finché le colonne sono ordinate.
3. Stampa lo stesso prezzo con `String.format(Locale.ITALY, "%.2f", prezzo)` e con `Locale.ROOT`: confronta i separatori.
4. Prova questi formati e annota cosa fanno: `%,d` su `1234567`, `%08.3f` su `3.14159`, `%x` su `255`, `%b` su `true`, `%10s|` e `%-10s|` su `"ciao"`, `%%`.
5. Usa `%1$s` per ripetere lo stesso argomento due volte nella stessa stringa.
6. Prova con un tipo sbagliato (per esempio `%d` con una `String`) e leggi l'eccezione.

**Cosa osservare:** le colonne devono restare allineate anche con il codice di lunghezza diversa.

**Verifica:**
- [ ] La tabella ha 4 righe e un'intestazione allineata.
- [ ] Hai provato tutti i formati del punto 4 e annotato i risultati.
- [ ] Sai dire quale eccezione lancia il tipo sbagliato.

**Rifletti:** perché in un log o in un JSON conviene usare `Locale.ROOT`? Con quale altro strumento puoi formattare numeri e valute (`NumberFormat`)?

### Es0206 · Text block `[L1]`
**File:** `Es0206_TextBlock.java`

**Obiettivo:** scrivere testi multilinea (JSON, SQL) in modo leggibile con i *text block*.

**Prima di iniziare:** un text block inizia con `"""` seguito da un a-capo e termina con `"""`. Java toglie l'indentazione comune a tutte le righe: la posizione del `"""` di chiusura influenza quanti spazi vengono tolti. In C# 11 esistono le *raw string literals* con la stessa sintassi.

**Cosa fare:**
1. Crea una variabile `String json` con un text block che rappresenta una prenotazione: campi `eventoId` (numero), `posto` (stringa), `utente` (stringa).
2. Sostituisci i valori con segnaposto (`%d`, `%s`) e riempili con `.formatted(...)`.
3. Stampa il risultato e verifica che sia un JSON valido (incollalo in un validatore online, o controlla a occhio le virgole).
4. Sposta il `"""` di chiusura di 4 spazi a destra, poi di 4 a sinistra: osserva ogni volta come cambia l'indentazione del testo stampato.
5. Aggiungi una riga con una virgoletta `"` dentro il testo e una con `\` e vedi quando serve l'escape.
6. Scrivi con un text block una query SQL su 4 righe (`SELECT ... FROM ... WHERE ...`).

**Cosa osservare:** gli spazi a fine riga e l'a-capo finale.

**Verifica:**
- [ ] Il JSON stampato non ha spazi iniziali indesiderati.
- [ ] Hai provato le due posizioni della chiusura.
- [ ] La query SQL è stampata su 4 righe.

**Rifletti:** quali sono i rischi di costruire una query SQL con `formatted` partendo da input dell'utente? (Pensa a SQL injection e a cosa userai nel progetto: `JPA` e parametri.)

### Es0207 · Ordinare codici posto `[L2]`
**File:** `Es0207_OrdinareCodici.java`

**Obiettivo:** distinguere l'ordine alfabetico delle stringhe dall'ordine "naturale" dei codici e scrivere un `Comparator`.

**Prima di iniziare:** `String.compareTo` ordina lettera per lettera: `"A10"` viene prima di `"A2"` perché `'1' < '2'`. Per ordinare per fila e poi per numero serve un `Comparator` che separa le due parti. Costruire un `Comparator` si fa con `Comparator.comparing(...)` e `thenComparingInt(...)`.

**Cosa fare:**
1. Crea una `List<String>` con `A1`, `A10`, `A2`, `B1`, `A20`, `B10`, `B2`, in disordine (`new ArrayList<>(List.of(...))`).
2. Ordinala con `Collections.sort(lista)` e stampala: scrivi prima la previsione.
3. Scrivi due metodi di appoggio: `static char fila(String codice)` e `static int numero(String codice)`.
4. Crea un `Comparator<String>` che ordina per fila e poi per numero, **prima** con una classe anonima e **poi** con `Comparator.comparing(...).thenComparingInt(...)`.
5. Ordina con `lista.sort(comparatore)` e stampa.
6. Aggiungi una variante: ordine inverso per numero (`reversed()`), ma sempre per fila crescente.
7. Spiega in un commento il costo del metodo `numero` chiamato a ogni confronto, e come lo ridurresti (suggerimento: trasforma prima i codici in oggetti).

**Cosa osservare:** l'ordine stabile dei codici uguali.

**Verifica:**
- [ ] Ordine finale `A1, A2, A10, A20, B1, B2, B10`.
- [ ] Hai provato sia la classe anonima sia la versione con `Comparator.comparing`.
- [ ] La previsione del punto 2 è scritta prima.

**Rifletti:** qual è la differenza tra implementare `Comparable` e passare un `Comparator`? Quando usi l'uno e quando l'altro?

### Es0208 · Validare un codice posto con una regex `[L3]` · *progetto*
**File:** `Es0208_ValidaCodice.java`

**Obiettivo:** validare l'input con le espressioni regolari, in modo sicuro rispetto a `null` e performante.

**Prima di iniziare:** `String.matches(regex)` confronta **tutta** la stringa con la regex (non serve `^` e `$`). Compilare la regex a ogni chiamata è uno spreco: si usa un `Pattern` dichiarato una sola volta come costante `private static final`. Una regex per `A1`..`Z99` è: una lettera maiuscola seguita da una o due cifre.

**Cosa fare:**
1. Dichiara `private static final Pattern PATTERN = Pattern.compile(...)` con la regex che accetta una lettera maiuscola da A a Z seguita da 1 o 2 cifre (il numero non può iniziare con 0).
2. Scrivi `static boolean valido(String codice)` che restituisce `false` se il codice è `null` e usa il `Pattern` (`PATTERN.matcher(codice).matches()`).
3. Crea una lista di 12 input: `"A1"`, `"E20"`, `"Z99"`, `"a12"`, `"A123"`, `"AA1"`, `"A0"`, `"A01"`, `""`, `" A1 "`, `null`, `"A-1"`.
4. Per ogni input stampa `input -> true/false`. Scrivi **prima** le previsioni.
5. Aggiungi una versione che prima fa `strip()` e porta in maiuscolo, e verifica che ora `" a1 "` sia valido.
6. Scrivi una versione che invece di restituire `boolean` lancia `IllegalArgumentException` con un messaggio chiaro quando il codice non è valido.

**Cosa osservare:** nessun input provoca una `NullPointerException`.

**Verifica:**
- [ ] 12 input, 12 previsioni, 12 risultati.
- [ ] `null` restituisce `false` senza eccezioni.
- [ ] Il `Pattern` è creato una sola volta.

**Rifletti:** dove userai questa validazione nel progetto? (Pensa alle *path variable* del controller.) Quando una regex è la scelta sbagliata?

**Parole da cercare:** `Pattern`, `Matcher`, `String.matches`.

### Es0209 · `equals` e `null` `[L1]`
**File:** `Es0209_EqualsNull.java`

**Obiettivo:** scrivere confronti sicuri, che non si rompono quando uno dei due valori è `null`.

**Prima di iniziare:** chiamare un metodo su `null` lancia `NullPointerException`. In `a.equals(b)` è `a` che deve essere non nullo; `b` può esserlo. Per questo si scrive `"COSTANTE".equals(x)`. `Objects.equals(a, b)` gestisce entrambi i casi.

**Cosa fare:**
1. Dichiara `String x = null;`.
2. Prova, ciascuna dentro un `try/catch (NullPointerException e)`: `x.equals("A12")`, `"A12".equals(x)`, `Objects.equals(x, "A12")`, `x == null`.
3. Stampa per ognuna il risultato o la scritta "NPE".
4. Poi ripeti con `String x = "A12";` e con `String x = "a12";`.
5. Aggiungi `"A12".equalsIgnoreCase(x)` e valuta anche questa con `x` nullo.
6. Scrivi un metodo `static boolean stessoPosto(String a, String b)` che ignora maiuscole e minuscole e considera uguali due `null`.

**Cosa osservare:** quale delle prime tre istruzioni lancia l'eccezione.

**Verifica:**
- [ ] Una tabella nel commento con espressione e risultato per ciascuno dei tre valori di `x`.
- [ ] `stessoPosto(null, null)` restituisce `true`, `stessoPosto("a1", null)` restituisce `false`.

**Rifletti:** quale stile preferisci per confrontare con una costante? In C# `x == "A12"` non lancia mai: cosa ti fa preferire l'una o l'altra scelta?

### Es0210 · Switch su stringhe `[L2]`
**File:** `Es0210_SwitchSuStringhe.java`

**Obiettivo:** usare lo switch con le stringhe e gestire in modo pulito i comandi sconosciuti.

**Prima di iniziare:** lo switch su `String` usa `equals` ed è sensibile a maiuscole e spazi. Se la stringa è `null`, lo switch lancia `NullPointerException` (a meno di aggiungere un `case null`, Java 21).

**Cosa fare:**
1. Scrivi `static String esegui(String comando)` con una switch expression a frecce che gestisce `"hold"`, `"conferma"`, `"annulla"` restituendo un messaggio per ciascuno.
2. Per qualsiasi altro comando lancia `IllegalArgumentException("Comando sconosciuto: " + comando)`.
3. Prima dello switch normalizza l'input: `strip()` e `toLowerCase()`.
4. Prova con: `"hold"`, `" HOLD "`, `"Conferma"`, `"annulla"`, `"boh"`, `""`, `null`.
5. Gestisci il `null` in due modi: con un controllo prima dello switch, e con `case null` dentro lo switch (Java 21).
6. Aggiungi un `enum Comando { HOLD, CONFERMA, ANNULLA }` e scrivi `static Comando daTesto(String testo)` che restituisce un `Optional<Comando>` senza eccezioni; poi uno switch sull'enum **senza** `default`.

**Cosa osservare:** la differenza di robustezza tra lo switch su stringa e quello su enum.

**Verifica:**
- [ ] Tutti i 7 input producono un messaggio o un'eccezione prevista.
- [ ] Nessun input provoca una `NullPointerException` non voluta.

**Rifletti:** perché in un'applicazione vera conviene convertire presto le stringhe in `enum`? Cosa fa il controller Spring con un parametro `enum` sbagliato?


## Cosa devi aver capito

- [ ] `==` su oggetti confronta i riferimenti: si usa `.equals()`.
- [ ] La cache degli `Integer` (da -128 a 127) rende il bug intermittente.
- [ ] `Integer.equals(Long)` è sempre `false`.
- [ ] `substring(inizio, fineEsclusa)` e `split` con regex.
- [ ] Le stringhe sono immutabili e in un ciclo si usa `StringBuilder`.
- [ ] `%s %d %.2f %n` e la dipendenza dalla `Locale`.
