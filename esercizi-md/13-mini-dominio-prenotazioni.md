# 13 · Mini-dominio: prenotazione posti in memoria

Obiettivo: costruire in puro Java, **senza Spring, database o Redis**, una versione ridotta del progetto. Alla fine avrai scritto a mano ciò che i framework faranno al posto tuo, e capirai meglio ogni annotazione.

Dove: un package `studio.dominio` (cartella `src/main/java/studio/dominio/`). Un solo programma con più classi.

Regole di lavoro:

- niente framework e nessuna libreria esterna (solo JDK);
- ogni passo ha **criteri di accettazione**: spuntali uno a uno;
- nessuna soluzione qui: se ti blocchi su un passo, incollami il tuo codice.

## Passo 1 · Il modello

**Consegna**
- `record Evento(long id, String nome, String sede, LocalDateTime data)`.
- `record Posto(long eventoId, String codice, BigDecimal prezzo)` con validazione nel compact constructor (codice del formato `A1`..`E20`, prezzo non negativo).
- `enum StatoPosto { LIBERO, IN_ATTESA, VENDUTO }` con `puoPassareA(...)` (file 03).
- `record Prenotazione(long id, long eventoId, String codicePosto, String utente, Instant creataIl)`.

**Criteri di accettazione**
- [ ] Nessuna classe ha setter: tutto è immutabile.
- [ ] Un `Posto` con codice `Z99` o prezzo negativo non si può creare.
- [ ] Il metodo `puoPassareA` rifiuta le 6 transizioni vietate.

## Passo 2 · Le eccezioni di dominio

**Consegna:** gerarchia `PrenotazioneException` (unchecked) con `EventoNonTrovatoException`, `PostoNonTrovatoException`, `PostoGiaOccupatoException`, `HoldScadutoException`, `HoldNonTuoException`, `LimiteHoldSuperatoException`. Ognuna ha un codice stringa e un messaggio con i dati utili.

**Criteri di accettazione**
- [ ] Un unico `catch (PrenotazioneException e)` le prende tutte.
- [ ] Nessuna eccezione espone dettagli interni nel messaggio.

## Passo 3 · Le interfacce

