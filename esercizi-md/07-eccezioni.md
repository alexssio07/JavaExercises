# 07 · Eccezioni

Obiettivo: capire la differenza tra eccezioni *checked* e *unchecked* (la novità più grande rispetto a C#), usare `try-with-resources` e scrivere eccezioni di dominio.

## Sintassi a confronto

```csharp
// C#
try { Esegui(); }
catch (FormatException ex) when (ex.Message.Contains("x")) { throw; }
catch (Exception ex) { throw new InvalidOperationException("errore", ex); }
finally { Pulisci(); }

using var r = new StreamReader(path);
```

```java
// Java
try { esegui(); }
catch (NumberFormatException | IllegalStateException e) { throw e; }    // multi-catch, niente "when"
catch (Exception e) { throw new IllegalStateException("errore", e); }   // causa come secondo parametro
finally { pulisci(); }

try (var r = Files.newBufferedReader(path)) { ... }    // chiusura automatica
```

**Albero delle eccezioni**

```text
Throwable
├── Error                  (OutOfMemoryError, StackOverflowError): non si cattura
└── Exception              ← checked: il compilatore ti obbliga a gestirle
    ├── IOException, SQLException, InterruptedException ...
    └── RuntimeException   ← unchecked: nessun obbligo
        ├── IllegalArgumentException, IllegalStateException
        ├── NullPointerException, ClassCastException
        └── IndexOutOfBoundsException, NoSuchElementException ...
```

**Un metodo che può lanciare una checked exception** deve dichiararla con `throws` oppure catturarla.

| C# | Java |
| --- | --- |
| `ArgumentException` | `IllegalArgumentException` |
| `ArgumentNullException` | `NullPointerException` (con `Objects.requireNonNull`) |
| `InvalidOperationException` | `IllegalStateException` |
| `NotImplementedException`, `NotSupportedException` | `UnsupportedOperationException` |
| `KeyNotFoundException` | `NoSuchElementException` |
| `IndexOutOfRangeException` | `ArrayIndexOutOfBoundsException` / `IndexOutOfBoundsException` |
| `FormatException` | `NumberFormatException` |
| `InvalidCastException` | `ClassCastException` |
| `DivideByZeroException` | `ArithmeticException` (solo per gli interi) |
| `IOException` | `IOException` (**checked**) |
| `OperationCanceledException` | `InterruptedException` (**checked**) |

## Esercizi

Ogni esercizio segue lo stesso schema: **Obiettivo**, **Prima di iniziare** (il concetto in breve), **Cosa fare** (passi in ordine), **Cosa osservare**, **Verifica** (checklist) e **Rifletti** (domande da colloquio).

### Es0701 · Provocale tutte `[L1]`
**File:** `Es0701_ProvocaEccezioni.java`

**Obiettivo:** riconoscere a colpo d'occhio le eccezioni più comuni leggendo lo *stack trace*.

**Prima di iniziare:** quando un'eccezione non è gestita, la JVM stampa il tipo, il messaggio e lo *stack trace*: l'elenco dei metodi attraversati, dal punto in cui è stata lanciata fino al `main`. La prima riga con il nome di una tua classe è quasi sempre quella da guardare.

**Cosa fare:**
1. Scrivi un metodo per ciascuno di questi errori, **senza try/catch**, e chiamali uno alla volta dal `main` (commenta gli altri):
   - `NullPointerException`: chiama `.length()` su una `String` a `null`.
   - `ArrayIndexOutOfBoundsException`: leggi l'indice 5 di un array da 3 elementi.
   - `StringIndexOutOfBoundsException`: `"abc".substring(5)`.
   - `ArithmeticException`: dividi un `int` per zero.
   - `NumberFormatException`: `Integer.parseInt("12a")`.
   - `ClassCastException`: castare un `Object` che contiene una `String` a `Integer`.
   - `IllegalArgumentException`: lanciala tu con `throw new ...` e un messaggio.
   - `UnsupportedOperationException`: aggiungi un elemento a `List.of(1, 2)`.
   - `ArrayStoreException` (curiosità): `Object[] a = new String[1]; a[0] = 1;`.
2. Per ognuna copia nel commento la **prima riga** dell'output e il numero di riga indicato per la tua classe.
3. Scrivi accanto un'etichetta: "bug mio" o "dati esterni sbagliati".

**Cosa osservare:** il messaggio di `ArrayIndexOutOfBoundsException` contiene l'indice e la lunghezza; quello di `NumberFormatException` contiene il testo errato.

**Verifica:**
- [ ] Almeno 8 eccezioni provocate e annotate.
- [ ] Sai trovare la riga della tua classe nello stack trace.

**Rifletti:** quali di queste in C# sono `NullReferenceException`, `IndexOutOfRangeException`, `FormatException`, `InvalidCastException`, `NotSupportedException`?

### Es0702 · Checked: le tre strade `[L2]`
**File:** `Es0702_Checked.java`

**Obiettivo:** gestire un'eccezione *checked* in tutti i modi legittimi.

**Prima di iniziare:** le eccezioni *checked* (es. `IOException`) sono controllate dal compilatore: o le gestisci con `try/catch`, o le dichiari con `throws` nella firma, o le avvolgi in un'eccezione *unchecked*. In C# non esiste nulla di simile.

**Cosa fare:**
1. Scrivi `static String leggi(String percorso) throws IOException` che chiama `Files.readString(Path.of(percorso))`.
2. Chiamala dal `main` **senza** gestire nulla e leggi l'errore del compilatore.
3. **Strada 1**: gestiscila con `try/catch` e restituisci un valore di ripiego (`""`).
4. **Strada 2**: dichiara `throws IOException` anche sul `main`.
5. **Strada 3**: scrivi `leggiOUnchecked(String percorso)` che cattura `IOException` e lancia `UncheckedIOException` (passando la causa).
6. Chiama le tre versioni con un file che non esiste e osserva le differenze di output.
7. Scrivi in 3 righe quando useresti ciascuna.

**Cosa osservare:** la strada 3 permette di usare il metodo dentro una lambda (vedi Es0709).

**Verifica:**
- [ ] Hai letto l'errore "unreported exception".
- [ ] Le tre versioni compilano e le hai provate con un file mancante.

**Rifletti:** perché Spring usa quasi solo eccezioni *unchecked*? Qual è l'argomento a favore delle *checked*?

### Es0703 · Flusso di `try/catch/finally` `[L2]`
**File:** `Es0703_Flusso.java`

**Obiettivo:** prevedere l'ordine esatto delle stampe in presenza di `finally`, anche con `return`.

**Prima di iniziare:** il blocco `finally` viene eseguito **sempre**, anche se il `try` fa `return` o se il `catch` rilancia. Un `return` dentro `finally` sostituisce quello del `try` (è una pessima idea, ma conoscerla evita sorprese).

**Cosa fare:**
1. Scrivi `static int prova(int x)` con `try { stampa "try"; se x == 0 lancia IllegalArgumentException; se x == 1 return 10; } catch (IllegalArgumentException e) { stampa "catch"; return 20; } finally { stampa "finally"; } stampa "dopo"; return 30;`.
2. **Prima di eseguire**, scrivi in un commento l'output completo (stampe e valore restituito) per `prova(0)`, `prova(1)` e `prova(2)`.
3. Esegui e confronta.
4. Aggiungi `return 99;` dentro `finally`: prevedi di nuovo e confronta.
5. Scrivi un metodo in cui il `finally` lancia a sua volta un'eccezione: quale delle due vedi?
6. Rimuovi il `catch` mantenendo `try/finally`: cosa succede a `prova(0)`?

**Cosa osservare:** al punto 4 il `return 99` "vince" su tutto e può nascondere un'eccezione.

**Verifica:**
- [ ] Tre previsioni scritte prima di eseguire.
- [ ] Hai spiegato il caso del `return` in `finally`.

**Rifletti:** quando serve davvero un `finally` oggi? (Pensa a `try-with-resources` nel prossimo esercizio.)

### Es0704 · `try-with-resources` `[L2]`
**File:** `Es0704_TryWithResources.java`

**Obiettivo:** chiudere automaticamente le risorse, in ordine inverso, anche in caso di errore.

**Prima di iniziare:** qualsiasi classe che implementa `AutoCloseable` può stare tra le parentesi di `try (...)`. Al termine del blocco (normale o con eccezione) viene chiamato `close()` in ordine **inverso** rispetto all'apertura. È l'equivalente di `using` in C#.

**Cosa fare:**
1. Crea `class Risorsa implements AutoCloseable` con un nome nel costruttore; il costruttore stampa `"apro X"`, `close()` stampa `"chiudo X"`.
2. Scrivi `try (Risorsa a = new Risorsa("A"); Risorsa b = new Risorsa("B")) { stampa "uso"; }` e osserva l'ordine.
3. Dentro il blocco lancia un'eccezione e controlla che le due risorse vengano chiuse (in che ordine?).
4. Fai lanciare un'eccezione anche a `close()` di `A`, mentre il corpo ne ha già lanciata una: leggi `e.getSuppressed()` e stampa l'eccezione soppressa.
5. Riscrivi il tutto con `try/finally` manuale e confronta la lunghezza e la correttezza (hai gestito le soppressioni?).
6. Prova a mettere nel `try (...)` una classe che **non** implementa `AutoCloseable` e leggi l'errore.

**Cosa osservare:** l'eccezione principale è quella del corpo; quella di `close()` finisce tra le soppresse.

**Verifica:**
- [ ] Ordine di chiusura previsto correttamente.
- [ ] Hai stampato almeno una eccezione soppressa.

**Rifletti:** in Spring useresti questo costrutto con `Connection`, `Jedis`, `Stream` di file. Perché `Files.lines` va chiuso?

### Es0705 · Eccezione di dominio `[L2]` · *progetto*
**File:** `Es0705_EccezioneDominio.java`

**Obiettivo:** progettare un'eccezione di dominio con dati strutturati.

**Prima di iniziare:** un'eccezione di dominio descrive un errore di *business* (non tecnico). Conviene portare con sé i dati utili (non solo un testo) e passare la causa originale quando ne avvolge un'altra.

**Cosa fare:**
1. Crea `PostoGiaPrenotatoException extends RuntimeException` con campi `final` `long eventoId` e `String codicePosto` con i getter.
2. Il messaggio (passato a `super`) deve contenere entrambi i dati: `"Posto A12 già prenotato per l'evento 7"`.
3. Aggiungi un secondo costruttore che accetta anche una `Throwable causa`.
4. Scrivi `PrenotazioneService` con un `Set<String>` di posti occupati: `prenota(long evento, String codice)` lancia l'eccezione se il posto c'è già.
5. Nel `main` cattura l'eccezione e stampa i dati leggendoli dai getter (non dal messaggio).
6. Aggiungi `serialVersionUID`? Cerca cosa è e se serve (è un avviso del compilatore con alcune opzioni).

**Cosa osservare:** i getter permettono a chi cattura di costruire una risposta senza analizzare il testo.

**Verifica:**
- [ ] L'eccezione è `unchecked`.
- [ ] Il `main` usa i getter.

**Rifletti:** perché un `String` di errore da parsare è un cattivo contratto? Come lo userai in `@RestControllerAdvice`?

### Es0706 · Catturare e rilanciare con una causa `[L2]`
**File:** `Es0706_Causa.java`

**Obiettivo:** non perdere l'errore originale quando si traduce un'eccezione tecnica in una di dominio.

**Prima di iniziare:** ogni `Throwable` può avere una *causa* (`getCause()`). Se rilanci una nuova eccezione senza passare la causa, perdi lo stack trace originale e il debug in produzione diventa un incubo.

**Cosa fare:**
1. Scrivi `caricaConfigurazione()` che lancia `IOException("file mancante")`.
2. Scrivi `avvia()` che chiama `caricaConfigurazione()` e, se fallisce, lancia `IllegalStateException("Impossibile avviare")` **senza** passare la causa. Stampa lo stack trace.
3. Correggi passando la causa come secondo argomento. Stampa di nuovo lo stack trace e cerca la riga che inizia con `Caused by:`.
4. Scrivi un ciclo che risale la catena `while (e != null) { stampa; e = e.getCause(); }`.
5. Aggiungi un terzo livello (`ServizioException`) e verifica la catena a tre elementi.
6. Prova il pattern sbagliato di "log e rilancio" (`catch (e) { log(e); throw e; }`) e scrivi perché genera log duplicati.

**Cosa osservare:** la riga `Caused by:` con "... N more".

**Verifica:**
- [ ] Hai visto lo stack trace con e senza causa.
- [ ] Il ciclo stampa tutta la catena.

**Rifletti:** dove si dovrebbe fare il *log* di un errore in un'applicazione a livelli? (Una volta sola, al confine.)

### Es0707 · Multi-catch e ordine `[L1]`
**File:** `Es0707_MultiCatch.java`

**Obiettivo:** combinare più tipi in un `catch` e rispettare le regole sull'ordine.

**Prima di iniziare:** `catch (A | B e)` gestisce due tipi nello stesso modo; i due tipi non possono essere uno sottotipo dell'altro. In una sequenza di `catch` il tipo più specifico va **prima**.

**Cosa fare:**
1. Scrivi un metodo che, in base a un parametro, lancia `NumberFormatException`, `ArithmeticException` o `NullPointerException`.
2. Gestiscili con un solo `catch (NumberFormatException | ArithmeticException e)` e un `catch` separato per `NullPointerException`.
3. Dentro il multi-catch prova a riassegnare `e`: leggi l'errore (è implicitamente `final`).
4. Prova `catch (RuntimeException | IllegalArgumentException e)`: leggi l'errore.
5. Metti `catch (Exception e)` **prima** di un `catch` specifico e leggi l'errore.
6. Metti `catch (IOException e)` su un blocco `try` che non può lanciarla: cosa dice il compilatore?

**Cosa osservare:** tre errori di compilazione diversi legati ai `catch`.

**Verifica:**
- [ ] Hai annotato i tre messaggi.
- [ ] Hai capito perché `IOException` non catturabile senza `throw` possibile.

**Rifletti:** quando conviene un `catch (Exception e)` generico? E quando è un'anti-pattern?

### Es0708 · Validare i parametri `[L1]`
**File:** `Es0708_Validazione.java`

**Obiettivo:** fallire subito e con un messaggio utile, con gli strumenti standard.

**Prima di iniziare:** `Objects.requireNonNull(x, "messaggio")` lancia `NullPointerException` con il messaggio; `IllegalArgumentException` è per valori non validi; `IllegalStateException` è per un oggetto in uno stato non adatto all'operazione. Si valida all'ingresso del metodo (*fail fast*).

**Cosa fare:**
1. Scrivi `class Prenotazione` con un costruttore `(String utente, int posti)`: utente non nullo (`requireNonNull`), posti tra 1 e 6 (altrimenti `IllegalArgumentException` che riporta il valore).
2. Aggiungi `conferma()` che lancia `IllegalStateException` se è già confermata.
3. Aggiungi `annulla()` che lancia `IllegalStateException` se non è confermata.
4. Nel `main` prova 5 chiamate sbagliate e stampa il messaggio di ciascuna eccezione.
5. Scrivi un piccolo metodo `static void controlla(boolean condizione, String messaggio)` e usalo al posto degli `if`.
6. Aggiungi l'asserzione `assert posti > 0` e vedi cosa succede con e senza l'opzione `-ea`.

**Cosa osservare:** i messaggi riportano il valore ricevuto, non solo "valore non valido".

**Verifica:**
- [ ] I tre tipi di eccezione sono usati nel modo giusto.
- [ ] Hai provato `assert` con e senza `-ea`.

**Rifletti:** quando conviene un'eccezione e quando un risultato di errore (es. `Optional`)? In Spring c'è anche `@Valid`: cosa farà al posto tuo?

### Es0709 · Checked e lambda `[L3]`
**File:** `Es0709_CheckedLambda.java`

**Obiettivo:** capire perché le eccezioni *checked* "non entrano" negli stream e come aggirare il problema.

**Prima di iniziare:** le interfacce funzionali standard (`Function`, `Consumer`, ...) non dichiarano `throws`. Quindi dentro una lambda non puoi lasciar passare una *checked*: devi gestirla lì o avvolgerla.

**Cosa fare:**
1. Prepara una lista di percorsi (alcuni inesistenti).
2. Scrivi `lista.stream().map(p -> Files.readString(Path.of(p))).toList()` e leggi l'errore.
3. Risolvi con un `try/catch` dentro la lambda che avvolge in `UncheckedIOException`. Come viene leggibile?
4. Estrai un metodo `static String leggiOUnchecked(String p)` e usa `map(Classe::leggiOUnchecked)`.
5. Scrivi un'interfaccia funzionale tua `FunzioneConEccezione<T, R> { R applica(T t) throws Exception; }` e un metodo `static <T, R> Function<T, R> avvolgi(FunzioneConEccezione<T, R> f)`.
6. Riscrivi lo stream con `avvolgi(...)`.
7. Cosa succede ai file mancanti? Gestisci il caso per ottenere comunque i file leggibili (suggerimento: restituire `Optional` o filtrare).

**Cosa osservare:** l'adattatore del punto 5 è un classico che incontrerai nel codice reale.

**Verifica:**
- [ ] Tre soluzioni funzionanti.
- [ ] Hai gestito i file mancanti senza interrompere lo stream.

**Rifletti:** è un limite del linguaggio o una scelta? Quali librerie risolvono il problema (es. Vavr)?

### Es0710 · Traduttore di errori in stile `@RestControllerAdvice` `[L3]` · *progetto*
**File:** `Es0710_TraduttoreErrori.java`

**Obiettivo:** simulare in modo semplice la trasformazione centrale delle eccezioni in risposte HTTP.

**Prima di iniziare:** in Spring un solo componente (`@RestControllerAdvice`) intercetta le eccezioni e le trasforma in risposte con codice di stato e corpo JSON. Qui lo simuli con una mappa di funzioni.

**Cosa fare:**
1. Usa le eccezioni dell'Es0511: `PostoGiaOccupatoException`, `HoldScadutoException`, `EventoNonTrovatoException`, più `IllegalArgumentException`.
2. Crea `record Risposta(int stato, String codice, String messaggio)`.
3. Scrivi `static Risposta traduci(Throwable t)` che usa uno `switch` con pattern matching per tipo e restituisce: 409 per posto occupato, 410 per hold scaduto, 404 per evento non trovato, 400 per argomenti non validi, 500 per qualsiasi altra (con messaggio generico, **mai** il dettaglio interno).
4. Scrivi `static Risposta esegui(Callable<?> azione)` che esegue l'azione e traduce l'eccezione con `traduci`.
5. Prova con 5 azioni diverse, di cui una che lancia `NullPointerException`.
6. Controlla che per il 500 non venga mai esposto lo stack trace né il messaggio originale.

**Cosa osservare:** tutta la logica di traduzione è in un posto solo.

**Verifica:**
- [ ] 5 casi con codici corretti.
- [ ] Il caso 500 non espone dettagli.

**Rifletti:** perché non è sicuro mostrare lo stack trace al client? Dove metteresti il log completo?

### Es0711 · `InterruptedException` `[L3]`
**File:** `Es0711_Interrupted.java`

**Obiettivo:** capire come si interrompe un thread in modo cooperativo e come comportarsi quando si cattura `InterruptedException`.

**Prima di iniziare:** un thread non si "uccide": gli si chiede di fermarsi con `interrupt()`. Se il thread è in attesa (`sleep`, `wait`, `join`) riceve `InterruptedException`; altrimenti trova il flag `isInterrupted()` impostato. Catturare l'eccezione e ignorarla è un errore: chi ha chiesto lo stop non viene rispettato.

**Cosa fare:**
1. Crea un thread che ripete `Thread.sleep(500)` stampando un contatore.
2. Dal `main` aspetta 2 secondi e chiama `interrupt()`. Il thread cattura `InterruptedException` e termina in modo pulito.
3. Osserva: dopo la cattura il flag di interruzione è `false`.
4. Scrivi una seconda versione in cui il thread **non** dorme ma fa un calcolo in ciclo: controlla `Thread.currentThread().isInterrupted()` ad ogni iterazione.
5. Scrivi una terza versione sbagliata che cattura e ignora l'eccezione, e osserva che il thread non si ferma.
6. Correggi la terza versione ripristinando il flag con `Thread.currentThread().interrupt()` nel `catch`.

**Cosa osservare:** differenza tra interrompere un thread che dorme e uno che calcola.

**Verifica:**
- [ ] Thread fermato in modo pulito in due modi.
- [ ] Hai spiegato perché si ripristina il flag.

**Rifletti:** cosa corrisponde in C# a `CancellationToken`? Come si ferma un `ExecutorService`?


## Cosa devi aver capito

- [ ] Checked contro unchecked, e perché Spring usa quasi solo le unchecked.
- [ ] `throws`, multi-catch, `finally`, `try-with-resources`.
- [ ] Non esiste `throw;`: si usa `throw e;` e si passa la causa quando si avvolge.
- [ ] Una gerarchia di eccezioni di dominio ben progettata.
- [ ] Un eventuale `catch (Exception e)` generico va fatto solo ai confini (controller, thread principale).
