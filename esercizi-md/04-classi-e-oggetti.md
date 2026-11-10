# 04 · Classi e oggetti

Obiettivo: scrivere classi Java "alla Java" (campi privati, getter/setter, `equals/hashCode`, immutabilità) e capire cosa fa Lombok al posto tuo.

## Sintassi a confronto

```csharp
// C#
public class Studente
{
    public string Nome { get; }
    public int Anno { get; set; }

    public Studente(string nome, int anno) { Nome = nome; Anno = anno; }
    public Studente(string nome) : this(nome, 1) { }

    public override string ToString() => $"{Nome} ({Anno})";
}
```

```java
// Java
public class Studente {
    private final String nome;                 // niente proprietà: campo + metodi
    private int anno;

    public Studente(String nome, int anno) {
        this.nome = nome;                      // "this" serve se parametro e campo hanno lo stesso nome
        this.anno = anno;
    }
    public Studente(String nome) { this(nome, 1); }   // this(...) al posto di : this(...)

    public String getNome() { return nome; }
    public int getAnno() { return anno; }
    public void setAnno(int anno) { this.anno = anno; }

    @Override
    public String toString() { return "%s (%d)".formatted(nome, anno); }
}
```

**Modificatori di accesso**

| Java | C# | Visibile da |
| --- | --- | --- |
| `public` | `public` | ovunque |
| `protected` | `protected internal` (approssimato) | sottoclassi **e tutto il package** |
| *(nessuno)* | `internal` (approssimato) | solo lo stesso package |
| `private` | `private` | solo la stessa classe |

**Metodi di `Object` che ogni classe eredita**: `toString()`, `equals(Object)`, `hashCode()`, `getClass()`. Il contratto: se `a.equals(b)` allora `a.hashCode() == b.hashCode()`.

**Costruire senza `new` esterno**: costruttore `private` più metodi `static` di *factory* (`Posto.of(...)`), oppure un *builder*.

## Esercizi

Ogni esercizio segue lo stesso schema: **Obiettivo**, **Prima di iniziare** (il concetto in breve), **Cosa fare** (passi in ordine), **Cosa osservare**, **Verifica** (checklist) e **Rifletti** (domande da colloquio).

### Es0401 · Prima classe `[L1]`
**File:** `Es0401_Posto.java` (con un `main` per provarla)

**Obiettivo:** scrivere una classe Java completa partendo da una C#.

**Prima di iniziare:** in Java non ci sono le proprietà `{ get; set; }`. Si dichiara un campo `private`, un costruttore e dei metodi `getX()`/`setX()`. La sola sostituzione per `ToString()` è `toString()`, con `@Override`.

**Cosa fare:**
1. Traduci questa classe C# in Java. Segui le convenzioni (campi `camelCase`, metodi `camelCase`, `String` maiuscola):
   ```csharp
   public class Posto
   {
       public string Codice { get; }
       public long EventoId { get; }
       public decimal Prezzo { get; set; }

       public Posto(string codice, long eventoId, decimal prezzo)
       {
           Codice = codice; EventoId = eventoId; Prezzo = prezzo;
       }

       public override string ToString() => $"{Codice} (evento {EventoId}) {Prezzo:F2}";
   }
   ```
2. I campi che nella versione C# hanno solo `get` diventano `private final`; `Prezzo` ha anche il setter.
3. Nel `toString` formatta il prezzo con due decimali usando `.formatted(...)`.
4. Nel `main` crea tre posti, stampali e modifica il prezzo del primo.
5. Prova a chiamare `setCodice(...)`: non esiste. Scrivi in un commento perché.
6. Prova a togliere `@Override` dal `toString` e poi a scrivere `tostring` (minuscolo): osserva la differenza nel comportamento della stampa.

**Cosa osservare:** senza `toString` valido la stampa mostra qualcosa come `Posto@1b6d3586`.

**Verifica:**
- [ ] Tre posti creati e stampati con il formato corretto.
- [ ] Nessun setter per i campi `final`.
- [ ] Hai provato l'errore di battitura nel `toString`.

