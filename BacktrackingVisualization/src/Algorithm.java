import java.util.ArrayList;

public class Algorithm {
	public final int size;
	private final boolean log;
	
	public ArrayList<boolean[][]> solutions = new ArrayList<boolean[][]>();
	
	public Algorithm(int size, boolean logResults) {
		this.size = size;
		this.log = logResults;
	}
	
	public Algorithm run() {
		boolean board[][] = new boolean[size][size];
		if (!solveBoard(board, 0) && solutions.size() == 0) System.out.println("No solutions found");
		else System.out.println("Printed " + solutions.size() + " solutions to place " + size + " Queens");
		return this;
	}
	
	public void printBoard(boolean[][] board) {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (board[i][j]) {
					System.out.print("Q ");
				} else System.out.print("x ");
			}
			System.out.print("\n");
		}
		System.out.print("\n");
	}
	
	private boolean solveBoard(boolean board[][], int row) {
		if (row >= size) {
			boolean[][] copy = board.clone();
			for (int i = 0; i < copy.length; i++)
			    copy[i] = copy[i].clone();
			
			solutions.add(copy);
			if (log) printBoard(board);
			
			return true;
		}
		
		for (int col = 0; col < size; col++) {
			if (isSafe(board, row, col)) {
				board[row][col] = true; //If position safe, mark it
				 
				solveBoard(board, row+1);
				
				//Backtrack and continue with the next column
				board[row][col] = false;
			}
		}
		//When all columns checked turned out to be unsafe, backtrack to the last row
		return false;
	}
	
	private boolean isSafe(boolean board[][], int row, int col) {
		int i, j;
		
		//Check up
        for (i = 0; i < row; i++) 
            if (board[i][col] == true)
                return false; 
  
        // `
        //  \
        //   \
        //    x
        for (i = row, j = col; i >= 0 && j >= 0; i--, j--) 
            if (board[i][j] == true)
                return false; 
  
        // 	  ´
        //   /
        //  /
        // x
        for (i = row, j = col; i >= 0 && j < size; i--, j++) 
            if (board[i][j] == true)
                return false; 
  
        return true; 
    } 
}
