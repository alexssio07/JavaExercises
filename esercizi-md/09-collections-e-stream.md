# 09 · Collection e Stream (il tuo LINQ)

Obiettivo: usare le collezioni di Java e riscrivere le query LINQ che conosci con le Stream API.

## Collezioni a confronto

| C# | Java | Note |
| --- | --- | --- |
| `List<T>` | `List<T>` → `ArrayList<T>` | Dichiara con l'interfaccia: `List<Posto> l = new ArrayList<>();` |
| `Dictionary<K,V>` | `Map<K,V>` → `HashMap` | `LinkedHashMap` mantiene l'ordine di inserimento; `TreeMap` ordina per chiave |
| `SortedDictionary` | `TreeMap` | |
| `HashSet<T>` | `HashSet<T>` | `TreeSet` ordinato |
| `Queue<T>` | `Queue<T>` → `ArrayDeque`, `LinkedList` | `PriorityQueue` per priorità |
| `Stack<T>` | `Deque<T>` → `ArrayDeque` | `push`, `pop`, `peek` |
| `ConcurrentDictionary` | `ConcurrentHashMap` | |
| `ImmutableList` | `List.of(...)`, `List.copyOf(...)` | **Immutabili**: `add` lancia eccezione |
| `list[i]` | `list.get(i)` | |
| `list.Count` | `list.size()` | |
| `list.Add(x)` | `list.add(x)` | |
| `list.Remove(x)` | `list.remove(x)` | **Trappola con `List<Integer>`**: `remove(1)` rimuove l'indice 1 |
| `dict[k] = v` | `map.put(k, v)` | |
| `dict.TryGetValue` | `map.get(k)` (restituisce `null`) o `getOrDefault(k, d)` | |
| `dict.ContainsKey` | `map.containsKey(k)` | |
| `foreach (var kv in dict)` | `for (var e : map.entrySet())` | `e.getKey()`, `e.getValue()` |

**Metodi utili di `Map`**: `getOrDefault`, `putIfAbsent`, `computeIfAbsent`, `merge`, `entrySet`, `keySet`, `values`.

## Da LINQ a Stream

| LINQ | Stream |
| --- | --- |
| `Where(p)` | `filter(p)` |
| `Select(f)` | `map(f)` |
| `SelectMany(f)` | `flatMap(f)` |
| `OrderBy(k)` | `sorted(Comparator.comparing(k))` |
| `OrderByDescending(k)` | `sorted(Comparator.comparing(k).reversed())` |
| `ThenBy(k)` | `thenComparing(k)` |
| `GroupBy(k)` | `collect(Collectors.groupingBy(k))` |
| `ToDictionary(k, v)` | `collect(Collectors.toMap(k, v))` |
| `ToList()` | `toList()` (immutabile, Java 16) oppure `collect(Collectors.toList())` |
| `Distinct()` | `distinct()` |
| `Take(n)` / `Skip(n)` | `limit(n)` / `skip(n)` |
| `Count()` | `count()` (restituisce `long`) |
| `Sum(f)` | `mapToInt(f).sum()`; per `BigDecimal`: `map(f).reduce(BigDecimal.ZERO, BigDecimal::add)` |
| `Average(f)` | `mapToDouble(f).average()` (restituisce `OptionalDouble`) |
| `Max(f)` / `Min(f)` | `max(comparator)` / `min(comparator)` (restituiscono `Optional`) |
| `Any(p)` / `All(p)` | `anyMatch(p)` / `allMatch(p)` (anche `noneMatch`) |
| `First()` / `FirstOrDefault()` | `findFirst()` (restituisce `Optional`) |
| `Aggregate` | `reduce` |
| `string.Join(",", xs)` | `collect(Collectors.joining(","))` |
| (nessuno) | `Collectors.partitioningBy(p)` per dividere in due gruppi |

**Differenze importanti**

- Uno stream si può usare **una sola volta**.
- È *lazy*: non succede nulla finché non c'è un'operazione terminale (`collect`, `toList`, `count`, `forEach`...).
- Parte da una collezione con `.stream()`; da un array con `Arrays.stream(...)`; da un intervallo con `IntStream.range(0, n)`.
- Per i numeri esistono `IntStream`, `LongStream`, `DoubleStream` (evitano il *boxing*).

