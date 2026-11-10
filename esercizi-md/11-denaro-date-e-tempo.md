# 11 · Denaro, date e tempo

Obiettivo: calcolare con `BigDecimal` senza perdere centesimi e usare `java.time` (con `Clock` e `Duration`) per la scadenza degli hold, in modo testabile.

## `BigDecimal` a confronto con `decimal`

| C# `decimal` | Java `BigDecimal` | Nota |
| --- | --- | --- |
| `10.90m` | `new BigDecimal("10.90")` | Sempre da **stringa**, mai da `double` |
| `a + b` | `a.add(b)` | Gli operatori non esistono |
| `a - b`, `a * b` | `a.subtract(b)`, `a.multiply(b)` | |
| `a / b` | `a.divide(b, 2, RoundingMode.HALF_UP)` | `divide(b)` da solo lancia eccezione se il risultato non è esatto |
| `a == b` | `a.compareTo(b) == 0` | `equals` considera anche la **scala**: `2.0` e `2.00` sono diversi |
| `a < b` | `a.compareTo(b) < 0` | |
| `Math.Round(x, 2)` | `x.setScale(2, RoundingMode.HALF_UP)` | `Math.Round` di C# arrotonda per default `0.5 → 0` (pari), Java scegli tu |
| `decimal.Zero` | `BigDecimal.ZERO` | anche `ONE`, `TEN` |
| `Math.Abs`, `-x` | `abs()`, `negate()` | |
| `x > 0` | `x.signum() > 0` | |
| `Sum` | `reduce(BigDecimal.ZERO, BigDecimal::add)` | |

**Modalità di arrotondamento** (`RoundingMode`): `HALF_UP` (la scolastica: 2.5 → 3), `HALF_EVEN` (bancario: 2.5 → 2), `DOWN`, `UP`, `FLOOR`, `CEILING`.

## `java.time` a confronto

| C# | Java | Uso |
| --- | --- | --- |
| `DateOnly` | `LocalDate` | Data senza ora (compleanno) |
| `TimeOnly` | `LocalTime` | Ora senza data |
| `DateTime` (senza fuso) | `LocalDateTime` | Data e ora **locali**, senza fuso |
| `DateTimeOffset` | `OffsetDateTime`, `ZonedDateTime` | Momento con fuso |
| `DateTime.UtcNow` | `Instant.now()` | Un **istante** sulla linea del tempo (UTC) |
| `TimeSpan` | `Duration` | Quantità di tempo (secondi, minuti) |
| (nessuno) | `Period` | Quantità in giorni, mesi, anni |
| `TimeZoneInfo` | `ZoneId` (`Europe/Rome`) | Fuso orario |
| `DateTime.ToString("dd/MM/yyyy")` | `DateTimeFormatter.ofPattern("dd/MM/yyyy")` | Formattazione |
| `TimeProvider` (.NET 8) | `Clock` | Orologio **iniettabile**, per i test |

**Regola pratica**: nei sistemi si salvano gli *istanti* (`Instant`) o le date con il fuso, e si convertono solo per la visualizzazione. `LocalDateTime` va bene per eventi locali ("concerto del 5 dicembre alle 21:00 a Roma").

## Esercizi

Ogni esercizio segue lo stesso schema: **Obiettivo**, **Prima di iniziare** (il concetto in breve), **Cosa fare** (passi in ordine), **Cosa osservare**, **Verifica** (checklist) e **Rifletti** (domande da colloquio).

### Es1101 · Scala e `equals` `[L2]`
**File:** `Es1101_ScalaEquals.java`

**Obiettivo:** scoprire che `new BigDecimal("2.0")` e `new BigDecimal("2.00")` non sono `equals`.

**Prima di iniziare:** un `BigDecimal` è fatto di un valore e di una *scala* (numero di decimali). `equals` confronta **entrambi**; `compareTo` confronta solo il valore numerico. Questo condiziona `HashSet`, `HashMap` e i test.

**Cosa fare:**
1. Crea `a = new BigDecimal("2.0")` e `b = new BigDecimal("2.00")`.
2. Stampa `a.equals(b)`, `a.compareTo(b) == 0`, `a.scale()`, `b.scale()`.
3. Prevedi, poi verifica: metti `a` e `b` in un `HashSet`: quanti elementi? In un `TreeSet`?
4. Usa `stripTrailingZeros()` su entrambi e riprova `equals`.
5. Stampa `new BigDecimal("100").stripTrailingZeros()` e `toString()`/`toPlainString()`: nota la notazione scientifica (`1E+2`).
6. Crea `new BigDecimal(0.1)` e `BigDecimal.valueOf(0.1)` e `new BigDecimal("0.1")`: stampali. Quale è esatto? Perché?
7. Scrivi in un commento la regola per i test sui soldi (`assertEquals` vs `compareTo`).

