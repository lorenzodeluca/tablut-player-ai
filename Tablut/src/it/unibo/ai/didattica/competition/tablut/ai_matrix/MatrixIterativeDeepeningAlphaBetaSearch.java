package it.unibo.ai.didattica.competition.tablut.ai_matrix;

import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import aima.core.search.framework.Metrics;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/*
-------------------PARALLELIZZAZIONE----------------
---Funzionamento della parallelizzazione---
Nella Root-Level Parallelism, i thread collaborano seguendo un modello di divisione del lavoro, 
dove il coordinamento(sincronizzazione) avviene all'inizio e alla fine di ogni ciclo di profondità dell'Iterative Deepening.

---Suddivisione dei compiti---
Il thread principale di Java(Master/Main) agisce come un supervisore. Prende l'elenco delle mosse legali possibili nello stato attuale e le inserisce in una coda di lavoro.
Grazie al parallelStream, Java non assegna un numero fisso di mosse a ogni thread all'inizio. Utilizza invece una coda: non appena un thread finisce di valutare una mossa, ne preleva immediatamente un'altra.

---Vantaggi--- 
A differenza della versione sequenziale riusciamo a scendere di 1 livello intero in più nella ricerca, quindi 5 invece di 4(intendendo per discesa di un livello il completamento di sia una iterazione min che di una iterazione max per ogni mossa possibile)

---Svantaggi--- 
Perdita di pruning tra i thread(il pruning avviene localmente al thread nella ricerca nei rami a lui assegnati): In una ricerca sequenziale, se il primo ramo analizzato è molto forte, 
si può scartare gran parte dei rami successivi senza nemmeno guardarli. Nella versione parallela alla radice, 
i thread partono quasi contemporaneamente sulle prime N mosse. Questo significa che i thread iniziali non possono beneficiare 
della potatura derivante dalle scoperte degli altri thread in tempo reale. Di conseguenza, la ricerca parallela esplora 
complessivamente più nodi rispetto a quella sequenziale per raggiungere la stessa profondità, perché "spreca" calcoli su rami che una ricerca sequenziale avrebbe potato subito.

---Perchè abbiamo dovuto reimplementare la makeDecision e le minValue/maxValue?---
La Root-Level Parallelism e la necessità di reimplementare i metodi di AIMA sono dettate da come la ricerca gestisce i dati e la memoria.
In una ricerca sequenziale standard, l'algoritmo valuta ogni mossa possibile alla radice (Root) una dopo l'altra. 
Con la Root-Level Parallelism, prendiamo l'elenco delle mosse legali iniziali e le distribuiamo simultaneamente su più core della CPU. 
Ogni core prende una mossa e calcola l'intero sotto-albero di gioco derivante da essa in totale autonomia. Questo permette di esplorare 
rami diversi nello stesso istante, aumentando drasticamente la velocità di analisi e la profondità raggiungibile nel tempo limite dei 60 secondi.
Non possiamo utilizzare i metodi stock della libreria AIMA (come super.minValue e super.maxValue) perché non sono thread-safe. 
La classe IterativeDeepeningAlphaBetaSearch originale è progettata per un'esecuzione a thread singolo e utilizza variabili di istanza globali per gestire il timer, 
i contatori dei nodi e i flag di stato. In un ambiente parallelo, se più thread chiamassero contemporaneamente i metodi originali, inizierebbero
a sovrascrivere queste variabili condivise. Il risultato è un comportamento errato dell'algoritmo.
Reimplementando i metodi come threadSafeMinValue e threadSafeMaxValue, abbiamo reso la ricerca "stateless". 
Ogni informazione necessaria, come il limite di profondità o il tempo di inizio, viene passata come parametro locale alla funzione. 
Questo garantisce che ogni thread lavori nella propria porzione di memoria protetta, utilizzando strumenti atomici come LongAdder per le metriche, 
assicurando che i core collaborino in modo pulito senza calpestarsi i piedi a vicenda.
*/
public class MatrixIterativeDeepeningAlphaBetaSearch extends IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn> {
    private final ForkJoinPool customThreadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    private final LongAdder nodesExpandedThreadSafe = new LongAdder();
    private final AtomicBoolean heuristicUsedThreadSafe = new AtomicBoolean(false);
    private final int timeoutSeconds;

    public MatrixIterativeDeepeningAlphaBetaSearch(Game<State, Action, State.Turn> game,double min,double max,int time) {
        super(game, min, max, time);
        this.timeoutSeconds = time;
    }

