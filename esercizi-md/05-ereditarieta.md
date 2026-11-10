# 05 · Ereditarietà e polimorfismo

Obiettivo: usare `extends`, `super`, classi astratte e polimorfismo, e conoscere le differenze con C# (metodi virtuali di default, campi non polimorfici, `final`/`sealed`).

## Sintassi a confronto

```csharp
// C#
public abstract class Animale
{
    protected string Nome;
    protected Animale(string nome) { Nome = nome; }
    public abstract string Verso();
    public virtual string Presenta() => $"{Nome}: {Verso()}";
}
public class Cane : Animale
{
    public Cane(string nome) : base(nome) { }
    public override string Verso() => "bau";
}
```

```java
// Java
public abstract class Animale {
    protected String nome;
    protected Animale(String nome) { this.nome = nome; }
    public abstract String verso();
    public String presenta() { return nome + ": " + verso(); }   // già "virtuale"
}
public class Cane extends Animale {                               // extends al posto di ":"
    public Cane(String nome) { super(nome); }                     // super(...) al posto di ": base(...)"
    @Override                                                     // facoltativa, ma obbligatoria per buona pratica
    public String verso() { return "bau"; }
}
```

| Tema | C# | Java |
| --- | --- | --- |
| Metodo ridefinibile | `virtual` | **tutti** i metodi non `final`, non `static`, non `private` |
| Ridefinire | `override` | `@Override` (annotazione) |
| Impedire l'override | `sealed override` | `final` |
| Impedire l'ereditarietà | `sealed class` | `final class` |
| Limitare chi può ereditare | nessun equivalente diretto | `sealed ... permits ...` (Java 17) |
| Ereditarietà multipla | no (solo interfacce) | no (solo interfacce) |
| Nascondere un membro | `new` | non c'è: i **campi** e i metodi `static` si *nascondono*, non si ridefiniscono |
| Chiamare il padre | `base.M()` | `super.m()` |
| Ritorno covariante | sì (C# 9) | sì |
| Tutto deriva da | `object` | `Object` |

## Esercizi

Ogni esercizio segue lo stesso schema: **Obiettivo**, **Prima di iniziare** (il concetto in breve), **Cosa fare** (passi in ordine), **Cosa osservare**, **Verifica** (checklist) e **Rifletti** (domande da colloquio).

### Es0501 · Prima gerarchia `[L1]`
**File:** `Es0501_Gerarchia.java`

**Obiettivo:** creare una classe figlia con `extends` e vedere il polimorfismo in azione.

**Prima di iniziare:** una variabile di tipo `Posto` può contenere un `PostoVip` (che *è un* `Posto`). Quando chiami un metodo, Java sceglie la versione dell'oggetto **reale**, non quella del tipo della variabile. Succede a runtime e si chiama *dynamic dispatch*.

**Cosa fare:**
1. Crea la classe `Posto` con `codice` e `prezzo`, un costruttore e un metodo `String descrizione()` che restituisce `"Posto " + codice`.
2. Crea `PostoVip extends Posto` con un campo `bool lounge` (usa `boolean`) e `descrizione()` ridefinita (con `@Override`) che aggiunge `" (VIP, lounge: sì/no)"`.
3. Crea `PostoDisabili extends Posto` con una descrizione che aggiunge `" (accessibile)"`.
4. Nel `main` crea una `List<Posto>` e inseriscivi due posti normali, un VIP e un posto per disabili.
5. Stampa la `descrizione()` di ciascun elemento con un ciclo `for-each`.
6. Prova ad accedere a `lounge` su un elemento della lista (variabile di tipo `Posto`): leggi l'errore del compilatore.

**Cosa osservare:** il ciclo chiama sempre `posto.descrizione()` ma stampa descrizioni diverse.

**Verifica:**
- [ ] Ogni elemento stampa la propria descrizione.
- [ ] Hai letto l'errore del punto 6 e sai spiegare perché il compilatore "non vede" `lounge`.

**Rifletti:** in che momento Java sceglie la versione del metodo da chiamare? (Compilazione o esecuzione?) Cosa succede se `descrizione()` è `static`?

### Es0502 · Ordine dei costruttori `[L2]`
**File:** `Es0502_OrdineCostruttori.java`

**Obiettivo:** conoscere l'ordine di esecuzione dei costruttori in una gerarchia e il pericolo di chiamare metodi ridefinibili dal costruttore.

**Prima di iniziare:** `new C()` esegue prima il costruttore (e i blocchi di istanza) della classe più "in alto", poi scende fino a `C`. Se il costruttore del padre chiama un metodo ridefinito dalla figlia, il metodo della figlia viene eseguito **prima** che i campi della figlia siano inizializzati.

**Cosa fare:**
1. Crea tre classi `A`, `B extends A`, `C extends B`. Ognuna ha un blocco di istanza e un costruttore senza parametri, che stampano `"A blocco"`, `"A costruttore"`, ecc.
2. Prevedi per iscritto l'output di `new C()` e poi eseguilo.
3. Aggiungi in `A` un metodo `void init()` che stampa `"A.init"` e chiamalo dal costruttore di `A`.
4. Ridefinisci `init()` in `C`: la versione di `C` stampa il valore di un campo `String nome = "Cane"` dichiarato in `C`.
5. Prevedi e poi stampa cosa vede `init()` quando viene chiamato dal costruttore di `A`.
6. Rendi il campo `final` e inizializzalo nel costruttore: cambia qualcosa?

**Cosa osservare:** al punto 5 il campo `nome` è ancora `null`.

**Verifica:**
- [ ] L'ordine delle stampe del punto 2 è stato previsto correttamente (o hai corretto la regola).
- [ ] Hai spiegato il valore `null` del punto 5.

**Rifletti:** perché chiamare metodi ridefinibili dal costruttore è considerato pericoloso? Succede lo stesso in C#?

### Es0503 · `@Override` ti protegge `[L1]`
**File:** `Es0503_Override.java`

**Obiettivo:** capire perché in Java si usa sempre `@Override` e come interagiscono visibilità e override.

**Prima di iniziare:** in Java qualsiasi metodo con la stessa firma del padre lo ridefinisce. Se sbagli una lettera nel nome, non ridefinisci nulla: crei un nuovo metodo e il codice compila lo stesso. L'annotazione `@Override` fa fallire la compilazione in quel caso.

**Cosa fare:**
1. Usa `Posto` e `PostoVip` dell'Es0501.
2. In `PostoVip` aggiungi `public String descrizzione()` (con due "z") **senza** `@Override`. Compila? Chiamalo da una variabile di tipo `Posto`: cosa succede?
3. Aggiungi `@Override` e leggi l'errore.
4. Correggi il nome.
5. Prova a ridurre la visibilità: in `Posto` il metodo è `public`; in `PostoVip` prova `protected`. Leggi l'errore.
6. Prova ad aumentare la visibilità: in `Posto` il metodo è `protected`, in `PostoVip` `public`: compila?
7. Prova a cambiare il tipo di ritorno del metodo ridefinito da `String` a `int`: leggi l'errore.

**Cosa osservare:** le tre regole dell'override: stesso nome e parametri, visibilità uguale o maggiore, tipo di ritorno compatibile.

**Verifica:**
- [ ] Hai visto tre errori di compilazione diversi.
- [ ] Hai scritto le tre regole in un commento.

**Rifletti:** perché in Spring userai `@Override` dappertutto (per esempio quando implementi interfacce o ridefinisci `toString`)?

### Es0504 · I campi non sono polimorfici `[L3]`
**File:** `Es0504_CampiNascosti.java`

**Obiettivo:** scoprire che solo i metodi di istanza sono polimorfici, non i campi né i metodi statici.

**Prima di iniziare:** se una sottoclasse dichiara un campo con lo stesso nome del padre, i due campi coesistono (il secondo *nasconde* il primo). Quale dei due leggi dipende dal **tipo della variabile**, non dall'oggetto. Lo stesso vale per i metodi `static`.

**Cosa fare:**
1. Scrivi `class A { String nome = "A"; String getNome() { return nome; } static String tipo() { return "tipo A"; } }`.
2. Scrivi `class B extends A { String nome = "B"; @Override String getNome() { return nome; } static String tipo() { return "tipo B"; } }`.
3. Nel `main`: `A a = new B();`.
4. Prevedi per iscritto cosa stampano queste quattro espressioni: `a.nome`, `a.getNome()`, `a.tipo()`, `((B) a).nome`.
5. Esegui e confronta.
6. Prova a togliere `static` dal `tipo()` di `B`: leggi l'errore.

**Cosa osservare:** `a.nome` e `a.getNome()` danno risultati diversi.

**Verifica:**
- [ ] 4 previsioni scritte prima.
- [ ] Hai spiegato perché i campi privati con getter evitano questa trappola.

**Rifletti:** che implicazioni ha per l'incapsulamento? Come ti proteggi in pratica?

### Es0505 · Classe astratta e *template method* `[L2]` · *progetto*
**File:** `Es0505_Biglietto.java`

**Obiettivo:** definire un algoritmo nella classe base lasciando alle figlie i passaggi variabili.

**Prima di iniziare:** una classe `abstract` non si può istanziare e può dichiarare metodi `abstract` (senza corpo) che le sottoclassi devono implementare. Un metodo concreto che chiama quelli astratti è un *template method*.

**Cosa fare:**
1. Crea `abstract class Biglietto` con campi `String codicePosto` e `BigDecimal prezzoBase`, un costruttore e un metodo astratto `BigDecimal prezzoFinale()`.
2. Aggiungi il metodo concreto `String ricevuta()` che restituisce due righe: il codice del posto e il prezzo finale (usa `prezzoFinale()`, formattato a due decimali).
3. Crea tre sottoclassi: `BigliettoIntero` (nessuna variazione), `BigliettoRidotto` (con un campo `percentuale`, sconto sul prezzo), `BigliettoVip` (con un campo `supplemento`, aggiunto al prezzo).
4. Nessuna sottoclasse deve ridefinire `ricevuta()`.
5. Nel `main` crea una `List<Biglietto>` con 4 biglietti, stampa tutte le ricevute e il totale.
6. Prova a scrivere `new Biglietto(...)` e leggi l'errore.
7. Dimentica di implementare `prezzoFinale()` in una sottoclasse e leggi l'errore.

**Cosa osservare:** nessuna ripetizione del codice della ricevuta.

**Verifica:**
- [ ] Totale corretto con `BigDecimal` e due decimali.
- [ ] Hai letto i due errori (istanziare e non implementare).

**Rifletti:** quale vantaggio dà il *template method* rispetto a copiare il codice nelle sottoclassi? Quando preferiresti un'interfaccia?

### Es0506 · Chiamare il metodo del padre con `super` `[L1]`
**File:** `Es0506_Super.java`

**Obiettivo:** estendere il comportamento del padre invece di sostituirlo.

**Prima di iniziare:** `super.metodo()` chiama la versione del padre. È utile quando la figlia vuole aggiungere qualcosa prima o dopo, senza riscrivere tutto.

**Cosa fare:**
1. Riusa `Biglietto` e `BigliettoVip` dell'esercizio precedente.
2. In `BigliettoVip` ridefinisci `ricevuta()` in modo che stampi tutto ciò che stampa il padre **più** una riga finale `"Accesso al lounge incluso"`.
3. Usa `super.ricevuta()` e non copiare il testo del padre.
4. Poi scrivi una seconda versione che aggiunge la riga **prima** di quella del padre.
5. Prova a chiamare `super.prezzoFinale()` in `BigliettoVip` e leggi l'errore (è un metodo astratto).

**Cosa osservare:** l'errore del punto 5 spiega perché non si può chiamare un metodo senza corpo.

**Verifica:**
- [ ] Le due versioni (dopo e prima) producono output diversi.
- [ ] Nessun testo del padre è copiato.

**Rifletti:** quando è meglio chiamare `super` e quando riscrivere tutto? Quali rischi ha una sottoclasse che dipende dal modo in cui il padre implementa il metodo?

### Es0507 · `final` e `sealed` `[L2]`
**File:** `Es0507_FinalSealed.java`

**Obiettivo:** limitare l'estendibilità di classi e metodi.

**Prima di iniziare:** `final` su un metodo impedisce di ridefinirlo, su una classe impedisce di estenderla. `sealed ... permits ...` permette di estendere una classe o interfaccia solo ad un elenco chiuso di tipi.

**Cosa fare:**
1. In una classe base dichiara un metodo `final` e prova a ridefinirlo in una figlia: leggi l'errore.
2. Dichiara una `final class` e prova ad estenderla.
3. Dichiara `sealed interface Pagamento permits Carta, Bonifico, Contanti`.
4. Implementa l'interfaccia con tre `record`: `Carta(String ultime4)`, `Bonifico(String iban)`, `Contanti()`.
5. Prova a creare un quarto tipo `Satispay implements Pagamento` fuori dalla lista `permits`: leggi l'errore.
6. Scrivi un metodo che prende un `Pagamento` e restituisce una descrizione con uno switch su pattern **senza** `default`.
7. Aggiungi `Satispay` all'elenco `permits` e osserva l'errore nello switch.

**Cosa osservare:** hai tre errori diversi (`final` metodo, `final` classe, tipo non permesso).

**Verifica:**
- [ ] Hai annotato i tre messaggi.
- [ ] Lo switch senza `default` compila con tre tipi e si rompe con quattro.

**Rifletti:** perché un `record` non può estendere un'altra classe? Quando usare `sealed` invece di un `enum`?

### Es0508 · Cast e `instanceof` `[L2]`
**File:** `Es0508_Cast.java`

**Obiettivo:** convertire un tipo in uno più specifico senza errori a runtime.

**Prima di iniziare:** un *upcast* (da `PostoVip` a `Posto`) è sempre sicuro. Un *downcast* (da `Posto` a `PostoVip`) può fallire con `ClassCastException`. `instanceof` con binding (`p instanceof PostoVip v`) controlla e converte in un solo passo.

**Cosa fare:**
1. Con `Posto p = new Posto(...);` scrivi `PostoVip v = (PostoVip) p;` dentro un `try/catch (ClassCastException e)` e stampa il messaggio.
2. Con `Posto p = new PostoVip(...);` ripeti: ora funziona.
3. Scrivi `static String lounge(Posto p)` che restituisce `"lounge: sì/no"` per i VIP e `"nessun lounge"` per gli altri, usando `instanceof` con binding.
4. Scrivi lo stesso metodo con un cast e un controllo `getClass()`: confronta la leggibilità.
5. Chiama il metodo con `null`: cosa restituisce?
6. Confronta con `as` di C# (che restituisce `null`): scrivi come lo emuleresti.

**Cosa osservare:** `null instanceof X` è sempre `false`, mai un'eccezione.

**Verifica:**
- [ ] Nessuna `ClassCastException` nella versione con `instanceof`.
- [ ] `lounge(null)` non lancia nulla.

**Rifletti:** un downcast è spesso segno di cattivo design? Cosa useresti al suo posto (polimorfismo, interfacce)?

### Es0509 · Tipo di ritorno covariante `[L3]`
**File:** `Es0509_Covarianza.java`

**Obiettivo:** restringere il tipo di ritorno in una sottoclasse per evitare i cast.

**Prima di iniziare:** un metodo ridefinito può restituire un sottotipo del tipo di ritorno del padre. Il chiamante che usa il tipo specifico non ha bisogno di cast.

**Cosa fare:**
1. In `Posto` scrivi `Posto copia()` che restituisce una copia.
2. In `PostoVip` ridefinisci `copia()` con tipo di ritorno `PostoVip`.
3. Nel `main`: `PostoVip v = new PostoVip(...); PostoVip c = v.copia();` e verifica che non serva il cast.
4. Con una variabile di tipo `Posto` che contiene un `PostoVip`, chiama `copia()` e stampa `getClass().getSimpleName()` del risultato.
5. Prova a restituire `Object` dal metodo ridefinito: leggi l'errore.
6. Prova a cambiare il tipo di un **parametro** nella ridefinizione (da `Posto` a `PostoVip`): compila? Cosa succede veramente?

**Cosa osservare:** al punto 6 non stai ridefinendo, ma facendo *overload*.

**Verifica:**
- [ ] Il punto 3 compila senza cast.
- [ ] Hai spiegato la differenza tra ridefinire e fare overload.

**Rifletti:** perché i tipi di ritorno possono essere più specifici ma i parametri no? (Principio di sostituzione di Liskov.)

### Es0510 · Ereditarietà o composizione `[L3]` · *progetto*
**File:** `Es0510_ComposizioneVsEreditarieta.java`

**Obiettivo:** sperimentare perché oggi si preferisce la composizione.

**Prima di iniziare:** l'ereditarietà lega due classi in modo rigido (la figlia dipende dai dettagli del padre). La composizione tiene un riferimento a un altro oggetto (spesso un'interfaccia) e lo usa. Spring è costruito sulla composizione e sull'iniezione delle dipendenze.

