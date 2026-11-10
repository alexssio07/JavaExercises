# 06 · Interfacce

Obiettivo: usare le interfacce come fa Spring: dipendere da astrazioni, sostituire le implementazioni, passare comportamenti con le lambda. Questo file è il più importante per il progetto.

## Sintassi a confronto

```csharp
// C#
public interface IStampabile
{
    string Stampa();
    string Titolo => "Documento";        // default interface member (C# 8)
}
public class Report : IStampabile, IDisposable { ... }
```

```java
// Java: niente prefisso "I"
public interface Stampabile {
    String stampa();                                  // implicitamente public abstract
    default String titolo() { return "Documento"; }   // metodo con corpo
    static Stampabile vuoto() { return () -> ""; }    // metodo statico
    int MAX = 10;                                     // un campo è implicitamente public static final
}
public class Report implements Stampabile, AutoCloseable { ... }   // più interfacce con la virgola
```

**Regole**

- Una classe `extends` una sola classe ma `implements` quante interfacce vuole.
- Un'interfaccia può `extends` più interfacce.
- Se due interfacce hanno lo stesso metodo `default`, la classe deve ridefinirlo e può scegliere con `Stampabile.super.titolo()`.
- Un'interfaccia con **un solo metodo astratto** è *funzionale*: si può implementare con una lambda (`@FunctionalInterface`).

