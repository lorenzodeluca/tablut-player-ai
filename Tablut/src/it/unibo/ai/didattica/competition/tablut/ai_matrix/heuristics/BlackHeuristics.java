package it.unibo.ai.didattica.competition.tablut.ai_matrix.heuristics;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class BlackHeuristics extends Heuristics {

    public static final double BLACK_PAWNS_COUNT = 16.0;
    public static final double WHITE_PAWNS_COUNT = 8.0;

    //to maximize
    public static double WEIGHT_ALIVE = 0.20; // premia il fatto che il nero abbia ancora molte pedine in gioco
    public static double WEIGHT_KILLED = 0.20; // premia il fatto che il nero abbia già eliminato pedine bianche, quindi il bianco ha meno difese attorno al re
    public static double WEIGHT_BLACK_NEAR_KING = 0.18; // quanta pressione c’è intorno al re di neri
    public static double WEIGHT_BLACK_ESCAPE_BLOCK = 0.06; // caselle che spesso servono per preparare il blocco dell’uscita
    public static double WEIGHT_BLACK_ESCAPE_BLOCK_MOST_IMPORTANT = 0.20; // caselle che spesso servono per preparare il blocco dell’uscita

    //to minimize
    public static double WEIGHT_KING_ESCAPE_ROUTES = 0.16; // peso della vicinanza del re a una casella di fuga sul bordo
    public static double WEIGHT_OPEN_LINES = 0.20; // quante direzioni dal re verso il bordo sono libere da ostacoli
    public static double WEIGHT_WHITE_NEAR_KING = 0.06; // quanta pressione/protezione c’è intorno al re di bianchi
    public static double WEIGHT_KING_MOBILITY = 0.08; // caselle libere vicino al re
    public static double WEIGHT_WHITE_ESCAPE_SUPPORT = 0.04; // caselle che spesso servono per preparare l’uscita


    public BlackHeuristics(State state) {
        super(state);
    }
    
    @Override
    public double evaluateState() {
        if (state.getTurn().equals(Turn.BLACKWIN)) return Double.POSITIVE_INFINITY;
        if (state.getTurn().equals(Turn.WHITEWIN)) return Double.NEGATIVE_INFINITY;
        if (state.getTurn().equals(Turn.DRAW)) return 0.5;
        
        int[] king = getKingPosition();
        int kr = king[0];
        int kc = king[1];

        double blackAlive = boardCount(Pawn.BLACK) / BLACK_PAWNS_COUNT;
        double whiteEaten = (WHITE_PAWNS_COUNT - boardCount(Pawn.WHITE)) / WHITE_PAWNS_COUNT;

        double kingDistance = 1.0 - (manhattanToNearestBorder(kr, kc) / 8.0); // già in [0,1], alto = re vicino al bordo
        double kingOpenLines = countKingOpenLines() / 4.0;
        double kingAdjBlack = blackPawnsNearKing() / 4.0;
        double kingAdjWhite = whitePawnsNearKing() / 4.0;
        double kingMobility = countFreeNeighbours(kr, kc) / 4.0;
        double blackEscapeBlock = countBlackOnEscapeRing() / 12.0;
        double whiteEscapeSupport = countWhiteOnEscapeRing() / 12.0;
        double blackMostImportantCellsBlocked = howManyPawnsOnBestCells()/bestCellsForBlack.length;
        boolean escapesNotProtected = blackMostImportantCellsBlocked != 1;

        double score = 0.0;

        //MODIFICA 
        //il bianco può vincere alla prossima mossa → posizione pessima
        if (whiteCanWinNext()) {
            score-=1;
        }

        // il nero può catturare il re alla prossima mossa → posizione ottima
        if (blackCanCaptureKingNext()) {
            score+=1; 
        }

        // Se il re può vincere subito, posizione pessima per il nero
        // Uso una penalizzazione piccola ma forte, compatibile con [0,1]
        if (hasImmediateKingWin()) {
            score -= 0.35;
        }

        //---dynamic heuristic
        basicHeuristic();
        //if(escapesNotProtected && boardCount(Pawn.BLACK)>=12)protectionHeuristic();
        //else if(boardCount(Pawn.WHITE)>5)terminatorHeuristic();
        //else chillHeuristic();

        // Parte continua dell'euristica:
        // premio ciò che favorisce il nero e penalizzo ciò che favorisce il bianco
        score += WEIGHT_ALIVE * blackAlive;
        score += WEIGHT_KILLED * whiteEaten;
        score -= WEIGHT_KING_ESCAPE_ROUTES * kingDistance;
        score -= WEIGHT_OPEN_LINES * kingOpenLines;
        score += WEIGHT_BLACK_NEAR_KING * kingAdjBlack;
        score -= WEIGHT_WHITE_NEAR_KING * kingAdjWhite;
        score -= WEIGHT_KING_MOBILITY * kingMobility;
        score += WEIGHT_BLACK_ESCAPE_BLOCK * blackEscapeBlock;
        score -= WEIGHT_WHITE_ESCAPE_SUPPORT * whiteEscapeSupport;
        score += WEIGHT_BLACK_ESCAPE_BLOCK_MOST_IMPORTANT * blackMostImportantCellsBlocked;

        int needed = kingRequiredCapturers();
        int blackNear = blackPawnsNearKing();

        // Bonus tattici di quasi-cattura del re, tutti piccoli e compatibili con [0,1]
        //le pedine nere sono un po' troppo suicide... bisognerebbe non dare questi bonus se gli altri lati del re sono bloccati
        if(countFreeNeighbours(kr, kc)+blackNear>=needed){
            if (needed == 2 && blackNear == 1) score += 0.02;
            if (needed == 2 && blackNear == 2) score += 0.04;

            if (needed == 3 && blackNear == 1) score += 0.02;
            if (needed == 3 && blackNear == 2) score += 0.04;
            if (needed == 3 && blackNear == 3) score += 0.06;

            if (needed == 4 && blackNear == 2) score += 0.02;
            if (needed == 4 && blackNear == 3) score += 0.04;
        }
        

        // Re sul trono: leggermente meglio per il nero, perché è meno vicino all'uscita
        if (isKingInCastle()) {
            score += 0.05;
        }

        return score;
    }

//basic weights
public void basicHeuristic(){ //goal: winning
    //to maximize
    WEIGHT_ALIVE = 0.20; // premia il fatto che il nero abbia ancora molte pedine in gioco
    WEIGHT_KILLED = 0.20; // premia il fatto che il nero abbia già eliminato pedine bianche, quindi il bianco ha meno difese attorno al re
    WEIGHT_BLACK_NEAR_KING = 0.18; // quanta pressione c’è intorno al re di neri
    WEIGHT_BLACK_ESCAPE_BLOCK = 0.06; // caselle che spesso servono per preparare il blocco dell’uscita
    WEIGHT_BLACK_ESCAPE_BLOCK_MOST_IMPORTANT = 0.20; // caselle che spesso servono per preparare il blocco dell’uscita

    //to minimize
    WEIGHT_KING_ESCAPE_ROUTES = 0.16; // peso della vicinanza del re a una casella di fuga sul bordo
    WEIGHT_OPEN_LINES = 0.20; // quante direzioni dal re verso il bordo sono libere da ostacoli
    WEIGHT_WHITE_NEAR_KING = 0.06; // quanta pressione/protezione c’è intorno al re di bianchi
    WEIGHT_KING_MOBILITY = 0.08; // caselle libere vicino al re
    WEIGHT_WHITE_ESCAPE_SUPPORT = 0.04; // caselle che spesso servono per preparare l’uscita
}

public void chillHeuristic(){ //goal: winning
    //to maximize
    WEIGHT_ALIVE = 2; // premia il fatto che il nero abbia ancora molte pedine in gioco
    WEIGHT_KILLED = 0.15; // premia il fatto che il nero abbia già eliminato pedine bianche, quindi il bianco ha meno difese attorno al re
    WEIGHT_BLACK_NEAR_KING = 0.10; // quanta pressione c’è intorno al re di neri
    WEIGHT_BLACK_ESCAPE_BLOCK = 0.01; // caselle che spesso servono per preparare il blocco dell’uscita
    WEIGHT_BLACK_ESCAPE_BLOCK_MOST_IMPORTANT = 0.24; // caselle che spesso servono per preparare il blocco dell’uscita

    //to minimize
    WEIGHT_KING_ESCAPE_ROUTES = 0.16; // peso della vicinanza del re a una casella di fuga sul bordo
    WEIGHT_OPEN_LINES = 0.20; // quante direzioni dal re verso il bordo sono libere da ostacoli
    WEIGHT_WHITE_NEAR_KING = 0.06; // quanta pressione/protezione c’è intorno al re di bianchi
    WEIGHT_KING_MOBILITY = 0.15; // caselle libere vicino al re
    WEIGHT_WHITE_ESCAPE_SUPPORT = 0.04; // caselle che spesso servono per preparare l’uscita
}
public void protectionHeuristic(){ //goal: blocking exits
    //to maximize
    WEIGHT_ALIVE = 2; // premia il fatto che il nero abbia ancora molte pedine in gioco
    WEIGHT_KILLED = 0.05; // premia il fatto che il nero abbia già eliminato pedine bianche, quindi il bianco ha meno difese attorno al re
    WEIGHT_BLACK_NEAR_KING = 0.05; // quanta pressione c’è intorno al re di neri
    WEIGHT_BLACK_ESCAPE_BLOCK = 0.01; // caselle che spesso servono per preparare il blocco dell’uscita
    WEIGHT_BLACK_ESCAPE_BLOCK_MOST_IMPORTANT = 0.39; // caselle che spesso servono per preparare il blocco dell’uscita

    //to minimize
    WEIGHT_KING_ESCAPE_ROUTES = 0.16; // peso della vicinanza del re a una casella di fuga sul bordo
    WEIGHT_OPEN_LINES = 0.05; // quante direzioni dal re verso il bordo sono libere da ostacoli
    WEIGHT_WHITE_NEAR_KING = 0.06; // quanta pressione/protezione c’è intorno al re di bianchi
    WEIGHT_KING_MOBILITY = 0.05; // caselle libere vicino al re
    WEIGHT_WHITE_ESCAPE_SUPPORT = 0.04; // caselle che spesso servono per preparare l’uscita
}
public void terminatorHeuristic(){ //terminator wants blood!
    //to maximize
    WEIGHT_ALIVE = 2; // premia il fatto che il nero abbia ancora molte pedine in gioco
    WEIGHT_KILLED = 0.30; // premia il fatto che il nero abbia già eliminato pedine bianche, quindi il bianco ha meno difese attorno al re
    WEIGHT_BLACK_NEAR_KING = 0.05; // quanta pressione c’è intorno al re di neri
    WEIGHT_BLACK_ESCAPE_BLOCK = 0.01; // caselle che spesso servono per preparare il blocco dell’uscita
    WEIGHT_BLACK_ESCAPE_BLOCK_MOST_IMPORTANT = 0.14; // caselle che spesso servono per preparare il blocco dell’uscita

    //to minimize
    WEIGHT_KING_ESCAPE_ROUTES = 0.16; // peso della vicinanza del re a una casella di fuga sul bordo
    WEIGHT_OPEN_LINES = 0.20; // quante direzioni dal re verso il bordo sono libere da ostacoli
    WEIGHT_WHITE_NEAR_KING = 0.06; // quanta pressione/protezione c’è intorno al re di bianchi
    WEIGHT_KING_MOBILITY = 0.15; // caselle libere vicino al re
    WEIGHT_WHITE_ESCAPE_SUPPORT = 0.04; // caselle che spesso servono per preparare l’uscita
}


//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  countKingOpenLines  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!//
/**
 * Conta quante direzioni dal re verso il bordo sono completamente libere.
 * 
 * Una "linea aperta" è una direzione in cui il re può teoricamente
 * scorrere fino al bordo senza trovare:
 * - altre pedine;
 * - campi/citadelle;
 * - il trono.
 * 
 * Il valore massimo è 4:
 * sopra, sotto, sinistra, destra.
 */
public int countKingOpenLines() {
    // prendo la posizione del re
    int[] king = getKingPosition();
    int kr = king[0];
    int kc = king[1];

    // inizializzo il contatore delle linee libere
    int openLines = 0;

    // controllo la direzione verso l'alto:
    // se tutto il percorso dal re al bordo è libero, incremento il contatore
    if (isLineOpen(kr, kc, -1, 0)) openLines++;

    // controllo la direzione verso il basso
    if (isLineOpen(kr, kc, 1, 0)) openLines++;

    // controllo la direzione verso sinistra
    if (isLineOpen(kr, kc, 0, -1)) openLines++;

    // controllo la direzione verso destra
    if (isLineOpen(kr, kc, 0, 1)) openLines++;

    // restituisco quante direzioni sono aperte
    return openLines;
}

private boolean isLineOpen(int row, int col, int dr, int dc) {
    // mi sposto nella prima casella nella direzione richiesta
    int r = row + dr;
    int c = col + dc;

    while (r >= 0 && r < 9 && c >= 0 && c < 9) { //bisogna controllare tutta la riga/colonna fino al bordo
       
        if (state.getPawn(r, c) != Pawn.EMPTY) { // // se trovo una pedina, la linea non è libera
            return false;
        }
       
        if (isCampCell(r, c)) { // se trovo un campo/citadella, la linea non è libera       
            return false;
        }


        if (r == 4 && c == 4) { // se trovo il trono, la linea non è libera
            return false;
        }

        // avanzo di una casella nella stessa direzione
        r += dr;
        c += dc;
    }

    // se sono arrivato al bordo senza ostacoli, la linea è aperta
    return true;
}

private boolean isCampCell(int y, int x) { // ci dice se è una cella di campo/citadella -> che è un ostacolo per il re
    return (y == 0 && (x == 3 || x == 4 || x == 5)) ||
           (y == 1 && x == 4) ||
           (y == 8 && (x == 3 || x == 4 || x == 5)) ||
           (y == 7 && x == 4) ||
           ((y == 3 || y == 4 || y == 5) && x == 0) ||
           (y == 4 && x == 1) ||
           ((y == 3 || y == 4 || y == 5) && x == 8) ||
           (y == 4 && x == 7);
}
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  countKingOpenLines  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!//


//se la cella è adiacente a un campo
public boolean isAdjacentToCamp(int y, int x) {
    return (isThereACampUnder(y,x) || isThereACampOnTop(y,x) || isThereACampOnTheLeft(y,x) || isThereACampOnTheRight(y,x)); 
} 

public int kingRequiredCapturers() {
        int[] king = getKingPosition(); 
        
        if(isKingInCastle()) {
            return 4; 
        } else if (isKingNearCastle()) {
            return 3; 
        } else if (isAdjacentToCamp( king[1], king[0])) {
            return 1; 
        } else return 2; 
    }





    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  countFreeNeighbours  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!//
    /**
     * Conta quante caselle adiacenti al re sono libere.
     * Considera solo le 4 direzioni ortogonali:
     * sopra, sotto, sinistra, destra.
     *
     * Una casella è "libera" se:
     * - è dentro la scacchiera;
     * - è EMPTY;
     * - non è un campo/citadella;
     * - non è il trono.
     *
     * Il massimo valore possibile è 4.
     */
    public int countFreeNeighbours(int kingRow, int kingCol) {
        int free = 0;

        if (isFreeCell(kingRow - 1, kingCol)) { // controllo la casella sopra il re
            free++;
        }

        if (isFreeCell(kingRow + 1, kingCol)) { // controllo la casella sotto il re
            free++;
        }

        if (isFreeCell(kingRow, kingCol - 1)) { // controllo la casella a sinistra del re
            free++;
        }

        if (isFreeCell(kingRow, kingCol + 1)) { // controllo la casella a destra del re
            free++;
        }

        return free;
    }

    private boolean isFreeCell(int r, int c) {
        if (r < 0 || r >= 9 || c < 0 || c >= 9) {  // controllo che le coordinate siano dentro la board
            return false;
        }
    
        if (state.getPawn(r, c) != Pawn.EMPTY) {  // se c'è una pedina, la casella non è libera
            return false;
        }

        if (r == 4 && c == 4) { // il trono non è una casella libera
            return false;
        }
    
        if (isCampCell(r, c)) { // i campi/citadelle non sono caselle libere
            return false;
        }

        return true;
    }
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  countFreeNeighbours  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!//




    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  countBlackOnEscapeRing  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!//
    /**
     * Conta quante pedine nere occupano le caselle "davanti" alle uscite.
     *
     * Uso direttamente l'array cellsInFrontOfEscapes definito nella classe Heuristics.
     * Queste 12 caselle sono strategiche perché aiutano il nero a controllare
     * le vie di fuga del re vicino al bordo.
     *
     * numero di pedine nere presenti in quelle 12 caselle
     * 
     * in poche parole:
     * conta quanti neri stanno nelle caselle strategiche vicine alle uscite del re,
     * cioè nelle zone da cui il bianco prova spesso ad aprire il passaggio verso il bordo
     */
    public int countBlackOnEscapeRing() { 
        // contatore delle pedine nere trovate nelle caselle strategiche
        int count = 0;

        // scorro tutte le caselle dell'anello già definite nella superclasse Heuristics
        for (int i = 0; i < cellsInFrontOfEscapes.length; i++) {
            int row = cellsInFrontOfEscapes[i][0];
            int col = cellsInFrontOfEscapes[i][1];

            // se nella casella corrente c'è una pedina nera, incremento il contatore
            if (state.getPawn(row, col) == Pawn.BLACK) {
                count++;
            }
        }

        // restituisco quante pedine nere controllano l'anello delle uscite
        return count;
    }
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  countBlackOnEscapeRing  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!//

public int howManyPawnsOnBestCells() { 
        // contatore delle pedine nere trovate nelle caselle strategiche
        int count = 0;

        // scorro tutte le caselle dell'anello già definite nella superclasse Heuristics
        for (int i = 0; i < bestCellsForBlack.length; i++) {
            int row = bestCellsForBlack[i][0];
            int col = bestCellsForBlack[i][1];

            // se nella casella corrente c'è una pedina nera, incremento il contatore
            if (state.getPawn(row, col) == Pawn.BLACK) {
                count++;
            }
        }

        // restituisco quante pedine nere controllano l'anello delle uscite
        return count;
    }

//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  countWhiteOnEscapeRing  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!//
/**
 * Conta quante pedine bianche occupano le caselle "davanti" alle uscite.
 *
 * Uso direttamente l'array cellsInFrontOfEscapes definito nella classe Heuristics.
 * Queste 12 caselle sono strategiche perché aiutano il bianco a preparare
 * la fuga del re verso il bordo.
 *
 * numero di pedine bianche presenti in quelle 12 caselle
 */
public int countWhiteOnEscapeRing() {
    // contatore delle pedine bianche trovate nelle caselle strategiche
    int count = 0;

    // scorro tutte le caselle dell'anello già definite nella superclasse Heuristics
    for (int i = 0; i < cellsInFrontOfEscapes.length; i++) {
        int row = cellsInFrontOfEscapes[i][0];
        int col = cellsInFrontOfEscapes[i][1];

        // se nella casella corrente c'è una pedina bianca, incremento il contatore
        if (state.getPawn(row, col) == Pawn.WHITE) {
            count++;
        }
    }

    // restituisco quante pedine bianche controllano l'anello delle uscite
    return count;
}
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  countWhiteOnEscapeRing  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!//



    public boolean hasImmediateKingWin() {
        return countKingOpenLines() >= 1;
    } 



//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  blackCanCaptureKingNext  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!//
    // verifica se il nero può catturare il re alla prossima mossa
    public boolean blackCanCaptureKingNext() {

        int[] king = getKingPosition();
        int kr = king[0];
        int kc = king[1];

        int[][] dirs = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}
        };

        // prova tutte le mosse nere
        for (int r1 = 0; r1 < 9; r1++) {
            for (int c1 = 0; c1 < 9; c1++) {

                if (state.getPawn(r1, c1) != Pawn.BLACK) continue;

                for (int[] d : dirs) {

                    int r2 = r1 + d[0];
                    int c2 = c1 + d[1];

                    if (!isFreeCell(r2, c2)) continue;
                    if (!isPathFree(r1, c1, r2, c2)) continue;

                    // verifica cattura reale dopo la mossa
                    if (isKingCapturedAfterBlackMove(r1, c1, r2, c2)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isKingCapturedAfterBlackMove(int fromR, int fromC, int toR, int toC) {

        int[] king = getKingPosition();
        int kr = king[0];
        int kc = king[1];

        int needed = kingRequiredCapturers();

        int blockedSides = 0;

        int[][] dirs = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}
        };

        for (int[] d : dirs) {

            int r = kr + d[0];
            int c = kc + d[1];

            // se la mossa del nero occupa questo lato
            if (r == toR && c == toC) {
                blockedSides++;
                continue;
            }

            // fuori board = blocco (bordo)
            if (r < 0 || r >= 9 || c < 0 || c >= 9) {
                blockedSides++;
                continue;
            }

            Pawn p = state.getPawn(r, c);

            // blocco valido secondo regole Tablut
            if (p == Pawn.BLACK || isCampCell(r, c) || (r == 4 && c == 4)) {
                blockedSides++;
            }
        }

        return blockedSides >= needed;
    }
    
    //Controlla se il percorso tra due caselle sulla stessa riga o colonna
    //è libero da pedine, campi e trono
    private boolean isPathFree(int r1, int c1, int r2, int c2) {
        int dr = Integer.compare(r2, r1);
        int dc = Integer.compare(c2, c1);

        int r = r1 + dr;
        int c = c1 + dc;

        while (r != r2 || c != c2) {

            if (state.getPawn(r, c) != Pawn.EMPTY) return false;
            if (isCampCell(r, c)) return false;
            if (r == 4 && c == 4) return false;

            r += dr;
            c += dc;
        }
        return true;
    }

/*
    // Verifica se esiste almeno una pedina nera che può arrivare
    // sulla casella obiettivo in linea retta.
    private boolean existsBlackThatCanReach(int targetR, int targetC) {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {

                if (state.getPawn(r, c) == Pawn.BLACK) {

                    // stessa riga
                    if (r == targetR && isPathFree(r, c, targetR, targetC)) {
                        return true;
                    }

                    // stessa colonna
                    if (c == targetC && isPathFree(r, c, targetR, targetC)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
    
    // Verifica se, occupando la casella (br, bc), il numero di neri
    // adiacenti al re diventa sufficiente per completare la cattura.
    private boolean wouldKingBeCapturedAfterMove(int kr, int kc, int br, int bc) {

        // simula: aggiungo un nero nella posizione (br, bc)
        int needed = kingRequiredCapturers();

        int count = blackPawnsNearKing() + 1;

        return count >= needed;
    }
*/
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  blackCanCaptureKingNext  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!//


//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  whiteCanWinNext  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!//
    public boolean whiteCanWinNext() {
        int[] king = getKingPosition();
        int kr = king[0];
        int kc = king[1];

        // per ogni direzione
        if (isWinningDirection(kr, kc, -1, 0)) return true;
        if (isWinningDirection(kr, kc, 1, 0)) return true;
        if (isWinningDirection(kr, kc, 0, -1)) return true;
        if (isWinningDirection(kr, kc, 0, 1)) return true;

        return false;
    }

    private boolean isWinningDirection(int r, int c, int dr, int dc) {
        r += dr;
        c += dc;

        while (r >= 0 && r < 9 && c >= 0 && c < 9) {

            if (state.getPawn(r, c) != Pawn.EMPTY) return false;
            if (isCampCell(r, c)) return false;
            if (r == 4 && c == 4) return false;

            // se arrivo al bordo -> vittoria
            if (r == 0 || r == 8 || c == 0 || c == 8) {
                return true;
            }

            r += dr;
            c += dc;
        }
        return false;
    }
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  whiteCanWinNext  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!//
 


}