**Cosa osservare:** `new BigDecimal(0.1)` stampa tante cifre sporche.

**Verifica:**
- [ ] 3 previsioni fatte prima.
- [ ] Regola finale scritta con parole tue.

**Rifletti:** in C# `decimal 2.0m == 2.00m` è `true`. Quale errore pericoloso può nascere in Java?

### Es1102 · `divide` `[L2]`
**File:** `Es1102_Divide.java`

**Obiettivo:** gestire le divisioni che non terminano.

**Prima di iniziare:** `BigDecimal.divide(altro)` lancia `ArithmeticException` se il risultato ha infinite cifre decimali (come 1/3). Bisogna dire quanti decimali vuoi e come arrotondare.

**Cosa fare:**
1. Calcola `new BigDecimal("1").divide(new BigDecimal("3"))` e leggi l'eccezione.
2. Calcola `1 / 4`: funziona? Perché?
3. Risolvi il caso 1/3 in due modi: (a) `divide(altro, 2, RoundingMode.HALF_UP)`; (b) `divide(altro, MathContext.DECIMAL64)`.
4. Stampa scala e valore dei due risultati.
5. Dividi 100 euro in 3 parti: ottieni 33,33 × 3 = 99,99. Dove vanno i centesimi persi? Scrivi `static List<BigDecimal> dividi(BigDecimal totale, int parti)` che distribuisce i centesimi residui in modo che la somma sia **esattamente** il totale.
6. Prova con 100/3, 10/4, 0,05/2, e verifica la somma.

**Cosa osservare:** non si può sommare tre volte 33,33 e ottenere 100.

**Verifica:**
- [ ] `dividi` restituisce parti che sommano esattamente il totale.
- [ ] Hai visto l'eccezione del punto 1.

**Rifletti:** come gestiresti la divisione del conto tra amici o uno sconto ripartito su più righe d'ordine?

### Es1103 · Modalità di arrotondamento `[L2]`
**File:** `Es1103_Arrotondamento.java`

**Obiettivo:** conoscere le principali `RoundingMode` e scegliere quella giusta.

**Prima di iniziare:** `HALF_UP` ("per eccesso da 0,5") è l'arrotondamento scolastico; `HALF_EVEN` (arrotondamento del banchiere) porta al pari più vicino ed è lo standard per ridurre gli errori cumulativi; `UP` e `DOWN` arrotondano allontanandosi o avvicinandosi allo zero; `CEILING` e `FLOOR` verso più e meno infinito.

**Cosa fare:**
1. Costruisci una tabella con i valori `2.5, 1.5, 0.5, -0.5, -1.5, -2.5, 1.15, 1.25, 1.35`.
2. Per ogni valore calcola `setScale(0, modo)` (e per gli ultimi tre `setScale(1, modo)`) con i modi `UP, DOWN, CEILING, FLOOR, HALF_UP, HALF_DOWN, HALF_EVEN`.
3. Stampa la tabella in modo allineato (usa `String.format`).
4. Prova `RoundingMode.UNNECESSARY` su un valore che richiede arrotondamento: cosa succede?
5. Scrivi due righe: quali modi sono sbilanciati (tendono sempre in su)?
6. Calcola l'IVA al 22% su 10 righe da 9,99 euro con arrotondamento per riga e con arrotondamento sul totale: differiscono?

**Cosa osservare:** per `x.5` `HALF_UP` e `HALF_EVEN` differiscono.

**Verifica:**
- [ ] Tabella completa con 7 modi.
- [ ] Hai spiegato la differenza tra arrotondare per riga o sul totale.

**Rifletti:** quale modo imposta la legge fiscale del tuo caso d'uso? (Chiedilo al cliente!)

### Es1104 · Totale del carrello `[L2]` · *progetto*
**File:** `Es1104_Carrello.java`

**Obiettivo:** calcolare importi con `BigDecimal` in un caso realistico e verificarli.

**Prima di iniziare:** per i soldi non si usa `double`. Ogni operazione di `BigDecimal` produce un **nuovo** oggetto (è immutabile): se non riassegni, il risultato si perde.

