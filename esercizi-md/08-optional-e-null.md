# 08 · `null`, `Optional` e `Objects`

Obiettivo: gestire l'assenza di valore senza `NullPointerException`. In C# hai le *nullable reference types* e gli operatori `?.` e `??`; in Java non esistono, e al loro posto hai `Optional` e i metodi di `Objects`.

## Sintassi a confronto

```csharp
// C#
string? citta = utente?.Indirizzo?.Citta ?? "n/d";
if (dict.TryGetValue(k, out var v)) { ... }
var primo = lista.FirstOrDefault();
```

```java
// Java
String citta = Optional.ofNullable(utente)
        .map(Utente::indirizzo)
        .map(Indirizzo::citta)
        .orElse("n/d");
Optional<Posto> primo = lista.stream().findFirst();   // mai null: Optional.empty() se la lista è vuota
```

**Metodi di `Optional<T>`**

| Metodo | Cosa fa |
| --- | --- |
| `Optional.of(x)` | crea; lancia NPE se `x` è `null` |
| `Optional.ofNullable(x)` | crea; vuoto se `x` è `null` |
| `Optional.empty()` | vuoto |
| `isPresent()` / `isEmpty()` | controllo (da usare poco) |
| `get()` | **sconsigliato**: lancia se vuoto |
| `orElse(v)` | valore di default (calcolato **sempre**) |
| `orElseGet(() -> v)` | default calcolato **solo se serve** |
| `orElseThrow(() -> new X())` | lancia la tua eccezione se vuoto |
| `map`, `flatMap`, `filter` | trasformazioni a catena |
| `ifPresent`, `ifPresentOrElse` | azione se presente (o alternativa) |

**Dove usare `Optional`**: come tipo di **ritorno** dei metodi che possono non trovare nulla (`findById`). **Non** per campi di una classe, parametri di metodo, né elementi di collezioni.

## Esercizi

Ogni esercizio segue lo stesso schema: **Obiettivo**, **Prima di iniziare** (il concetto in breve), **Cosa fare** (passi in ordine), **Cosa osservare**, **Verifica** (checklist) e **Rifletti** (domande da colloquio).

### Es0801 · La `NullPointerException` `[L1]`
**File:** `Es0801_NullPointer.java`

**Obiettivo:** riconoscere le situazioni in cui compare un `NullPointerException` e leggere i messaggi utili del Java moderno.

**Prima di iniziare:** in Java ogni tipo riferimento può essere `null`; non esiste (di default) un tipo "non nullable" come in C# con i *nullable reference types*. Dalle versioni recenti il messaggio dell'eccezione dice **quale** espressione era nulla (*helpful NPE messages*).

**Cosa fare:**
1. Crea una classe `Evento` con un campo `Sede sede`; `Sede` ha un campo `String citta`.
2. Nel `main` crea un `Evento` senza sede e prova `evento.sede.citta.length()`: leggi il messaggio.
3. Provoca un NPE in altri quattro modi: metodo su `String null`; unboxing di un `Integer` nullo in `int`; `switch` su una `String` nulla; `throw null`.
4. Per ognuno copia nel commento il messaggio dell'eccezione.
5. Aggiungi `-XX:+ShowCodeDetailsInExceptionMessages` se non fosse attivo e osserva la differenza (da Java 15 è attivo di default).
6. Scrivi 3 righe: come ti protegge un controllo con `if` e perché non basta.

**Cosa osservare:** il messaggio nomina la variabile o il metodo che ha restituito `null`.

**Verifica:**
- [ ] 5 NPE provocati e annotati.
- [ ] Sai quale parte dell'espressione era nulla in ciascuno.

**Rifletti:** perché si dice che `null` è "l'errore da un miliardo di dollari"? Cosa fa C# di diverso?

### Es0802 · Creare e consumare un `Optional` `[L1]`
**File:** `Es0802_Optional.java`

**Obiettivo:** conoscere le tre fabbriche di `Optional` e i principali modi di usarlo.

**Prima di iniziare:** `Optional<T>` è un contenitore che può avere un valore o essere vuoto. Si crea con `Optional.of(x)` (x non nullo, altrimenti NPE), `Optional.ofNullable(x)` (x può essere nullo) e `Optional.empty()`.

