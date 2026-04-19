package it.unibo.ai.didattica.competition.tablut.ai_matrix;

import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import aima.core.search.adversarial.Game;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class MatrixIterativeDeepeningAlphaBetaSearch extends IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn>{
    public MatrixIterativeDeepeningAlphaBetaSearch(Game<State, Action, State.Turn> game, double min, double max, int time) {
        super(game, min, max, time);
    }

    @Override
    protected double eval(State state, State.Turn player) {
        super.eval(state, player);
        return game.getUtility(state, player);
    }

}
