package it.unibo.ai.didattica.competition.tablut.ai_matrix.heuristics;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;

/**
 * Classe astratta base per la valutazione euristica degli stati di gioco nel Tablut.
 *
 * Fornisce metodi per analizzare lo stato della scacchiera,
 **/

public abstract class Heuristics {
    protected State state;
	public static double BLACK_PAWNS_COUNT = 16;
    public static double WHITE_PAWNS_COUNT = 8;
	int[][] escapes = {{0,1},{0,2},{0,6},{0,7},{1,0},{2,0},{1,8},{2,8},{0,6},{0,7},{8,6},{8,7},{0,1},{8,1},{8,2},{8,6},{8,7},};
	int[][] cellsInFrontOfEscapes = {{1,1},{1,2},{1,6},{1,7},{2,1},{2,7},{6,1},{6,7},{7,1},{7,2},{7,6},{7,7}};

    /**
     * Costruisce un oggetto Heuristics associato a uno stato di gioco.
     *
     * @param s lo stato della partita da analizzare
     */
	public Heuristics(State s) {
        this.state = s;
    }

    /**
     * Valuta lo stato corrente del gioco.
     * 
     *  @return restituisce un valore numerico che rappresenta la qualità dello stato 
     * **/
    public double evaluateState() {
        return 0;
    }

    /**
     * Conta il numero di pedine di un certo tipo presenti sulla scacchiera.
     *
     * @param pawn il tipo di pedina da contare (WHITE, BLACK)
     * @return il numero di pedine trovate
     */
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

    /**
     * Restituisce la posizione del re sulla scacchiera.
     *
     * @return un array di due interi [riga, colonna] che rappresenta
     *         la posizione del re
     */
	public int[] getKingPosition() {
        int[] king= new int[2];
        State.Pawn[][] board = state.getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (state.getPawn(i, j).equalsPawn("K")) {
                    king[0] = i;
                    king[1] = j;
                }
            }
        }
        return king;
    }

    /**
     * Calcola il numero di pedine nere adiacenti al re, controllando
     * le quattro direzioni principali (alto, basso, sinistra, destra).
     *
     * @return il numero di pedine nere vicine al re
     */
	public int blackPawnsNearKing() {
		int blackPawnsNearKing = 0;
        int[] king= getKingPosition();
        State.Pawn[][] board = state.getBoard();
		//sopra al re
        if(board[king[0]+1][king[1]]==Pawn.BLACK)blackPawnsNearKing++;
		//sotto al re
		if(board[king[0]-1][king[1]]==Pawn.BLACK)blackPawnsNearKing++;
		//sinistra del re
        if(board[king[0]][king[1]-1]==Pawn.BLACK)blackPawnsNearKing++;
		//destra del re
		if(board[king[0]][king[1]+1]==Pawn.BLACK)blackPawnsNearKing++;
        return blackPawnsNearKing;
    }

	
    /**
     * Verifica se il re si trova nella casella centrale (castello).
     *
     * @return true se il re è nel castello, false altrimenti
     */
	public boolean isKingInCastle(){
        int[] king = getKingPosition();
        return king[0]==4 && king[1]==4;
    }

    /**
     * Verifica se il re si trova in una casella vicina (adiacente) alla casella centrale (castello).
     *
     * @return true se il re è vicino al castello, false altrimenti
     */
	public boolean isKingNearCastle(){
        int[] king = getKingPosition();
        boolean leftRight = king[0]==4 && (king[1]==3 || king[1]==5);
		boolean topBottom = king[1]==4 && (king[0]==3 || king[0]==5);
		return leftRight||topBottom;
    }
	
    /**
     * Verifica se il giocatore bianco ha vinto.
     * (il bianco vince quando il re raggiunge il bordo della scacchiera)
     *
     * @return true se il bianco ha vinto, false altrimenti
     */
	public boolean whiteWin(){
        int[] king = getKingPosition();
        return king[0] == 0 || king[0] == 8 || king[1] == 0 || king[1] == 8;
    }

    /**
     * Verifica se il giocatore nero ha vinto.
     * (il nero vince quando il re viene catturato, cioè circondato
     * da un numero sufficiente di pedine nere)
     *
     * @return true se il nero ha vinto, false altrimenti
     */
	public boolean blackWin(){
        if(isKingInCastle() && blackPawnsNearKing()==4)return true;
        if(isKingInCastle() && blackPawnsNearKing()==3)return true;
		return blackPawnsNearKing()==2;
    }
}
