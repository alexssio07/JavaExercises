# 10 · Generics

Obiettivo: usare e scrivere tipi e metodi generici sapendo cosa cambia rispetto a C#: in Java i generics esistono solo in fase di compilazione (*type erasure*).

## Sintassi a confronto

```csharp
// C#
public class Scatola<T> where T : IComparable<T>
{
    public T Valore { get; }
    public Scatola(T v) { Valore = v; }
    public static T Max<T>(IEnumerable<T> xs) where T : IComparable<T> { ... }
}
```

```java
// Java
public class Scatola<T extends Comparable<T>> {           // il vincolo sta dentro le parentesi angolari
    private final T valore;
    public Scatola(T valore) { this.valore = valore; }
    public T getValore() { return valore; }

    public static <U extends Comparable<U>> U max(Iterable<U> xs) { ... }   // <U> prima del tipo di ritorno
}
```

| Tema | C# | Java |
| --- | --- | --- |
| Tipi generici a runtime | reificati (`typeof(T)`) | **cancellati** (*erasure*) |
| Valori primitivi come `T` | `List<int>` | **non si può**: `List<Integer>` |
| `new T()` | con `where T : new()` | non si può |
| `typeof(T)` | sì | non si può: si passa `Class<T>` come parametro |
| `T[]` | sì | non si può creare (`new T[n]`) |
| Vincoli | `where T : X` | `T extends X` (anche `T extends A & B`) |
| Covarianza | `IEnumerable<out T>` | wildcard `List<? extends T>` |
| Controvarianza | `Action<in T>` | wildcard `List<? super T>` |
| `List<Cane>` assegnabile a `List<Animale>` | no (le classi sono invarianti) | no |
| Metodo generico | `T Prima<T>(List<T> l)` | `<T> T prima(List<T> l)` |

**PECS**: *Producer Extends, Consumer Super*. Se la collezione **produce** valori che leggi usa `? extends T`; se li **consuma** (ci scrivi) usa `? super T`.

## Esercizi

Ogni esercizio segue lo stesso schema: **Obiettivo**, **Prima di iniziare** (il concetto in breve), **Cosa fare** (passi in ordine), **Cosa osservare**, **Verifica** (checklist) e **Rifletti** (domande da colloquio).

### Es1001 · Cosa cancella l'*erasure* `[L2]`
**File:** `Es1001_Erasure.java`

**Obiettivo:** scoprire cosa resta dei generici a runtime e quali operazioni diventano impossibili.

**Prima di iniziare:** in Java i tipi generici esistono solo per il compilatore. A runtime `List<String>` e `List<Integer>` sono la stessa classe `List` (*type erasure*). In C# invece i generici sono "reificati" e restano a runtime.

