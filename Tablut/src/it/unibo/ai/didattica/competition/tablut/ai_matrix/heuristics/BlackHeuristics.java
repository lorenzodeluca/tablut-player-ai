package it.unibo.ai.didattica.competition.tablut.ai_matrix.heuristics;

import java.util.HashMap;

import aima.core.agent.State;
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;

public class BlackHeuristics extends Heuristics{

    public BlackHeuristics(State state) {
        super(state);
    }
    
    @Override
    public double evaluateState() {
        return 0;
    }
}