**Cosa fare:**
1. **Versione 1**: `PostoScontato extends Posto` con un campo `percentuale` e un metodo `prezzoFinale()`.
2. Aggiungi un secondo tipo di sconto (per esempio uno sconto fisso in euro) creando `PostoConSconto2 extends Posto`. Poi un terzo: "sconto del 10% **e** sconto fisso di 2 euro". Nota cosa succede alla gerarchia.
3. **Versione 2**: un'interfaccia `Sconto { BigDecimal applica(BigDecimal) }`, una sola classe `Posto` con un campo `Sconto sconto`, e tre implementazioni (percentuale, fisso, composto che ne applica due).
4. Per entrambe le versioni misura il lavoro necessario ad aggiungere uno sconto "solo il lunedì".
5. Scrivi 5 righe di commento: quale versione è stata più facile da estendere e perché.

**Cosa osservare:** nella versione 2 `Posto` non viene toccata dall'aggiunta di sconti.

**Verifica:**
- [ ] Due versioni funzionanti.
- [ ] Il commento finale conclude con una scelta motivata.

**Rifletti:** cosa significa "preferire la composizione all'ereditarietà"? Quando l'ereditarietà è ancora la scelta giusta?

### Es0511 · Gerarchia di eccezioni di dominio `[L2]` · *progetto*
**File:** `Es0511_EccezioniDominio.java`

