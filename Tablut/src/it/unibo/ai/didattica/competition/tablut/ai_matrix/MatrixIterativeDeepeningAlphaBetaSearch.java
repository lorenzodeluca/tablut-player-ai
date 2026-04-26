package it.unibo.ai.didattica.competition.tablut.ai_matrix;

import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import aima.core.search.adversarial.Game;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

/**
 * Implementazione personalizzata dell'algoritmo Iterative Deepening Alpha-Beta
 * per il gioco del Tablut.
 *
 * Questa classe estende l'algoritmo generico fornito dalla libreria AIMA
 * e lo adatta al dominio specifico del gioco, delegando la valutazione
 * degli stati alla funzione di utilità definita nel gioco.
 *
 * L'algoritmo esplora l'albero delle mosse aumentando progressivamente
 * la profondità di ricerca fino a esaurimento del tempo disponibile,
 * utilizzando il pruning Alpha-Beta per ottimizzare l'esplorazione.
 */

public class MatrixIterativeDeepeningAlphaBetaSearch extends IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn>{

    /**
     * Costruisce un'istanza dell'algoritmo Iterative Deepening Alpha-Beta.
     *
     * @param game il gioco su cui eseguire la ricerca 
     * @param min il valore minimo possibile dell'utilità
     * @param max il valore massimo possibile dell'utilità
     * @param time il tempo massimo (in millisecondi) disponibile per la ricerca
    */
    public MatrixIterativeDeepeningAlphaBetaSearch(Game<State, Action, State.Turn> game, double min, double max, int time) {
        super(game, min, max, time);
    }

    /**
     * Valuta uno stato del gioco dal punto di vista di un determinato giocatore.
     *
     * Questo metodo viene chiamato dall'algoritmo Alpha-Beta quando raggiunge
     * un nodo foglia (limite di profondità o tempo) e delega la valutazione
     * alla funzione di utilità del gioco.
     *
     * @param state lo stato della partita da valutare
     * @param player il giocatore per cui calcolare l'utilità
     * @return un valore numerico che rappresenta la qualità dello stato
     *         per il giocatore specificato
     */
    @Override
    protected double eval(State state, State.Turn player) {
        super.eval(state, player);
        return game.getUtility(state, player);
    }

}