**Rifletti:** quante righe in più servono rispetto a C#? Come potresti ridurle (`record`, Lombok)?

### Es0402 · `equals` e `hashCode` a mano `[L2]`
**File:** `Es0402_EqualsHashCode.java`

**Obiettivo:** vedere cosa si rompe nelle collezioni quando `equals` e `hashCode` non sono coerenti.

**Prima di iniziare:** `HashSet` e `HashMap` usano prima `hashCode()` per scegliere un "cassetto" e poi `equals()` per confrontare gli elementi nel cassetto. Il contratto: **se `a.equals(b)` allora `a.hashCode() == b.hashCode()`**. Se lo violi, due oggetti "uguali" finiscono in cassetti diversi e la collezione non li riconosce.

**Cosa fare:**
1. Riusa la classe `Posto` dell'esercizio precedente (o copiala in un nuovo file).
2. Due posti sono **uguali** se hanno lo stesso `eventoId` e lo stesso `codice` (il prezzo non conta).
3. **Versione A**: scrivi solo `equals` (con la firma `public boolean equals(Object o)`). Crea due posti uguali, mettine uno in un `HashSet<Posto>` e stampa `set.contains(secondo)` e, dopo averli aggiunti entrambi, `set.size()`.
4. **Versione B**: aggiungi `hashCode` con `Objects.hash(eventoId, codice)` e ripeti le stesse stampe.
5. Fai lo stesso con una `HashMap<Posto, String>`: `put` con il primo, `get` con il secondo.
6. Prova a scrivere `equals(Posto p)` (con parametro `Posto` invece di `Object`) e osserva cosa succede nel `HashSet` e con `@Override`.
7. Stampa `hashCode()` dei due posti uguali e di uno diverso.

**Cosa osservare:** con la sola `equals` la `size()` del set è 2.

**Verifica:**
- [ ] Hai tre risultati diversi (A, B, `equals(Posto)`) e li hai spiegati.
- [ ] `HashMap.get` restituisce il valore atteso solo nella versione B.

**Rifletti:** perché `equals` ha la firma `equals(Object o)`? Quali campi devono entrare in `hashCode`? È un problema se un campo usato nell'`hashCode` cambia dopo l'inserimento in un `HashSet`?

### Es0403 · Riferimenti e passaggio dei parametri `[L2]`
**File:** `Es0403_PassaggioParametri.java`

**Obiettivo:** capire che Java passa sempre **per valore**, anche quando passa oggetti.

**Prima di iniziare:** quando passi un oggetto a un metodo, il metodo riceve una **copia del riferimento**. Con quella copia può modificare l'oggetto (`p.setPrezzo(...)`), ma se riassegna il parametro (`p = new Posto(...)`) cambia solo la copia locale. Non esiste `ref` né `out`.

**Cosa fare:**
1. Usa la classe `Posto` con `prezzo` modificabile.
2. Scrivi `static void modificaPrezzo(Posto p)` che imposta il prezzo a `99`.
3. Scrivi `static void sostituisci(Posto p)` che fa `p = new Posto("ZZ", 0, new BigDecimal("1"));`.
4. Scrivi `static void scambia(Posto a, Posto b)` che prova a scambiare i due riferimenti usando una variabile temporanea.
5. Nel `main` crea `p1` e `p2`. **Prima di eseguire**, scrivi in un commento cosa vedrai stampando `p1` e `p2` dopo ognuna delle tre chiamate.
6. Esegui e confronta.
7. Fai lo stesso con un `int` e una `String` come parametri: modifica il parametro dentro il metodo e osserva che il chiamante non cambia.
8. Scrivi `static Posto[] scambiaArray(Posto[] coppia)` che scambia gli elementi **dentro** un array e verifica che funziona. Spiega perché.

**Cosa osservare:** nel caso `scambia` il chiamante non vede nessun cambiamento.

**Verifica:**
- [ ] Previsioni scritte prima.
- [ ] Sai spiegare le differenze tra i tre metodi e il punto 8.

**Rifletti:** come ottieni in Java quello che in C# fai con `out` (restituire più valori)? (Suggerimento: un `record`.)

