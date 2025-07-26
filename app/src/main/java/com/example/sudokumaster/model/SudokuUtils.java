package com.example.sudokumaster.model;

public class SudokuUtils {
    public static boolean isMoveValid(SudokuCell[][] board, int row, int col, int num) {
        for (int i = 0; i < 9; i++) {
            if (board[row][i].value == num && i != col) return false;
            if (board[i][col].value == num && i != row) return false;
        }
        int boxRow = row - row % 3, boxCol = col - col % 3;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                int r = boxRow + i, c = boxCol + j;
                if (board[r][c].value == num && (r != row || c != col)) return false;
            }
        return true;
    }

    public static boolean isBoardFull(SudokuCell[][] board) {
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                if (board[i][j].value == 0) return false;
        return true;
    }
}
