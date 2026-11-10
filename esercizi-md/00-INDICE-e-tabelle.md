# Esercizi Java per chi viene da C# / .NET

Raccolta di esercizi **senza soluzioni**, pensata per essere scritta a mano in IntelliJ e ripassata nel tempo.
Ogni esercizio diventa un file `.java` tuo: nel file lasci un commento `// IMPARATO: ...` con il concetto, così il ripasso è un `grep` o una scorsa veloce.

## Come usare questi esercizi

1. **Un esercizio = un file** `.java` con un suo `main`. Nome file = nome classe (regola di Java), per esempio `Es0101_PrimoProgramma.java` contiene `public class Es0101_PrimoProgramma`.
2. **Dove metterli**: in `src/main/java/` (mai dentro `resources`). Consiglio una cartella per argomento, con il `package` uguale al percorso:
   `src/main/java/studio/stringhe/Es0201_StringPool.java` → prima riga `package studio.stringhe;`
   Se lasci i file direttamente in `src/main/java` senza cartelle, non scrivere nessun `package`.
3. **Esegui** con la freccia verde accanto al `main` (IntelliJ), oppure con `Shift+F10`.
4. **Prima prevedi, poi esegui.** Dove c'è "prevedi l'output", scrivi la previsione in un commento nel file *prima* di lanciare il programma. Le previsioni sbagliate sono la parte che insegna di più.
5. **Non copiare da internet** finché non hai provato da solo. Quando sei bloccato: leggi il messaggio del compilatore per intero, poi la documentazione ufficiale (`docs.oracle.com/en/java/javase/21/docs/api`), poi chiedi.
6. **Niente soluzioni qui dentro.** Quando hai finito un esercizio puoi farlo correggere incollando il codice.
7. **Livelli**: `[L1]` base, `[L2]` richiede di aver capito il concetto, `[L3]` sfida (può richiedere ricerca).
8. **Assert**: per verificare i risultati senza JUnit (arriva in settimana 2) usa `assert condizione : "messaggio";` e avvia il programma con l'opzione VM `-ea` (Run → Edit Configurations → Modify options → Add VM options).

## Indice dei file

| File | Argomento | Quando (piano di studio) |
| --- | --- | --- |
| [01-sintassi-e-tipi.md](01-sintassi-e-tipi.md) | Struttura, tipi, cicli, array, switch | Sett. 1, giorno 2 |
| [02-stringhe-e-uguaglianza.md](02-stringhe-e-uguaglianza.md) | `String`, `==` contro `equals`, formattazione, cache degli `Integer` | Sett. 1, giorno 2 |
| [03-record-enum-switch.md](03-record-enum-switch.md) | `record`, `enum` con dati, switch moderno, pattern matching, `sealed` | Sett. 1, giorno 2 |
| [04-classi-e-oggetti.md](04-classi-e-oggetti.md) | Classi, costruttori, `equals/hashCode`, immutabilità, Lombok | Sett. 1, giorno 3 |
| [05-ereditarieta.md](05-ereditarieta.md) | `extends`, `super`, classi astratte, polimorfismo, gerarchia di eccezioni | Sett. 1, giorno 3 |
| [06-interfacce.md](06-interfacce.md) | Interfacce, `default`, funzionali, `Comparator`, strategy, repository, DI a mano | Sett. 1, giorno 3 |
| [07-eccezioni.md](07-eccezioni.md) | Checked contro unchecked, `try-with-resources`, eccezioni di dominio | Sett. 1, giorno 5 |
| [08-optional-e-null.md](08-optional-e-null.md) | `null`, `Optional`, `Objects`, unboxing | Sett. 1, giorno 3 |
| [09-collections-e-stream.md](09-collections-e-stream.md) | Collection, `Map`, Stream API (il tuo LINQ) | Sett. 1, giorno 4 |
| [10-generics.md](10-generics.md) | Type erasure, wildcard, metodi generici | Sett. 1, giorno 5 |
| [11-denaro-date-e-tempo.md](11-denaro-date-e-tempo.md) | `BigDecimal`, `java.time`, `Clock`, `Duration` | Prima della sett. 3 |
| [12-concorrenza-base.md](12-concorrenza-base.md) | Thread, race condition, `Atomic*`, `ExecutorService`, `CompletableFuture` | Prima della sett. 3 |
| [13-mini-dominio-prenotazioni.md](13-mini-dominio-prenotazioni.md) | Versione in memoria del progetto, a passi | Sett. 1, giorno 6 |

## Tabella di traduzione dei tipi