### Es0404 · Costruttori e ordine di inizializzazione `[L2]`
**File:** `Es0404_Costruttori.java`

**Obiettivo:** conoscere l'ordine esatto in cui Java inizializza una classe e i suoi oggetti.

**Prima di iniziare:** all'inizio i membri `static` e i blocchi `static { }` vengono eseguiti **una sola volta** quando la classe è caricata. A ogni `new`, vengono eseguiti nell'ordine in cui sono scritti gli inizializzatori dei campi e i blocchi di istanza `{ }`, e **poi** il corpo del costruttore. Un costruttore può chiamarne un altro con `this(...)`, che deve essere la prima istruzione.

**Cosa fare:**
1. Crea una classe con: un campo statico inizializzato da un metodo che stampa `"campo statico"`, un blocco `static { }` che stampa `"blocco statico"`, un campo di istanza inizializzato da un metodo che stampa `"campo istanza"`, un blocco di istanza `{ }` che stampa `"blocco istanza"`.
2. Aggiungi due costruttori: uno senza parametri che chiama `this(1)` e stampa `"costruttore senza parametri"`, e uno con un parametro che stampa `"costruttore con parametro"`.
3. Metti il `main` in una **classe separata** (stesso file o file diverso). Nel `main` stampa `"inizio main"`, poi crea **due** oggetti con `new` e stampa `"fine main"`.
4. **Prima di eseguire**, scrivi nel commento l'ordine esatto delle righe che vedrai.
5. Esegui e confronta.
6. Sposta il blocco di istanza **sopra** e **sotto** il campo di istanza e osserva se cambia l'ordine.

