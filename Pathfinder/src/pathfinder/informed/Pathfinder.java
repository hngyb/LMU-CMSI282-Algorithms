//name: Hongyeob Kim
package pathfinder.informed;

import java.util.*;

/**
 * Maze Pathfinding algorithm that implements a basic, uninformed, breadth-first
 * tree search.
 */
public class Pathfinder {

	/**
	 * Given a MazeProblem, which specifies the actions and transitions available in
	 * the search, returns a solution to the problem as a sequence of actions that
	 * leads from the initial to a goal state.
	 * 
	 * @param problem A MazeProblem that specifies the maze, actions, transitions.
	 * @return An ArrayList of Strings representing actions that lead from the
	 *         initial to the goal state, of the format: ["R", "R", "L", ...]
	 */
	public static ArrayList<String> solve(MazeProblem problem) {

		MazeState target = problem.KEY_STATE;
		PriorityQueue<SearchTreeNode> frontier = new PriorityQueue<>();
		HashSet<MazeState> graveyard = new HashSet<MazeState>();

		frontier.add(new SearchTreeNode(problem.INITIAL_STATE, null, null, 0, 0));
		SearchTreeNode key = findPath(problem, target, frontier, graveyard);

		if (key != null) {
			PriorityQueue<MazeState> Goals = new PriorityQueue<MazeState>(
					(state1, state2) -> getDistance(state1, key.state) - getDistance(state2, key.state));

			for (MazeState Goal : problem.GOAL_STATES) {
				Goals.add(Goal);
			}

			while (!Goals.isEmpty()) {
				frontier.clear();
				graveyard.clear();
				target = Goals.remove();
				frontier.add(key);
				SearchTreeNode goal = findPath(problem, target, frontier, graveyard);
				if (goal != null) {
					while (goal != null) {
						return pathBuild(goal);
					}
				}
			}

		}
		return null;
	}

	public static int getDistance(MazeState s1, MazeState s2) {
		return Math.abs(s1.col - s2.col) + Math.abs(s1.row - s2.row);
	}

	private static SearchTreeNode findPath(MazeProblem problem, MazeState target,
			PriorityQueue<SearchTreeNode> frontier, HashSet<MazeState> graveyard) {
		while (!frontier.isEmpty()) {
			SearchTreeNode expanding = frontier.poll();

			if (expanding.state.equals(target)) {
				return expanding;
			}

			graveyard.add(expanding.state);
			Map<String, MazeState> transitions = problem.getTransitions(expanding.state);
			for (Map.Entry<String, MazeState> transition : transitions.entrySet()) {
				frontier.add(new SearchTreeNode(transition.getValue(), transition.getKey(), expanding,
						expanding.pastCost + problem.getCost(transition.getValue()),
						getDistance(target, transition.getValue())));
			}
		}
		return null;
	}

	private static ArrayList<String> pathBuild(SearchTreeNode goal) {
		ArrayList<String> path = new ArrayList<String>();
		SearchTreeNode current = goal;
		while (current.action != null) {
			path.add(0, current.action);
			current = current.parent;
		}
		return path;
	}

}

/**
 * SearchTreeNode that is used in the Search algorithm to construct the Search
 * tree. [!] NOTE: Feel free to change this however you see fit to adapt your
 * solution for A* (including any fields, changes to constructor, additional
 * methods)
 */
class SearchTreeNode implements Comparable<SearchTreeNode> {

	MazeState state;
	String action;
	SearchTreeNode parent;
	int pastCost;
	int futureCost;

	/**
	 * Constructs a new SearchTreeNode to be used in the Search Tree.
	 * 
	 * @param state      The MazeState (row, col) that this node represents.
	 * @param action     The action that *led to* this state / node.
	 * @param parent     Reference to parent SearchTreeNode in the Search Tree.
	 * @param pastCost   The past cost of exploration to this node
	 * @param futureCost The cost to get to the target from this node
	 */
	SearchTreeNode(MazeState state, String action, SearchTreeNode parent, int pastCost, int futureCost) {
		this.state = state;
		this.action = action;
		this.parent = parent;
		this.pastCost = pastCost;
		this.futureCost = futureCost;
	}

	public int totalCost() {
		return this.pastCost + this.futureCost;
	}

	@Override
	public int compareTo(SearchTreeNode o) {
		return this.totalCost() - o.totalCost();
	}
}