**Consegna:** definisci (senza implementare):
- `EventoRepository` e `PostoRepository` (ricerca con `Optional`, elenco per evento, salvataggio).
- `PrenotazioneRepository` (salva, `perUtente`, `esisteGiaVenduto(eventoId, codice)`).
- `HoldStore` (come nell'Es0609: `tentaHold`, `chiHaIlHold`, `rilascia`).

**Criteri di accettazione**
- [ ] Nessuna interfaccia ha prefisso `I`.
- [ ] Nessun metodo restituisce `null`.
- [ ] I tipi restituiti sono `Optional`, `List` o `boolean`, mai `null` per "non trovato".

## Passo 4 · Le implementazioni in memoria

**Consegna:** `InMemoryEventoRepository`, `InMemoryPostoRepository`, `InMemoryPrenotazioneRepository`, `InMemoryHoldStore`. Usano `ConcurrentHashMap`. `InMemoryHoldStore` riceve un `Clock` e rispetta la scadenza. Quella delle prenotazioni deve **rifiutare** una seconda vendita dello stesso posto (come il vincolo `UNIQUE`).

**Criteri di accettazione**
- [ ] Nessuna classe di dominio conosce le implementazioni (usano solo le interfacce).
- [ ] Il `HoldStore` rilascia da solo un hold scaduto, provato con un `Clock` controllabile.
- [ ] Tentare di salvare due prenotazioni per lo stesso posto lancia un'eccezione.

## Passo 5 · Il servizio

**Consegna:** `PrenotazioneService` con le dipendenze nel costruttore. Metodi:
- `List<PostoConStato> mappa(long eventoId)` (un `record` che unisce `Posto` e stato calcolato: venduto dal repository, in attesa dall'hold store, altrimenti libero);
- `void tieniPosto(long eventoId, String codice, String utente)` (rispetta il limite di 5 hold attivi per utente);
- `void rilascia(long eventoId, String codice, String utente)` (solo chi ha l'hold);
- `Prenotazione conferma(long eventoId, String codice, String utente)` (verifica che l'hold sia tuo e non scaduto, salva la prenotazione, rilascia l'hold).

**Criteri di accettazione**
- [ ] Ogni caso di errore lancia l'eccezione giusta del passo 2.
- [ ] La durata dell'hold arriva da fuori (non è scritta dentro il servizio).
- [ ] Il servizio non chiama `Instant.now()`: usa il `Clock`.
- [ ] Nessun `if (x == null)` nel servizio.

## Passo 6 · Il menu a console

**Consegna:** una classe `Main` che costruisce a mano tutti gli oggetti (la *composition root*), carica un evento con 5×20 posti e offre un menu: `1) mappa`, `2) tieni posto`, `3) conferma`, `4) rilascia`, `5) esci`, usando `Scanner`. Le eccezioni di dominio vengono mostrate come messaggio leggibile, **senza** stack trace.

**Criteri di accettazione**
- [ ] Puoi tenere un posto, aspettare la scadenza (con una durata breve in configurazione) e vederlo tornare libero.
- [ ] Un input sbagliato non chiude il programma.
- [ ] `Main` è l'unico punto con `new` delle implementazioni.

## Passo 7 · Il test di concorrenza

**Consegna:** un programma separato che lancia 50 thread, partenza comune con `CountDownLatch`, che chiamano `tieniPosto` sullo **stesso** posto con utenti diversi. Poi ripeti con la `conferma`.

**Criteri di accettazione**
- [ ] Esattamente 1 successo e 49 `PostoGiaOccupatoException`.
- [ ] Il risultato è corretto per 50 esecuzioni consecutive.
- [ ] Hai dovuto usare un'operazione atomica nel `HoldStore` (non un "controlla poi scrivi").
- [ ] Sai spiegare perché questo test è la versione in memoria di quello contro Redis e PostgreSQL.

## Passo 8 · Report con gli stream

**Consegna:** carica 200 prenotazioni casuali e produci con gli stream: incasso per evento, i 5 posti più richiesti, gli utenti con più prenotazioni, il prezzo medio per fila, la percentuale di occupazione per evento.

**Criteri di accettazione**
- [ ] Nessun ciclo `for` classico per i calcoli.
- [ ] Gli importi sono `BigDecimal` arrotondati a due decimali.
- [ ] Nessuna `NullPointerException` con eventi senza prenotazioni.

## Cosa diventerà ciascun pezzo, con Spring

| Qui (puro Java) | Nel progetto vero |
| --- | --- |
| `record` del modello | Entity JPA e DTO |
| `PostoRepository` + `InMemory...` | `JpaRepository<Posto, Long>` generato da Spring Data |
| `HoldStore` + `InMemoryHoldStore` | `RedisHoldStore` con `SET NX` e TTL |
| `Clock` iniettato | Un `@Bean Clock` (e `Clock.fixed` nei test) |
| `PrenotazioneService` e costruttore con dipendenze | `@Service` con iniezione da costruttore |
| `Main` (composition root) | Il container di Spring |
| Menu a console | `@RestController` |
| Eccezioni di dominio | `@RestControllerAdvice` e risposte `404`, `409`, `410` |
| Test con 50 thread | Test con Testcontainers, stesso schema |
| Limite di 5 hold | Rate limiting con `INCR` + `EXPIRE` su Redis |

## Domande da colloquio che questo esercizio ti fa allenare

- [ ] Perché dipendere dalle interfacce e non dalle classi concrete?
- [ ] Cos'è la *dependency injection* e cosa fa Spring al posto del `Main`?
- [ ] Che differenza c'è tra un lock in memoria e uno distribuito?
- [ ] Perché serve un vincolo `UNIQUE` anche se hai già l'hold?
- [ ] Come testi il codice che dipende dal tempo?