**Cosa fare:**
1. Crea tre `Optional<String>`: uno con `of`, uno con `ofNullable(null)`, uno con `empty()`.
2. Prova `Optional.of(null)` e osserva l'eccezione.
3. Per ciascuno usa: `isPresent`, `isEmpty`, `get` (prova `get` su un vuoto e leggi l'eccezione), `orElse("n/d")`, `orElseThrow()`, `ifPresent(System.out::println)`, `ifPresentOrElse(...)`.
4. Usa `map(String::toUpperCase)` e `filter(s -> s.length() > 3)` per concatenare trasformazioni.
5. Scrivi `static Optional<Integer> parseInt(String s)` che restituisce vuoto se il testo non è un numero (usa `try/catch`).
6. Prova `parseInt` con tre valori diversi.

**Cosa osservare:** `map` su un `Optional` vuoto non fa nulla e non lancia eccezioni.

**Verifica:**
- [ ] Hai provato tutte le operazioni del punto 3.
- [ ] `parseInt` funziona con tre input.

**Rifletti:** perché `get()` senza controllo è considerato una cattiva abitudine? (Cerca `orElseThrow` come alternativa.)

### Es0803 · Repository che restituisce `Optional` `[L2]` · *progetto*
**File:** `Es0803_RepositoryOptional.java`

**Obiettivo:** progettare un metodo di ricerca che dichiara "potrebbe non esserci" nel tipo restituito.

**Prima di iniziare:** un metodo che restituisce `null` obbliga il chiamante a ricordarsi di controllare. Uno che restituisce `Optional` lo obbliga a decidere. Spring Data usa esattamente `Optional<T> findById(ID)`.

**Cosa fare:**
1. Crea `record Evento(long id, String nome)` e `EventoRepository` con una `Map<Long, Evento>`.
2. Scrivi `Optional<Evento> trovaPerId(long id)`.
3. Scrivi `Optional<Evento> trovaPerNome(String nome)` con uno stream e `findFirst()`.
4. Scrivi un `EventoService` con il metodo `Evento prendi(long id)` che lancia `EventoNonTrovatoException` se manca (`orElseThrow` con un supplier).
5. Scrivi `String nomeOpure(long id, String ripiego)` con `map(...).orElse(...)`.
6. Scrivi `void stampaSeEsiste(long id)` con `ifPresent`.
7. Scrivi `List<Evento> trovaPiu(List<Long> ids)` che restituisce solo quelli trovati (suggerimento: `Optional::stream`).

**Cosa osservare:** il servizio non contiene nessun `if (x == null)`.

**Verifica:**
- [ ] Nessun `null` restituito o confrontato nel codice.
- [ ] `trovaPiu` ignora gli ID mancanti.

**Rifletti:** in che punto dell'applicazione trasformi "assente" in errore HTTP 404? Perché lì e non nel repository?

### Es0804 · Da `?.` e `??` a `Optional` `[L2]`
**File:** `Es0804_DaCSharp.java`

**Obiettivo:** tradurre gli operatori null-safe di C# nell'equivalente Java.

**Prima di iniziare:** Java non ha `?.` né `??`. Si usano `Optional` (con `map`, `flatMap`, `orElse`), il ternario o `Objects.requireNonNullElse`.

**Cosa fare:**
1. Con `Evento`, `Sede`, `Indirizzo` (ognuno può essere `null`), scrivi in un commento il C# `evento?.Sede?.Indirizzo?.Citta ?? "n/d"` .
2. Scrivi la traduzione Java con una catena di `if` annidati.
3. Poi con una catena di `Optional.ofNullable(...).map(...).map(...).orElse("n/d")`.
4. Poi con `Objects.requireNonNullElse(valore, "n/d")` per un solo livello.
5. Scrivi un metodo in cui uno dei passaggi restituisce già un `Optional` (es. `Sede.indirizzo()`) e usa `flatMap`.
6. Prova le versioni con tre oggetti: tutto presente, sede mancante, città mancante.

**Cosa osservare:** `map` e `flatMap`: quando serve l'uno e quando l'altro.

**Verifica:**
- [ ] Tre versioni equivalenti.
- [ ] Hai usato `flatMap` almeno una volta e sai perché.

**Rifletti:** la catena di `map` è sempre più leggibile degli `if`? Quando preferiresti gli `if`?

### Es0805 · `orElse` contro `orElseGet` `[L3]`
**File:** `Es0805_OrElse.java`

**Obiettivo:** scoprire che `orElse` valuta sempre il suo argomento, anche quando non serve.

**Prima di iniziare:** `orElse(valore)` riceve un valore già calcolato; `orElseGet(supplier)` riceve una funzione chiamata **solo se** l'`Optional` è vuoto. Se il ripiego è costoso (database, rete) la differenza è enorme.

**Cosa fare:**
1. Scrivi `static String ripiegoCostoso()` che stampa `"calcolo ripiego"` e restituisce `"X"`.
2. Con un `Optional` **pieno** chiama `orElse(ripiegoCostoso())`: quante volte compare la stampa?
3. Fai lo stesso con `orElseGet(Classe::ripiegoCostoso)`: quante volte?
4. Ripeti entrambi con un `Optional` vuoto.
5. Scrivi 3 righe: in che caso la differenza conta (chiamate a database, creazioni di oggetti, effetti collaterali)?
6. Confronta con `orElseThrow(() -> new ...)`: l'eccezione viene creata sempre o solo se serve?

**Cosa osservare:** al punto 2 la stampa compare anche se il valore c'era.

**Verifica:**
- [ ] Quattro esecuzioni con conteggio delle stampe.
- [ ] Regola scritta: "se il ripiego costa, usa `orElseGet`".

**Rifletti:** che cosa è un *supplier* e dove altro lo incontrerai? (`Map.computeIfAbsent`, `CompletableFuture.supplyAsync`.)

### Es0806 · Riscrivere il brutto `[L2]`
**File:** `Es0806_Riscrivi.java`

**Obiettivo:** riconoscere e correggere usi sbagliati di `Optional`.

**Prima di iniziare:** il modo classico di sprecare `Optional` è usarlo come un `null` con più passaggi: `if (opt.isPresent()) { x = opt.get(); ... }`.

**Cosa fare:**
1. Scrivi quattro metodi "brutti":
   - uno con `if (opt.isPresent()) return opt.get(); else return "n/d";`
   - uno con `if (opt.isPresent()) { stampa(opt.get()); }`
   - uno con `opt.isPresent() ? opt.get().length() : 0`
   - uno con un `if` annidato che controlla due `Optional` insieme
2. Riscrivi ciascuno nella forma più idiomatica (rispettivamente `orElse`, `ifPresent`, `map(...).orElse(0)`, e una catena di `flatMap`/`map`).
3. Verifica che le coppie "brutto/bello" diano lo stesso risultato su 3 input.
4. Scrivi anche un caso in cui il brutto è **meglio** (per esempio quando devi fare molte cose diverse nei due rami) e spiega perché.

**Cosa osservare:** le versioni nuove sono più corte, ma non sempre più chiare.

**Verifica:**
- [ ] 4 coppie equivalenti.
- [ ] Un esempio in cui `if` resta preferibile.

**Rifletti:** cosa significa "non usare `Optional` come argomento di un metodo"? (Es0807.)

### Es0807 · Dove NON usare `Optional` `[L1]`
**File:** `Es0807_OptionalNo.java`

**Obiettivo:** conoscere i casi in cui `Optional` è sconsigliato.

**Prima di iniziare:** `Optional` è stato pensato **solo come tipo di ritorno** di un metodo. Non è un tipo per campi, parametri, elementi di collezioni o DTO serializzati.

**Cosa fare:**
1. Scrivi una classe con un campo `Optional<String> note`. Cosa succede alla serializzazione e all'uguaglianza? (Spiega in un commento perché non è `Serializable`.)
2. Scrivi un metodo con un parametro `Optional<String> filtro`. Mostra come il chiamante deve scrivere `Optional.empty()` e perché un overload o un `null` esplicito sono migliori.
3. Scrivi `List<Optional<String>>` e spiega perché è meglio filtrare prima.
4. Scrivi `Optional<List<String>>` e spiega perché è meglio restituire una lista vuota.
5. Scrivi `Optional<Integer>` e cerca `OptionalInt`: perché esiste?
6. Compila una tabella a 2 colonne: "dove sì" / "dove no".

**Cosa osservare:** il tipo di ritorno `Optional<List<…>>` è quasi sempre un segnale di cattivo design.

**Verifica:**
- [ ] 5 esempi di uso sbagliato con motivazione.
- [ ] Tabella "sì/no" completa.

**Rifletti:** cosa restituisce Spring Data per "nessun risultato" quando il metodo ritorna una lista? (Lista vuota, mai `null`.)

### Es0808 · La classe `Objects` `[L1]`
**File:** `Es0808_Objects.java`

**Obiettivo:** conoscere le funzioni di utilità null-safe di `java.util.Objects`.

**Prima di iniziare:** `Objects` contiene metodi statici che accettano `null` senza problemi: `equals`, `hash`, `toString`, `isNull`, `nonNull`, `requireNonNull`, `requireNonNullElse`, `compare`.

**Cosa fare:**
1. Confronta `a.equals(b)` e `Objects.equals(a, b)` quando `a` è `null`.
2. Scrivi una classe `Posto` con `equals` e `hashCode` che usano `Objects.equals` e `Objects.hash`.
3. Stampa `Objects.toString(null)` e `Objects.toString(null, "nessuno")`.
4. Filtra una lista con nulli usando `Objects::nonNull`.
5. Usa `requireNonNullElse(valore, "default")` e `requireNonNullElseGet(valore, supplier)`.
6. Usa `Objects.compare(a, b, comparator)` per confrontare due stringhe senza dimenticare i nulli.

**Cosa osservare:** `Objects.hash(a, b)` accetta campi nulli.

**Verifica:**
- [ ] Ogni metodo provato con e senza `null`.
- [ ] `equals`/`hashCode` coerenti su due oggetti uguali.

**Rifletti:** cosa genera automaticamente un `record` rispetto al tuo `equals` scritto a mano?

### Es0809 · Unboxing nascosto `[L2]`
**File:** `Es0809_Unboxing.java`

**Obiettivo:** scoprire gli NPE che nascono da conversioni automatiche tra wrapper e primitivi.

**Prima di iniziare:** un `Integer` può essere `null`, un `int` no. Quando il compilatore converte automaticamente (*unboxing*) un wrapper nullo in primitivo, scatta un NPE **senza** che nel codice compaia alcun punto ovvio.

**Cosa fare:**
1. Crea una `Map<String, Integer>` e leggi un valore mancante in un `int`: `int n = mappa.get("assente");` e osserva l'errore.
2. Scrivi un ternario "misto": `Integer x = cond ? 1 : null;` va bene? E `int y = cond ? 1 : null;`? E `Integer z = cond ? 1 : (Integer) null;`?
3. Scrivi un confronto `Integer a = 1000, b = 1000; a == b` (di nuovo la cache -128..127) e `a.equals(b)`.
4. Usa un `Boolean` nullo in un `if`.
5. Scrivi un contatore `Map<String, Integer>` e incrementalo in due modi: con `get/put` e con `merge(k, 1, Integer::sum)`.
6. Correggi ciascun caso con `getOrDefault`, `Objects.requireNonNullElse` o `merge`.

**Cosa osservare:** il ternario del punto 2 dà NPE solo nella forma con `int`.

**Verifica:**
- [ ] 4 NPE provocati e spiegati.
- [ ] Contatore con `merge`.

**Rifletti:** come eviti l'unboxing nei cicli sensibili alle prestazioni? (Stream primitivi: `IntStream`.)


## Cosa devi aver capito

- [ ] `Optional` è un tipo di ritorno, non un campo né un parametro.
- [ ] Mai `isPresent()` seguito da `get()`: usa `map`, `orElse`, `orElseThrow`.
- [ ] `orElse` valuta sempre il default, `orElseGet` solo se serve.
- [ ] L'*unboxing* di un wrapper nullo lancia NPE (`Integer`, `Long`, `Boolean`...).
- [ ] `Objects` per i controlli `null`-safe.
