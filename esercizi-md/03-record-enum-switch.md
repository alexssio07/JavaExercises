# 03 · Record, enum e switch moderno

Obiettivo: modellare dati e stati in modo compatto. È la base degli oggetti del progetto (posto, stato del posto, esito di un hold).

## Sintassi a confronto

```csharp
// C#
public record Libro(string Titolo, int Pagine);
var l2 = l1 with { Pagine = 300 };
public enum Genere { Giallo, Fantasy }
var s = genere switch { Genere.Giallo => "G", _ => "?" };
```

```java
// Java
public record Libro(String titolo, int pagine) {}           // accessor: l.titolo(), non l.Titolo
// non esiste "with": si scrive un metodo che restituisce una copia
public enum Genere { GIALLO, FANTASY }                      // valori in MAIUSCOLO
String s = switch (genere) {
    case GIALLO -> "G";
    case FANTASY -> "F";                                    // enum completo: nessun default
};
```

**Un `record` Java ha**: costruttore canonico, accessor (`titolo()`), `equals`, `hashCode`, `toString`, campi `private final`. Può avere metodi, costruttori aggiuntivi, metodi `static` e un *compact constructor* per validare:

```java
public record Intervallo(int da, int a) {
    public Intervallo {                       // compact constructor: niente parametri e niente "this.x ="
        if (da > a) throw new IllegalArgumentException("da > a");
    }
}
```

**Un `enum` Java è una classe**: può avere campi, costruttore (sempre privato), metodi e corpi diversi per ogni costante. Metodi automatici: `values()`, `valueOf(String)`, `name()`, `ordinal()`, `compareTo`.

## Esercizi

Ogni esercizio segue lo stesso schema: **Obiettivo**, **Prima di iniziare** (il concetto in breve), **Cosa fare** (passi in ordine), **Cosa osservare**, **Verifica** (checklist) e **Rifletti** (domande da colloquio).

### Es0301 · Primo `record` `[L1]`
**File:** `Es0301_PrimoRecord.java`

**Obiettivo:** capire cosa genera il compilatore per un `record` e in cosa differisce da una classe normale.

**Prima di iniziare:** `record Evento(Long id, String nome, LocalDateTime data) {}` dichiara in una riga: i campi `private final`, il costruttore con tutti i parametri, gli accessor `id()`, `nome()`, `data()` (senza `get`), e i metodi `equals`, `hashCode` e `toString`. Un record è **immutabile**: non ci sono setter.

**Cosa fare:**
1. Crea il `record Evento` con i tre campi e importa `java.time.LocalDateTime`.
2. Nel `main` crea due eventi con **gli stessi valori** (usa `LocalDateTime.of(2026, 12, 5, 21, 0)` per la data) e un terzo con un nome diverso.
3. Stampa un evento con `System.out.println(evento)` e commenta il formato.
4. Prevedi e poi stampa: `e1 == e2`, `e1.equals(e2)`, `e1.hashCode() == e2.hashCode()`, `e1.equals(e3)`.
5. Prova `e1.nome = "x"` e `e1.setNome("x")`: leggi i due errori del compilatore.
6. Aggiungi al record un metodo `String descrizione()` che restituisce `nome + " (" + data + ")"` e uno `static` di nome `diTest()` che crea un evento fisso.
7. Aggiungi un campo extra al record e osserva cosa devi cambiare nelle chiamate al costruttore.

**Cosa osservare:** `toString` generato include nome del record e valori dei campi.

**Verifica:**
- [ ] Due record con gli stessi valori sono `equals` e con lo stesso `hashCode`, ma non `==`.
- [ ] Hai letto i due errori del punto 5.
- [ ] Il record ha un metodo di istanza e uno statico.

**Rifletti:** qual è la differenza tra un `record` Java e una classe con getter e setter? Perché sono ottimi per i DTO? In C# a cosa corrisponde `record`?

### Es0302 · Validare nel compact constructor `[L2]`
**File:** `Es0302_RecordValidato.java`

**Obiettivo:** garantire che un record contenga sempre valori validi e normalizzati.