    /**
     * Esegue la decisione della mossa migliore utilizzando una ricerca Iterative Deepening
     * con potatura Alpha-Beta, parallelizzata a livello di radice (Root-Level Parallelism).
     *
     * Il metodo sfrutta un ForkJoinPool per distribuire la valutazione dei rami iniziali
     * su tutti i core disponibili della CPU.
     *
     * @param state Lo stato attuale del gioco da cui partire per la ricerca.
     * @return L'azione (mossa) migliore individuata entro i limiti di tempo e profondità.
     */
    @Override
    public Action makeDecision(State state) {
        nodesExpandedThreadSafe.reset();
        State.Turn player = this.game.getPlayer(state);

        // Otteniamo e ordiniamo le mosse legali per migliorare l'efficacia della potatura
        List<Action> results = this.orderActions(state, this.game.getActions(state), player, 0);

        long startTime = System.currentTimeMillis();
        long duration = 1000L * timeoutSeconds;
        this.currDepthLimit = 0;
        Action bestAction = results.get(0);

        // Loop dell'Iterative Deepening: aumenta la profondità ad ogni iterazione
        while (true) {
            this.currDepthLimit++;
            heuristicUsedThreadSafe.set(false);
            final int depth = this.currDepthLimit;
            final List<Action> rootActions = results;

            // Parallelizzazione: ogni mossa alla radice viene valutata in un thread separato
            List<MoveValue> evaluatedMoves = customThreadPool.submit(() ->
                rootActions.parallelStream().map(action -> {
                    // Creiamo uno stato risultante per ogni mossa
                    State nextState = this.game.getResult(state, action); 
                    // Chiamiamo la NOSTRA versione thread-safe di minValue
                    double value = threadSafeMinValue(nextState, player, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1, depth, startTime, duration);
                    return new MoveValue(action, value);
                }).collect(Collectors.toList())
            ).join();

            // Sort dei risultati: permette alla prossima iterazione (più profonda)
            // di analizzare per prime le mosse che si sono dimostrate migliori finora
            evaluatedMoves.sort(Comparator.comparingDouble(mv -> -mv.value));
            results = evaluatedMoves.stream().map(mv -> mv.action).collect(Collectors.toList());
            bestAction = results.get(0);

            System.out.println("Completed Depth: " +depth +" | Best move: " +bestAction +" | Value: " + evaluatedMoves.get(0).value);

            // Condizione 1: Trovata una vittoria matematica (utilità massima)
            if (evaluatedMoves.get(0).value >= utilMax) break;

            // Condizione 2: Tempo quasi esaurito (margine di 2 secondi) o albero esplorato interamente
            if ((System.currentTimeMillis() - startTime) > (duration - 2000) ||!heuristicUsedThreadSafe.get() ) break;
        }

        return bestAction;
    }

    /**
     * Versione thread-safe del valore MIN (giocatore avversario).
     * Cerca di minimizzare l'utilità per il giocatore che ha effettuato la mossa iniziale.
     *
     * @param state Stato attuale da valutare.
     * @param player Il giocatore che sta effettuando la ricerca (MAX).
     * @param alpha Il valore migliore già trovato per MAX lungo il percorso.
     * @param beta Il valore migliore già trovato per MIN lungo il percorso.
     * @param depth Profondità attuale nell'albero.
     * @param limit Limite massimo di profondità per questa iterazione.
     * @param start Timestamp di inizio ricerca (per controllo timeout).
     * @param duration Durata massima consentita in millisecondi.
     * @return Il valore di utilità stimato per questo stato.
     */
    private double threadSafeMinValue(State state, State.Turn player, double alpha, double beta, int depth, int limit, long start, long duration) {
        nodesExpandedThreadSafe.increment();

        // Condizione di arresto: stato terminale, limite profondità raggiunto o timeout imminente
        if (this.game.isTerminal(state) || depth >= limit || (System.currentTimeMillis() - start) > (duration - 500)) {
            return this.eval(state, player);
        }

        double value = Double.POSITIVE_INFINITY;
        for (Action action : this.game.getActions(state)) {
            value = Math.min(value,threadSafeMaxValue(this.game.getResult(state, action),player,alpha,beta,depth + 1,limit,start,duration));
            // Potatura Alpha-Beta
            if (value <= alpha) return value;
            beta = Math.min(beta, value);
        }
        return value;
    }

    /**
     * Versione thread-safe del valore MAX (il nostro giocatore).
     * Cerca di massimizzare l'utilità analizzando le risposte dell'avversario.
     * @param state Stato attuale da valutare.
     * @param player Il giocatore che sta effettuando la ricerca (MAX).
     * @param alpha Il valore migliore già trovato per MAX lungo il percorso.
     * @param beta Il valore migliore già trovato per MIN lungo il percorso.
     * @param depth Profondità attuale nell'albero.
     * @param limit Limite massimo di profondità per questa iterazione.
     * @param start Timestamp di inizio ricerca (per controllo timeout).
     * @param duration Durata massima consentita in millisecondi.
     * @return Il valore di utilità stimato per questo stato.
     */
    private double threadSafeMaxValue(State state,State.Turn player,double alpha,double beta,int depth,int limit,long start,long duration) {
        nodesExpandedThreadSafe.increment();

        if (this.game.isTerminal(state) ||depth >= limit ||(System.currentTimeMillis() - start) > (duration - 500)) {
            return this.eval(state, player);
        }

        double value = Double.NEGATIVE_INFINITY;
        for (Action action : this.game.getActions(state)) {
            value = Math.max(value,threadSafeMinValue(this.game.getResult(state, action), player, alpha, beta, depth + 1, limit, start, duration));
            // Potatura Alpha-Beta
            if (value >= beta) return value;
            alpha = Math.max(alpha, value);
        }
        return value;
    }

    @Override
    protected double eval(State state, State.Turn player) {
        if (!this.game.isTerminal(state)) {
            heuristicUsedThreadSafe.set(true);
        }
        return this.game.getUtility(state, player);
    }

    /**
     * Classe di supporto per mappare una specifica azione al suo valore numerico calcolato.
     * Utilizzata per raccogliere e ordinare i risultati dei vari thread.
     */
    private static class MoveValue {
        Action action; // La mossa alla radice
        double value; // Il valore di utilità restituito dalla ricerca Minimax
        MoveValue(Action a, double v) {
            this.action = a;
            this.value = v;
        }
    }
}
