package com.example.sudokumaster.model;

import java.util.Random;

public class SudokuGenerator {
    private final Random random = new Random();

    public int[][] generate(int difficulty) {
        int[][] board = new int[9][9];

        fillDiagonal(board);
        solve(board);
        removeCells(board, difficulty);

        return board;
    }

    private void fillDiagonal(int[][] board) {
        for (int i = 0; i < 9; i += 3) {
            fillBox(board, i, i);
        }
    }

    private void fillBox(int[][] board, int row, int col) {
        int num;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                do {
                    num = random.nextInt(9) + 1;
                } while (!isUnusedInBox(board, row, col, num));
                board[row + i][col + j] = num;
            }
    }

    private boolean isUnusedInBox(int[][] board, int rowStart, int colStart, int num) {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (board[rowStart + i][colStart + j] == num)
                    return false;
        return true;
    }

    private boolean isUnusedInRow(int[][] board, int row, int num) {
        for (int i = 0; i < 9; i++)
            if (board[row][i] == num)
                return false;
        return true;
    }

    private boolean isUnusedInCol(int[][] board, int col, int num) {
        for (int i = 0; i < 9; i++)
            if (board[i][col] == num)
                return false;
        return true;
    }

    private boolean isSafe(int[][] board, int row, int col, int num) {
        return isUnusedInRow(board, row, num) &&
                isUnusedInCol(board, col, num) &&
                isUnusedInBox(board, row - row % 3, col - col % 3, num);
    }

    private boolean solve(int[][] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    for (int num = 1; num <= 9; num++) {
                        if (isSafe(board, row, col, num)) {
                            board[row][col] = num;
                            if (solve(board)) {
                                return true;
                            }
                            board[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private void removeCells(int[][] board, int difficulty) {
        int cellsToRemove;

        if (difficulty == 1) {
            cellsToRemove = 30; // Easy
        } else if (difficulty == 3) {
            cellsToRemove = 55; // Hard
        } else {
            cellsToRemove = 45; // Medium
        }

        while (cellsToRemove > 0) {
            int row = random.nextInt(9);
            int col = random.nextInt(9);
            if (board[row][col] != 0) {
                board[row][col] = 0;
                cellsToRemove--;
            }
        }
    }
}