**Cosa fare:**
1. Definisci `record RigaOrdine(String descrizione, BigDecimal prezzoUnitario, int quantita)`.
2. Crea un ordine con 4 righe (almeno una con quantità > 1 e prezzo con centesimi).
3. Calcola: subtotale, sconto del 10% (solo se il subtotale supera 100), imponibile, IVA al 22%, totale. Arrotonda a 2 decimali con `HALF_UP` **al momento giusto** (indica nel commento dove).
4. Stampa uno scontrino allineato con `String.format("%-20s %3d x %8.2f = %9.2f%n", ...)` (attenzione ai formati con `BigDecimal`: `%f` va bene, `%d` no).
5. Dimentica volutamente la riassegnazione in un punto (`totale.add(x);`) e osserva l'errore logico.
6. Esegui gli stessi calcoli con `double` e confronta il risultato su un ordine scelto per far emergere la differenza (es. 0,1 + 0,2).
7. Scrivi `static BigDecimal totale(List<RigaOrdine> righe)` con `stream().map(...).reduce(BigDecimal.ZERO, BigDecimal::add)`.

**Cosa osservare:** con `double` 0,1 + 0,2 non è 0,3.

**Verifica:**
- [ ] Totale corretto con tutti i passaggi.
- [ ] Scontrino ben formattato.

**Rifletti:** come salveresti questi importi nel database? (`NUMERIC(12,2)` oppure centesimi in `long`.)

### Es1105 · Oggi e adesso `[L1]`
**File:** `Es1105_JavaTime.java`

**Obiettivo:** conoscere i tipi principali di `java.time` e quando usare ciascuno.

**Prima di iniziare:** `LocalDate` (solo data), `LocalTime` (solo ora), `LocalDateTime` (data e ora **senza** fuso), `Instant` (un istante assoluto sulla linea del tempo, in UTC), `ZonedDateTime` (data, ora e fuso), `OffsetDateTime` (con offset fisso). Sono tutti **immutabili**.

**Cosa fare:**
1. Stampa `LocalDate.now()`, `LocalTime.now()`, `LocalDateTime.now()`, `Instant.now()`, `ZonedDateTime.now()`, `ZonedDateTime.now(ZoneId.of("America/New_York"))`.
2. Crea una data specifica con `LocalDate.of(2026, 3, 29)` e una con `LocalDate.of(2026, 2, 30)`: leggi l'eccezione.
3. Aggiungi giorni, mesi e anni con `plusDays`, `plusMonths(1)` a `31 gennaio`: cosa ottieni?
4. Ottieni il giorno della settimana (`getDayOfWeek`) e il giorno dell'anno.
5. Trasforma un `LocalDateTime` in `ZonedDateTime` (Europe/Rome) e poi in `Instant`.
6. Calcola il numero di giorni tra due date con `ChronoUnit.DAYS.between` e `Period.between`.
7. Prova a modificare una data: `data.plusDays(1);` senza riassegnare. Cosa vedi?

**Cosa osservare:** `plusMonths(1)` su 31 gennaio dà 28/29 febbraio.

**Verifica:**
- [ ] Tutti i tipi stampati.
- [ ] Sai dire quale useresti per una colonna `created_at`.

**Rifletti:** a cosa corrispondono in C# `DateTime`, `DateTimeOffset`, `DateOnly`, `TimeOnly`?

### Es1106 · `Duration` e scadenza `[L2]` · *progetto*
**File:** `Es1106_Scadenza.java`

**Obiettivo:** calcolare scadenze con `Duration` e `Instant`, come per l'hold dei posti.

**Prima di iniziare:** `Duration` misura intervalli in secondi/nanosecondi (giorni di 24 ore); `Period` misura intervalli di calendario (anni, mesi, giorni). Per una scadenza di 10 minuti serve `Duration`.

**Cosa fare:**
1. Crea `record Hold(String posto, Instant creato, Duration durata)` con un metodo `Instant scadenza()` e `boolean scaduto(Instant adesso)`.
2. Crea un hold di 10 minuti e stampa la scadenza.
3. Calcola il tempo rimasto con `Duration.between(adesso, scadenza)` e stampalo in formato `mm:ss` (usa `toMinutes()` e `toSecondsPart()`).
4. Gestisci il caso "già scaduto": il `Duration` può essere negativo (`isNegative`). Stampa "scaduto da X secondi".
5. Prova `Duration.parse("PT10M")`, `Duration.ofMinutes(10)`, `Duration.ofHours(1).plusMinutes(30)`: stampa i `toString()` e capisci il formato ISO-8601.
6. Confronta due durate con `compareTo` e prova `isZero`.
7. Aggiungi un metodo `Hold rinnova(Instant adesso)` che ritorna un **nuovo** hold (immutabile).