**Obiettivo:** progettare una gerarchia di eccezioni che si possa gestire sia in modo specifico sia in modo generale.

**Prima di iniziare:** `catch` seleziona le eccezioni per tipo, compresi i sottotipi. Con più blocchi `catch` vale il **primo** che corrisponde: il compilatore rifiuta di mettere il padre prima del figlio, perché il figlio sarebbe irraggiungibile.

**Cosa fare:**
1. Crea `PrenotazioneException extends RuntimeException` con un campo `String codice` e un costruttore `(codice, messaggio)`.
2. Crea tre sottoclassi con un codice proprio: `PostoGiaOccupatoException` ("POSTO_OCCUPATO"), `HoldScadutoException` ("HOLD_SCADUTO"), `EventoNonTrovatoException` ("EVENTO_NON_TROVATO"). Ogni messaggio include i dati utili (evento, posto).
3. Scrivi tre metodi che lanciano ciascuno una delle tre eccezioni.
4. Nel `main` chiama i tre metodi in un ciclo (con un array di `Runnable`) e gestisci le eccezioni con **un solo** `catch (PrenotazioneException e)` che stampa codice e messaggio.
5. Poi scrivi un secondo `main` con tre `catch` specifici (uno per tipo) e un quarto più generale in fondo.
6. Metti il `catch` generale **prima** di quelli specifici e leggi l'errore.

