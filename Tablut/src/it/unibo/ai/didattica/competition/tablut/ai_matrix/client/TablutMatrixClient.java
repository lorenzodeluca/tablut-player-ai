/* GenAI statement
 * For writing the code included in this project GenAI tools were used mainly with the goal of supporting the design and 
 * optimization of the evaluation heuristics for the Matrix Tablut AI.
 * These tools were put to use to optimize board scanning and move selection algorithms.  
 * We take responsibility for all the modifications and optimizations we made due to the GenAI tools advices.
 * 
 * Tools: GitHub Copilot, Gemini, Perplexity
 * Queries/use period: 14/3/2026 - 12/5/2026
 * Reason: Coding support, shortening development time, help with spotting coding mistakes.
 *  
 */

package it.unibo.ai.didattica.competition.tablut.ai_matrix.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import it.unibo.ai.didattica.competition.tablut.ai_matrix.MatrixConfigs;
import it.unibo.ai.didattica.competition.tablut.ai_matrix.MatrixIterativeDeepeningAlphaBetaSearch;
import it.unibo.ai.didattica.competition.tablut.client.TablutClient;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.domain.StateBrandub;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;

public class TablutMatrixClient extends TablutClient{
    private static String role = "";
    private static String name = "Matrix";
    private static String ipAddress = "localhost";
    private static int timeout = 60;

	public TablutMatrixClient(String player, String name, int timeout, String ipAddress) throws UnknownHostException, IOException {
		super(player, name, timeout, ipAddress);
	}
	
	public TablutMatrixClient(String player, int timeout, String ipAddress) throws UnknownHostException, IOException {
		this(player, "Matrix", timeout, ipAddress);
	}

	public TablutMatrixClient(String player) throws UnknownHostException, IOException {
		this(player, "Matrix", timeout, "localhost");
	}


	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
		if (args.length < 1) {
			System.out.println("You must specify which player you are (WHITE or BLACK)");
			System.exit(-1);
		} else {
			System.out.println("ROLE: "+ args[0]); 
			role = args[0].toUpperCase();
		}
		if (args.length == 2) {
			System.out.println(args[1]);
			timeout = Integer.parseInt(args[1]);
		}
		if (args.length == 3) {
			ipAddress = args[2];
		}

		TablutMatrixClient client = new TablutMatrixClient(role, name, timeout, ipAddress);
		client.run();
	}

	@Override
	public void run() {
		try {
			this.declareName();
		} catch (Exception e) {
			e.printStackTrace();
		}

		State state;

		GameAshtonTablut game = null;
        state = new StateTablut();
        state.setTurn(State.Turn.WHITE);
        game = new GameAshtonTablut(state, 0, -1, "logs", "white_matrix", "black_matrix");
        System.out.println("Ashton Tablut game - matrix client");

		List<int[]> pawns = new ArrayList<int[]>();
		List<int[]> empty = new ArrayList<int[]>();

		System.out.println("You are player " + this.getPlayer().toString() + "!");

		while (true) {
			try {
				this.read(); //scarica lo stato dal server
			} catch (ClassNotFoundException | IOException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
			System.out.println("Current state:");
			state = this.getCurrentState();
			System.out.println(state.toString());

			if (this.getPlayer().equals(Turn.WHITE)) { //se il giocatore è bianco  
				if (this.getCurrentState().getTurn().equals(StateTablut.Turn.WHITE)) { //if its white turn
                    if(MatrixConfigs.DEBUG_MODE)System.out.println("my turn(white)...");         
					MatrixIterativeDeepeningAlphaBetaSearch search = new MatrixIterativeDeepeningAlphaBetaSearch(game, Double.MIN_VALUE, Double.MAX_VALUE, timeout );
					search.setLogEnabled(true);
					Action move = search.makeDecision(state); //trovo qual è mossa migliore
					try {
						this.write(move);
					} catch (ClassNotFoundException | IOException e) {
						e.printStackTrace();
					}
                    if(MatrixConfigs.DEBUG_MODE)System.out.println("move(white): "+move.toString());
				}
				// Turno dell'avversario
				else if (state.getTurn().equals(StateTablut.Turn.BLACK)) {
					System.out.println("Waiting for your opponent move... ");
				}
				// ho vinto
				else if (state.getTurn().equals(StateTablut.Turn.WHITEWIN)) {
					System.out.println("YOU WIN!");
					System.exit(0);
				}
				// ho perso
				else if (state.getTurn().equals(StateTablut.Turn.BLACKWIN)) {
					System.out.println("YOU LOSE!");
					System.exit(0);
				}
				// pareggio
				else if (state.getTurn().equals(StateTablut.Turn.DRAW)) {
					System.out.println("DRAW!");
					System.exit(0);
				}

			} else { //altrimenti se il giocatore è nero 
				if (this.getCurrentState().getTurn().equals(StateTablut.Turn.BLACK)) { //se è il turno del nero 
                    if(MatrixConfigs.DEBUG_MODE)System.out.println("my turn(black)...");
                    MatrixIterativeDeepeningAlphaBetaSearch search = new MatrixIterativeDeepeningAlphaBetaSearch(game, Double.MIN_VALUE, Double.MAX_VALUE, timeout );
					Action move = search.makeDecision(state); //trovo qual è mossa migliore
                    if(MatrixConfigs.DEBUG_MODE)System.out.println("move(black): "+move.toString());
					try {
						this.write(move);
					} catch (ClassNotFoundException | IOException e) {
						e.printStackTrace();
					}
				}

				else if (state.getTurn().equals(StateTablut.Turn.WHITE)) {
					System.out.println("Waiting for your opponent move... ");
				} else if (state.getTurn().equals(StateTablut.Turn.WHITEWIN)) {
					System.out.println("YOU LOSE!");
					System.exit(0);
				} else if (state.getTurn().equals(StateTablut.Turn.BLACKWIN)) {
					System.out.println("YOU WIN!");
					System.exit(0);
				} else if (state.getTurn().equals(StateTablut.Turn.DRAW)) {
					System.out.println("DRAW!");
					System.exit(0);
				}

			}
		}

	}
}
