package it.unibo.ai.didattica.competition.tablut.ai_matrix;

import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import aima.core.search.adversarial.Game;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class MatrixIterativeDeepeningAlphaBetaSearch extends IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn>{
    public MatrixIterativeDeepeningAlphaBetaSearch(Game<State, Action, State.Turn> game, double utilMin, double utilMax, int time) {
        super(game, utilMin, utilMax, time);
    }


    @Override
    protected double eval(State state, State.Turn player) {
        // needed to make heuristicEvaluationUsed = true, if the state evaluated isn't terminal
        super.eval(state, player);

        // return heuristic value for given state
        return 0;//game.getUtility(state, player);
    }


    /**
     * Method controlling the search. It is based on iterative
     * deepening and tries to make to a good decision in limited time.
     * It is overrided to print metrics.
     */
    @Override
    public Action makeDecision(State state) {
        Action a = super.makeDecision(state);
       // System.out.println("Explored a total of " + getMetrics().get(METRICS_NODES_EXPANDED) + " nodes, reaching a depth limit of " + getMetrics().get(METRICS_MAX_DEPTH));
        return  a;
    }
}