**Prima di iniziare:** il *compact constructor* si scrive senza parentesi né parametri: `public Posto { ... }`. Dentro puoi leggere e **riassegnare** i parametri (`codice = codice.strip();`); i campi vengono assegnati automaticamente alla fine. Per rifiutare un valore si lancia un'eccezione.

**Cosa fare:**
1. Crea `record Posto(String codice, BigDecimal prezzo)`.
2. Nel compact constructor: se `codice` è `null` o vuoto (dopo `strip`) lancia `IllegalArgumentException("codice obbligatorio")`; se `prezzo` è `null` lancia una `NullPointerException` con un messaggio (usa `Objects.requireNonNull`); se `prezzo` è negativo (`signum() < 0`) lancia `IllegalArgumentException` con il valore nel messaggio.
3. **Normalizza** il codice: togli gli spazi e portalo in maiuscolo, riassegnando il parametro.
4. Prova a creare: `new Posto(" a12 ", new BigDecimal("10"))`, `new Posto("", ...)`, `new Posto(null, ...)`, `new Posto("A1", new BigDecimal("-1"))`, `new Posto("A1", null)`. Ogni creazione in un `try/catch (RuntimeException e)` che stampa classe e messaggio.
5. Stampa i campi del primo posto: il codice deve essere `A12`.

**Cosa osservare:** non scrivi `this.codice = codice;` nel compact constructor (se lo fai il compilatore protesta: prova).

**Verifica:**
- [ ] Il primo posto ha codice `A12`.
- [ ] Gli altri quattro tentativi lanciano eccezioni diverse con messaggi chiari.

**Rifletti:** dove metterai invece la validazione dell'input HTTP (Bean Validation, `@Valid`)? Quali controlli vanno nel dominio e quali nel controller?

### Es0303 · Record con metodi e *factory* `[L2]`
**File:** `Es0303_RecordConMetodi.java`

**Obiettivo:** incapsulare un valore (il prezzo) in un tipo dedicato con operazioni sicure.

**Prima di iniziare:** un record può avere metodi di istanza e metodi `static`. I record immutabili restituiscono **nuovi** oggetti dalle operazioni, come `BigDecimal`. Un metodo statico `of(...)` è una *factory*: nasconde il costruttore e può accettare tipi comodi (una `String`).