**Cosa osservare:** l'errore del punto 6 dice che il tipo "è già stato catturato".

**Verifica:**
- [ ] Un solo `catch` gestisce tutte e tre le eccezioni.
- [ ] Hai visto l'errore dell'ordine sbagliato.

**Rifletti:** nel progetto ogni eccezione si tradurrà in un codice HTTP (409, 410, 404): come le raggrupperesti? Meglio un campo `codice` o una sottoclasse per ogni caso?

### Es0512 · I metodi di `Object` `[L1]`
**File:** `Es0512_Object.java`

**Obiettivo:** conoscere cosa ogni classe eredita da `Object` e perché `toString` è fondamentale.

**Prima di iniziare:** ogni classe estende implicitamente `Object`, quindi ha `toString`, `equals`, `hashCode`, `getClass`. Le versioni predefinite di `toString` e `equals` sono poco utili: stampano un identificatore e confrontano i riferimenti.

**Cosa fare:**
1. Crea una classe `Cosa` senza alcun metodo e stampane un'istanza.
2. Stampa `getClass().getName()`, `getClass().getSimpleName()` e `hashCode()` di due istanze diverse.
3. Chiama `equals` tra due istanze e tra un'istanza e se stessa.
4. Scrivi un `toString` e verifica che l'output della stampa cambi.
5. Scrivi un log con `"Errore sul posto " + posto` e osserva cosa compare con e senza `toString`.

