# 12 · Concorrenza di base

Obiettivo: vedere con i tuoi occhi una *race condition*, risolverla in più modi e costruire il test a 50 thread della settimana 3, ancora senza database né Spring.

## C# → Java

| C# | Java | Nota |
| --- | --- | --- |
| `Thread` | `Thread` | `new Thread(() -> ...)`; `start()` avvia, `run()` **non** crea un thread |
| `Task.Run`, `Task<T>` | `ExecutorService.submit`, `Future<T>` | |
| `async`/`await` | `CompletableFuture` | Non c'è `await`: si concatena con `thenApply`, `thenCompose` |
| `lock (o)` | `synchronized (o)` o `ReentrantLock` | `synchronized` può stare anche su un metodo |
| `Interlocked.Increment` | `AtomicInteger.incrementAndGet()` | `AtomicLong`, `AtomicBoolean`, `AtomicReference` |
| `ConcurrentDictionary` | `ConcurrentHashMap` | `putIfAbsent`, `computeIfAbsent`, `merge` atomici |
| `SemaphoreSlim` | `Semaphore` | |
| `CountdownEvent` / `ManualResetEvent` | `CountDownLatch` | |
| `CancellationToken` | `Thread.interrupt()` + `InterruptedException` | |
| `Task.WhenAll` | `CompletableFuture.allOf` | |
| `volatile` | `volatile` | Visibilità tra thread |
| Thread pool | `Executors.newFixedThreadPool(n)` | **Va chiuso** con `shutdown()` |
| (non c'è un equivalente diretto) | thread virtuali (Java 21) | `Executors.newVirtualThreadPerTaskExecutor()` |

## Esercizi

Ogni esercizio segue lo stesso schema: **Obiettivo**, **Prima di iniziare** (il concetto in breve), **Cosa fare** (passi in ordine), **Cosa osservare**, **Verifica** (checklist) e **Rifletti** (domande da colloquio).

### Es1201 · Il primo thread `[L1]`
**File:** `Es1201_PrimoThread.java`

**Obiettivo:** avviare thread in più modi e aspettarne la fine.

**Prima di iniziare:** un `Thread` esegue un `Runnable`. `start()` crea un nuovo thread; `run()` chiamato direttamente esegue il codice nel thread corrente (errore classico). `join()` aspetta la fine del thread.

**Cosa fare:**
1. Crea un thread con una classe che `extends Thread` e ridefinisce `run`.
2. Crea un secondo con `new Thread(new Runnable() {...})`, un terzo con una lambda.
3. Dai un nome ai thread e stampa `Thread.currentThread().getName()` dentro `run`.
4. Avviali con `start()` e senza `join()` stampa `"fine main"`: in che ordine escono le stampe?
5. Aggiungi `join()` e riprova.
6. Sostituisci `start()` con `run()` e osserva il nome del thread che esegue.
7. Chiama `start()` due volte sullo stesso thread: leggi l'eccezione.
8. Prova un thread *daemon* (`setDaemon(true)`) con un ciclo infinito: il programma termina?

**Cosa osservare:** l'ordine di stampa varia a ogni esecuzione.

**Verifica:**
- [ ] Tre modi di creare un thread.
- [ ] Hai spiegato la differenza tra `start` e `run`.

**Rifletti:** quanto costa un thread? Perché di solito non si creano direttamente? (Es1204.)

### Es1202 · La race condition di un contatore `[L2]`
**File:** `Es1202_RaceCondition.java`

**Obiettivo:** vedere con i tuoi occhi un risultato sbagliato causato dalla concorrenza.

**Prima di iniziare:** `contatore++` non è atomico: sono tre passaggi (leggi, aumenta, scrivi). Se due thread li intrecciano, un aggiornamento va perso. È l'esempio base di *race condition*.

**Cosa fare:**
1. Crea una classe `Contatore` con un `int valore` e `void incrementa() { valore++; }`.
2. Avvia 4 thread che incrementano ciascuno 100.000 volte lo stesso contatore.
3. Aspetta la fine di tutti con `join()` e stampa il valore. Che risultato ti aspetti? Cosa ottieni?
4. Ripeti l'esecuzione 5 volte e annota i risultati: sono uguali?
5. Prova con 1 solo thread: il risultato è sempre corretto?
6. Aggiungi `Thread.yield()` o una piccola attesa all'interno dell'incremento e vedi se peggiora.
7. Spiega in un commento le 3 operazioni nascoste di `valore++` e l'intreccio che perde un aggiornamento.

**Cosa osservare:** il risultato è minore di 400.000 e cambia a ogni esecuzione.

**Verifica:**
- [ ] 5 esecuzioni annotate.
- [ ] Spiegazione scritta dell'intreccio.

**Rifletti:** perché il bug può non comparire nei test e comparire in produzione? Cosa significa "non deterministico"?

### Es1203 · Tre correzioni `[L2]`
**File:** `Es1203_TreFix.java`

**Obiettivo:** correggere il contatore in tre modi e confrontarne il costo.

**Prima di iniziare:** si può rendere l'incremento atomico con `synchronized` (blocco esclusivo), con `AtomicInteger` (istruzioni atomiche della CPU), o con `LongAdder` (contatore a celle per alta contesa).

**Cosa fare:**
1. Parti dal contatore dell'Es1202.
2. **Versione A**: metti `synchronized` sul metodo `incrementa()` (e anche su `getValore()`: perché?).
3. **Versione B**: sostituisci con `AtomicInteger` e `incrementAndGet()`.
4. **Versione C**: usa `LongAdder` con `increment()` e `sum()`.
5. Con 8 thread × 1.000.000 di incrementi, misura il tempo di ciascuna con `System.nanoTime()` e verifica che il totale sia corretto.
6. Prova `volatile int` con `valore++`: risolve il problema? Perché no?
7. Scrivi la tabella: soluzione / quando la usi / prestazioni osservate.

**Cosa osservare:** `volatile` da solo non rende atomico `++`.

**Verifica:**
- [ ] Tre soluzioni corrette.
- [ ] Tre tempi misurati.

**Rifletti:** quale useresti per un contatore di richieste in un servizio molto caricato? E per proteggere un'invariante tra due campi?

### Es1204 · `ExecutorService` `[L2]`
**File:** `Es1204_Executor.java`

**Obiettivo:** gestire i thread tramite un *pool* invece di crearli a mano.

**Prima di iniziare:** un `ExecutorService` riceve compiti (`Runnable` o `Callable`) e li esegue con un numero limitato di thread riutilizzati. `submit` restituisce un `Future` da cui leggere il risultato con `get()`. Va sempre chiuso con `shutdown()`.

**Cosa fare:**
1. Crea `Executors.newFixedThreadPool(4)`.
2. Sottoponi 10 compiti che stampano il nome del thread e dormono 200 ms: quanti nomi diversi vedi?
3. Sottoponi 5 `Callable<Integer>` che restituiscono il quadrato di un numero e raccogli i `Future`; stampa i risultati con `get()`.
4. Fai lanciare un'eccezione a un `Callable`: quando la vedi? (Dentro `get()`, avvolta in `ExecutionException`.)
5. Usa `invokeAll` per lanciarli tutti e aspettare.
6. Chiudi con `shutdown()` e `awaitTermination(5, TimeUnit.SECONDS)`.
7. Dimentica `shutdown()` e osserva che il programma non termina.
8. Usa `try (var executor = Executors.newFixedThreadPool(4)) {...}` (Java 19+, `ExecutorService` è `AutoCloseable`).

**Cosa osservare:** l'eccezione del compito non appare finché non chiami `get()`.

**Verifica:**
- [ ] Hai visto i nomi dei thread riutilizzati.
- [ ] Hai provato `ExecutionException` e il programma che non termina.

**Rifletti:** a cosa corrispondono `Task.Run` e `await` in C#? Qual è la differenza fra thread di un pool e `async/await`?

### Es1205 · Lo starter `CountDownLatch` `[L3]` · *progetto*
**File:** `Es1205_Latch.java`

**Obiettivo:** far partire N thread esattamente nello stesso istante, come servirà al test dei 50 utenti.

**Prima di iniziare:** un `CountDownLatch(1)` fa aspettare i thread con `await()` finché qualcuno chiama `countDown()`. Un secondo latch con valore N permette al `main` di attendere che tutti abbiano finito.

**Cosa fare:**
1. Crea 50 thread (o 50 compiti in un executor da 50 thread) che aspettano su un `startSignal`.
2. Ogni compito, dopo il via, registra il proprio ID in una lista sincronizzata (`Collections.synchronizedList`) e decrementa un `doneSignal` da 50.
3. Il `main` attende che tutti siano pronti (un terzo latch `pronti`), poi chiama `startSignal.countDown()`.
4. Il `main` aspetta `doneSignal.await()` e stampa la lista.
5. Confronta con la versione senza latch (i thread partono uno alla volta): l'ordine è molto più regolare?
6. Misura con `System.nanoTime()` il tempo fra il primo e l'ultimo avvio con e senza starter.
7. Prova `CyclicBarrier(50)` come alternativa e annota le differenze.

**Cosa osservare:** senza starter i primi thread finiscono prima che gli ultimi partano.

**Verifica:**
- [ ] 50 thread partono "insieme".
- [ ] Hai spiegato la differenza tra latch e barrier.

**Rifletti:** perché per scoprire una race condition serve la contesa massima? Cosa ottieni da un test con 2 thread?

### Es1206 · Prenotazione ingenua `[L3]` · *progetto*
**File:** `Es1206_PrenotazioneIngenua.java`

**Obiettivo:** riprodurre il bug del progetto: due utenti prenotano lo stesso posto.

**Prima di iniziare:** la sequenza *"controllo se è libero, poi lo occupo"* (*check-then-act*) non è atomica. Tra il controllo e la scrittura un altro thread può inserirsi.

**Cosa fare:**
1. Crea `PrenotazioneIngenua` con una `Map<String, String>` (posto → utente), non sincronizzata, e un metodo `boolean prenota(String posto, String utente)` scritto così: `if (!mappa.containsKey(posto)) { Thread.sleep(1); mappa.put(posto, utente); return true; } return false;`.
2. Usa il codice dell'Es1205: 50 utenti tentano lo **stesso** posto.
3. Conta quanti `prenota` restituiscono `true`. Quanti ne dovrebbero essere?
4. Stampa il valore finale nella mappa e quale utente "ha vinto".
5. Ripeti 10 volte e annota quante volte il test fallisce.
6. Togli la `Thread.sleep(1)`: il bug sparisce o diventa più raro? Cosa significa?
7. Usa una `HashMap` al posto di una `ConcurrentHashMap` e osserva se cambia qualcosa (la sola struttura thread-safe basta?).

**Cosa osservare:** il numero di "vincitori" è maggiore di 1.

**Verifica:**
- [ ] Bug riprodotto in almeno 8 esecuzioni su 10.
- [ ] Hai spiegato perché `ConcurrentHashMap` da solo non basta.

**Rifletti:** questo è esattamente ciò che succede con due richieste HTTP simultanee e un `SELECT` seguito da un `INSERT`. Come lo risolve un `UNIQUE` nel database?

### Es1207 · Fix con `synchronized` e `ReentrantLock` `[L3]`
**File:** `Es1207_LockFix.java`

**Obiettivo:** risolvere la prenotazione con un lock e capirne i limiti.

**Prima di iniziare:** `synchronized` e `ReentrantLock` garantiscono che un solo thread alla volta esegua la sezione critica. `ReentrantLock` offre di più: `tryLock` con timeout, equità, condizioni; ma va sempre rilasciato in un `finally`.

**Cosa fare:**
1. **Versione A**: aggiungi `synchronized` al metodo `prenota`. Esegui il test dei 50 utenti: quanti vincono?
2. **Versione B**: usa un `ReentrantLock` con `lock()` / `unlock()` in `try/finally`.
3. Dimentica volutamente l'`unlock` in un ramo e osserva cosa succede ai thread successivi.
4. **Versione C**: usa `tryLock(100, TimeUnit.MILLISECONDS)` e restituisci un errore chiaro se non ottieni il lock.
5. Passa da un lock **globale** a un lock **per posto** (una mappa `posto → lock`, creata con `computeIfAbsent`). Prenota 50 posti diversi: quanto tempo ci mette rispetto al lock globale?
6. Scrivi i limiti: cosa succede con **due istanze** dell'applicazione? (Il lock vale solo dentro un processo.)

**Cosa osservare:** con un lock globale, anche posti diversi si bloccano a vicenda.

**Verifica:**
- [ ] Un solo vincitore in tutte le versioni.
- [ ] Hai spiegato perché non basta con più server.

**Rifletti:** come cambia il problema se le istanze sono due? (Redisson `RLock`, `SELECT FOR UPDATE`, vincolo UNIQUE.)

### Es1208 · Fix atomico: `compareAndSet` e `putIfAbsent` `[L3]` · *progetto*
**File:** `Es1208_Atomico.java`

**Obiettivo:** risolvere senza lock usando operazioni atomiche, l'idea alla base di `SET NX` in Redis.

**Prima di iniziare:** `ConcurrentHashMap.putIfAbsent(k, v)` è un'unica operazione atomica che inserisce solo se la chiave manca e restituisce il valore precedente (o `null`). `AtomicReference.compareAndSet(atteso, nuovo)` cambia il valore solo se è quello atteso. In Redis `SET chiave valore NX EX 600` fa lo stesso sul server.

**Cosa fare:**
1. Riscrivi `prenota` con `ConcurrentHashMap.putIfAbsent(posto, utente) == null`.
2. Esegui il test dei 50 utenti: quanti vincono?
3. Scrivi un `PostoAtomico` con un `AtomicReference<String> proprietario` e `prenota` con `compareAndSet(null, utente)`.
4. Scrivi `rilascia(posto, utente)` con `mappa.remove(posto, utente)` (versione a due argomenti): perché è meglio di `get` + `remove`?
5. Scrivi la variante con scadenza: valore `record Hold(String utente, Instant scadenza)` e `compute` per sostituire un hold scaduto in modo atomico.
6. Esegui il test dei 50 utenti anche con la scadenza (usando un `Clock` finto) e verifica un solo vincitore.
7. Scrivi la tabella di corrispondenza: `putIfAbsent` ↔ `SET NX`, `remove(k, v)` ↔ script Lua di rilascio, `compute` ↔ `EVAL`.

**Cosa osservare:** nessun `synchronized` e nessun `if` separato dal `put`.

**Verifica:**
- [ ] Un solo vincitore con 50 thread, per 10 esecuzioni.
- [ ] Tabella di corrispondenza con Redis scritta.

**Rifletti:** quale proprietà rende `SET NX` la scelta giusta per l'hold? Perché non `GET` + `SET`?

### Es1209 · Thread virtuali `[L3]`
**File:** `Es1209_Virtuali.java`

**Obiettivo:** provare i *virtual threads* (Java 21) e capire quando servono.

**Prima di iniziare:** un thread virtuale è leggero (gestito dalla JVM, non dal sistema operativo): puoi averne centinaia di migliaia. Va bene per compiti che **aspettano** (rete, database); non porta vantaggi per calcoli intensivi.

**Cosa fare:**
1. Con Java 21 crea `Thread.ofVirtual().start(() -> ...)` e `Thread.startVirtualThread(...)`.
2. Stampa `Thread.currentThread()` dentro un thread virtuale e dentro uno normale: cosa vedi?
3. Lancia 10.000 compiti che dormono 1 secondo con `Executors.newVirtualThreadPerTaskExecutor()`: quanto impiega il programma?
4. Fai lo stesso con `newFixedThreadPool(200)`: quanto impiega?
5. Prova a lanciare 10.000 thread normali con `new Thread(...)`: cosa succede alla memoria o ai tempi?
6. Esegui un calcolo intensivo (ciclo di milioni di operazioni) su 10.000 compiti virtuali e confronta con il pool fisso: c'è un vantaggio?
7. Cerca come abilitarli in Spring Boot (`spring.threads.virtual.enabled=true`).

**Cosa osservare:** i 10.000 compiti che aspettano finiscono in circa un secondo.

**Verifica:**
- [ ] Tempi misurati per tre casi.
- [ ] Hai scritto quando usare e quando no i thread virtuali.

**Rifletti:** come si confrontano con `async/await`? Cosa significa "bloccare è di nuovo accettabile"?

### Es1210 · `CompletableFuture` `[L3]`
**File:** `Es1210_CompletableFuture.java`

**Obiettivo:** comporre operazioni asincrone senza attendere ogni passo.

**Prima di iniziare:** `CompletableFuture<T>` rappresenta un risultato futuro e permette di concatenare passi (`thenApply`, `thenCompose`, `thenCombine`), gestire errori (`exceptionally`, `handle`) e aspettare più operazioni (`allOf`, `anyOf`). È il parente di `Task<T>` di C#.

**Cosa fare:**
1. Scrivi `CompletableFuture<Integer> prezzo = CompletableFuture.supplyAsync(() -> { dormi(300); return 50; });`.
2. Concatena `thenApply` per applicare lo sconto del 10% e `thenAccept` per stampare.
3. Chiama `join()` e `get()`: quali differenze nelle eccezioni?
4. Combina due futuri (`prezzoBase` e `commissione`) con `thenCombine` e ottieni il totale.
5. Usa `thenCompose` per un'operazione che dipende dal risultato dell'altra (es. leggi l'ID e poi carica l'evento): perché non `thenApply`?
6. Lancia 5 operazioni in parallelo e aspetta con `allOf`: misura il tempo totale (circa quanto la più lenta).
7. Fai fallire un passo e gestiscilo con `exceptionally(e -> ...)`; poi con `handle((r, e) -> ...)`.
8. Passa un tuo `Executor` come secondo argomento a `supplyAsync` invece di usare il pool comune.

**Cosa osservare:** con `thenApply` otterresti un `CompletableFuture<CompletableFuture<...>>`.

**Verifica:**
- [ ] Hai usato almeno 6 metodi diversi.
- [ ] Tempo di `allOf` coerente con l'esecuzione parallela.

**Rifletti:** perché non conviene usare il pool comune per chiamate bloccanti? Cosa fa `@Async` in Spring?

### Es1211 · Visibilità e `volatile` `[L3]`
**File:** `Es1211_Volatile.java`

**Obiettivo:** scoprire che un thread può non vedere le modifiche di un altro anche senza race condition sui dati.

**Prima di iniziare:** ogni thread può mantenere copie locali (cache, registri) dei campi. Senza una sincronizzazione (`volatile`, `synchronized`, `Atomic*`), non c'è garanzia che un thread veda la scrittura di un altro. Questo è il *Java Memory Model*.

**Cosa fare:**
1. Crea un campo `boolean fermati = false` (non volatile) e un thread che gira in `while (!fermati) { }`.
2. Il `main` aspetta 1 secondo e imposta `fermati = true`, poi `join()` con timeout di 3 secondi: il thread termina? (Può dipendere dal JIT: se non si blocca prova con un ciclo più "caldo" o con `-server`.)
3. Dichiara il campo `volatile` e riprova.
4. Sostituisci con un `AtomicBoolean`.
5. Sostituisci con `synchronized` su lettura e scrittura.
6. Scrivi un esempio con un campo `int dato` e un `boolean pronto`: il thread scrittore imposta `dato = 42; pronto = true;`; il lettore aspetta `pronto` e stampa `dato`. Perché `pronto` deve essere `volatile`? (Garantisce che `dato` sia già visibile: relazione *happens-before*.)
7. Scrivi un commento: cosa garantisce `volatile` e cosa **non** garantisce.

**Cosa osservare:** il ciclo senza `volatile` può non terminare mai (dipende dalla JVM).

**Verifica:**
- [ ] Hai provato 4 varianti.
- [ ] Hai spiegato *happens-before* con parole tue.

**Rifletti:** in C# esiste `volatile` e `Volatile.Read`: sono uguali? Perché `volatile` non sostituisce un lock?

### Es1212 · Deadlock `[L3]`
**File:** `Es1212_Deadlock.java`

**Obiettivo:** provocare, diagnosticare e prevenire un deadlock.

**Prima di iniziare:** un deadlock avviene quando due thread aspettano a vicenda risorse tenute dall'altro. Si previene prendendo i lock sempre nello stesso ordine o usando `tryLock` con timeout.

**Cosa fare:**
1. Crea due oggetti `lockA` e `lockB`.
2. Thread 1: `synchronized (lockA) { dormi(100); synchronized (lockB) { ... } }`.
3. Thread 2: `synchronized (lockB) { dormi(100); synchronized (lockA) { ... } }`.
4. Avvia entrambi: il programma si blocca. Ottieni un *thread dump* (`jstack <pid>` oppure Ctrl+Break su Windows / il pulsante di IntelliJ) e trova la sezione "Found one Java-level deadlock".
5. Correggi imponendo un ordine globale dei lock (sempre A poi B).
6. Correggi in un secondo modo usando `ReentrantLock.tryLock(timeout)` con rilascio e nuovo tentativo.
7. Applica il caso a un esempio realistico: trasferire un posto tra due prenotazioni con lock per prenotazione; ordina i lock per ID.
8. Scrivi la lista delle quattro condizioni di Coffman e quale rompi in ciascuna correzione.

**Cosa osservare:** il thread dump nomina le risorse e i thread coinvolti.

**Verifica:**
- [ ] Deadlock riprodotto e diagnosticato.
- [ ] Due correzioni funzionanti.

**Rifletti:** come ti accorgi di un deadlock in produzione? (Thread dump, metriche, timeout.) Perché i lock per-posto richiedono attenzione all'ordine?


## Cosa devi aver capito

- [ ] `start()` avvia un thread, `run()` no.
- [ ] `contatore++` non è atomico; `synchronized`, `Atomic*`, `ConcurrentHashMap` risolvono.
- [ ] "Controlla poi scrivi" è il bug; la soluzione è un'operazione atomica.
- [ ] Un lock in memoria vale solo in un processo: con più istanze serve Redis o il database.
- [ ] Un `ExecutorService` va chiuso.