**Cosa fare:**
1. Crea `List<String> a = new ArrayList<>()` e `List<Integer> b = new ArrayList<>()`. Confronta `a.getClass() == b.getClass()`.
2. Prova `a instanceof List<String>` e leggi l'errore. Poi `a instanceof List<?>`.
3. In una classe `Scatola<T>` prova a scrivere `new T()`, `new T[10]`, `T.class`, `x instanceof T`: annota l'errore di ciascuno.
4. Prova ad avere due metodi `void stampa(List<String> l)` e `void stampa(List<Integer> l)` nella stessa classe: leggi l'errore (stessa cancellazione).
5. Mostra un *heap pollution* controllato: inserisci un `Integer` in una `List<String>` passando per `List` grezza e osserva dove avviene la `ClassCastException` (non all'inserimento!).
6. Scrivi in un commento la tabella "in C# posso / in Java no".

**Cosa osservare:** l'eccezione del punto 5 compare quando leggi il valore, non quando lo inserisci.

**Verifica:**
- [ ] Almeno 5 operazioni impossibili provate e annotate.
- [ ] Sai spiegare perché il punto 5 fallisce in ritardo.

**Rifletti:** come aggirare le limitazioni? (Passare `Class<T>`: vedi Es1008.)

### Es1002 · Classe generica `Risultato<T>` `[L2]` · *progetto*
**File:** `Es1002_Risultato.java`

**Obiettivo:** scrivere una classe generica realistica per rappresentare successo o errore senza eccezioni.

**Prima di iniziare:** `Risultato<T>` è un contenitore che ha o un valore o un messaggio di errore. È un uso comune dei generici: una sola classe per qualsiasi tipo di dato.

**Cosa fare:**
1. Dichiara `final class Risultato<T>` con campi `private final T valore` e `private final String errore`.
2. Costruttori privati e due fabbriche statiche: `static <T> Risultato<T> ok(T valore)` e `static <T> Risultato<T> errore(String messaggio)`.
3. Aggiungi `boolean riuscito()`, `T valore()` (lancia eccezione se è un errore), `String errore()`.
4. Aggiungi `<U> Risultato<U> mappa(Function<T, U> f)`: se è un errore lo propaga, altrimenti applica la funzione.
5. Aggiungi `T oppure(T ripiego)`.
6. Usa `Risultato<Integer>` per un `parse` e `Risultato<Posto>` per una ricerca; poi concatena un `mappa` sulle due.
7. Prova a scrivere `Risultato<int>` e leggi l'errore.

**Cosa osservare:** in `static <T> Risultato<T> ok(...)` il `<T>` davanti al tipo di ritorno è un parametro del **metodo**, non della classe.

**Verifica:**
- [ ] Il `mappa` propaga l'errore senza eseguire la funzione.
- [ ] Hai spiegato perché i metodi statici non possono usare il `T` della classe.

**Rifletti:** quando sceglieresti un `Risultato` invece delle eccezioni? Che relazione ha con `Optional`?

### Es1003 · Metodo generico `[L2]`
**File:** `Es1003_MetodoGenerico.java`

**Obiettivo:** scrivere metodi generici indipendenti dalla classe e capire la deduzione del tipo.

**Prima di iniziare:** un metodo generico dichiara i suoi parametri di tipo prima del tipo di ritorno: `static <T> T primo(List<T> l)`. Il compilatore deduce `T` dagli argomenti.

**Cosa fare:**
1. Scrivi `static <T> T primo(List<T> lista)` che restituisce il primo elemento o lancia un'eccezione chiara se vuota.
2. Scrivi `static <T> void scambia(T[] array, int i, int j)`.
3. Scrivi `static <T> List<T> ripeti(T elemento, int volte)`.
4. Scrivi `static <A, B> Map<A, B> abbina(List<A> chiavi, List<B> valori)` che lancia eccezione se hanno lunghezze diverse.
5. Chiama ognuno con tipi diversi (`String`, `Integer`, un tuo `record`).
6. Chiama `Collections.<String>emptyList()` con tipo esplicito e poi lascia dedurre. Quando serve l'indicazione esplicita?
7. Prova `scambia` con un array di primitivi (`int[]`): errore? Perché?

**Cosa osservare:** l'errore del punto 7 rimanda alla regola "niente primitivi nei generici".

**Verifica:**
- [ ] Quattro metodi funzionanti con più tipi.
- [ ] Hai provato la sintassi esplicita.

**Rifletti:** che differenza c'è tra `<T> void f(List<T> l)` e `void f(List<?> l)`?

### Es1004 · Tipi con vincolo `[L3]`
**File:** `Es1004_Vincoli.java`

**Obiettivo:** limitare i tipi accettati con `extends` e combinare più vincoli.

**Prima di iniziare:** `<T extends Comparable<T>>` significa "qualsiasi T che sa confrontarsi con se stesso". Per più vincoli si usa `&`: `<T extends Number & Comparable<T>>` (prima la classe, poi le interfacce). È l'equivalente di `where T : IComparable<T>` in C#.

**Cosa fare:**
1. Scrivi `static <T extends Comparable<T>> T massimo(List<T> lista)` e provalo con `Integer`, `String` e un tuo `record` che implementa `Comparable`.
2. Prova `massimo` con una lista di un tipo che **non** è `Comparable` e leggi l'errore.
3. Scrivi `static <T extends Number> double somma(List<T> l)` usando `doubleValue()`.
4. Scrivi `static <T extends Number & Comparable<T>> T maggiore(T a, T b)`.
5. Prova a invertire l'ordine dei vincoli (interfaccia prima della classe) e leggi l'errore.
6. Scrivi una classe `Intervallo<T extends Comparable<T>>` con `contiene(T x)` e usala con numeri e date.
7. Perché `Comparable<T>` e non `Comparable`? Prova senza `<T>` e leggi gli avvisi.

**Cosa osservare:** il compilatore ti permette di chiamare i metodi del vincolo su `T`.

**Verifica:**
- [ ] Quattro esempi con vincolo.
- [ ] L'errore del punto 2 spiegato.

**Rifletti:** perché `Collections.max` ha la firma `<T extends Object & Comparable<? super T>>`? (Torna dopo l'Es1005.)

### Es1005 · Wildcard e PECS `[L3]`
**File:** `Es1005_Wildcard.java`

**Obiettivo:** capire `? extends`, `? super` e la regola PECS (*Producer Extends, Consumer Super*).

**Prima di iniziare:** i generici in Java sono **invarianti**: `List<Integer>` non è una `List<Number>`. Le wildcard introducono variazione: `List<? extends Number>` legge dei `Number` (producer) ma non ci puoi inserire; `List<? super Integer>` ci puoi scrivere `Integer` (consumer) ma leggi solo `Object`.

**Cosa fare:**
1. Prova `List<Number> n = new ArrayList<Integer>();` e leggi l'errore.
2. Con `List<? extends Number> p = List.of(1, 2, 3)` prova a leggere un `Number` (ok) e ad aggiungere un `Integer` (errore: perché?).
3. Con `List<? super Integer> c = new ArrayList<Number>()` prova ad aggiungere un `Integer` (ok) e a leggere un `Integer` (errore, ma `Object` va).
4. Scrivi `static double somma(Collection<? extends Number> origine)`.
5. Scrivi `static void riempi(Collection<? super Integer> destinazione, int quanti)`.
6. Scrivi `static <T> void copia(List<? extends T> origine, List<? super T> destinazione)`.
7. Prova `copia` da `List<Integer>` a `List<Number>` e a `List<Object>`.
8. Confronta con gli array: `Number[] a = new Integer[3];` compila: cosa succede se assegni un `Double`?

**Cosa osservare:** gli array sono covarianti (e pericolosi), i generici invarianti.

**Verifica:**
- [ ] Tutti i tentativi del punto 2 e 3 provati e annotati.
- [ ] Regola PECS scritta con parole tue.

**Rifletti:** a cosa corrispondono `in` e `out` dei generici di C# (`IEnumerable<out T>`)?

### Es1006 · Niente primitivi `[L1]`
**File:** `Es1006_NoPrimitivi.java`

**Obiettivo:** capire il costo del *boxing* quando si usano le collezioni con numeri.

**Prima di iniziare:** i generici accettano solo tipi riferimento. `List<int>` non esiste; si usa `List<Integer>`. Ogni valore è un oggetto "incapsulato" (*boxing*); questo ha un costo di memoria e di tempo.

**Cosa fare:**
1. Prova `List<int>` e leggi l'errore.
2. Somma 10 milioni di numeri con `List<Integer>` e con `int[]`; misura il tempo con `System.nanoTime()`.
3. Misura la memoria approssimativa (`Runtime.getRuntime().totalMemory() - freeMemory()`).
4. Ripeti con `IntStream.rangeClosed(1, 10_000_000).sum()` e con `mapToInt` su uno stream di `Integer`.
5. Prova `Integer a = 127, b = 127; a == b` e `Integer c = 128, d = 128; c == d`.
6. Elenca i tipi primitivi con la loro versione "scatolata" e le versioni di stream/optional dedicati (`IntStream`, `OptionalInt`, `ToIntFunction`).

**Cosa osservare:** differenza di prestazioni tra `int[]` e `List<Integer>`.

**Verifica:**
- [ ] Tre tempi misurati e annotati.
- [ ] Tabella con i primitivi e i wrapper.

**Rifletti:** quando il boxing è un problema reale? (Dati numerosi, cicli caldi.) Come lo risolve la futura "Valhalla"?

### Es1007 · Avvisi sui *raw types* `[L2]`
**File:** `Es1007_RawTypes.java`

**Obiettivo:** riconoscere e correggere l'uso di tipi generici senza parametro.

**Prima di iniziare:** `List l = new ArrayList();` è un *raw type*: il compilatore non controlla i tipi e il codice può rompersi a runtime. Esiste per compatibilità con codice pre-Java 5.

**Cosa fare:**
1. Scrivi `List lista = new ArrayList();` e aggiungi una `String` e un `Integer`.
2. Compila con `-Xlint:unchecked` e leggi gli avvisi.
3. Leggi gli elementi con `String s = (String) lista.get(1);` e osserva la `ClassCastException`.
4. Correggi con `List<String>` e vedi dove il compilatore ora ti ferma.
5. Passa una `List` grezza a un metodo che si aspetta `List<String>` e leggi l'avviso.
6. Usa `@SuppressWarnings("unchecked")` in un caso giustificato (un cast generico controllato) e scrivi nel commento perché è sicuro.
7. Prova `List<?>` e spiega come differisce dal raw type.

**Cosa osservare:** l'errore passa dal momento della scrittura (compilazione) a quello dell'esecuzione.

**Verifica:**
- [ ] Avvisi letti e spiegati.
- [ ] Un uso di `@SuppressWarnings` motivato.

**Rifletti:** perché `@SuppressWarnings` va applicato al minor ambito possibile?

### Es1008 · Il parametro `Class<T>` `[L3]`
**File:** `Es1008_ClassToken.java`

**Obiettivo:** usare `Class<T>` per recuperare a runtime l'informazione di tipo cancellata.

**Prima di iniziare:** passare `String.class` (di tipo `Class<String>`) permette a un metodo di sapere a runtime quale tipo creare o convertire. È il trucco usato da librerie come Jackson (`readValue(json, Posto.class)`) e Spring (`getBean(Foo.class)`).

**Cosa fare:**
1. Scrivi `static <T> T crea(Class<T> tipo)` che crea un'istanza con il costruttore senza parametri (`tipo.getDeclaredConstructor().newInstance()`).
2. Provalo con una tua classe e con `ArrayList.class`.
3. Scrivi `static <T> T converti(String testo, Class<T> tipo)` che gestisce `Integer`, `Boolean` e `String` (con un `if` o `switch` su `tipo`) e lancia un'eccezione per gli altri.
4. Scrivi `static <T> List<T> filtraPerTipo(List<?> lista, Class<T> tipo)` che restituisce solo gli elementi di quel tipo (`tipo.isInstance`, `tipo.cast`).
5. Prova a ottenere `Class<List<String>>`: `List<String>.class` compila? Come ti arrangi? (Cerca *super type token* e `TypeReference` di Jackson.)
6. Stampa `getClass().getName()` e `getSimpleName()` di vari oggetti.

**Cosa osservare:** `Class<T>` evita cast non controllati.

**Verifica:**
- [ ] I tre metodi funzionano con più tipi.
- [ ] Hai spiegato perché `List<String>.class` non esiste.

**Rifletti:** in Spring dove incontri i *class token*? (`@Autowired`, `getBean`, `RestTemplate.getForObject(url, Posto.class)`.)

### Es1009 · Deduzione e `var` `[L1]`
**File:** `Es1009_Var.java`

**Obiettivo:** usare `var` e il *diamond* `<>` capendo cosa deduce il compilatore.

**Prima di iniziare:** `var` (da Java 10) vale solo per **variabili locali** e dedue il tipo dall'inizializzatore. Il tipo resta statico (non è `dynamic`). Il *diamond* `<>` omette gli argomenti di tipo a destra.

**Cosa fare:**
1. Scrivi `var lista = new ArrayList<String>();` e `List<String> l2 = new ArrayList<>();` e verifica il tipo con una chiamata specifica di `ArrayList`.
2. Prova `var x;` (senza inizializzatore), `var y = null;`, `var z = () -> 1;`: leggi gli errori.
3. Prova `var` come campo, parametro e tipo di ritorno: leggi gli errori.
4. Scrivi `var m = new HashMap<String, List<Integer>>();` e `var e = m.entrySet().iterator().next();` e controlla il tipo di `e` con l'IDE.
5. Scrivi `var anonimo = new Object() { int n = 5; };` e usa `anonimo.n`: funziona?
6. Prova `var lista2 = new ArrayList<>();`: quale tipo viene dedotto? (`ArrayList<Object>`).
7. Scrivi 5 righe: quando `var` migliora la leggibilità e quando la peggiora.

**Cosa osservare:** il punto 5 funziona solo con `var`.

**Verifica:**
- [ ] Almeno 5 errori letti e annotati.
- [ ] Regole di stile scritte.

**Rifletti:** `var` di Java e di C# sono uguali? Quali differenze?


## Cosa devi aver capito

- [ ] Type erasure: niente `new T()`, `instanceof List<String>`, `T[]`.
- [ ] Niente primitivi nei generics: `Integer`, `Long`, ...
- [ ] `<T extends X>` come vincolo; metodo generico con `<T>` prima del tipo di ritorno.
- [ ] `? extends` per leggere, `? super` per scrivere.
- [ ] `Class<T>` per ricostruire il tipo a runtime.
