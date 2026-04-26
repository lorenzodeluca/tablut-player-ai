package it.unibo.ai.didattica.competition.tablut.ai_matrix.heuristics;

import java.util.HashMap;

import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;

public class BlackHeuristics extends Heuristics{
    public static double BLACK_PAWNS_COUNT = 16;
    public static double WHITE_PAWNS_COUNT = 8;
    public static double WEIGHT_ALIVE = 0.5;
    public static double WEIGHT_KILLED = 0.5;

    
    public BlackHeuristics(State s) {
        super(s);
    }

    //euristica nero
    /*
        ogni valore per la valutazione dello stato deve essere tra 0 e 1
    */
    public double evaluateState() {
        double blackPawnsAlivePercentage = boardCount(Pawn.BLACK)/BLACK_PAWNS_COUNT;
        double whitePawnsEatenPercentage = 1-(boardCount(Pawn.WHITE)/WHITE_PAWNS_COUNT);

        double res=WEIGHT_ALIVE*blackPawnsAlivePercentage+WEIGHT_KILLED*whitePawnsEatenPercentage;
        if(whiteWin())return 0;
        if(blackWin())return 1;
        return res;
    }
}
