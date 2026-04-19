package it.unibo.ai.didattica.competition.tablut.ai_matrix;

import java.util.Arrays;
import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class MatrixGameForDeepeningAlphaBetaSearch extends GameAshtonTablut implements aima.core.search.adversarial.Game<State, Action, State.Turn> {

    public MatrixGameForDeepeningAlphaBetaSearch(State state, int repeated_moves_allowed, int cache_size,
            String logs_folder, String whiteName, String blackName) {
        super(state, repeated_moves_allowed, cache_size, logs_folder, whiteName, blackName);
    }

    @Override
    public List<Action> getActions(State arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getActions'");
    }

    @Override
    public State getInitialState() {
        // not needed for DeepeningAlphaBetaSearch
        throw new UnsupportedOperationException("Unimplemented method 'getInitialState'");
    }

    
    @Override
    public Turn getPlayer(State state) { //the next player to play
        return state.getTurn();
    }

    @Override
    public Turn[] getPlayers() {
        return Arrays.copyOfRange(State.Turn.values(), 0, 2);
    }

    @Override
    public State getResult(State arg0, Action arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getResult'");
    }

    //method that uses heuristics
    @Override
    public double getUtility(State state, Turn turn) {
        if ((turn.equals(State.Turn.WHITE) && state.getTurn().equals(State.Turn.WHITEWIN)) || (turn.equals(State.Turn.BLACK) && state.getTurn().equals(State.Turn.BLACKWIN)) ){
            //win
            return Double.POSITIVE_INFINITY;
        } 
		else if ((turn.equals(State.Turn.WHITE) && state.getTurn().equals(State.Turn.BLACKWIN)) || (turn.equals(State.Turn.BLACK) && state.getTurn().equals(State.Turn.WHITEWIN)) ) {
            //game lost
            return Double.NEGATIVE_INFINITY;
        }


			// if it isn't a terminal state
			Heuristics heuristics = null;
			if (turn.equals(State.Turn.WHITE)) {
				heuristics = new WhiteMoonHeuristics(state);
			} else {
				heuristics = new BlackHoleHeuristics(state);
			}
			return  heuristics.evaluateState();
    }

    //check if the state is terminal(it means the game end)
    @Override
    public boolean isTerminal(State arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isTerminal'");
    }
    
}