## Esercizi

Ogni esercizio segue lo stesso schema: **Obiettivo**, **Prima di iniziare** (il concetto in breve), **Cosa fare** (passi in ordine), **Cosa osservare**, **Verifica** (checklist) e **Rifletti** (domande da colloquio).

### Es0901 · `ArrayList` e la trappola di `remove` `[L1]`
**File:** `Es0901_ArrayList.java`

**Obiettivo:** usare le operazioni base di `List` e scoprire l'ambiguità di `remove` su `List<Integer>`.

**Prima di iniziare:** `List` ha due versioni di `remove`: `remove(int indice)` e `remove(Object elemento)`. Con `List<Integer>` la scelta dipende dal tipo statico dell'argomento: `int` rimuove per posizione, `Integer` rimuove per valore.

**Cosa fare:**
1. Crea `List<String> nomi = new ArrayList<>()` e aggiungi 5 nomi con `add`; inserisci uno in posizione 2 con `add(2, x)`.
2. Prova `get`, `set`, `size`, `indexOf`, `contains`, `isEmpty`, `subList(1, 3)` e stampa il risultato di ciascuno.
3. Crea `List<Integer> numeri = new ArrayList<>(List.of(10, 20, 30, 1, 2))`.
4. Prevedi e poi verifica il risultato di `numeri.remove(1)`.
5. Prevedi e poi verifica `numeri.remove(Integer.valueOf(1))`.
6. Prova `numeri.remove(10)`: elimina l'elemento 10 o la posizione 10? Cosa succede?
7. Stampa la lista con `System.out.println(numeri)` e con `String.join(", ", ...)` (cosa devi fare perché funzioni con numeri?).

**Cosa osservare:** i punti 4–6 danno risultati molto diversi pur usando "lo stesso" nome.

**Verifica:**
- [ ] Tre previsioni scritte prima.
- [ ] Hai spiegato perché `remove(10)` lancia un'eccezione.

**Rifletti:** quale nome useresti per evitare l'ambiguità? Cosa corrisponde a `RemoveAt` e `Remove` di C#?

### Es0902 · Liste immutabili `[L2]`
**File:** `Es0902_ListeImmutabili.java`

**Obiettivo:** distinguere tra liste immutabili, viste e copie difensive.

**Prima di iniziare:** `List.of(...)` e `.toList()` degli stream restituiscono liste che non si possono modificare. `Collections.unmodifiableList(l)` restituisce una **vista**: se la lista originale cambia, cambia anche la vista. `List.copyOf(l)` crea una copia immutabile indipendente.

**Cosa fare:**
1. Crea `List<String> a = List.of("x", "y")` e prova `a.add("z")`: leggi l'eccezione.
2. Prova `List.of("a", null)`: cosa succede?
3. Crea `ArrayList<String> originale` con 3 elementi e una vista `unmodifiableList(originale)`.
4. Aggiungi un elemento a `originale` e stampa la vista: cosa vedi?
5. Crea `List.copyOf(originale)`, aggiungi ancora un elemento all'originale e stampa la copia.
6. Scrivi una classe `Evento` con una `List<Posto> posti` privata e un getter. Prova tre versioni: restituisce la lista originale; restituisce la vista; restituisce la copia. Per ognuna prova a modificare la lista dall'esterno e verifica se l'oggetto interno cambia.

**Cosa osservare:** la vista non è una copia.

**Verifica:**
- [ ] Tre comportamenti verificati.
- [ ] Hai scelto una versione del getter motivando.

**Rifletti:** a cosa corrisponde `IReadOnlyList<T>` o `ImmutableList<T>` in C#? Che costo ha una copia difensiva?

### Es0903 · `ConcurrentModificationException` `[L2]`
**File:** `Es0903_ModificaDuranteIterazione.java`

**Obiettivo:** capire perché non si rimuove da una lista mentre la si scorre con `for-each`, e come si fa correttamente.