**Cosa osservare:** le righe statiche compaiono una sola volta, prima del primo `new` (non prima di `"inizio main"`, se la classe è un'altra).

**Verifica:**
- [ ] Ordine previsto per iscritto.
- [ ] Hai provato anche lo spostamento del punto 6.

**Rifletti:** quando serve davvero un blocco `static`? Qual è l'equivalente in C# (costruttore statico)?

### Es0405 · Classe di utilità e costanti `[L1]`
**File:** `Es0405_Codici.java`

**Obiettivo:** scrivere una classe di sole funzioni statiche, dato che `static class` non esiste in Java.

**Prima di iniziare:** per impedire di creare istanze di una classe di utilità, si rende `final` (nessuno può ereditarla) e si dichiara un costruttore `private`. Le costanti si scrivono `public static final` in MAIUSCOLO.

**Cosa fare:**
1. Crea `public final class Codici` con costruttore `private`.
2. Aggiungi la costante `public static final int MAX_POSTI_PER_FILA = 20;` e `public static final char ULTIMA_FILA = 'E';`.
3. Aggiungi `static String codice(char fila, int numero)` (restituisce `A12`), `static char fila(String codice)`, `static int numero(String codice)`.
4. Ogni metodo valida i parametri e lancia `IllegalArgumentException` se sono fuori range (usa le costanti).
5. Da una **seconda classe** (nello stesso package) chiama i metodi e prova `new Codici()`: leggi l'errore.
6. Prova a fare `class Estesa extends Codici` e leggi l'errore.

**Cosa osservare:** le due classi sono in file diversi; l'import non serve nello stesso package.

**Verifica:**
- [ ] I tre metodi funzionano e rifiutano input non validi.
- [ ] Hai letto i due errori (istanziare ed estendere).

**Rifletti:** perché la classe è `final`? In Spring le classi di utilità sono comuni o si preferiscono i bean?

### Es0406 · Classe immutabile con copia difensiva `[L2]`
**File:** `Es0406_EventoImmutabile.java`

**Obiettivo:** impedire che una classe "immutabile" venga modificata dall'esterno attraverso una collezione che espone.

**Prima di iniziare:** campi `final` e assenza di setter non bastano se un campo è un oggetto modificabile (una `List`): chi riceve il riferimento può cambiarla. Servono due difese: copiare la lista in ingresso (`List.copyOf`) e restituirne una vista non modificabile in uscita.

**Cosa fare:**
1. Crea `Evento` con `nome` (`String`), `data` (`LocalDateTime`) e `posti` (`List<String>` di codici), tutti `final`, senza setter.
2. **Versione A (ingenua)**: nel costruttore `this.posti = posti;` e il getter restituisce `posti`.
3. Nel `main` crea una lista, un evento, poi modifica la **lista originale** (aggiungi un posto) e stampa l'evento. Poi chiama `evento.getPosti().add("Z99")` e stampa di nuovo.
4. **Versione B (difensiva)**: nel costruttore `List.copyOf(posti)`; nel getter restituisci la lista così com'è (è già non modificabile) oppure `Collections.unmodifiableList`.
5. Ripeti le due modifiche del punto 3 e stampa. Annota il tipo di eccezione che compare.
6. Prova con `new ArrayList<>(posti)` nel costruttore e `Collections.unmodifiableList(posti)` nel getter: scrivi la differenza tra una **copia** e una **vista**.

**Cosa osservare:** nella versione A la lista dell'evento cambia senza che tu abbia toccato l'evento.

**Verifica:**
- [ ] Nella versione B nessuna delle due modifiche cambia l'evento.
- [ ] Hai spiegato la differenza tra copia e vista.

**Rifletti:** `List.copyOf` accetta elementi `null`? (Prova.) Perché i `record` non risolvono da soli questo problema?

### Es0407 · Builder `[L3]`
**File:** `Es0407_Builder.java`

**Obiettivo:** costruire oggetti con molti campi opzionali in modo leggibile.

**Prima di iniziare:** un *builder* è una classe di appoggio con un metodo per ogni campo (che restituisce `this`, per concatenare le chiamate) e un metodo `build()` che crea l'oggetto, validandolo. È il pattern dietro `@Builder` di Lombok e dietro molti client HTTP.

**Cosa fare:**
1. Crea la classe `Prenotazione` con campi `final`: `eventoId` (obbligatorio), `posto` (obbligatorio), `utente` (obbligatorio), `creataIl` (`Instant`, opzionale, default adesso), `note` (opzionale, default vuota).
2. Costruttore **privato** che accetta il builder.
3. Classe statica interna `Builder` con i metodi `eventoId(...)`, `posto(...)`, `utente(...)`, `creataIl(...)`, `note(...)`, ciascuno che restituisce il builder.
4. Metodo `build()` che verifica i campi obbligatori e lancia un'eccezione con un messaggio che elenca **tutti** i campi mancanti.
5. Metodo statico `Prenotazione.builder()`.
6. Nel `main` crea una prenotazione con la sintassi concatenata; poi prova un `build()` senza utente e senza posto.

**Cosa osservare:** il messaggio d'errore elenca entrambi i campi mancanti.

**Verifica:**
- [ ] `Prenotazione.builder().eventoId(1L).posto("A12").utente("u1").build()` funziona.
- [ ] Il `build()` incompleto dà un messaggio con i campi mancanti.
- [ ] Il costruttore di `Prenotazione` non è accessibile dall'esterno.

**Rifletti:** quando serve un builder e quando basta un `record`? Cosa genererebbe l'annotazione `@Builder` di Lombok?

### Es0408 · Modificatori di accesso tra package `[L2]`
**File:** cartelle `studio/a/` e `studio/b/` con più file (`Es0408_Base.java`, `Es0408_Figlia.java`, `Es0408_Esterna.java`)

**Obiettivo:** conoscere con certezza chi può vedere cosa, soprattutto il livello senza modificatore.

**Prima di iniziare:** `public` è visibile ovunque; `protected` dentro il package **e** nelle sottoclassi (anche di altri package); senza modificatore solo dentro lo stesso package (*package-private*); `private` solo dentro la classe.

**Cosa fare:**
1. Nel package `studio.a` crea una classe `Base` con quattro metodi: `public`, `protected`, senza modificatore e `private`.
2. Nel package `studio.a` crea una classe `StessoPackage` che prova a chiamarli tutti e quattro.
3. Nel package `studio.b` crea `Esterna` (non è una sottoclasse) che prova a chiamarli, e `Figlia extends Base` che li chiama sul proprio `this`.
4. **Prima di compilare**, compila una tabella nel commento (righe: i quattro metodi, colonne: `StessoPackage`, `Esterna`, `Figlia`) con "compila" o "non compila".
5. Scrivi poi il codice e verifica la tabella con gli errori di IntelliJ.
6. In `Figlia`, prova a chiamare il metodo `protected` **su un'istanza di `Base`** (non su `this`): leggi l'errore.

**Cosa osservare:** il punto 6 è la trappola più sottile di `protected`.

**Verifica:**
- [ ] La tabella è scritta prima e corretta alla fine.
- [ ] Hai spiegato il caso del punto 6.

**Rifletti:** perché `protected` in Java dà accesso anche agli altri classi del package, a differenza di C#? Quale modificatore userai quasi sempre per i campi?

### Es0409 · Classi annidate, interne e anonime `[L3]`
**File:** `Es0409_ClassiAnnidate.java`

**Obiettivo:** riconoscere i quattro tipi di classe dichiarata dentro un'altra e sapere quando servono.

**Prima di iniziare:** una `static class` annidata è una classe normale "ospitata" (come i *nested types* di C#). Una classe **interna** (non `static`) tiene un riferimento nascosto all'oggetto esterno e può leggerne i campi. Una classe **anonima** è un'implementazione senza nome creata sul posto. Una **lambda** è la versione breve per le interfacce funzionali.

**Cosa fare:**
1. Crea una classe `Esterna` con un campo `String nome` e un metodo che usa le classi seguenti.
2. Dentro `Esterna`: una `static class Annidata` con un metodo che stampa "annidata"; una `class Interna` il cui metodo stampa il campo `nome` di `Esterna`.
3. Nel `main`, crea un'istanza di `Annidata` (senza istanza di `Esterna`) e un'istanza di `Interna` (che richiede un'istanza di `Esterna`: la sintassi è `esterna.new Interna()`).
4. Crea una **classe anonima** che implementa `Runnable` (`new Runnable() { public void run() { ... } }`), eseguila con `run()`.
5. Scrivi la stessa cosa con una lambda.
6. Spiega in un commento: quale delle due classi annidate può causare un *memory leak* tenendo in vita l'oggetto esterno e perché.

**Cosa osservare:** la classe interna non si crea senza un oggetto esterno.

**Verifica:**
- [ ] Tutte e quattro le versioni eseguono.
- [ ] Hai scritto la sintassi `esterna.new Interna()`.

**Rifletti:** in Spring le classi `static` annidate sono comuni per i DTO. Perché si preferisce `static`?

### Es0410 · Entity in stile JPA `[L2]` · *progetto*
**File:** `Es0410_EntityStyle.java`

**Obiettivo:** scrivere una classe come ti verrà chiesto da JPA, e capire i problemi di `equals` con l'`id` assente.

**Prima di iniziare:** JPA richiede un costruttore senza argomenti (può essere `protected`), campi non `final` e un `id` che vale `null` finché l'entity non è stata salvata. Questo rende delicato `equals/hashCode` basato sull'`id`.

**Cosa fare:**
1. Crea `Event` con `Long id` (nullo all'inizio), `String name`, `LocalDateTime eventDate`.
2. Costruttore senza argomenti `protected` e costruttore pubblico con `name` e `eventDate`.
3. Getter per tutti i campi; un metodo di **package** (`void assegnaId(Long id)`) per simulare il salvataggio.
4. `equals` e `hashCode` basati su `id`: se l'`id` è `null`, due oggetti diversi sono uguali solo se sono lo stesso oggetto.
5. Nel `main` crea un evento, mettilo in un `HashSet`, simula il salvataggio assegnando l'id e verifica `set.contains(evento)`.
6. Ripeti con `equals/hashCode` basati su `name` + `eventDate` (chiave naturale) e confronta i comportamenti.

**Cosa osservare:** con `hashCode` basato su `id`, l'oggetto "sparisce" dall'`HashSet` dopo l'assegnazione dell'id.

**Verifica:**
- [ ] Hai riprodotto l'oggetto "perso" nel set.
- [ ] Hai scritto in un commento le tre strategie possibili per `equals` di una entity.

**Rifletti:** perché `@Data` di Lombok è sconsigliato sulle entity JPA? Cosa raccomanda la documentazione di Hibernate?

### Es0411 · `getClass` e `instanceof` in `equals` `[L2]`
**File:** `Es0411_GetClass.java`

**Obiettivo:** capire come l'ereditarietà complica `equals`.

**Prima di iniziare:** `a.getClass() == b.getClass()` richiede che le due classi siano **esattamente** la stessa; `b instanceof Posto` accetta anche le sottoclassi. Il contratto di `equals` richiede simmetria (`a.equals(b)` se e solo se `b.equals(a)`).

**Cosa fare:**
1. Crea `Posto(codice)` con `equals` basato su `getClass()` e `PostoVip extends Posto`.
2. Crea `new Posto("A1")` e `new PostoVip("A1")` e stampa `p.equals(v)` e `v.equals(p)`.
3. Riscrivi `equals` di `Posto` usando `instanceof` e ripeti le stampe.
4. Aggiungi a `PostoVip` un campo `lounge` e un suo `equals` che lo include (con `instanceof`). Rivaluta `p.equals(v)` e `v.equals(p)`: la simmetria regge?
5. Scrivi nel commento quale scelta eviterebbe il problema (hint: `final class`, record, composizione).

**Cosa osservare:** al punto 4 i due confronti danno risultati diversi.

**Verifica:**
- [ ] Hai la tabella dei quattro confronti per le due versioni.
- [ ] Hai spiegato quale proprietà del contratto viene violata.

**Rifletti:** perché per le entity JPA con proxy Hibernate il confronto con `getClass()` è problematico?

### Es0412 · Lombok `[L2]`
**File:** `Es0412_Lombok.java`

**Obiettivo:** ridurre il codice ripetitivo con Lombok e capire cosa genera.

**Prima di iniziare:** Lombok è un *annotation processor*: genera codice (getter, costruttori, `equals`...) **in fase di compilazione**. Perché IntelliJ lo riconosca serve il plugin Lombok (incluso nelle versioni recenti) e l'opzione *Enable annotation processing* nelle impostazioni del compilatore.

**Cosa fare:**
1. Aggiungi la dipendenza `org.projectlombok:lombok` (scope `provided`, come indicato nella documentazione ufficiale) al `pom.xml` e ricarica Maven.
2. In IntelliJ abilita *Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing*.
3. Riscrivi la classe `Posto` dell'Es0401 con `@Getter`, `@Setter` (solo sul prezzo), `@ToString`, `@EqualsAndHashCode` (solo `eventoId` e `codice`) e `@AllArgsConstructor`.
4. Verifica che compili e che il `main` dell'Es0401 funzioni senza modifiche.
5. Apri la vista *Structure* di IntelliJ e controlla che i metodi generati compaiano.
6. Prova `@Data` e `@Value` su due classi di prova e leggi cosa generano; scrivi un commento sulle differenze.

**Cosa osservare:** il file `.class` in `target/classes` contiene i metodi che non hai scritto (puoi ispezionarlo con `javap -p`).

**Verifica:**
- [ ] La classe ha 6 righe di codice in meno e lo stesso comportamento.
- [ ] Hai visto i metodi generati nella vista Structure.

**Rifletti:** quando preferisci un `record` a Lombok? Quali sono i rischi di `@Data` e `@Setter` ovunque (soprattutto sulle entity)?


## Cosa devi aver capito

- [ ] Niente proprietà: campo `private` + getter/setter (o `record`).
- [ ] Il contratto tra `equals` e `hashCode`, e cosa si rompe in `HashSet` e `HashMap`.
- [ ] Java passa tutto per valore; i riferimenti si copiano.
- [ ] Non esiste `static class` né `ref`.
- [ ] Copia difensiva e vista non modificabile per l'immutabilità.
- [ ] Una entity ha `id` nullo e un costruttore senza argomenti.
