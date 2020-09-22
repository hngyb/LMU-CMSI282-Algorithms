package t3;

import java.util.*;

/**
 * Artificial Intelligence responsible for playing the game of T3! Implements
 * the alpha-beta-pruning mini-max search algorithm
 */
public class T3Player {

	/**
	 * Workhorse of an AI T3Player's choice mechanics that, given a game state,
	 * makes the optimal choice from that state as defined by the mechanics of the
	 * game of Tic-Tac-Total. Note: In the event that multiple moves have
	 * equivalently maximal minimax scores, ties are broken by move col, then row,
	 * then move number in ascending order (see spec and unit tests for more info).
	 * The agent will also always take an immediately winning move over a delayed
	 * one (e.g., 2 moves in the future).
	 * 
	 * @param state The state from which the T3Player is making a move decision.
	 * @return The T3Player's optimal action.
	 */
	public T3Action choose(T3State state) {
		int[] bestChoose = alphabeta(state, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
		T3Action bestAction = new T3Action(bestChoose[1], bestChoose[2], bestChoose[3]);
		return bestAction;

	}

	public static int utilityScore(T3State state, boolean maximizingPlayer) {
		if (state.isWin() && maximizingPlayer) {
			return -1;
		} else if (state.isWin() && !maximizingPlayer) {
			return 1;
		} else {
			return 0;
		}
	}

	public int[] alphabeta(T3State state, int alpha, int beta, boolean maximizingPlayer) {
		Map<T3Action, T3State> children = state.getTransitions(state);

		int bestRow = -1;
		int bestCol = -1;
		int bestMove = -1;

		if (state.isTie() || state.isWin()) {
			return new int[] { utilityScore(state, maximizingPlayer), bestCol, bestRow, bestMove };
		}

		if (maximizingPlayer) {
			int vertex = Integer.MIN_VALUE;
			for (T3Action i : children.keySet()) {

				if (children.get(i).isWin()) {
					return new int[] { utilityScore(children.get(i), maximizingPlayer), i.col, i.row, i.move };
				}

				T3State child = children.get(i);
				int v = alphabeta(child, alpha, beta, !maximizingPlayer)[0];
				if (vertex < v) {
					vertex = v;
					bestCol = i.col;
					bestRow = i.row;
					bestMove = i.move;
				}

				alpha = Math.max(alpha, vertex);
				if (beta <= alpha) {
					break;
				}
			}

			return new int[] { vertex, bestCol, bestRow, bestMove };
		}

		else {
			int vertex = Integer.MAX_VALUE;
			for (T3Action i : children.keySet()) {

				if (children.get(i).isWin()) {
					return new int[] { utilityScore(children.get(i), !maximizingPlayer), i.col, i.row, i.move };
				}
				T3State child = children.get(i);
				int v = alphabeta(child, alpha, beta, maximizingPlayer)[0];
				if (vertex > v) {
					vertex = v;
					bestCol = i.col;
					bestRow = i.row;
					bestMove = i.move;
				}
				beta = Math.min(beta, vertex);
				if (beta <= alpha) {
					break;
				}

			}
			return new int[] { vertex, bestCol, bestRow, bestMove };
		}

	}
}
