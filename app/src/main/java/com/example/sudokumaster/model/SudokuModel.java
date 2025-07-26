package com.example.sudokumaster.model;

public class SudokuModel {
    private final SudokuCell[][] board = new SudokuCell[9][9];

    public void generateBoard(int[][] generatedBoard) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                board[i][j] = new SudokuCell(generatedBoard[i][j], generatedBoard[i][j] != 0);
            }
        }
    }

    public SudokuCell[][] getBoard() {
        return board;
    }

    public void setValue(int row, int col, int value) {
        if (row >= 0 && row < 9 && col >= 0 && col < 9) {
            board[row][col].value = value;
        }
    }

    public boolean isMoveValid(int row, int col, int value) {
        return SudokuUtils.isMoveValid(board, row, col, value);
    }

    public boolean isBoardFull() {
        return SudokuUtils.isBoardFull(board);
    }
}