| C# | Java | Nota |
| --- | --- | --- |
| `int`, `long`, `double`, `float`, `short`, `char` | uguali | Java non ha tipi `unsigned` (`uint`, `ulong`, `ushort`) |
| `byte` (0..255) | `short` o `int` | In Java `byte` è **con segno** (-128..127), trappola classica |
| `sbyte` | `byte` | |
| `bool` | `boolean` | |
| `string` | `String` (maiuscola) | È una classe, immutabile come in C# |
| `object` | `Object` | |
| `decimal` | `BigDecimal` | Per i soldi, mai `double`. Si crea da stringa: `new BigDecimal("10.90")` |
| `int?`, `long?` | `Integer`, `Long` | Versioni "oggetto" dei primitivi, possono essere `null` |
| `var` | `var` | Solo per variabili **locali**, non per campi o parametri |
| `const` | `static final` | Per i campi. In locale basta `final` |
| `readonly` | `final` | Il campo si assegna una volta sola |
| `List<T>` | `List<T>` (implementazione `ArrayList<T>`) | `new ArrayList<>()` |
| `Dictionary<K,V>` | `Map<K,V>` (implementazione `HashMap<K,V>`) | |
| `HashSet<T>` | `Set<T>` (implementazione `HashSet<T>`) | |
| `Queue<T>`, `Stack<T>` | `Deque<T>` (implementazione `ArrayDeque<T>`) | Usa `ArrayDeque` anche per le pile |
| `T[]` | `T[]` | `arr.length` senza parentesi |
| `x.Length` (array) / `s.Length` (stringa) / `list.Count` | `arr.length` / `s.length()` / `list.size()` | Tre sintassi diverse, trappola classica |
| `IEnumerable<T>` | `Iterable<T>`, `Collection<T>` o `Stream<T>` | Dipende dall'uso |
| `Func<T,R>` | `Function<T,R>` | Vedi file 06 per la tabella completa |
| `Action<T>` | `Consumer<T>` | |
| `Predicate<T>` | `Predicate<T>` | |
| `Tuple`, `(int, string)` | `record` | In Java non ci sono tuple |
| `Guid` | `UUID` | `UUID.randomUUID()` |
| `DateTime` | `LocalDateTime` / `Instant` | Dipende da cosa rappresenta (vedi file 11) |
| `DateOnly` | `LocalDate` | |
| `DateTimeOffset` | `OffsetDateTime` / `ZonedDateTime` | |
| `TimeSpan` | `Duration` (tempo) / `Period` (giorni, mesi) | |
| `Task<T>` | `CompletableFuture<T>` / `Future<T>` | |
| `struct` | non esiste | Usa `record` per i dati immutabili |

## Tabella di traduzione della sintassi

| Concetto | C# | Java |
| --- | --- | --- |
| Punto d'ingresso | `static void Main(string[] args)` | `public static void main(String[] args)` |
| Namespace | `namespace Studio;` | `package studio;` (minuscolo, uguale alla cartella) |
| Importare | `using System.Collections.Generic;` | `import java.util.List;` |
| Output | `Console.WriteLine(x)` | `System.out.println(x)` |
| Input | `Console.ReadLine()` | `new Scanner(System.in).nextLine()` |
| Ereditare | `class B : A` | `class B extends A` |
| Implementare | `class B : IA` | `class B implements IA` |
| Chiamare il padre | `base.Metodo()`, `: base(x)` | `super.metodo()`, `super(x)` |
| Metodi virtuali | `virtual` / `override` | **Tutti virtuali di default**; si usa `@Override` |
| Vietare l'override | `sealed override` | `final` sul metodo |
| Vietare l'ereditarietà | `sealed class` | `final class` |
| Classe statica | `static class` | non esiste: `final class` con costruttore `private` |
| Campo costante | `const int Max = 10;` | `static final int MAX = 10;` |
| Visibilità `internal` | `internal` | nessuna parola chiave (*package-private*) |
| Proprietà | `public int X { get; set; }` | campo `private` + `getX()` / `setX()` (o `record`, o Lombok) |
| Controllo tipo | `x is Posto p` | `x instanceof Posto p` |
| Cast sicuro | `x as Posto` | `instanceof` + cast (non esiste `as`) |
| Tipo come valore | `typeof(Posto)` | `Posto.class` |
| Tipo a runtime | `x.GetType()` | `x.getClass()` |
| Ciclo foreach | `foreach (var x in xs)` | `for (var x : xs)` |
| Parametri variabili | `params int[] n` | `int... n` |
| Risorse da chiudere | `using var r = ...;` | `try (var r = ...) { }` |
| Lock | `lock (o) { }` | `synchronized (o) { }` |
| Stringhe interpolate | `$"Ciao {nome}"` | `"Ciao %s".formatted(nome)` |
| Stringa multilinea | `@"..."` / `"""..."""` | text block `"""..."""` |
| Confronto stringhe | `a == b` | `a.equals(b)` |
| Null condizionale | `a?.b` | `Optional` oppure `if` esplicito |
| Null coalescing | `a ?? b` | `Objects.requireNonNullElse(a, b)` o `Optional.orElse` |
| Parametri `ref`/`out` | `ref`, `out` | non esistono: si restituisce un oggetto o un `record` |
| Rilanciare | `throw;` | `throw e;` |
| Catturare più tipi | `catch (A) when ...` | `catch (A \| B e)` (nessun filtro `when`) |
| `ToString/Equals/GetHashCode` | maiuscole | `toString/equals/hashCode` (minuscole) |
| Delegate/eventi | `Func`, `Action`, `event` | interfacce funzionali e listener (interfacce) |
| Overload di operatori | `operator +` | non esiste |
| Metodi di estensione | `this` nel primo parametro | non esistono |
| `async`/`await` | `async Task` | `CompletableFuture`, oppure thread virtuali (Java 21) |