**Interfacce funzionali standard (equivalenti dei delegate di C#)**

| C# | Java | Metodo | Esempio |
| --- | --- | --- | --- |
| `Func<T, R>` | `Function<T, R>` | `R apply(T)` | `p -> p.prezzo()` |
| `Func<T, T>` | `UnaryOperator<T>` | `T apply(T)` | `s -> s.trim()` |
| `Func<T, T, T>` | `BinaryOperator<T>` | `T apply(T, T)` | `BigDecimal::add` |
| `Func<R>` | `Supplier<R>` | `R get()` | `() -> Instant.now()` |
| `Action<T>` | `Consumer<T>` | `void accept(T)` | `System.out::println` |
| `Predicate<T>` | `Predicate<T>` | `boolean test(T)` | `p -> p.prezzo().signum() > 0` |
| `IComparer<T>` | `Comparator<T>` | `int compare(T, T)` | `Comparator.comparing(Posto::prezzo)` |
| `IComparable<T>` | `Comparable<T>` | `int compareTo(T)` | ordinamento naturale |
| `IEnumerable<T>` | `Iterable<T>` | `Iterator<T> iterator()` | `for (var x : oggetto)` |
| `IDisposable` | `AutoCloseable` | `void close()` | `try (var r = ...)` |

**Riferimenti a metodo**: `Posto::codice` (metodo di istanza), `Integer::parseInt` (statico), `BigDecimal::new` (costruttore), `System.out::println` (istanza di un oggetto).

## Esercizi

Ogni esercizio segue lo stesso schema: **Obiettivo**, **Prima di iniziare** (il concetto in breve), **Cosa fare** (passi in ordine), **Cosa osservare**, **Verifica** (checklist) e **Rifletti** (domande da colloquio).

### Es0601 · Prima interfaccia `[L1]`
**File:** `Es0601_PrimaInterfaccia.java`

**Obiettivo:** definire e implementare un'interfaccia, usando il tipo dell'interfaccia al posto di quello concreto.

**Prima di iniziare:** un'interfaccia descrive **cosa** sa fare un oggetto, non **come**. Più classi senza legami di parentela possono implementarla, e il codice che le usa dipende solo dall'interfaccia.

**Cosa fare:**
1. Dichiara `interface Prezzabile { BigDecimal prezzo(); }`.
2. Crea `Posto` (con codice e prezzo) e `Servizio` (per esempio `"Parcheggio"`, `"Guardaroba"`, con un prezzo) che implementano `Prezzabile`, con `@Override` sul metodo.
3. Crea una `List<Prezzabile>` con due posti e due servizi.
4. Scrivi `static BigDecimal totale(List<Prezzabile> elementi)` che somma i prezzi usando **solo** il tipo `Prezzabile` (nessun `instanceof`).
5. Stampa il totale.
6. Prova a creare `new Prezzabile()` e leggi l'errore.
7. Dimentica `public` nel metodo implementato e leggi l'errore: perché serve?

**Cosa osservare:** i metodi di un'interfaccia sono implicitamente `public`, quindi l'implementazione deve esserlo.

**Verifica:**
- [ ] Il metodo `totale` non conosce `Posto` né `Servizio`.
- [ ] Hai visto gli errori dei punti 6 e 7.

**Rifletti:** quando un'interfaccia è meglio di una classe astratta? In C# come chiameresti l'interfaccia (e in Java, perché senza `I`)?

### Es0602 · Più interfacce insieme `[L2]`
**File:** `Es0602_PiuInterfacce.java`

**Obiettivo:** implementare più interfacce in una classe e rispettare il contratto di `Comparable`.

**Prima di iniziare:** `implements A, B, C` elenca più interfacce. `Comparable<T>` ha un solo metodo `int compareTo(T altro)` (negativo, zero o positivo); permette l'ordinamento "naturale" con `Collections.sort(lista)`.

**Cosa fare:**
1. Crea `interface Descrivibile { String descrizione(); }`.
2. Fai implementare a `Posto` tre interfacce: `Prezzabile`, `Descrivibile` e `Comparable<Posto>`.
3. Il `compareTo` confronta prima il prezzo (`BigDecimal.compareTo`), poi, a parità di prezzo, il codice.
4. Crea una lista di 6 posti (con prezzi ripetuti) in disordine e ordinala con `Collections.sort(lista)`; stampa.
5. Ordina anche con `lista.sort(null)` e verifica che dia lo stesso risultato.
6. Aggiungi due posti **con stesso codice e stesso prezzo ma oggetti diversi** e mettili in un `TreeSet<Posto>`: quanti ne restano? Poi in un `HashSet`: quanti ne restano?
7. Spiega la differenza tra i due insiemi in un commento.

**Cosa osservare:** il `TreeSet` usa `compareTo` per decidere se due elementi sono uguali; l'`HashSet` usa `equals`.

**Verifica:**
- [ ] Lista ordinata per prezzo e poi per codice.
- [ ] Hai spiegato il risultato dei due insiemi.

**Rifletti:** cosa succede se `compareTo` restituisce 0 ma `equals` dice `false`? Quale regola dovrebbero rispettare insieme?

### Es0603 · Metodi `default` `[L2]`
**File:** `Es0603_Default.java`

**Obiettivo:** aggiungere comportamento a un'interfaccia senza rompere chi la implementa, e risolvere i conflitti tra due interfacce.

**Prima di iniziare:** un metodo `default` ha un corpo ed è ereditato da chi implementa l'interfaccia (che può ridefinirlo). Se due interfacce hanno lo stesso `default`, la classe deve scegliere: ridefinisce il metodo e chiama una delle due versioni con `NomeInterfaccia.super.metodo()`.

**Cosa fare:**
1. In `Prezzabile` aggiungi `default boolean gratuito() { return prezzo().signum() == 0; }`.
2. Verifica che `Posto` e `Servizio` lo ereditino senza modifiche.
3. Ridefinisci `gratuito()` in `Servizio` (per esempio sempre `false`).
4. Crea due interfacce `A` e `B` con lo stesso metodo `default String saluta()` e una classe `C` che le implementa entrambe: leggi l'errore.
5. Risolvi ridefinendo `saluta()` in `C` che chiama la versione di `A` e poi quella di `B` con la sintassi `A.super.saluta()`.
6. Prova a dichiarare un metodo `default` che usa `Object.equals`: cosa dice il compilatore?

**Cosa osservare:** l'errore del punto 4 dice esplicitamente che c'è un conflitto.

**Verifica:**
- [ ] `gratuito()` funziona su un `Posto` con prezzo zero.
- [ ] Il conflitto è risolto con `A.super.saluta()`.

**Rifletti:** perché sono stati introdotti i `default`? (Cerca come è stata estesa `List` con `sort` e `forEach` in Java 8.)

### Es0604 · Metodi `static` e `private` nelle interfacce `[L2]`
**File:** `Es0604_StaticPrivate.java`

**Obiettivo:** organizzare funzioni di supporto dentro l'interfaccia.

**Prima di iniziare:** un'interfaccia può contenere metodi `static` (si chiamano con `NomeInterfaccia.metodo()`, non sono ereditati) e metodi `private` (solo per riuso interno dai metodi `default`).

**Cosa fare:**
1. In `Prezzabile` aggiungi `static Prezzabile gratis()` che restituisce un'implementazione con prezzo zero (usa una lambda).
2. Aggiungi due metodi `default`: `String prezzoFormattato()` e `String prezzoConValuta()`; entrambi devono usare lo stesso metodo `private` di appoggio.
3. Prova a chiamare il metodo privato da fuori e leggi l'errore.
4. Prova a chiamare `posto.gratis()` su un'istanza: leggi l'errore.
5. Chiama `Prezzabile.gratis().prezzoFormattato()` e verifica che funzioni.

**Cosa osservare:** i metodi `static` di interfaccia non si ereditano.

**Verifica:**
- [ ] I metodi `default` riusano il `private`.
- [ ] Hai letto i due errori.

**Rifletti:** quando un `static` in un'interfaccia è meglio di una classe di utilità separata? (Es. `List.of`, `Comparator.comparing`.)

### Es0605 · Interfaccia funzionale e lambda `[L2]`
**File:** `Es0605_Lambda.java`

**Obiettivo:** implementare la stessa interfaccia in quattro modi e arrivare alle interfacce standard.

**Prima di iniziare:** un'interfaccia con **un solo metodo astratto** è *funzionale* e si può implementare con una lambda: `prezzo -> prezzo.multiply(...)`. Un riferimento a metodo (`Classe::metodo`) è una lambda abbreviata.

**Cosa fare:**
1. Dichiara `@FunctionalInterface interface Sconto { BigDecimal applica(BigDecimal prezzo); }`.
2. Implementala in quattro modi, ciascuno per lo sconto del 10%: (a) una classe con nome `ScontoDieciPercento`; (b) una classe anonima; (c) una lambda; (d) un riferimento a metodo (scrivi un metodo statico `static BigDecimal meno10(BigDecimal p)` e usalo con `Classe::meno10`).
3. Scrivi `static List<BigDecimal> applicaATutti(List<BigDecimal> prezzi, Sconto s)` e provalo con le quattro implementazioni.
4. Sostituisci l'interfaccia `Sconto` con `UnaryOperator<BigDecimal>` (stessa firma) e riscrivi.
5. Componi due sconti con `andThen` (prima 10%, poi 2 euro fissi, non sotto zero) e applicali.
6. Aggiungi un secondo metodo astratto a `Sconto`: leggi l'errore dell'annotazione `@FunctionalInterface`.

**Cosa osservare:** le quattro versioni producono lo stesso risultato.

**Verifica:**
- [ ] Quattro implementazioni e una composizione con `andThen`.
- [ ] Hai letto l'errore del punto 6.

**Rifletti:** quando serve un'interfaccia tua e quando basta una standard? Quale corrisponde a `Func<T, R>` e quale a `Action<T>`?

### Es0606 · `Comparator` e `Comparable` `[L2]`
**File:** `Es0606_Comparator.java`

**Obiettivo:** ordinare le stesse liste in modi diversi senza modificare la classe.

**Prima di iniziare:** `Comparable` ordina "come dice la classe"; un `Comparator` ordina "come dice chi chiama". `Comparator.comparing(chiave)` costruisce un comparatore; `thenComparing` aggiunge un criterio secondario; `reversed()` inverte; `nullsFirst/nullsLast` gestiscono i valori nulli.

**Cosa fare:**
1. Crea un record `Posto(String codice, BigDecimal prezzo)` e una lista di 8 posti, con alcuni prezzi uguali.
2. Ordina: (a) per prezzo crescente; (b) per prezzo decrescente e poi per codice; (c) per fila e poi per numero (usa le funzioni dell'Es0207).
3. Inserisci tra i posti uno con `prezzo = null` (se il record lo permette senza validazione, oppure usa una classe) e ordina con `nullsLast`.
4. Ordina con `Comparator.comparing(Posto::prezzo, Comparator.reverseOrder())`.
5. Scrivi un `Comparator` con una classe anonima, poi con lambda, poi con `Comparator.comparing`.
6. Stampa ogni lista ordinata.

**Cosa osservare:** l'ordinamento è **stabile**: i posti con stesso prezzo mantengono l'ordine di partenza.

**Verifica:**
- [ ] Almeno 4 ordinamenti diversi sulla stessa lista.
- [ ] Il caso con `null` non lancia `NullPointerException`.

**Rifletti:** qual è la differenza tra ordinamento naturale (`Comparable`) ed esterno (`Comparator`)? Quando fornisci il secondo?

### Es0607 · *Strategy* per i prezzi `[L3]` · *progetto*
**File:** `Es0607_StrategyPrezzi.java`

**Obiettivo:** scegliere l'algoritmo di calcolo del prezzo a runtime tramite un'interfaccia.

**Prima di iniziare:** il pattern *Strategy* incapsula un algoritmo dietro un'interfaccia. Aggiungere un nuovo algoritmo significa aggiungere una classe, senza toccare il codice esistente. Spring lo supporta iniettando tutte le implementazioni in una `Map<String, Interfaccia>`.

**Cosa fare:**
1. Crea `record Utente(String id, int eta, boolean studente)` e `record Posto(String codice, BigDecimal prezzoBase)`.
2. Dichiara `interface PoliticaPrezzo { BigDecimal calcola(Posto posto, Utente utente); }`.
3. Implementa tre politiche: `PrezzoStandard` (prezzo base), `PrezzoRidotto` (-30% se l'utente ha meno di 18 anni o è studente), `PrezzoLastMinute` (-50% se mancano meno di 24 ore all'evento; riceve un `Clock` e la data dell'evento nel costruttore).
4. Registra le tre politiche in una `Map<String, PoliticaPrezzo>` con le chiavi `"standard"`, `"ridotto"`, `"lastminute"`.
5. Scrivi `static BigDecimal prezzoPer(String politica, Posto p, Utente u, Map<String, PoliticaPrezzo> mappa)` che sceglie la politica dal nome e lancia un'eccezione chiara se non esiste.
6. Prova tutte le politiche con tre utenti diversi.
7. Aggiungi una quarta politica `PrezzoOver65` **senza modificare le classi esistenti**: verifica che serva solo registrarla nella mappa.

**Cosa osservare:** il metodo `prezzoPer` non cambia quando aggiungi politiche.

**Verifica:**
- [ ] Quattro politiche funzionanti.
- [ ] Il `Clock` è iniettato (nessun `Instant.now()` nella politica `lastminute`).
- [ ] L'ultimo punto richiede di aggiungere solo una classe e una riga di registrazione.

**Rifletti:** in Spring questa mappa te la fornisce il container (`Map<String, PoliticaPrezzo>` iniettata, con i nomi dei bean come chiavi): ti ricorda qualcosa dei servizi registrati in C#?

### Es0608 · Repository in memoria `[L3]` · *progetto*
**File:** `Es0608_RepositoryInMemoria.java`

**Obiettivo:** separare **cosa** si fa con i dati da **dove** sono memorizzati.

**Prima di iniziare:** un *repository* è l'interfaccia verso la persistenza. Il codice che lo usa conosce solo l'interfaccia, quindi sostituire la memoria con un database richiede solo una nuova implementazione. I metodi di ricerca restituiscono `Optional` per esprimere "non trovato".

**Cosa fare:**
1. Crea `record Posto(long eventoId, String codice, BigDecimal prezzo)`.
2. Dichiara `interface PostoRepository` con: `Optional<Posto> trova(long eventoId, String codice)`, `List<Posto> perEvento(long eventoId)`, `void salva(Posto posto)`, `boolean esiste(long eventoId, String codice)`.
3. Scrivi `InMemoryPostoRepository` con una `Map<String, Posto>` interna; la chiave è la combinazione di evento e codice (decidi tu il formato e documentalo).
4. `perEvento` restituisce una lista **non modificabile** ordinata per codice.
5. Nel `main` dichiara `PostoRepository repo = new InMemoryPostoRepository();` (tipo dell'interfaccia!) e carica 10 posti su 2 eventi.
6. Prova le quattro operazioni, comprese le ricerche di posti inesistenti.
7. Scrivi `static Posto prendi(PostoRepository repo, long evento, String codice)` che lancia un'eccezione se il posto non esiste, usando `orElseThrow`.

**Cosa osservare:** il `main` non nomina mai `InMemoryPostoRepository`, tranne alla riga del `new`.

**Verifica:**
- [ ] Cambiare implementazione richiede di modificare una sola riga.
- [ ] Nessun metodo restituisce `null`.

**Rifletti:** Spring Data JPA ti genererà l'implementazione a partire dall'interfaccia: come cambia il tuo modo di pensare? Quali metodi come `trova` si generano dal nome?

### Es0609 · `HoldStore` con scadenza `[L3]` · *progetto*
**File:** `Es0609_HoldStore.java`

**Obiettivo:** costruire il cuore concorrente del progetto in memoria: tenere un posto per un tempo limitato in modo atomico.

**Prima di iniziare:** un *hold* è una prenotazione provvisoria con scadenza. Deve essere **atomico**: se due utenti tentano insieme lo stesso posto, uno solo vince. In Redis lo farai con `SET NX` e un TTL. In memoria ci arrivi con `ConcurrentHashMap` (`compute` e `putIfAbsent` sono atomici sulla singola chiave) e confrontando la scadenza con un `Clock` iniettato.

**Cosa fare:**
1. Dichiara `interface HoldStore` con: `boolean tentaHold(long eventoId, String posto, String utente, Duration durata)`, `Optional<String> chiHaIlHold(long eventoId, String posto)`, `void rilascia(long eventoId, String posto, String utente)`.
2. Crea `record Hold(String utente, Instant scadenza)`.
3. Implementa `InMemoryHoldStore(Clock clock)` con una `ConcurrentHashMap<String, Hold>`. Un hold scaduto deve essere trattato come inesistente (e quindi sovrascrivibile).
4. `tentaHold` usa un'operazione atomica della mappa (`compute`): deve restituire `true` se il posto era libero **o** l'hold esistente era scaduto, `false` se qualcun altro lo ha ancora attivo. Se lo stesso utente lo ritenta, restituisce `true` e rinnova la scadenza.
5. `rilascia` rimuove l'hold **solo** se appartiene all'utente indicato.
6. Scrivi un `Clock` modificabile per i test (una classe che estende `Clock` e ha un metodo `avanza(Duration)`).
7. Prova con asserzioni (`assert`): hold riuscito; secondo utente rifiutato; dopo `avanza(Duration.ofMinutes(11))` il secondo utente riesce; il rilascio di un altro utente non fa nulla.

**Cosa osservare:** nessun `Thread.sleep` per far scadere l'hold.

**Verifica:**
- [ ] Le 4 asserzioni del punto 7 passano (esegui con `-ea`).
- [ ] `tentaHold` non ha una sequenza "leggi, controlla, scrivi" fuori da un'operazione atomica.

**Rifletti:** questa interfaccia avrà un'implementazione Redis. Quali operazioni dovranno essere atomiche anche lì? Perché `rilascia` deve controllare il proprietario?

### Es0610 · `Iterable` e `for-each` `[L3]`
**File:** `Es0610_Iterable.java`

**Obiettivo:** rendere una tua classe utilizzabile in un `for-each`.

**Prima di iniziare:** il `for-each` funziona con qualsiasi oggetto che implementa `Iterable<T>`, cioè che restituisce un `Iterator<T>` con `hasNext()` e `next()`. In C# lo faresti con `IEnumerable` e `yield return`; in Java non c'è `yield`, scrivi l'iteratore a mano.

**Cosa fare:**
1. Crea `MappaPosti` che contiene una griglia (`Posto[][]` oppure una lista di liste).
2. Implementa `Iterable<Posto>`: `iterator()` restituisce un `Iterator<Posto>` che percorre la griglia **per file e poi per numero**, saltando le caselle vuote (`null`).
3. Implementa l'iteratore come classe interna con due indici.
4. Usa la mappa in un `for (Posto p : mappa)`.
5. Usa `StreamSupport.stream(mappa.spliterator(), false)` per ottenere uno stream e conta i posti con un filtro.
6. Fai lanciare `NoSuchElementException` a `next()` quando non ci sono altri elementi.

**Cosa osservare:** il ciclo scorre tutti i posti nell'ordine voluto, anche con caselle vuote.

**Verifica:**
- [ ] Il ciclo stampa tutti i posti presenti.
- [ ] L'uso dello stream funziona.

**Rifletti:** cosa corrisponde a `yield return` in Java? Quando conviene restituire direttamente una `List` o uno `Stream`?

### Es0611 · Interfaccia generica `[L3]` · *progetto*
**File:** `Es0611_RepositoryGenerico.java`

**Obiettivo:** scrivere a mano una versione semplificata di `JpaRepository<T, ID>`.

**Prima di iniziare:** un'interfaccia generica con due parametri di tipo (`T` per l'entità, `ID` per la chiave) descrive le operazioni comuni a qualsiasi repository. Una classe astratta può implementarle una volta sola in memoria, lasciando alle sottoclassi solo i dettagli (come estrarre l'id).

**Cosa fare:**
1. Dichiara `interface Repository<T, ID>` con `Optional<T> findById(ID id)`, `List<T> findAll()`, `T save(T entity)`, `void deleteById(ID id)`, `boolean existsById(ID id)`.
2. Scrivi `abstract class InMemoryRepository<T, ID> implements Repository<T, ID>` con una `Map<ID, T>` interna e un metodo astratto `protected abstract ID idDi(T entity)`.
3. Crea `record Evento(Long id, String nome)` e `record Posto(String codice, BigDecimal prezzo)`.
4. Scrivi `EventoRepository extends InMemoryRepository<Evento, Long>` e `PostoRepository extends InMemoryRepository<Posto, String>`: ciascuna deve avere meno di 10 righe.
5. Aggiungi in `EventoRepository` un metodo `Optional<Evento> findByNome(String nome)`.
6. Prova entrambi i repository nel `main`.

**Cosa osservare:** le due classi concrete implementano solo `idDi`.

**Verifica:**
- [ ] I due repository funzionano con tipi di chiave diversi (`Long`, `String`).
- [ ] `findByNome` usa lo stream sui valori della mappa.

**Rifletti:** in Spring Data basta dichiarare `interface EventoRepository extends JpaRepository<Evento, Long>` e `findByNome` viene generato dal nome: cosa ti aspetti che faccia al posto tuo?

### Es0612 · Classe astratta o interfaccia `[L2]`
**File:** `Es0612_Scelta.java` (con un lungo commento) oppure un file `.md` di appunti

**Obiettivo:** allenare la domanda da colloquio "interfaccia o classe astratta?" con un criterio scritto.

**Prima di iniziare:** non c'è una risposta unica, ma esistono criteri: stato condiviso, costruttori, ereditarietà multipla, evoluzione dell'API, uso con le lambda, facilità di testare.

**Cosa fare:**
1. Disegna una tabella con tre colonne (interfaccia, classe astratta, `record`) e almeno 8 righe di criteri: può avere stato? ha costruttori? si può combinare con altre? si usa con lambda? si può mockare facilmente? come evolve nel tempo? permette `private`/`protected`? fa parte di una gerarchia con `extends`?
2. Compila la tabella con "sì", "no" o una nota.
3. Per ciascun caso scegli uno strumento e motiva in una riga: `HoldStore`, `Utente` (cliente/operatore), `PoliticaPrezzo`, `EsitoHold`, `Posto`, `Clock`.
4. Scrivi la risposta che daresti in un colloquio in 30 secondi.

**Cosa osservare:** quali criteri compaiono più spesso nelle tue motivazioni.

**Verifica:**
- [ ] Tabella completa e 6 scelte motivate.
- [ ] Una risposta da 30 secondi.

**Rifletti:** perché Spring "ama" le interfacce? (Pensa ai proxy, ai mock e alle implementazioni alternative.)

### Es0613 · Test double a mano `[L2]` · *progetto*
**File:** `Es0613_FakeHoldStore.java`

**Obiettivo:** scrivere a mano gli oggetti finti che in Spring creerai con Mockito.

**Prima di iniziare:** un *test double* è un'implementazione finta di un'interfaccia usata nei test al posto di quella vera. I tipi principali sono *stub* (restituisce risposte fisse), *fake* (implementazione semplificata) e *mock* (verifica anche come è stato chiamato).

**Cosa fare:**
1. Usa l'interfaccia `HoldStore` dell'Es0609.
2. Scrivi `HoldStoreSempreRifiuta` (restituisce sempre `false` per `tentaHold`) e `HoldStoreSempreAccetta`.
3. Scrivi un piccolo `ServizioPrenotazioni` che riceve un `HoldStore` nel costruttore e ha il metodo `void tieniPosto(long evento, String posto, String utente)` che lancia `PostoGiaOccupatoException` se l'hold è rifiutato.
4. Verifica con `assert` che con il primo finto il metodo lancia l'eccezione e con il secondo no.
5. Scrivi uno `HoldStoreSpia` che registra le chiamate ricevute in una lista (utente, posto) e permette di verificarle alla fine.
6. Usa la spia per verificare che `tieniPosto` chiami `tentaHold` **una sola volta**.

**Cosa osservare:** `ServizioPrenotazioni` non è stato modificato per essere testato.

**Verifica:**
- [ ] Tre test double e tre verifiche.
- [ ] Il servizio dipende solo dall'interfaccia.

**Rifletti:** questo è ciò che farà Mockito con `when(...).thenReturn(...)` e `verify(...)`. Cosa ti risparmia?

### Es0614 · Iniezione delle dipendenze a mano `[L3]` · *progetto*
**File:** `Es0614_DIaMano.java`

**Obiettivo:** capire cosa fa il container di Spring costruendo a mano un grafo di oggetti.

**Prima di iniziare:** l'*iniezione da costruttore* passa le dipendenze come parametri del costruttore, memorizzate in campi `final`. Il punto in cui tutti gli oggetti vengono creati e collegati si chiama *composition root*; in Spring è il container, qui è il `main`.

**Cosa fare:**
1. Crea `BookingService` con tre campi `private final`: `PostoRepository`, `HoldStore`, `Clock`, assegnati dal costruttore.
2. Scrivi un metodo `String tieniPosto(long eventoId, String codice, String utente)` che: verifica che il posto esista, tenta l'hold con durata di 10 minuti, restituisce un messaggio con la scadenza calcolata dal `Clock`.
3. Nel `main` costruisci a mano `InMemoryPostoRepository`, `InMemoryHoldStore`, `Clock.systemUTC()` e passali al servizio.
4. Sostituisci `HoldStore` con il finto dell'esercizio precedente e il `Clock` con uno fisso, **senza modificare** `BookingService`.
5. Controlla che `BookingService` non contenga alcun `new` di una sua dipendenza.
6. Aggiungi una quarta dipendenza (`PoliticaPrezzo`) e conta quante classi hai dovuto modificare (suggerimento: `BookingService` e il `main`).

**Cosa osservare:** tutta la "configurazione" sta in un solo punto.

**Verifica:**
- [ ] `BookingService` funziona con le implementazioni vere e con quelle finte.
- [ ] Nessun `new` di dipendenze dentro il servizio.

**Rifletti:** il tuo `main` è esattamente ciò che fa Spring (e `ServiceCollection` in .NET). Cosa fanno `@Service`, `@Component` e `@Autowired` in confronto?


## Cosa devi aver capito

- [ ] `implements`, più interfacce, `default`, `static` e `private` nelle interfacce.
- [ ] Le interfacce funzionali standard e le lambda.
- [ ] `Comparable` e `Comparator`.
- [ ] Dipendere dall'astrazione e passare le dipendenze dal costruttore.
- [ ] Un test double è una classe che implementa la stessa interfaccia.
