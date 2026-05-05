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
    private int timeout;

    public MatrixIterativeDeepeningAlphaBetaSearch(Game<State, Action, State.Turn> game, double min, double max, int time) {
        super(game, min, max, time);
        timeout = time;
    }

    @Override
    public Action makeDecision(State state) {
        nodesExpandedThreadSafe.reset();
        State.Turn player = this.game.getPlayer(state);
        List<Action> results = this.orderActions(state, this.game.getActions(state), player, 0);
        
        // Timer is already started inside the AIMA library usually, 
        // but ensure it's accessible or re-implement timing logic.
        long startTime = System.currentTimeMillis();
        long duration = 1000L * timeout; 

        this.currDepthLimit = 0;
        Action bestAction = results.get(0);

        do {
            this.currDepthLimit++;
            heuristicUsedThreadSafe.set(false);

            // Parallel evaluation of root actions
            List<Action> actionsToEvaluate = results;
            List<MoveValue> evaluatedMoves = customThreadPool.submit(() ->
                actionsToEvaluate.parallelStream().map(action -> {
                    // Each thread evaluates its branch
                    double value = minValue(this.game.getResult(state, action), 
                                            player, Double.NEGATIVE_INFINITY, 
                                            Double.POSITIVE_INFINITY, 1);
                    return new MoveValue(action, value);
                }).collect(Collectors.toList())
            ).join();

            // Sort results by value descending (best move first)
            evaluatedMoves.sort(Comparator.comparingDouble(mv -> -mv.value));
            
            // Check if we found a winning move or if time is up
            if (!evaluatedMoves.isEmpty()) {
                results = evaluatedMoves.stream().map(mv -> mv.action).collect(Collectors.toList());
                bestAction = results.get(0);
                
                // Optional: break if utility is maxed (win found)
                if (evaluatedMoves.get(0).value >= utilMax) break;
            }

        } while ((System.currentTimeMillis() - startTime) < (duration - 2000) && heuristicUsedThreadSafe.get());

        return bestAction;
    }

    @Override
    public double minValue(State state, State.Turn player, double alpha, double beta, int depth) {
        nodesExpandedThreadSafe.increment();
        return super.minValue(state, player, alpha, beta, depth);
    }

    @Override
    protected double eval(State state, State.Turn player) {
        heuristicUsedThreadSafe.set(true);
        return game.getUtility(state, player);
    }

    @Override
    public Metrics getMetrics() {
        Metrics m = super.getMetrics();
        m.set(METRICS_NODES_EXPANDED, nodesExpandedThreadSafe.sum());
        return m;
    }

    // Helper class for parallel results
    private static class MoveValue {
        Action action;
        double value;
        MoveValue(Action a, double v) { this.action = a; this.value = v; }
    }
}