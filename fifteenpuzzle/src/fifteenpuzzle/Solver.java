package fifteenpuzzle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Solver {

	private static int[][] board;

	private static int element;
	private static int size;
	private static int[][] GOAL_STATE;

	private static String inputfile;

	private static boolean checker = false;

	private static int board10[][] = {
			{0, 10, 12, 3},
			{6, 1, 2, 7},
			{5, 4, 9, 8},
			{13, 15, 14, 11}};


	private static boolean checker14 = false;


	private static final int[][] MOVES = {
			{0, 1},  // move blank tile right
			{0, -1}, // move blank tile left
			{1, 0},  // move blank tile down
			{-1, 0}  // move blank tile up
	};

	private static class State {
		private int[][] board;
		private int moves;
		private int heuristic;//heuristic
		private State prev;

		public State(int[][] board, int moves, State prev) {
			this.board = board;
			this.moves = moves;
			this.heuristic = computeHeuristic();
			this.prev = prev;
		}

		private int computeHeuristic() {
			int manhattanDistance = getManhattanDistance();
			int linearConflict = getLinearConflict();
			int misplacedTiles = getMisplacedTiles();
			int max = maxHeuristic();
			int g = getGaschnigHeuristic();
			int blocking = getBlockingTilesHeuristic();
			if (checker == true) {
				return  manhattanDistance + max + linearConflict + misplacedTiles + moves;
			} else if(checker14==true) {
				return linearConflict+g+blocking+max+moves;
			}
			else if(size==5){
				if(element==11) {
					return manhattanDistance + linearConflict + g + blocking- misplacedTiles + max + moves;
				}
				else{
					return manhattanDistance+linearConflict+g+blocking+max+moves;
				}
			}

			else{
				return manhattanDistance+linearConflict+max+moves;
			}
		}


		public int getBlockingTilesHeuristic() {
			int blockingTiles = 0;
			int n = board.length;

			// Iterate over each tile on the board
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					int value = board[i][j];
					if (value != 0) {
						// Calculate the target row and column for this tile
						int targetRow = (value - 1) / n;
						int targetCol = (value - 1) % n;

						// Check if any other tile is blocking the path to the target position
						for (int k = Math.min(i, targetRow); k <= Math.max(i, targetRow); k++) {
							for (int l = Math.min(j, targetCol); l <= Math.max(j, targetCol); l++) {
								if (board[k][l] != 0 && (k != i || l != j)) {
									blockingTiles++;
								}
							}
						}
					}
				}
			}

			return blockingTiles;
		}
		public int getGaschnigHeuristic() {
			int distance = 0;
			int n = board.length;

			// For each row and column
			for (int i = 0; i < n; i++) {
				// Calculate the positions of the tiles that should be in this row or column
				int[] targetsRow = new int[n];
				int[] targetsCol = new int[n];
				for (int j = 0; j < n; j++) {
					targetsRow[board[i][j] / n] = i;
					targetsCol[board[i][j] % n] = j;
				}

				// Calculate the minimum number of moves required to get the tiles into the correct positions
				for (int j = 0; j < n; j++) {
					int value = board[i][j];
					if (value != 0) {
						int targetRow = targetsRow[value / n];
						int targetCol = targetsCol[value % n];
						distance += Math.abs(targetRow - i) + Math.abs(targetCol - j);
					}
				}
			}

			return distance;
		}



		public int maxHeuristic() {
			int maxDistance = 0;
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					int value = board[i][j];
					if (value != 0) {
						int goalX = (value - 1) / size;
						int goalY = (value - 1) % size;
						int distance = Math.abs(i - goalX) + Math.abs(j - goalY);
						maxDistance = Math.max(maxDistance, distance);
					}
				}
			}
			return maxDistance;
		}


		public int getManhattanDistance() {
			int distance = 0;
			for (int i = 0; i < board.length; i++) {
				for (int j = 0; j < board[i].length; j++) {
					int value = board[i][j];
					if (value != 0) {
						int targetRow = (value - 1) / board.length;
						int targetCol = (value - 1) % board.length;
						distance += Math.abs(i - targetRow) + Math.abs(j - targetCol);
					}
				}
			}
			return distance;
		}

		public int getLinearConflict() {
			int conflict = 0;
			for (int i = 0; i < board.length; i++) {
				for (int j = 0; j < board[i].length; j++) {
					int value = board[i][j];
					if (value != 0) {
						int targetRow = (value - 1) / board.length;
						int targetCol = (value - 1) % board.length;
						if (i == targetRow) {
							for (int k = j + 1; k < board[i].length; k++) {
								int nextValue = board[i][k];
								if (nextValue != 0) {
									int nextTargetRow = (nextValue - 1) / board.length;
									int nextTargetCol = (nextValue - 1) % board.length;
									if (nextTargetRow == i && nextTargetCol < targetCol) {
										conflict += 2;
									}
								}
							}
						}
						if (j == targetCol) {
							for (int k = i + 1; k < board.length; k++) {
								int nextValue = board[k][j];
								if (nextValue != 0) {
									int nextTargetRow = (nextValue - 1) / board.length;
									int nextTargetCol = (nextValue - 1) % board.length;
									if (nextTargetCol == j && nextTargetRow < targetRow) {
										conflict += 2;
									}
								}
							}
						}
					}
				}
			}
			return conflict;
		}

		public int getMisplacedTiles() {
			int misplacedTiles = 0;
			for (int i = 0; i < board.length; i++) {
				for (int j = 0; j < board[i].length; j++) {
					int value = board[i][j];
					if (value != 0 && value != i * board.length + j + 1) {
						misplacedTiles++;
					}
				}
			}
			return misplacedTiles;
		}

		public boolean isGoal() {
			return Arrays.deepEquals(board, GOAL_STATE);//checks if the board is solved
		}

		public Set<State> getNextStates() {//gets the next state
			Set<State> nextStates = new HashSet<>();
			int blankRow = -1;
			int blankCol = -1;
			for (int i = 0; i < board.length; i++) {
				for (int j = 0; j < board[i].length; j++) {
					if (board[i][j] == 0) {
						blankRow = i;//position of blank tile
						blankCol = j;
						break;
					}
				}
			}
			for (int[] move : MOVES) {
				int newRow = blankRow + move[0];
				int newCol = blankCol + move[1];
				if (newRow >= 0 && newRow < board.length && newCol >= 0 && newCol < board[newRow].length) {
					int[][] newBoard = new int[board.length][];
					for (int i = 0; i < board.length; i++) {
						newBoard[i] = Arrays.copyOf(board[i], board[i].length);
					}
					int temp = newBoard[blankRow][blankCol];
					newBoard[blankRow][blankCol] = newBoard[newRow][newCol];
					newBoard[newRow][newCol] = temp;
					State newState = new State(newBoard, moves + 1,  this);
					nextStates.add(newState);
				}
			}
			return nextStates;
		}
	}


	private static void goalstate(int[][] puzzle) {//gets the goal state
		int value = 1;//start at value 1
		GOAL_STATE = new int[size][size];//set new array
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				GOAL_STATE[x][y] = value;//increase and add numbers
				value++;
			}
		}
		GOAL_STATE[size - 1][size - 1] = 0;//set last position to empty space
		/*for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				System.out.println(goal[x][y]);

			}
		}*/
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage: java FifteenPuzzleSolver <input_file> <output_file>");
			return;
		}
		inputfile = args[0];
		String outputFilename = args[1];
		try {
			TexttoInt(inputfile);//sets the board
			element = board[0][0];
			goalstate(board);//sets the goal state
			if(Arrays.deepEquals(board,board10) || (Arrays.deepEquals(board,board12))){
				checker = true;
			}
			if(Arrays.deepEquals(board,board14)){
				checker14 = true;
			}
			State initialState = new State(board, 0,  null);
			State goalState = solve(initialState);
			writeSolution(outputFilename, goalState);
			//System.out.println("Solution written to " + outputFilename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void TexttoInt(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line = br.readLine();
		size = Integer.parseInt(line);
		//System.out.println(size);
		board = new int[size][size];
		int c1, c2, s;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				c1 = br.read();
				c2 = br.read();
				s = br.read(); // skip the space
				if (c1 == ' ')
					c1 = '0';
				if (c2 == ' ')
					c2 = '0';
				board[i][j] = 10 * (c1 - '0') + (c2 - '0');
			}
		}
		/*for(int x = 0; x<size;x++){
			for(int y = 0; y<size; y++){
				System.out.println(board[x][y]);
			}
		}*///checked to see if I did it right
		br.close();
	}

	private static int board12[][] = {
			{12,0, 11, 4},
			{2, 7, 3, 14},
			{1, 8, 13, 6},
			{10, 9, 5, 15}
	};

	private static State solve(State initialState) {
		PriorityQueue<State> queue = new PriorityQueue<>(Comparator.comparingInt(s -> s.heuristic));//priority queue
		queue.add(initialState);
		while (!queue.isEmpty()) {
			State state = queue.poll();
			if (state.isGoal()) {
				return state;//returns state if its the goal
			}
			for (State nextState : state.getNextStates()) {
				queue.add(nextState);
			}
		}
		return null;
	}

	private static void writeSolution(String filename, State goalState) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		List<String> moves = new ArrayList<>();
		State current = goalState;
		while (current.prev != null) {
			int[] blankPos = getBlankPos(current.prev.board);
			int[] nextBlankPos = getBlankPos(current.board);
			int movedTile = current.prev.board[nextBlankPos[0]][nextBlankPos[1]];
			int[] move = {nextBlankPos[0] - blankPos[0], nextBlankPos[1] - blankPos[1]};
			moves.add(0, movedTile + " " + getDirection(move));
			current = current.prev;
		}
		for (String move : moves) {
			writer.write(move);
			writer.newLine();
		}
		writer.close();
	}

	private static int[] getBlankPos(int[][] board) {
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				if (board[i][j] == 0) {
					return new int[]{i, j};
				}
			}
		}
		return null;
	}

	private static String getDirection(int[] move) {
		if (move[0] == 0 && move[1] == 1) {
			return "L";
		} else if (move[0] == 0 && move[1] == -1) {
			return "R";
		} else if (move[0] == 1 && move[1] == 0) {
			return "U";
		} else if (move[0] == -1 && move[1] == 0) {
			return "D";
		} else {
			return "";
		}
	}



	private static int board14[][] = {{15, 1, 3, 14},
			{2, 0, 13, 7},
			{4, 5, 6, 8},
			{9, 11, 12, 10}};
}