**Cosa osservare:** `Duration.toString()` produce `PT10M`.

**Verifica:**
- [ ] `scaduto` funziona con istanti prima e dopo la scadenza.
- [ ] Tempo rimasto in formato `mm:ss`.

**Rifletti:** in Redis il TTL verrebbe in secondi: quale metodo di `Duration` useresti per convertirlo?

### Es1107 · `Clock` iniettabile `[L3]` · *progetto*
**File:** `Es1107_Clock.java`

**Obiettivo:** rendere il codice che dipende dal tempo testabile.

**Prima di iniziare:** chiamare `Instant.now()` direttamente rende il codice non testabile (il tempo cambia a ogni esecuzione). Ogni `now()` ha una versione che accetta un `Clock`: `Instant.now(clock)`. Così nei test puoi iniettare un orologio fisso o controllabile.

**Cosa fare:**
1. Scrivi una classe `ServizioHold` che usa `Instant.now()` direttamente per decidere se un hold è scaduto.
2. Prova a scrivere un test (con `assert`) che verifichi la scadenza dopo 10 minuti: perché è impossibile senza attendere?
3. Riscrivi la classe facendo ricevere un `Clock` dal costruttore e usando `Instant.now(clock)`.
4. Crea un orologio fisso con `Clock.fixed(Instant.parse("2026-03-29T10:00:00Z"), ZoneOffset.UTC)` e verifica "non scaduto".
5. Crea un orologio che puoi far avanzare: una piccola classe `OrologioFinto extends Clock` con `avanza(Duration)` (devi implementare `getZone`, `withZone`, `instant`).
6. Scrivi il test completo con asserzioni: hold creato, dopo 9 min non scaduto, dopo 11 min scaduto.
7. Usa `Clock.systemUTC()` nel `main` di produzione.

**Cosa osservare:** il test impiega millisecondi, non 10 minuti.

**Verifica:**
- [ ] Nessun `Instant.now()` senza `clock` nella classe.
- [ ] Test con tre asserzioni sul tempo.

**Rifletti:** in .NET c'è `TimeProvider` (e `FakeTimeProvider`): somiglia a `Clock`? In Spring come lo dichiari come `@Bean`?

### Es1108 · Il cambio dell'ora legale `[L3]`
**File:** `Es1108_OraLegale.java`

**Obiettivo:** capire perché `plusHours(24)` e `plusDays(1)` possono dare risultati diversi.

**Prima di iniziare:** un `ZonedDateTime` tiene conto delle regole del fuso. Nel giorno del passaggio all'ora legale una giornata dura 23 ore (ad ottobre 25). `plusDays` ragiona in giorni di calendario; `plus(Duration)` in tempo fisico.

**Cosa fare:**
1. Prendi il giorno del passaggio all'ora legale in Europa (ultima domenica di marzo; nel 2026 è il 29 marzo): crea `ZonedDateTime` a `2026-03-28T12:00` in `Europe/Rome`.
2. Aggiungi `plusDays(1)` e `plusHours(24)`: stampa entrambi. Quale ora ottieni?
3. Calcola `Duration.between` tra le 00:00 e le 23:59 del 29 marzo.
4. Crea l'ora inesistente `LocalDateTime.of(2026, 3, 29, 2, 30)` con `atZone(Rome)`: cosa fa Java?
5. Fai lo stesso nel passaggio all'ora solare (25 ottobre 2026) con l'ora ambigua `02:30`: usa `withEarlierOffsetAtOverlap` e `withLaterOffsetAtOverlap`.
6. Scrivi in 4 righe quando salvare in UTC (`Instant`) e quando in `LocalDateTime` (per un evento in una sala, che inizia "alle 21:00 locali").

**Cosa osservare:** l'ora inesistente viene spostata avanti automaticamente.

**Verifica:**
- [ ] Differenze tra `plusDays` e `plusHours` annotate.
- [ ] Gestiti ora inesistente e ora ambigua.

**Rifletti:** per un biglietto di un concerto, che tipo di dato usi per l'inizio? Perché?

