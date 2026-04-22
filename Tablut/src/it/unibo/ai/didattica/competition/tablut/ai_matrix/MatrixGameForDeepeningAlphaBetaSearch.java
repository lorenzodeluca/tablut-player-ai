package it.unibo.ai.didattica.competition.tablut.ai_matrix;

import java.util.Arrays;
import java.util.List;

import it.unibo.ai.didattica.competition.tablut.ai_matrix.heuristics.BlackHeuristics;
import it.unibo.ai.didattica.competition.tablut.ai_matrix.heuristics.WhiteHeuristics;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
/**
 * Classe che aggiunge alla classe base 'GameAshtonTablut' con le meccaniche del gioco
 * l'implementazione dei metodi della classe  aima.core.search.adversarial.Game
 * in modo da poter usare l'implementazione della DeepeningAlphaBetaSearch
 * della libreria aima.
 */
public class MatrixGameForDeepeningAlphaBetaSearch extends GameAshtonTablut implements aima.core.search.adversarial.Game<State, Action, State.Turn> {

    public MatrixGameForDeepeningAlphaBetaSearch(State state, int repeated_moves_allowed, int cache_size,
            String logs_folder, String whiteName, String blackName) {
        super(state, repeated_moves_allowed, cache_size, logs_folder, whiteName, blackName);
    }

    /**
     * descrizione di esempio
     * 
     * @param arg0 spiegazione del parametro
     * @return ritorna una lista con le possibili mosse
     */
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
    public Turn getPlayer(State state) { //il prossimo giocatore a fare la mossa
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

    //metodi che usano l'euristicas
    @Override
    public double getUtility(State state, Turn turn) {
        if ((turn.equals(State.Turn.WHITE) && state.getTurn().equals(State.Turn.WHITEWIN)) || (turn.equals(State.Turn.BLACK) && state.getTurn().equals(State.Turn.BLACKWIN)) ){
            //vittoria
            return Double.POSITIVE_INFINITY;
        } 
		else if ((turn.equals(State.Turn.WHITE) && state.getTurn().equals(State.Turn.BLACKWIN)) || (turn.equals(State.Turn.BLACK) && state.getTurn().equals(State.Turn.WHITEWIN)) ) {
            //sconfitta
            return Double.NEGATIVE_INFINITY;
        }


			// se non è uno stato finale 
			Heuristics heuristics = null;
			if (turn.equals(State.Turn.WHITE)) {
				heuristics = new WhiteHeuristics(state);
			} else {
				heuristics = new BlackHeuristics(state);
			}
			return  heuristics.evaluateState();
    }

    //controlla se lo stato è uno stato finale(d)
    @Override
    public boolean isTerminal(State arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isTerminal'");
    }
    
}
