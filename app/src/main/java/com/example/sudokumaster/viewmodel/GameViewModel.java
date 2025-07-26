package com.example.sudokumaster.viewmodel;

import android.os.CountDownTimer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sudokumaster.model.SudokuModel;
import com.example.sudokumaster.model.SudokuCell;
import com.example.sudokumaster.model.SudokuGenerator;
import com.example.sudokumaster.model.SudokuUtils;

import java.util.Stack;

public class GameViewModel extends ViewModel {
    private final SudokuModel model = new SudokuModel();
    private final MutableLiveData<SudokuCell[][]> boardLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> gameCompletedLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Long> timeLeftLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> timeUpLiveData = new MutableLiveData<>();

    private final Stack<Move> moveStack = new Stack<>();
    private CountDownTimer timer;

    public void startGame(int difficulty) {
        SudokuGenerator generator = new SudokuGenerator();
        int[][] generated = generator.generate(difficulty);
        model.generateBoard(generated);

        moveStack.clear();
        gameCompletedLiveData.setValue(false);
        timeUpLiveData.setValue(false);

        long timeLeft = difficulty == 1 ? 900_000 : difficulty == 3 ? 420_000 : 600_000;
        startTimer(timeLeft);

        boardLiveData.setValue(model.getBoard());
    }

    private void startTimer(long timeLeft) {
        if (timer != null) timer.cancel();

        timer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftLiveData.setValue(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                timeUpLiveData.setValue(true);
            }
        };
        timer.start();
    }

    public boolean isValidMove(int row, int col, int value) {
        return model.isMoveValid(row, col, value);
    }

    public void setCellValue(int row, int col, int value) {
        SudokuCell[][] board = model.getBoard();
        if (board[row][col].isFixed) return;

        if (value == 0) {
            if (board[row][col].value != 0) {
                moveStack.push(new Move(row, col, board[row][col].value));
                model.setValue(row, col, 0);
                boardLiveData.setValue(model.getBoard());
            }
            return;
        }

        if (board[row][col].value != value) {
            moveStack.push(new Move(row, col, board[row][col].value));
            model.setValue(row, col, value);
            boardLiveData.setValue(model.getBoard());

            if (model.isBoardFull()) {
                if (timer != null) timer.cancel();
                gameCompletedLiveData.setValue(true);
            }
        }
    }

    public int[] undoLastMove() {
        if (moveStack.isEmpty()) {
            errorMessageLiveData.setValue("Nothing to undo");
            return new int[]{-1, -1};
        }

        Move lastMove = moveStack.pop();
        model.setValue(lastMove.row, lastMove.col, lastMove.previousValue);
        boardLiveData.setValue(model.getBoard());

        return new int[]{lastMove.row, lastMove.col};
    }

    public int[] getValidNumbers(int row, int col) {
        SudokuCell[][] board = model.getBoard();
        if (board[row][col].isFixed) return new int[0];

        int[] validNumbers = new int[9];
        int count = 0;
        for (int i = 1; i <= 9; i++) {
            if (SudokuUtils.isMoveValid(board, row, col, i)) {
                validNumbers[count++] = i;
            }
        }

        int[] result = new int[count];
        System.arraycopy(validNumbers, 0, result, 0, count);
        return result;
    }

    public int[] findNextEmptyCell(int currentRow, int currentCol) {
        SudokuCell[][] board = model.getBoard();

        int nextRow = currentRow, nextCol = currentCol + 1;
        if (nextCol >= 9) {
            nextCol = 0;
            nextRow++;
        }

        for (int row = nextRow; row < 9; row++) {
            int startCol = (row == nextRow) ? nextCol : 0;
            for (int col = startCol; col < 9; col++) {
                if (board[row][col].value == 0 && !board[row][col].isFixed) {
                    return new int[]{row, col};
                }
            }
        }

        for (int row = 0; row <= currentRow; row++) {
            int endCol = (row == currentRow) ? currentCol : 9;
            for (int col = 0; col < endCol; col++) {
                if (board[row][col].value == 0 && !board[row][col].isFixed) {
                    return new int[]{row, col};
                }
            }
        }

        return new int[]{-1, -1};
    }

    // ADDED: Method to set custom error messages
    public void setErrorMessage(String message) {
        errorMessageLiveData.setValue(message);
    }

    public void clearErrorMessage() {
        errorMessageLiveData.setValue(null);
    }

    public void cleanup() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cleanup();
    }

    // LiveData getters
    public LiveData<SudokuCell[][]> getBoard() { return boardLiveData; }
    public LiveData<Boolean> getGameCompleted() { return gameCompletedLiveData; }
    public LiveData<String> getErrorMessage() { return errorMessageLiveData; }
    public LiveData<Long> getTimeLeft() { return timeLeftLiveData; }
    public LiveData<Boolean> getTimeUp() { return timeUpLiveData; }

    public static class Move {
        public final int row, col, previousValue;
        public Move(int row, int col, int previousValue) {
            this.row = row;
            this.col = col;
            this.previousValue = previousValue;
        }
    }
}