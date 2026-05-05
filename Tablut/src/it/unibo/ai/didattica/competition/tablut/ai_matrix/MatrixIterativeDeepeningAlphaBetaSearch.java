package it.unibo.ai.didattica.competition.tablut.ai_matrix;

import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import aima.core.search.framework.Metrics;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import aima.core.search.adversarial.Game;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class MatrixIterativeDeepeningAlphaBetaSearch extends IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn> {

    private final ForkJoinPool customThreadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    private final LongAdder nodesExpandedThreadSafe = new LongAdder();
    private final AtomicBoolean heuristicUsedThreadSafe = new AtomicBoolean(false);
    private final int timeoutSeconds;

    public MatrixIterativeDeepeningAlphaBetaSearch(Game<State, Action, State.Turn> game, double min, double max, int time) {
        super(game, min, max, time);
        this.timeoutSeconds = time;
    }

    @Override
    public Action makeDecision(State state) {
        nodesExpandedThreadSafe.reset();
        State.Turn player = this.game.getPlayer(state);
        List<Action> results = this.orderActions(state, this.game.getActions(state), player, 0);
        final List<Action> orderedResults = results;

        long startTime = System.currentTimeMillis();
        long duration = 1000L * timeoutSeconds;
        this.currDepthLimit = 0;
        Action bestAction = results.get(0);

        // Iterative Deepening Loop
        while (true) {
            this.currDepthLimit++;
            heuristicUsedThreadSafe.set(false);

            final int depth = this.currDepthLimit;
            
            // Valutazione parallela delle mosse alla radice
            final List<Action> currentResults = results;
            List<MoveValue> evaluatedMoves = customThreadPool.submit(() ->
                currentResults.parallelStream().map(action -> {
                    // Creiamo uno stato risultante per ogni mossa
                    State nextState = this.game.getResult(state, action); 
                    // Chiamiamo la NOSTRA versione thread-safe di minValue
                    double value = threadSafeMinValue(nextState, player, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1, depth, startTime, duration);
                    return new MoveValue(action, value);
                }).collect(Collectors.toList())
            ).join();

            // Ordiniamo i risultati (best move first per la prossima iterazione)
            evaluatedMoves.sort(Comparator.comparingDouble(mv -> -mv.value));
            results = evaluatedMoves.stream().map(mv -> mv.action).collect(Collectors.toList());
            bestAction = results.get(0);

            // Logging immediato della profondità completata
            System.out.println("Completed Depth: " + depth + " | Best move: " + bestAction + " | Value: " + evaluatedMoves.get(0).value);

            // Condizioni di uscita:
            // 1. Trovata vittoria certa
            if (evaluatedMoves.get(0).value >= utilMax) break; 
            // 2. Timeout o non abbiamo usato euristiche (albero finito)
            if ((System.currentTimeMillis() - startTime) > (duration - 2000) || !heuristicUsedThreadSafe.get()) break;
        }

        return bestAction;
    }

    // Metodo MIN thread-safe (non usa super.minValue)
    private double threadSafeMinValue(State state, State.Turn player, double alpha, double beta, int depth, int limit, long start, long duration) {
        nodesExpandedThreadSafe.increment();
        
        if (this.game.isTerminal(state) || depth >= limit || (System.currentTimeMillis() - start) > (duration - 500)) {
            return this.eval(state, player);
        }

        double value = Double.POSITIVE_INFINITY;
        for (Action action : this.game.getActions(state)) {
            value = Math.min(value, threadSafeMaxValue(this.game.getResult(state, action), player, alpha, beta, depth + 1, limit, start, duration));
            if (value <= alpha) return value;
            beta = Math.min(beta, value);
        }
        return value;
    }

    // Metodo MAX thread-safe (non usa super.maxValue)
    private double threadSafeMaxValue(State state, State.Turn player, double alpha, double beta, int depth, int limit, long start, long duration) {
        nodesExpandedThreadSafe.increment();

        if (this.game.isTerminal(state) || depth >= limit || (System.currentTimeMillis() - start) > (duration - 500)) {
            return this.eval(state, player);
        }

        double value = Double.NEGATIVE_INFINITY;
        for (Action action : this.game.getActions(state)) {
            value = Math.max(value, threadSafeMinValue(this.game.getResult(state, action), player, alpha, beta, depth + 1, limit, start, duration));
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

    private static class MoveValue {
        Action action;
        double value;
        MoveValue(Action a, double v) { this.action = a; this.value = v; }
    }
}