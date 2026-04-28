package it.unibo.ai.didattica.competition.tablut.ai_matrix.heuristics;

import it.unibo.ai.didattica.competition.tablut.ai_matrix.heuristics.Heuristics;
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class WhiteHeuristics extends Heuristics {
    public static double STARTING_BLACK_PAWNS_COUNT = 16;
    public static double STARTING_WHITE_PAWNS_COUNT = 8;
    public static double WEIGHT_ALIVE = 0.15;
    public static double WEIGHT_KILLED = 0.15;
    public static double WEIGHT_KING_ESCAPE_ROUTES = 0.45; // peso della distanza del re dalla migliore via di fuga (una tra riga 2, riga 6, colonna 2, colonna 6)
    public static double WEIGHT_KING_GUARDS = 0.25; // "guardie" (pedine bianche) attorno al re
    //TODO considerare se aggiungere un peso al numero di pedine nere intorno al re


    public WhiteHeuristics(State state) {
        super(state);
    }

    // euristica
    /*
     * ogni valore per la valutazione dello stato deve essere tra 0 e 1
     */
    public double evaluateState() {
        //if (whiteWin())
        //    return 1.0;
        //if (blackWin())
        //    return 0.0;
        if (state.getTurn().equals(Turn.WHITEWIN)) return 1.0;
        if (state.getTurn().equals(Turn.BLACKWIN)) return 0.0;
        if (state.getTurn().equals(Turn.DRAW)) return 0.5;

        int[] kingPos = getKingPosition();
        int kingRow = kingPos[0];
        int kingCol = kingPos[1];

        // Tutti i punteggi vengono normalizzati tra 0 e 1

        // alive paws vs eaten pawns
        double current_white_pawns=boardCount(Pawn.WHITE);
        double current_black_pawns=boardCount(Pawn.BLACK);
        double whitePawnsAliveValue = current_white_pawns / STARTING_WHITE_PAWNS_COUNT;
        double blackPawnsEatenValue = 1 - (boardCount(Pawn.BLACK) / STARTING_BLACK_PAWNS_COUNT);

        // calcolo della possibile via di fuga (autostrade)
        double escapeValue = escapeRoute(kingPos);

        // numero di soldati bianchi adiacenti al re
        double guardsNearKing = whitePawnsNearKing() / 4.0;

        
        //dynamic weights
        if(blackPawnsEatenValue<=5){
            WEIGHT_KILLED = 0.30;
            WEIGHT_KING_ESCAPE_ROUTES = 0.30;
        }else{
            WEIGHT_KILLED = 0.15;
            WEIGHT_KING_ESCAPE_ROUTES = 0.45;
        }

        // calcolo dei pesi
        double aliveWeighted = WEIGHT_ALIVE * whitePawnsAliveValue;
        double killedWeighted = WEIGHT_KILLED * blackPawnsEatenValue;
        double kingDistanceWeighted = WEIGHT_KING_ESCAPE_ROUTES * escapeValue;
        double kingGuardsWeighted = WEIGHT_KING_GUARDS * guardsNearKing;

        double res = aliveWeighted + killedWeighted + kingDistanceWeighted + guardsNearKing;

   

        return res;
    }

    /*
     * Questa funzione controlla se ci sono delle vie di fuga ideali libere
     * (chiamiamole "autostrade")
     * Si controllano 4 linee: la riga 2, la riga 6, la colonna 2 e la colonna 6
     * Per ognuna di esse si fanno 2 check:
     * 1) se la linea in questione non presenta ostacoli (inteso come numero di
     * pedine, bianche o nere, al suo interno)
     * 2) se è libera da ostacoli, si controlla se il re ha un percorso pulito privo
     * di ostacoli per arrivarci
     * 
     * È previsto un controllo extra se il re è nella posizione centrale, ovvero
     * [4,4]
     */

    public double escapeRoute(int[] kingPos) {
        double res = 0;
        int obstacles = 0;

        // riga 2
        for (int i = 0; i < 9; i++) {
            if (state.getPawn(2, i).equalsPawn(State.Pawn.BLACK.toString())
                    || state.getPawn(2, i).equalsPawn(State.Pawn.WHITE.toString())) {
                obstacles++; // incrementa il contatore degli ostacoli se ne trova uno su questa autostrada
            }
        }

        if (obstacles == 0) {
            int[] target = { 2, kingPos[1] };

            boolean freePath = isPathClear(state, kingPos, target);

            // se il percorso tra re e riga 2 è libero, procedo al controllo finale sulla
            // posizione del re
            if (freePath) {

                // controllo se il re è sul trono
                if (kingPos[0] == 4 && kingPos[1] == 4) {
                    if ((state.getPawn(3, 3).equalsPawn(State.Pawn.WHITE.toString()) &&
                            state.getPawn(3, 5).equalsPawn(State.Pawn.WHITE.toString())) || freeRow(3)) {
                        res = 1;
                    }
                } else {
                    // il re non è sul trono
                    res = 1;
                }
            }
        }

        // riga 6
        obstacles = 0;
        for (int i = 0; i < 9; i++) {
            if (state.getPawn(6, i).equalsPawn(State.Pawn.BLACK.toString())
                    || state.getPawn(6, i).equalsPawn(State.Pawn.WHITE.toString())) {
                obstacles++; // incrementa il contatore degli ostacoli se ne trova uno su questa autostrada
            }
        }

        if (obstacles == 0) {
            int[] target = { 6, kingPos[1] };

            boolean freePath = isPathClear(state, kingPos, target);

            // se il percorso tra re e riga 6 è libero, procedo al controllo finale sulla
            // posizione del re
            if (freePath) {

                // controllo se il re è sul trono
                if (kingPos[0] == 4 && kingPos[1] == 4) {
                    if ((state.getPawn(5, 3).equalsPawn(State.Pawn.WHITE.toString()) &&
                            state.getPawn(5, 5).equalsPawn(State.Pawn.WHITE.toString())) || freeRow(5)) {
                        res = 1;
                    }
                } else {
                    // il re non è sul trono
                    res = 1;
                }
            }
        }

        // colonna 2
        obstacles = 0;
        for (int i = 0; i < 9; i++) {
            if (state.getPawn(i, 2).equalsPawn(State.Pawn.BLACK.toString())
                    || state.getPawn(i, 2).equalsPawn(State.Pawn.WHITE.toString())) {
                obstacles++; // incrementa il contatore degli ostacoli se ne trova uno su questa autostrada
            }
        }

        if (obstacles == 0) {
            int[] target = { kingPos[0], 2 };

            boolean freePath = isPathClear(state, kingPos, target);

            // se il percorso tra re e colonna 2 è libero, procedo al controllo finale sulla
            // posizione del re
            if (freePath) {

                // controllo se il re è sul trono
                if (kingPos[0] == 4 && kingPos[1] == 4) {
                    if ((state.getPawn(3, 3).equalsPawn(State.Pawn.WHITE.toString()) &&
                            state.getPawn(5, 3).equalsPawn(State.Pawn.WHITE.toString())) || freeColumn(3)) {
                        res = 1;
                    }
                } else {
                    // il re non è sul trono
                    res = 1;
                }
            }
        }

        // colonna 6
        obstacles = 0;
        for (int i = 0; i < 9; i++) {
            if (state.getPawn(i, 6).equalsPawn(State.Pawn.BLACK.toString())
                    || state.getPawn(i, 6).equalsPawn(State.Pawn.WHITE.toString())) {
                obstacles++; // incrementa il contatore degli ostacoli se ne trova uno su questa autostrada
            }
        }

        if (obstacles == 0) {
            int[] target = { kingPos[0], 6 };

            boolean freePath = isPathClear(state, kingPos, target);

            // se il percorso tra re e colonna 6 è libero, procedo al controllo finale sulla
            // posizione del re
            if (freePath) {

                // controllo se il re è sul trono
                if (kingPos[0] == 4 && kingPos[1] == 4) {
                    if ((state.getPawn(3, 5).equalsPawn(State.Pawn.WHITE.toString()) &&
                            state.getPawn(5, 5).equalsPawn(State.Pawn.WHITE.toString())) || freeColumn(5)) {
                        res = 1;
                    }
                } else {
                    // il re non è sul trono
                    res = 1;
                }
            }
        }

        return res;
    }

    private boolean isPathClear(State state, int[] startPosition, int[] targetPosition) {
        int row = startPosition[0];
        int column = startPosition[1];

        // mi definisco un "vettore di direzione" confrontando la posizione del re con
        // quella obiettivo
        int rowDir = Integer.compare(targetPosition[0], row);
        int colDir = Integer.compare(targetPosition[1], column);

        // mi sposto dalla posizione di partenza verso il target
        row += rowDir;
        column += colDir;

        // controllo con un while tutte le pedine intermedie tra la posizione di
        // partenza e quella obiettivo
        while (row != targetPosition[0] || column != targetPosition[1]) {
            int[] currentPosition = { row, column };

            if (checkOccupiedPosition(state, currentPosition)) {
                return false; // stoppo il controllo perché ho trovato nel mezzo una casella occupata
            }

            // se non ho ostacoli, faccio un nuovo passo nella direzione prevista dal
            // vettore prima calcolato
            row += rowDir;
            column += colDir;
        }

        // se il ciclo while finisce senza ritornare false, il percorso è libero
        return true;
    }

    // metodo di utilità che controlla se una casella della scacchiera è occupata da
    // una pedina
    public boolean checkOccupiedPosition(State state, int[] position) {
        return !state.getPawn(position[0], position[1]).equals(State.Pawn.EMPTY);
    }

    // metodo di utilità che controlla se una riga della scacchiera è libera
    public boolean freeRow(int i) {
        for (int j = 0; j < 9; j++) {
            if (!state.getPawn(i, j).equals(State.Pawn.EMPTY)) {
                return false;
            }
        }

        return true;
    }

    // metodo di utilità che controlla se una colonna della scacchiera è libera
    public boolean freeColumn(int j) {
        for (int i = 0; i < 9; i++) {
            if (!state.getPawn(i, j).equals(State.Pawn.EMPTY)) {
                return false;
            }
        }

        return true;
    }

}