**Cosa osservare:** il formato `NomeClasse@codiceEsadecimale`.

**Verifica:**
- [ ] Hai visto l'output predefinito e quello personalizzato.
- [ ] Sai spiegare da dove viene il numero dopo la `@`.

**Rifletti:** cosa dice un log di produzione se le tue classi non hanno `toString`? Cosa fanno i `record` in proposito?

### Es0513 · Utenti e ruoli `[L2]` · *progetto*
**File:** `Es0513_Utenti.java`

**Obiettivo:** gestire una collezione eterogenea con `instanceof` e polimorfismo.

**Prima di iniziare:** una `List<Utente>` può contenere sottoclassi con campi diversi. Per accedere ai campi specifici serve il tipo specifico (pattern matching), oppure si sposta il comportamento nelle sottoclassi.

**Cosa fare:**
1. Crea `abstract class Utente` con `id` ed `email` e un metodo astratto `String ruolo()`.
2. Crea `Cliente` (con `int punti`) e `Operatore` (con `String badge`), che implementano `ruolo()`.
3. Crea una lista con 2 clienti e 2 operatori.
4. Con un ciclo stampa il ruolo di ciascuno e, **solo per i clienti**, i punti, usando `instanceof` con binding (nessun cast esplicito).
5. Riscrivi lo stesso con un metodo `String dettaglio()` astratto implementato in ciascuna sottoclasse (nessun `instanceof`).
6. Confronta le due versioni in un commento: quale cresce meglio se aggiungi un terzo tipo di utente?

**Cosa osservare:** nella seconda versione il ciclo è di una sola riga.

**Verifica:**
- [ ] Due versioni funzionanti con lo stesso output.
- [ ] Nessun cast nel codice.

**Rifletti:** quando conviene `instanceof` e quando spostare il comportamento nella classe?


## Cosa devi aver capito

- [ ] Metodi virtuali di default; `@Override` sempre.
- [ ] `super(...)` e `super.metodo()`; ordine di esecuzione dei costruttori.
- [ ] I campi non sono polimorfici, i metodi sì.
- [ ] `abstract`, `final`, `sealed`.
- [ ] Gerarchie di eccezioni e ordine dei `catch`.
- [ ] Composizione prima dell'ereditarietà.
