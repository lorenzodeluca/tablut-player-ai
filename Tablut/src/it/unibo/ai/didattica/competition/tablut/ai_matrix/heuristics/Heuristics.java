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

    public int whitePawnsNearKing() {
        int whitePawnsNearKing = 0;
        int[] king= getKingPosition();
        State.Pawn[][] board = state.getBoard();
		//sopra al re
        if(board[king[0]+1][king[1]]==Pawn.WHITE)whitePawnsNearKing++;
		//sotto al re
		if(board[king[0]-1][king[1]]==Pawn.WHITE)whitePawnsNearKing++;
		//sinistra del re
        if(board[king[0]][king[1]-1]==Pawn.WHITE)whitePawnsNearKing++;
		//destra del re
		if(board[king[0]][king[1]+1]==Pawn.WHITE)whitePawnsNearKing++;
        return whitePawnsNearKing;
    }

    public boolean isThereACampUnder(int y, int x){
        //bottom camps
        if(y==1&&(x==3||x==5))return true;
        if(y==2&&x==4)return true;
        //left camps
        if(y==5&&x==1)return true;
        //right camps
        if(y==5&&x==7)return true;
        return false;
    }

    public boolean isThereACampOnTop(int y, int x){
        //top camps
        if(y==7&&(x==3||x==5))return true;
        if(y==6&&x==4)return true;
        //left camps
        if(y==3&&x==1)return true;
        //right camps
        if(y==3&&x==7)return true;
        return false;
    }

    public boolean isThereACampOnTheLeft(int y, int x){
        //left camps
        if(x==1&&(y==3||y==5))return true;
        if(x==2&&y==4)return true;
        //top camps
        if(y==7&&x==5)return true;
        //bottom camps
        if(y==1&&x==5)return true;
        return false;
    }

    public boolean isThereACampOnTheRight(int y, int x){
        //right camps
        if(x==7&&(y==3||y==5))return true;
        if(x==6&&y==4)return true;
        //top camps
        if(y==7&&x==3)return true;
        //bottom camps
        if(y==1&&x==3)return true;
        return false;
    }

    public boolean blackPawnsCampCapture() {
        int[] king= getKingPosition();
        State.Pawn[][] board = state.getBoard();
		//nero sopra al re e campo sotto
        if(board[king[0]+1][king[1]]==Pawn.BLACK && isThereACampUnder(king[0], king[1]))return true;
		//nero sotto al re e campo sopra
		if(board[king[0]-1][king[1]]==Pawn.BLACK && isThereACampOnTop(king[0], king[1]))return true;
		//nero a sinistra del re e campo a destra
        if(board[king[0]][king[1]-1]==Pawn.BLACK && isThereACampOnTheRight(king[0], king[1]))return true;
		//nero a destra del re
		if(board[king[0]][king[1]+1]==Pawn.BLACK && isThereACampOnTheLeft(king[0], king[1]))return true;
        return false;
    }

    public boolean isKingCloseToCamp(){
        int[] king= getKingPosition();
        State.Pawn[][] board = state.getBoard();
        return isThereACampUnder(king[0], king[1])||isThereACampOnTop(king[0], king[1])||isThereACampOnTheRight(king[0], king[1])||isThereACampOnTheLeft(king[0], king[1]);
    }

    public boolean twoPawnsKingCapture(){
        int[] king= getKingPosition();
        State.Pawn[][] board = state.getBoard();
        if(board[king[0]][king[1]-1]==Pawn.BLACK && board[king[0]][king[1]+1]==Pawn.BLACK)return true;
        if(board[king[0]-1][king[1]]==Pawn.BLACK && board[king[0]+1][king[1]]==Pawn.BLACK)return true;
        return false;
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
		return isNearCastle(king[0],king[1]);
    }

    public boolean isNearCastle(int y, int x){
        boolean leftRight = y==4 && (x==3 || x==5);
		boolean topBottom = x==4 && (y==3 || y==5);
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
        if(isKingNearCastle() && blackPawnsNearKing()==3)return true;
        if(blackPawnsCampCapture())return true;
        if(twoPawnsKingCapture())return true;
		return false;
    }

    public double pawnsToEatKing(){
        if(isKingInCastle())return 4;
        if(isKingNearCastle())return 3;
        if(isKingCloseToCamp())return 1;
		return 2;
    }

    //i want to count how many white pawns are in a position where they could get eaten by just 1 black pawns
    public double whitePawnsInUnsafePositions(){
        State.Pawn[][] board = state.getBoard();
        int counter = 0;
		for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j]==Pawn.WHITE && (isThereACampUnder(i, j)||isThereACampOnTop(i, j)||isThereACampOnTheRight(i, j)||isThereACampOnTheLeft(i, j)||isNearCastle(i,j))) {
                    counter++;
                }
            }
        }
        return counter;
    }

    // trova il bordo della scacchiera più vicino alla posizione attuale del re
    public double manhattanToNearestBorder(int kingRow, int kingColumn) {
        int distTopToBottom = Math.min(kingRow, 8 - kingRow);
        int distLeftToRight = Math.min(kingColumn, 8 - kingColumn);
        return Math.min(distTopToBottom, distLeftToRight);
    }
}
