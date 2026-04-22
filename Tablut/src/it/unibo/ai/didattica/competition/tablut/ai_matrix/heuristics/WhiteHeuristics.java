package it.unibo.ai.didattica.competition.tablut.ai_matrix.heuristics;

import aima.core.agent.State;
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;

public class WhiteHeuristics extends Heuristics{
    public static double BLACK_PAWNS_COUNT = 16;
    public static double WHITE_PAWNS_COUNT = 8;
    public static double WEIGHT_ALIVE = 0.5;
    public static double WEIGHT_KILLED = 0.5;

    public WhiteHeuristics(State state) {
        super(state);
    }
    
    //euristica
    /*
        ogni valore per la valutazione dello stato deve essere tra 0 e 1
    */
    public double evaluateState() {
        double whitePawnsAlivePercentage = boardCount(Pawn.WHITE)/WHITE_PAWNS_COUNT;
        double blackPawnsEatenPercentage = 1-(boardCount(Pawn.BLACK)/BLACK_PAWNS_COUNT);

        double res=WEIGHT_ALIVE*whitePawnsAlivePercentage+WEIGHT_KILLED*blackPawnsEatenPercentage;
        return res;
    }
}