**Prima di iniziare:** gli iteratori di `ArrayList` sono *fail-fast*: se la lista viene modificata (fuori dall'iteratore) durante lo scorrimento, al `next()` successivo viene lanciata `ConcurrentModificationException`.

**Cosa fare:**
1. Crea una lista di 6 numeri e rimuovi i pari con un `for-each` che chiama `lista.remove(n)`: osserva l'eccezione.
2. Fai lo stesso su una lista di 2 elementi rimuovendo il **penultimo**: l'eccezione c'è o no? Perché?
3. Risolvi in tre modi: (a) `Iterator` con `it.remove()`; (b) `lista.removeIf(n -> n % 2 == 0)`; (c) creando una nuova lista con stream e `filter`.
4. Prova a modificare dal `for` con indice (all'indietro) e spiega perché va all'indietro.
5. Prova lo stesso problema con una `Map` (rimuovere mentre scorri `keySet()`) e risolvilo con `entrySet().removeIf(...)`.

**Cosa osservare:** il caso del punto 2 non lancia nulla: è un comportamento "fortunato", non garantito.

**Verifica:**
- [ ] Eccezione provocata e tre soluzioni funzionanti.
- [ ] Soluzione anche per la `Map`.

**Rifletti:** quale delle tre soluzioni useresti di solito? Come si comporta `CopyOnWriteArrayList`?

### Es0904 · `Map` in pratica `[L2]`
**File:** `Es0904_Map.java`

**Obiettivo:** padroneggiare i metodi moderni di `Map` per contare, raggruppare e inizializzare.

**Prima di iniziare:** oltre a `put` e `get`, `Map` offre `getOrDefault`, `putIfAbsent`, `computeIfAbsent`, `computeIfPresent`, `merge`. Servono a evitare il solito "controllo, poi inserisco".

**Cosa fare:**
1. Data la frase `"il gatto e il cane e il topo"`, conta le occorrenze di ogni parola in tre modi: `get/put` con `if`; `getOrDefault`; `merge(parola, 1, Integer::sum)`.
2. Costruisci una `Map<String, List<String>>` (fila → posti) con `computeIfAbsent(fila, k -> new ArrayList<>()).add(posto)`.
3. Stampa la mappa. Poi ripeti con `TreeMap` e con `LinkedHashMap`: cosa cambia nell'ordine?
4. Itera con `for (Map.Entry<String, ...> e : mappa.entrySet())` e con `mappa.forEach((k, v) -> ...)`.
5. Prova a mettere una chiave `null` in `HashMap`, `TreeMap` e `ConcurrentHashMap`.
6. Usa `Map.of(...)` e prova a inserire un duplicato.

**Cosa osservare:** differenze di ordinamento e di tolleranza ai `null`.

**Verifica:**
- [ ] Tre modi di contare.
- [ ] Tabella con ordinamento e `null` delle tre implementazioni.

**Rifletti:** quale corrisponde a `Dictionary<K,V>` e `SortedDictionary`? Perché `ConcurrentHashMap` non accetta `null`?

### Es0905 · `Set` `[L1]`
**File:** `Es0905_Set.java`

**Obiettivo:** usare gli insiemi per eliminare duplicati e verificare l'appartenenza in tempo costante.

**Prima di iniziare:** un `Set` non ha duplicati. `HashSet` è veloce senza ordine; `LinkedHashSet` mantiene l'ordine di inserimento; `TreeSet` mantiene l'ordine naturale.

**Cosa fare:**
1. Inserisci la lista `[3, 1, 2, 3, 2, 1]` in tre insiemi diversi e stampali.
2. Controlla che `add` restituisca `false` se l'elemento c'è già.
3. Calcola unione (`addAll`), intersezione (`retainAll`) e differenza (`removeAll`) di due insiemi, **su copie** (perché modificano l'insieme).
4. Crea un `Set<Posto>` con un `record`: due posti con gli stessi valori sono considerati uguali?
5. Ripeti con una classe tua **senza** `equals`/`hashCode`: cosa succede?
6. Aggiungi `equals` e `hashCode` e verifica di nuovo.
7. Usa `Set.of(...)` e `TreeSet` per `first()`, `last()`, `headSet`, `tailSet`.

**Cosa osservare:** la classe senza `equals`/`hashCode` permette duplicati.

**Verifica:**
- [ ] Tre insiemi confrontati.
- [ ] Hai visto l'effetto di `equals`/`hashCode`.

**Rifletti:** cosa può andare storto se modifichi un campo usato da `hashCode` dopo aver inserito l'oggetto in un `HashSet`?

### Es0906 · Code e priorità `[L2]`
**File:** `Es0906_Code.java`

**Obiettivo:** usare `Queue`, `Deque` e `PriorityQueue` per gestire sequenze di lavoro.

**Prima di iniziare:** `Queue` è FIFO; `Deque` è una coda a due estremi (anche pila); `PriorityQueue` restituisce sempre l'elemento "minimo" secondo l'ordine naturale o un `Comparator`.

**Cosa fare:**
1. Usa `ArrayDeque` come coda: `offer`, `poll`, `peek`. Cosa restituisce `poll` su una coda vuota? E `remove()`?
2. Usa `ArrayDeque` come pila: `push`, `pop`, `peek`.
3. Crea una `PriorityQueue<Richiesta>` con `record Richiesta(String utente, int priorita)` e un comparatore per priorità crescente. Inserisci 6 richieste e stampa estraendole una alla volta.
4. Stampa la `PriorityQueue` con `System.out.println(pq)`: l'ordine è quello di estrazione? Perché no?
5. Aggiungi un secondo criterio (a parità di priorità, in ordine di arrivo) usando un contatore.
6. Prova `LinkedList` come coda e come lista e annota perché si usa poco.

**Cosa osservare:** la stampa diretta di una `PriorityQueue` non è ordinata.

**Verifica:**
- [ ] Estrazione in ordine corretto di priorità.
- [ ] Spiegazione del punto 4.

**Rifletti:** per la coda di attesa dei posti, quale struttura useresti? In Redis a cosa corrisponde? (liste, sorted set.)

### Es0907 · Leggere un CSV `[L2]` · *progetto*
**File:** `Es0907_Csv.java`

**Obiettivo:** trasformare righe di testo in oggetti e produrre un riepilogo.

**Prima di iniziare:** `String.split(",")` ha una particolarità: scarta le stringhe vuote **finali** a meno di passare `-1` come secondo argomento. Per file reali si usano librerie CSV, ma qui esercitiamo il codice base.

**Cosa fare:**
1. Metti in una `List<String>` o in un file `posti.csv` queste righe di intestazione e dati: `codice,fila,numero,prezzo,stato` poi 8 righe (alcune `LIBERO`, alcune `OCCUPATO`, una con prezzo mancante).
2. Definisci `record Posto(String codice, char fila, int numero, BigDecimal prezzo, Stato stato)` ed `enum Stato { LIBERO, OCCUPATO, BLOCCATO }`.
3. Scrivi `static Posto parse(String riga)`. Se il prezzo manca, usa `BigDecimal.ZERO`; se lo stato è sconosciuto, lancia un'eccezione con il testo della riga.
4. Leggi tutto il file con `Files.readAllLines`, salta l'intestazione e converti con `map(Classe::parse)`.
5. Stampa: quanti posti per stato, il totale dei prezzi dei liberi, il posto più caro.
6. Prova `"a,b,,".split(",")` e `"a,b,,".split(",", -1)` e annota la lunghezza dei due array.
7. Gestisci le righe sbagliate senza interrompere tutto: contale e stampale in coda.

**Cosa osservare:** il punto 6 spiega un bug tipico quando l'ultima colonna è vuota.

**Verifica:**
- [ ] Il programma non si ferma alla prima riga errata.
- [ ] Riepilogo corretto.

**Rifletti:** in una vera applicazione perché si preferisce una libreria (OpenCSV, Jackson CSV)? Cosa gestisce che `split` non gestisce?

### Es0908 · Da LINQ a Stream `[L2]`
**File:** `Es0908_Stream.java`

**Obiettivo:** tradurre le query LINQ più comuni in Stream.

**Prima di iniziare:** uno `Stream<T>` è una sequenza di elementi su cui applichi operazioni intermedie (`filter`, `map`, `sorted`, `distinct`, `limit`) seguite da una **operazione terminale** (`toList`, `collect`, `count`, `findFirst`, `anyMatch`, `reduce`, `forEach`).

**Cosa fare:**
1. Parti da una `List<Posto>` con almeno 12 posti (fila, numero, prezzo, stato).
2. Scrivi in un commento il C# e poi il Java di queste query:
   - `Where` + `Select`: i codici dei posti liberi
   - `OrderBy` / `OrderByDescending` / `ThenBy`: per prezzo decrescente e poi per codice
   - `First` / `FirstOrDefault`: il primo posto libero (con `findFirst`)
   - `Any` / `All` / `Count`: `anyMatch`, `allMatch`, `count`
   - `Sum` / `Average` / `Max`: usa `mapToInt`/`mapToDouble` o `reduce` per i `BigDecimal`
   - `Take` / `Skip`: `limit` / `skip`
   - `Distinct`: le file diverse
   - `SelectMany`: `flatMap`
3. Stampa il risultato di ciascuna.
4. Scrivi un'ultima query che combina almeno cinque operazioni.

**Cosa osservare:** `Average` e `Max` restituiscono tipi `Optional...` (perché lo stream potrebbe essere vuoto).

**Verifica:**
- [ ] Almeno 10 query tradotte.
- [ ] La query finale ha cinque operazioni e risultato corretto.

**Rifletti:** cosa corrisponde a `IEnumerable` e `IQueryable`? Perché lo `Stream` non è riutilizzabile? (Es0909.)

### Es0909 · Stream monouso e lazy `[L2]`
**File:** `Es0909_StreamLazy.java`

**Obiettivo:** capire che uno stream si consuma una volta sola e che le operazioni intermedie sono *pigre*.

**Prima di iniziare:** le operazioni intermedie non fanno nulla finché non viene chiamata l'operazione terminale. Inoltre, uno stream usato non si può riusare: lanciare una seconda operazione terminale dà `IllegalStateException`.

**Cosa fare:**
1. Crea `Stream<String> s = lista.stream().filter(...)` e **non** chiamare nulla di terminale. Aggiungi un `peek(x -> System.out.println("passo " + x))` e verifica che non stampi niente.
2. Aggiungi `.count()` (terminale) e osserva l'output. Attenzione: con alcuni stream il `count` può saltare i passaggi: prova con `List.of(...)`.
3. Chiama due volte un'operazione terminale sullo stesso stream e leggi l'eccezione.
4. Scrivi uno `Supplier<Stream<String>>` per riutilizzare la stessa pipeline.
5. Mostra la *short-circuit evaluation*: con `filter + findFirst` e `peek`, quanti elementi vengono elaborati?
6. Prova `Stream.iterate(1, x -> x * 2).limit(10)` e `Stream.generate(...)`: perché servono `limit` o `takeWhile`?

**Cosa osservare:** `findFirst` ferma la pipeline appena trova un elemento.

**Verifica:**
- [ ] Hai visto la pigrizia con `peek`.
- [ ] Hai letto l'errore "stream has already been operated upon or closed".

**Rifletti:** a che cosa corrisponde nell'LINQ l'*esecuzione differita*? Ci sono differenze di comportamento?

### Es0910 · `toList()` o `Collectors.toList()` `[L2]`
**File:** `Es0910_ToList.java`

**Obiettivo:** conoscere le differenze tra i vari modi di raccogliere uno stream in una lista.

**Prima di iniziare:** da Java 16 esiste `stream.toList()`, che restituisce una lista **non modificabile** e accetta `null`. `Collectors.toList()` restituisce una lista modificabile (di tipo non garantito); `Collectors.toUnmodifiableList()` non accetta `null`; `toCollection(ArrayList::new)` ti dà il tipo scelto da te.

**Cosa fare:**
1. Raccogli gli stessi elementi con i quattro metodi.
2. Prova `add` su ciascun risultato e annota quali lanciano eccezione.
3. Prova con uno stream che contiene un `null` e verifica quali metodi lo accettano.
4. Stampa `getClass()` dei quattro risultati.
5. Raccogli in un `Set` con `Collectors.toSet()` e `toCollection(TreeSet::new)`.
6. Raccogli in una `Map` con `Collectors.toMap(chiave, valore)`: provoca l'errore con chiavi duplicate e risolvi con la funzione di merge.
7. Usa `Collectors.joining(", ", "[", "]")` per costruire una stringa.

**Cosa osservare:** `toMap` lancia `IllegalStateException` sui duplicati.

**Verifica:**
- [ ] Tabella "metodo / modificabile / accetta null".
- [ ] `toMap` corretto con merge.

**Rifletti:** quando vuoi una lista modificabile dopo la pipeline? Quando ti basta l'immutabile?

### Es0911 · Raggruppare e partizionare `[L3]`
**File:** `Es0911_Grouping.java`

**Obiettivo:** usare `groupingBy` e `partitioningBy` con collector a valle.

**Prima di iniziare:** `Collectors.groupingBy(classificatore, collectorAValle)` produce una `Map<K, V>`; il secondo argomento decide cosa metti come valore (lista, conteggio, somma, massimo…). `partitioningBy` divide in due gruppi (`true`/`false`).

**Cosa fare:**
1. Dalla lista di posti: raggruppa per fila (`Map<Character, List<Posto>>`).
2. Raggruppa per fila contando (`counting()`).
3. Raggruppa per stato e somma i prezzi (`mapping` + `reducing`, oppure `summingInt` sui centesimi).
4. Raggruppa per fila ottenendo solo i **codici** (`mapping(Posto::codice, toList())`).
5. Raggruppa per fila e stato (due livelli: `groupingBy(.., groupingBy(..))`).
6. Usa `partitioningBy(p -> p.stato() == LIBERO)` e stampa quanti per gruppo.
7. Usa `TreeMap::new` come secondo argomento per avere le file ordinate.
8. Trova, per ogni fila, il posto più caro con `maxBy`.

**Cosa osservare:** `maxBy` restituisce `Optional` anche nel collector.

**Verifica:**
- [ ] 7 raggruppamenti diversi funzionanti.
- [ ] Output delle file in ordine.

**Rifletti:** a quale `GroupBy` di LINQ corrisponde? Quando preferiresti un ciclo con `Map.merge`?

### Es0912 · Mappa dei posti `[L3]` · *progetto*
**File:** `Es0912_MappaPosti.java`

**Obiettivo:** costruire e stampare una vista della sala con tutte le collezioni viste.

**Prima di iniziare:** una sala è una griglia (file × numeri). Puoi rappresentarla con una `TreeMap<Character, TreeMap<Integer, Posto>>` che mantiene l'ordine di file e numeri.

**Cosa fare:**
1. Genera i posti di una sala con file `A`–`E` e numeri 1–8 (usa due cicli o `IntStream`). Prezzo diverso per fila: A e B 50 euro, C e D 35, E 20.
2. Segna come occupati i posti: `A3`, `A4`, `B1`, `D8`, `E5` (tramite un `Set<String>`).
3. Costruisci la mappa con `TreeMap` annidate.
4. Stampa la sala come griglia: `[ ]` libero e `[X]` occupato, con la lettera della fila a sinistra e i numeri in testa.
5. Stampa un riepilogo: liberi e occupati per fila (con `groupingBy`), incasso potenziale dei liberi, fila con più posti liberi.
6. Scrivi `static List<Posto> trovaGruppo(int quanti)` che trova `quanti` posti **consecutivi liberi** nella stessa fila (il primo gruppo disponibile) e restituisce la lista.
7. Prova `trovaGruppo(3)`, `trovaGruppo(6)` e `trovaGruppo(9)`.

**Cosa osservare:** il caso di 9 posti consecutivi non esiste (la fila ha 8 posti).

**Verifica:**
- [ ] La griglia ha le file ordinate.
- [ ] `trovaGruppo` gestisce il caso "non trovato" senza `null`.

**Rifletti:** questo è il problema del "posti vicini" del progetto: quale struttura servirebbe per farlo in modo efficiente su una sala da 10.000 posti?


## Cosa devi aver capito

- [ ] `List`, `Map`, `Set` e quali implementazioni scegliere.
- [ ] Le collezioni immutabili (`List.of`) lanciano su `add`.
- [ ] LINQ → Stream, con `Optional` al posto di `FirstOrDefault`.
- [ ] Uno stream è lazy e si usa una volta sola.
- [ ] `BigDecimal` si somma con `reduce`, non con `sum()`.