## Convenzioni di nome

| Cosa | Convenzione | Esempio |
| --- | --- | --- |
| Package | minuscolo, nessun underscore | `com.studio.booking` |
| Classi, interfacce, record, enum | `PascalCase` | `PostoRepository` |
| **Interfacce** | **niente prefisso `I`** | `HoldStore`, non `IHoldStore` |
| Metodi e variabili | `camelCase` (anche i metodi pubblici) | `calcolaPrezzo()` |
| Costanti e valori di enum | `MAIUSCOLO_CON_UNDERSCORE` | `MAX_POSTI`, `IN_ATTESA` |
| Implementazione di interfaccia | prefisso o suffisso descrittivo | `InMemoryHoldStore`, `RedisHoldStore` |
| File | `NomeClasse.java` | una classe pubblica per file |

## Libreria standard: equivalenze utili

| C# / .NET | Java |
| --- | --- |
| `StringBuilder` | `StringBuilder` (`append`) |
| `File.ReadAllLines(path)` | `Files.readAllLines(Path.of(path))` |
| `int.Parse(s)` | `Integer.parseInt(s)` |
| `int.TryParse` | non esiste: `try/catch` su `NumberFormatException` |
| `int.MaxValue` | `Integer.MAX_VALUE` |
| `Math.Max(a, b)` | `Math.max(a, b)` |
| `Thread.Sleep(ms)` | `Thread.sleep(ms)` (lancia una checked exception) |
| `Stopwatch` | `System.nanoTime()` o `Instant` / `Duration` |
| `Random` | `Random`, oppure `ThreadLocalRandom.current()` |
| `ILogger<T>` | SLF4J `Logger` (con Logback) |
| `System.Text.Json` | Jackson `ObjectMapper` |
| `HttpClient` | `RestClient` (Spring) / `java.net.http.HttpClient` |
| xUnit / NUnit | JUnit 5 |
| Moq | Mockito |
| FluentAssertions | AssertJ |
| EF Core | Spring Data JPA + Hibernate |

## Le 12 trappole da C# a Java (con il file dove allenarle)

| # | Trappola | File |
| --- | --- | --- |
| 1 | `==` sugli oggetti confronta i riferimenti, non i valori | 02 |
| 2 | Cache degli `Integer` da -128 a 127 | 02 |
| 3 | `byte` ha il segno; non esistono i tipi `unsigned` | 01 |
| 4 | `switch` classico con *fallthrough* se dimentichi `break` | 01 |
| 5 | `split` usa le espressioni regolari | 02 |
| 6 | Stringhe immutabili: `s.toUpperCase();` da solo non fa nulla | 02 |
| 7 | I metodi sono virtuali di default; `@Override` ti protegge dai refusi | 05 |
| 8 | I campi non sono polimorfici, solo i metodi | 05 |
| 9 | Checked exception da dichiarare o catturare | 07 |
| 10 | Type erasure: nessun `new T()`, nessun `typeof(T)` | 10 |
| 11 | `BigDecimal`: `equals` considera la scala (`2.0` diverso da `2.00`) | 11 |
| 12 | `list.remove(1)` su `List<Integer>` rimuove per **indice**, non per valore | 09 |

## Cosa prepara per il progetto di prenotazione

| Esercizio | Diventa, nel progetto vero |
| --- | --- |
| 03 · macchina a stati dello `Stato` del posto | Stati `LIBERO / IN_ATTESA / VENDUTO` |
| 05 · gerarchia di eccezioni di dominio | `@RestControllerAdvice` e risposte `409`, `404` |
| 06 · `HoldStore` in memoria | Hold su Redis con `SET NX` + TTL (settimana 4) |
| 06 · `PostoRepository` in memoria | `JpaRepository` (settimana 2-3) |
| 06 · iniezione delle dipendenze a mano | `@Service` e DI di Spring |
| 11 · `Clock` e `Duration` | Scadenza dell'hold e test senza `sleep` |
| 11 · `BigDecimal` | Prezzi e `NUMERIC(10,2)` in PostgreSQL |
| 12 · race condition con 50 thread | Il test di concorrenza della settimana 3 |
| 13 · mini-dominio | Il progetto, ma senza framework: capirai cosa fa Spring al posto tuo |