### Es1109 · Parsing `[L2]`
**File:** `Es1109_Parsing.java`

**Obiettivo:** trasformare testo in date e importi e viceversa con i formattatori.

**Prima di iniziare:** `LocalDate.parse("2026-03-29")` accetta il formato ISO. Per altri formati serve `DateTimeFormatter.ofPattern("dd/MM/yyyy")`. Attenzione ai pattern: `MM` è il mese, `mm` i minuti; `yyyy` è l'anno, `YYYY` quello "della settimana" (trappola).

**Cosa fare:**
1. Parsa `"2026-03-29"`, `"2026-03-29T10:15:00"`, `"2026-03-29T10:15:00Z"` rispettivamente con `LocalDate`, `LocalDateTime`, `Instant`.
2. Parsa `"29/03/2026"` con un formattatore e fallo fallire con `"31/02/2026"` e con `"29-03-2026"`: leggi `DateTimeParseException`.
3. Formatta una data come `"29 marzo 2026"` usando `Locale.ITALIAN` e il pattern `dd MMMM yyyy`.
4. Prova `YYYY` al posto di `yyyy` con la data `2025-12-29`: cosa stampa? (`DateTimeFormatter` in Java 8+ interpreta `YYYY` come anno basato sulle settimane.)
5. Parsa un importo `"1.234,56"` con `NumberFormat.getInstance(Locale.ITALY)` e un `DecimalFormat` con `setParseBigDecimal(true)`.
6. Formatta `BigDecimal("1234.5")` come valuta italiana (`NumberFormat.getCurrencyInstance(Locale.ITALY)`).
7. Scrivi `static Optional<LocalDate> provaParse(String s)` che tenta tre formati e restituisce il primo che funziona.

**Cosa osservare:** `getCurrencyInstance` usa il separatore e il simbolo del `Locale`.

**Verifica:**
- [ ] 3 formati parsati e 3 errori letti.
- [ ] `provaParse` funziona con tre formati.

**Rifletti:** cosa succede se un server è impostato con un `Locale` diverso dal tuo? Perché specificare sempre il `Locale`?

### Es1110 · Il tipo giusto per ogni campo `[L1]` · *progetto*
**File:** `Es1110_TipiGiusti.md` oppure `.java` con commenti

**Obiettivo:** scegliere i tipi del modello dati prima di scrivere il database.

**Prima di iniziare:** la scelta del tipo condiziona la correttezza (soldi, tempo), la persistenza (colonna del DB) e il JSON. È una domanda tipica nei colloqui di progettazione.

**Cosa fare:**
1. Crea una tabella con le colonne: **campo**, **tipo Java**, **tipo PostgreSQL**, **tipo JSON**, **perché**.
2. Compila una riga per ciascun campo dell'applicazione: `evento.id`, `evento.nome`, `evento.inizio`, `evento.fusoOrario`, `posto.codice`, `posto.fila`, `posto.numero`, `posto.prezzo`, `posto.stato`, `prenotazione.id` (UUID), `prenotazione.creataIl`, `prenotazione.scadenzaHold`, `utente.email`, `prenotazione.versione` (per il lock ottimistico), `prenotazione.importoTotaleCentesimi`.
3. Per `evento.inizio` scegli tra `LocalDateTime + fusoOrario` e `Instant` e motiva.
4. Per `stato` scegli tra `enum` salvato come stringa o come numero e motiva.
5. Per ogni campo di importo scegli tra `NUMERIC(12,2)` con `BigDecimal` e `BIGINT` in centesimi con `long`.
6. Verifica che nessun campo di denaro sia `double`/`float`.

**Cosa osservare:** `@Enumerated(EnumType.STRING)` è quasi sempre meglio di `ORDINAL`.

**Verifica:**
- [ ] Tabella con almeno 15 righe e motivazione.
- [ ] Nessun `double` per i soldi.

**Rifletti:** perché un `Long id` e non `int`? E un `UUID` come chiave: pro e contro?


## Cosa devi aver capito

- [ ] `BigDecimal` si crea da stringa; non ha operatori; `equals` considera la scala.
- [ ] `divide` richiede scala e arrotondamento; `setScale` per i due decimali.
- [ ] `Instant` per i momenti, `LocalDateTime` per gli eventi locali, `Duration` per le durate.
- [ ] `Clock` iniettabile al posto di `Instant.now()` nella logica.
