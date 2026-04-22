package it.unibo.ai.didattica.competition.tablut.ai_matrix.heuristics;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;

public class Heuristics {
    protected State state;
	
	public Heuristics(aima.core.agent.State s) {
        this.state = s;
    }

    public double evaluateState() {
        return 0;
    }

    public double boardCount(Pawn pawn) {
		double count = 0;
		for (int i = 0; i < state.getBoard()[0].length; i++) {
			for (int j = 0; j < state.getBoard()[0].length; j++) {
				if (state.getBoard()[i][j].equals(pawn)) {
                    count++;
				}
			}
		}
		return count;
	}
}