**Cosa fare:**
1. `record Prezzo(BigDecimal valore)` con compact constructor che rifiuta `null` e valori negativi e porta il valore a due decimali (`setScale(2, RoundingMode.HALF_UP)`).
2. Metodo statico `of(String testo)` che crea un `Prezzo` da una stringa come `"10.90"`.
3. Metodi di istanza: `Prezzo piu(Prezzo altro)`, `Prezzo per(int quantita)`, `Prezzo meno(Prezzo altro)` (che non può dare risultato negativo: lancia un'eccezione), `boolean maggioreDi(Prezzo altro)`.
4. Una costante `static final Prezzo ZERO`.
5. Nel `main` calcola il totale di 3 biglietti da `10.90` più un supplemento di `2.50` e stampa il risultato.
6. Prova `Prezzo.of("10.999")` e `Prezzo.of("abc")` e osserva cosa succede.

**Cosa osservare:** `Prezzo.of("10.90").per(3)` non modifica l'oggetto originale.

**Verifica:**
- [ ] `Prezzo.of("10.90").per(3).piu(Prezzo.of("2.50"))` stampa `35.20`.
- [ ] Un prezzo con tre decimali è arrotondato.

**Rifletti:** perché incapsulare il denaro in un tipo dedicato (*value object*)? Come lo mapperesti su una colonna `NUMERIC` con JPA?

### Es0304 · Il `with` che non c'è `[L2]`
**File:** `Es0304_Wither.java`

**Obiettivo:** modificare un valore immutabile creandone una copia, come fa `with` in C#.

**Prima di iniziare:** in C# `p with { Stato = X }` crea una copia con un campo diverso. In Java non esiste: si scrive un metodo che chiama il costruttore passando i campi invariati e il nuovo valore.

**Cosa fare:**
1. Crea `enum StatoPrenotazione { IN_ATTESA, CONFERMATA, ANNULLATA }` e `record Prenotazione(Long id, String posto, String utente, StatoPrenotazione stato)`.
2. Aggiungi il metodo `Prenotazione conStato(StatoPrenotazione nuovo)` che restituisce una nuova prenotazione.
3. Aggiungi `Prenotazione conferma()` e `Prenotazione annulla()`, implementate con `conStato`, che lanciano `IllegalStateException` se la transizione non è ammessa (per esempio non puoi confermare una prenotazione già annullata).
4. Nel `main` crea una prenotazione `IN_ATTESA`, confermala e stampa **entrambe** (l'originale e la copia).
5. Prova ad annullare quella confermata e quella già annullata.

**Cosa osservare:** l'originale resta `IN_ATTESA`.

**Verifica:**
- [ ] Due oggetti distinti con stati diversi.
- [ ] Le transizioni vietate lanciano un'eccezione con un messaggio che indica stato di partenza e di arrivo.

**Rifletti:** quando vale la pena l'immutabilità e quando invece un'entity JPA deve essere mutabile? Cosa preferiresti per un ordine di acquisto?

### Es0305 · Macchina a stati del posto `[L2]` · *progetto*
**File:** `Es0305_StatoPosto.java`

**Obiettivo:** modellare le transizioni di stato di un posto con un `enum` e verificarle in modo sistematico.

**Prima di iniziare:** un posto può essere `LIBERO`, `IN_ATTESA` (hold) o `VENDUTO`. Le transizioni consentite sono tre: `LIBERO → IN_ATTESA`, `IN_ATTESA → LIBERO` (l'hold scade o viene annullato), `IN_ATTESA → VENDUTO`. Tutte le altre sono vietate, compresa `VENDUTO → qualsiasi stato` e le transizioni da uno stato a se stesso.

**Cosa fare:**
1. Crea `enum Stato { LIBERO, IN_ATTESA, VENDUTO }`.
2. Aggiungi il metodo `boolean puoPassareA(Stato prossimo)` **senza** una catena di `if`: sceglie tu come (uno `switch` sull'enum, oppure un insieme di stati ammessi per ogni costante).
3. Aggiungi `Stato passaA(Stato prossimo)` che restituisce il nuovo stato o lancia `IllegalStateException("da X a Y non ammesso")`.
4. Nel `main` percorri **tutte le 9 combinazioni** (due cicli sui `values()`) e stampa una tabella 3x3 con `OK` o `--`.
5. Prima di eseguire scrivi in un commento la tabella che ti aspetti.
6. Simula un flusso reale: un posto parte `LIBERO`, passa a `IN_ATTESA`, poi a `VENDUTO`; prova un'ulteriore transizione e verifica l'eccezione.

**Cosa osservare:** la riga di `VENDUTO` è tutta vietata.

**Verifica:**
- [ ] Esattamente 3 celle su 9 sono `OK`.
- [ ] La tabella reale coincide con la tua previsione.
- [ ] Nessun `if` con più di una condizione dentro `puoPassareA`.

**Rifletti:** potresti esprimere le transizioni come **dato** (un campo dell'enum con l'elenco degli stati raggiungibili)? Quali vantaggi ha rispetto al codice?

### Es0306 · `values`, `valueOf`, `ordinal` `[L1]`
**File:** `Es0306_MetodiDellEnum.java`

**Obiettivo:** conoscere i metodi che ogni `enum` ha già e i loro rischi.

**Prima di iniziare:** ogni enum ha `values()` (array di tutte le costanti), `valueOf(String)` (cerca per nome, lancia eccezione se non esiste), `name()`, `ordinal()` (posizione, da 0) e `compareTo`. L'ordine di dichiarazione determina `ordinal()` e l'ordine di `compareTo`.

**Cosa fare:**
1. Con un `for-each` su `Stato.values()` stampa per ogni costante `name()` e `ordinal()`.
2. Chiama `Stato.valueOf("VENDUTO")`, poi `Stato.valueOf("venduto")` e `Stato.valueOf("SCONOSCIUTO")` (le ultime due in un `try/catch`): annota il tipo di eccezione e il messaggio.
3. Confronta due costanti con `==` e con `equals`: sono equivalenti? Perché per gli enum `==` è sicuro?
4. Confronta `Stato.LIBERO.compareTo(Stato.VENDUTO)`.
5. Scrivi `static Optional<Stato> cerca(String testo)` che restituisce un `Optional` senza lanciare eccezioni (ignorando maiuscole/minuscole).
6. Immagina di **riordinare** le costanti nell'enum: cosa cambia per `ordinal()`? Scrivilo in un commento.

**Cosa osservare:** `valueOf` è sensibile alle maiuscole.

**Verifica:**
- [ ] `cerca("venduto")` restituisce `Optional[VENDUTO]` e `cerca("boh")` un Optional vuoto.
- [ ] Hai annotato il tipo di eccezione di `valueOf`.

**Rifletti:** perché non si salva mai `ordinal()` in un database? (In JPA si usa `@Enumerated(EnumType.STRING)`.)

### Es0307 · `EnumMap` `[L2]`
**File:** `Es0307_EnumMap.java`

**Obiettivo:** raggruppare e contare per valore di un enum con la mappa specializzata.

**Prima di iniziare:** `EnumMap<K, V>` è una mappa ottimizzata per chiavi enum: usa un array interno, mantiene le chiavi nell'ordine di dichiarazione e non ammette chiavi `null`. `EnumSet` è l'analogo per gli insiemi.

**Cosa fare:**
1. Genera una lista di 30 stati casuali (`Stato.values()[random.nextInt(3)]`, con un `Random` con seme fisso, per risultati ripetibili).
2. Conta quanti posti ci sono per stato con un `EnumMap<Stato, Integer>`, inizializzando tutti gli stati a 0 (cerca come con `values()`).
3. Fai lo stesso con una normale `HashMap<Stato, Integer>` usando `merge`.
4. Stampa entrambe e confronta l'ordine delle chiavi.
5. Crea un `EnumSet` con gli stati "occupati" (`IN_ATTESA`, `VENDUTO`) usando `EnumSet.of`, e il complementare con `EnumSet.complementOf`.
6. Usa l'`EnumSet` per filtrare la lista del punto 1 e contare quanti posti sono occupati.

**Cosa osservare:** l'ordine di stampa dell'`EnumMap` segue quello delle costanti, quello della `HashMap` no (o non necessariamente).

**Verifica:**
- [ ] I due conteggi sono uguali per ogni stato.
- [ ] `EnumSet.complementOf` restituisce il solo stato `LIBERO`.

**Rifletti:** quando conviene `EnumMap` rispetto a una `HashMap`? Dove la useresti nel progetto (report per stato)?

### Es0308 · Enum con campi e metodi `[L2]`
**File:** `Es0308_EnumConDati.java`

**Obiettivo:** associare dati e comportamento a ogni costante di un enum.

**Prima di iniziare:** un enum può avere campi, un costruttore (sempre `private`, anche se non lo scrivi) e metodi. Ogni costante si dichiara passando gli argomenti del costruttore: `VIP("Vip", new BigDecimal("2.5"))`.

**Cosa fare:**
1. Crea `enum Fascia` con le costanti `BASE` (coefficiente 1.0), `PLATEA` (1.5) e `VIP` (2.5).
2. Ogni costante ha un'**etichetta** italiana (`"Base"`, `"Platea"`, `"Vip"`) e un coefficiente `BigDecimal` (creato da stringa). Aggiungi i getter.
3. Aggiungi il metodo `BigDecimal prezzoPer(BigDecimal base)` **dentro** l'enum (arrotondato a due decimali).
4. Scrivi anche una versione `static` fuori dall'enum con la stessa funzione e confronta le due chiamate: `Fascia.VIP.prezzoPer(base)` e `calcola(base, Fascia.VIP)`.
5. Aggiungi `static Fascia daEtichetta(String etichetta)` che cerca ignorando le maiuscole e lancia `IllegalArgumentException` con l'elenco delle etichette valide se non trova nulla.
6. Prova a dichiarare il costruttore come `public` e leggi l'errore.

**Cosa osservare:** l'elenco delle etichette valide nel messaggio d'errore si costruisce dinamicamente da `values()`.

**Verifica:**
- [ ] `Fascia.daEtichetta("platea")` restituisce `PLATEA`.
- [ ] `Fascia.VIP.prezzoPer(new BigDecimal("10.90"))` stampa `27.25`.
- [ ] Un'etichetta sconosciuta lancia un'eccezione con messaggio utile.

**Rifletti:** quale delle due forme (dentro o fuori l'enum) è più idiomatica? E se i coefficienti dovessero stare in un database?

### Es0309 · Comportamento diverso per ogni costante `[L3]`
**File:** `Es0309_EnumComportamento.java`

**Obiettivo:** sostituire un `switch` con il polimorfismo dentro un enum.

**Prima di iniziare:** un enum può dichiarare un metodo `abstract`; ogni costante deve allora fornire il proprio corpo (*constant-specific class body*), con la sintassi `NOME { ... }`. È un modo di dare a ogni valore un comportamento proprio senza `if` o `switch`.

**Cosa fare:**
1. Crea `enum Sconto` con il metodo astratto `BigDecimal applica(BigDecimal prezzo)`.
2. Dichiara quattro costanti, ciascuna con il proprio corpo: `NESSUNO` (non cambia il prezzo), `STUDENTE` (-20%), `OVER65` (-30%), `LASTMINUTE` (-50%, ma mai meno di 5 euro).
3. Tutte le operazioni usano `BigDecimal` e arrotondano a due decimali.
4. Nel `main` applica ciascuno sconto a tre prezzi (`4.00`, `10.90`, `100.00`) e stampa una tabella.
5. Aggiungi un metodo concreto (non astratto) `String etichetta()` condiviso da tutte, che usa `name().toLowerCase()`.

**Cosa osservare:** nel `main` non c'è nessun `if` né `switch` per scegliere lo sconto.

**Verifica:**
- [ ] La tabella ha 4 righe e 3 colonne di risultati corretti.
- [ ] Il caso `LASTMINUTE` su `4.00` restituisce un valore coerente con la regola dei 5 euro (decidi tu l'interpretazione e scrivila in un commento).

**Rifletti:** questa soluzione è facile da estendere? E se gli sconti dovessero provenire da un database o da una configurazione? (Pensa all'interfaccia `PoliticaPrezzo` dell'esercizio 0607.)

### Es0310 · Switch con blocchi e `yield` `[L2]`
**File:** `Es0310_SwitchYield.java`

**Obiettivo:** scrivere rami di switch expression che contengono più istruzioni.

**Prima di iniziare:** nella switch expression, se un ramo ha bisogno di più istruzioni usa un blocco `{ ... }` e restituisce il valore con `yield`. Se il ramo è un'espressione singola basta la freccia.

**Cosa fare:**
1. Scrivi `static String messaggio(Stato stato)` come switch expression sull'enum `Stato`.
2. `LIBERO` restituisce direttamente `"Il posto è libero"`.
3. `IN_ATTESA` esegue un blocco: stampa un log (`System.out.println("[LOG] posto in attesa")`), calcola un messaggio in una variabile locale e lo restituisce con `yield`.
4. `VENDUTO` restituisce `"Il posto è venduto"`.
5. Chiama il metodo con i tre stati e verifica che il log compaia **solo** per `IN_ATTESA`.
6. Prova a usare `return` al posto di `yield` dentro il blocco: leggi l'errore.

**Cosa osservare:** non serve `default` perché i tre valori dell'enum sono tutti coperti.

**Verifica:**
- [ ] Il log compare una volta sola.
- [ ] Hai letto l'errore del `return` nello switch expression.

**Rifletti:** quando `yield` è necessario e quando basta la freccia? In C# un ramo con più istruzioni si scrive in modo diverso: come?

### Es0311 · `instanceof` e pattern matching `[L3]`
**File:** `Es0311_PatternMatching.java`

**Obiettivo:** riconoscere il tipo di un oggetto senza cast espliciti.

**Prima di iniziare:** `if (o instanceof Posto p)` controlla il tipo e dichiara `p` già convertito. Con Java 21 lo switch accetta anche *pattern* (`case Posto p ->`) e condizioni (`when`). Un `case null` gestisce esplicitamente il `null`.

**Cosa fare:**
1. Crea due record (`Posto(String codice, BigDecimal prezzo)`, `Evento(String nome)`).
2. Scrivi `static String descrivi(Object o)` con una catena di `instanceof` con binding: `Posto`, `Evento`, `String`, `Integer`, e un caso finale per qualsiasi altro tipo. Gestisci `null` all'inizio.
3. Chiamalo con `new Posto("A1", ...)`, `new Evento("Concerto")`, `"ciao"`, `42`, `3.14`, `null`.
4. Riscrivi lo stesso metodo con uno **switch su pattern**: `case Posto p ->`, `case Evento e ->`, `case String s ->`, `case Integer i when i > 100 ->`, `case Integer i ->`, `case null ->`, `default ->`.
5. Prova a invertire l'ordine di due casi (per esempio `Integer i` prima di `Integer i when i > 100`) e leggi l'errore.

**Cosa osservare:** il caso più specifico (con `when`) deve venire prima di quello generico.

**Verifica:**
- [ ] Le due versioni producono lo stesso output per le sei chiamate.
- [ ] `descrivi(null)` non lancia `NullPointerException`.
- [ ] Hai letto l'errore del punto 5.

**Rifletti:** quando conviene il polimorfismo (un metodo sull'oggetto) e quando il pattern matching? Cosa corrisponde in C# (`is`, `switch` con pattern)?

### Es0312 · `sealed` e risultati `[L3]` · *progetto*
**File:** `Es0312_SealedRisultato.java`

**Obiettivo:** modellare l'esito di un'operazione con un tipo che elenca tutti i casi possibili, e lasciar controllare al compilatore che li gestisci tutti.

**Prima di iniziare:** un'interfaccia `sealed` dichiara con `permits` quali tipi possono implementarla. Con un `record` per ogni caso e uno switch su pattern **senza `default`**, il compilatore verifica che tu abbia coperto tutti i casi.

**Cosa fare:**
1. Dichiara `sealed interface EsitoHold permits HoldRiuscito, HoldRifiutato`.
2. Crea `record HoldRiuscito(String posto, Instant scadenza) implements EsitoHold` e `record HoldRifiutato(String motivo) implements EsitoHold`.
3. Scrivi `static EsitoHold tentaHold(String posto, boolean libero)` che restituisce uno dei due esiti.
4. Scrivi `static String messaggio(EsitoHold esito)` con una switch expression su pattern **senza** `default`.
5. Aggiungi un terzo record `HoldInCoda(int posizione)` che implementa l'interfaccia e **osserva l'errore del compilatore** nello switch; poi aggiungi il ramo mancante.
6. Prova ad aggiungere un'implementazione dell'interfaccia in un file diverso senza metterla in `permits`.

**Cosa osservare:** l'errore di compilazione ti dice quale caso manca.

**Verifica:**
- [ ] Lo switch non ha `default` e compila con tre casi.
- [ ] Hai visto i due errori dei punti 5 e 6.

**Rifletti:** quando restituire un esito (tipo *sum*) invece di lanciare un'eccezione? Cosa ti dà in più rispetto a un `enum`?


## Cosa devi aver capito

- [ ] `record` = dati immutabili con `equals/hashCode/toString` gratis; accessor senza `get`.
- [ ] Il *compact constructor* per validare; non esiste `with`.
- [ ] Un `enum` è una classe con campi, metodi e comportamento per costante.
- [ ] `ordinal()` non si persiste; `valueOf` lancia eccezione se sbagli.
- [ ] Switch expression: frecce, `yield`, esaustività senza `default` su `enum` e `sealed`.
