package it.unibo.ai.didattica.competition.tablut.ai_matrix.heuristics;

import it.unibo.ai.didattica.competition.tablut.ai_matrix.heuristics.Heuristics;
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class WhiteHeuristics extends Heuristics {
    public static double STARTING_BLACK_PAWNS_COUNT = 16;
    public static double STARTING_WHITE_PAWNS_COUNT = 8;

    //per aggiornare i weights di default modificare resetToStockWeights();
    public static double WEIGHT_ALIVE = 0;
    public static double WEIGHT_KILLED = 0;
    public static double WEIGHT_KING_ESCAPE_ROUTES = 0; // peso della distanza del re dalla migliore via di fuga (una tra riga 2, riga 6, colonna 2, colonna 6)
    public static double WEIGHT_KING_GUARDS = 0; // "guardie" (pedine bianche) attorno al re
    public static double WEIGHT_BLACK_NEAR_KING = 0; // pedine nere attorno al re
    public static double WEIGHT_WHITE_PAWNS_IN_UNSAFE_POSITION = 0; // aggiunta per scoraggiare catture in posizioni non sicure se fossero possibili catture equivalenti in posizioni sicure

    public WhiteHeuristics(State state) {
        super(state);
    }

    // euristica
    /*
     * ogni valore per la valutazione dello stato deve essere tra 0 e 1
     */
    public double evaluateState() {
        resetToDefaultStrategyWeights();
        if (whiteWin()) return Double.POSITIVE_INFINITY;
        if (blackWin()) return Double.NEGATIVE_INFINITY;
        if (state.getTurn().equals(Turn.DRAW)) return 0.5;

        int[] kingPos = getKingPosition();
        int kingRow = kingPos[0];
        int kingCol = kingPos[1];

        // Tutti i punteggi vengono normalizzati tra 0 e 1

        // alive paws vs eaten pawns
        double current_white_pawns=boardCount(Pawn.WHITE);
        double current_black_pawns=boardCount(Pawn.BLACK);
        double eatenBlackPawns = STARTING_BLACK_PAWNS_COUNT - current_black_pawns;

        double whitePawnsAliveValue = current_white_pawns / STARTING_WHITE_PAWNS_COUNT;
        double blackPawnsEatenValue = 1 - (current_black_pawns / STARTING_BLACK_PAWNS_COUNT);

        // calcolo della possibile via di fuga (autostrade)
        double escapeValue = escapeRoute(kingPos);

        // numero di soldati bianchi adiacenti al re
        double guardsNearKing = whitePawnsNearKing() / 4.0;

        // numero di pedine nere adiacenti al re
        double blacksNearKing = blackPawnsNearKing() / 4.0;

        //numero di pedine in posizioni non sicure
        double whitePawnsInUnsafePositions = whitePawnsInUnsafePositions()/current_white_pawns;

        
        // ---dynamic weights---

        //molte volte perdiamo perchè ci mangiano il re dopo averlo intrappolato... idea: sarebbe bello premiare il mangiare dei pawns vicino al re
        if(blacksNearKing>0 && ((blacksNearKing+1)*2)>=pawnsToEatKing())protectKingStrategyWeights();

        //especially at the beginning of the game maybe its the best to try to minimize the number of enemies
        else if(eatenBlackPawns <= 5) eatEnemiesStrategyWeights();

        else resetToDefaultStrategyWeights();

        // calcolo dei pesi
        double aliveWeighted = WEIGHT_ALIVE * whitePawnsAliveValue;
        double killedWeighted = WEIGHT_KILLED * blackPawnsEatenValue;
        double kingDistanceWeighted = WEIGHT_KING_ESCAPE_ROUTES * escapeValue;
        double kingGuardsWeighted = WEIGHT_KING_GUARDS * guardsNearKing;
        double kingDangerWeighted = WEIGHT_BLACK_NEAR_KING * (1-blacksNearKing);
        double whitePawnsInUnsafePositionsWeighted = WEIGHT_WHITE_PAWNS_IN_UNSAFE_POSITION * whitePawnsInUnsafePositions;

        double res = aliveWeighted + killedWeighted + kingDistanceWeighted + kingGuardsWeighted + kingDangerWeighted;

        return res;
    }

    //3 modalità: chill(resetToDefaultStrategyWeights), difesa(protectKingStrategyWeights), terminator(eatEnemiesStrategyWeights)
    public void resetToDefaultStrategyWeights(){ // main goal: default strategy, the goal is for the king to escape
        WEIGHT_ALIVE = 0.30; // abbiamo poche pedine... dobbiamo evitare il più possibile di perderle
        WEIGHT_KILLED = 0.15;
        WEIGHT_KING_ESCAPE_ROUTES = 0.20;
        WEIGHT_KING_GUARDS = 0.15; 
        WEIGHT_BLACK_NEAR_KING = 0.15;
        WEIGHT_WHITE_PAWNS_IN_UNSAFE_POSITION = 0.05; 
    }
    public void protectKingStrategyWeights(){ // main goal: protect king
        WEIGHT_ALIVE = 0.30;
        WEIGHT_KILLED = 0; // the eating is focused to the pawns near the king
        WEIGHT_KING_ESCAPE_ROUTES= 0.20;
        WEIGHT_KING_GUARDS = 0.20;
        WEIGHT_BLACK_NEAR_KING = 0.25;
        WEIGHT_WHITE_PAWNS_IN_UNSAFE_POSITION = 0.05; 
    }
    public void eatEnemiesStrategyWeights(){ // main goal: kill enemies
        WEIGHT_ALIVE = 0.35;
        WEIGHT_KILLED = 0.45;
        WEIGHT_KING_ESCAPE_ROUTES= 0.05;
        WEIGHT_KING_GUARDS = 0.05;
        WEIGHT_BLACK_NEAR_KING = 0.05;
        WEIGHT_WHITE_PAWNS_IN_UNSAFE_POSITION = 0.05; 
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

    // aggiunta a escapeRoute una logica: se il re ha 2 vie di fuga libere, punteggio maggiore (il nero ne può bloccare solo una)
    // se ne ha solo una, si favoriscono le mosse in tale direzione ma non vince in automatico poiché può ancora essere bloccato
    public double escapeRoute(int[] kingPos) {
        double res = 0;
        int validRoutes = 0;
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
                        validRoutes++;
                    }
                } else {
                    // il re non è sul trono
                    validRoutes++;
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
                        validRoutes++;
                    }
                } else {
                    // il re non è sul trono
                    validRoutes++;
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
                        validRoutes++;
                    }
                } else {
                    // il re non è sul trono
                    validRoutes++;
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
                        validRoutes++;
                    }
                } else {
                    // il re non è sul trono
                    validRoutes++;
                }
            }
        }

        if(validRoutes == 0) {
            res = 0.0;
        } else if (validRoutes == 1) {
            res = 0.70; // ho una via ottima, ma è solo una: il nero può bloccarla 
        } else {
            res = 1.0; // 2 o più vie di fuga libere
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
