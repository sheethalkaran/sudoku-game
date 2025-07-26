package com.example.sudokumaster.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.sudokumaster.R;
import com.example.sudokumaster.model.SudokuCell;
import com.example.sudokumaster.viewmodel.GameViewModel;

import java.util.Locale;

public class GameActivity extends AppCompatActivity {
    private GameViewModel viewModel;
    private GridLayout gridLayout;
    private GridLayout numberPad;
    private TextView timerText;
    private final EditText[][] cellViews = new EditText[9][9];
    private int selectedRow = -1, selectedCol = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        timerText = findViewById(R.id.timerText);
        gridLayout = findViewById(R.id.sudokuGrid);
        numberPad = findViewById(R.id.numberPad);

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        setupObservers();
        setupButtons();

        int difficulty = getIntent().getIntExtra("difficulty", 2);
        viewModel.startGame(difficulty);

        gridLayout.post(this::renderBoard);
    }

    private void setupObservers() {
        viewModel.getBoard().observe(this, this::updateBoardValues);
        viewModel.getGameCompleted().observe(this, completed -> {
            if (completed) showWinDialog();
        });
        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                viewModel.clearErrorMessage();
            }
        });
        viewModel.getTimeLeft().observe(this, timeLeft -> {
            int min = (int) (timeLeft / 1000) / 60;
            int sec = (int) (timeLeft / 1000) % 60;
            timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", min, sec));
        });
        viewModel.getTimeUp().observe(this, timeUp -> {
            if (timeUp) showTimeUpDialog();
        });
    }

    private void setupButtons() {
        findViewById(R.id.btnErase).setOnClickListener(v -> {
            if (selectedRow >= 0 && selectedCol >= 0) {
                // Immediately clear the cell visually
                if (cellViews[selectedRow][selectedCol] != null) {
                    cellViews[selectedRow][selectedCol].setText("");
                }
                // Update ViewModel
                viewModel.setCellValue(selectedRow, selectedCol, 0);
            } else {
                Toast.makeText(this, "Select a cell to erase", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnReset).setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Reset Game")
                        .setMessage("Are you sure you want to reset the game? All progress will be lost.")
                        .setPositiveButton("Yes", (dialog, which) -> recreate())
                        .setNegativeButton("Cancel", null)
                        .show()
        );


        findViewById(R.id.btnUndo).setOnClickListener(v -> {
            int[] undoneCell = viewModel.undoLastMove();
            if (undoneCell[0] != -1) {
                // Immediately update the undone cell
                EditText undoCell = cellViews[undoneCell[0]][undoneCell[1]];
                if (undoCell != null) {
                    SudokuCell[][] board = viewModel.getBoard().getValue();
                    if (board != null) {
                        String value = board[undoneCell[0]][undoneCell[1]].value == 0 ?
                                "" : String.valueOf(board[undoneCell[0]][undoneCell[1]].value);
                        undoCell.setText(value);
                    }
                }
                focusCell(undoneCell[0], undoneCell[1]);
                numberPad.setVisibility(View.GONE);
            }
        });
    }

    private void renderBoard() {
        gridLayout.removeAllViews();
        gridLayout.setColumnCount(9);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int cellSize = (screenWidth - dpToPx(64)) / 9;

        SudokuCell[][] board = viewModel.getBoard().getValue();
        if (board == null) return;

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                cellViews[row][col] = createCell(row, col, cellSize, board[row][col]);
                gridLayout.addView(cellViews[row][col]);
            }
        }
    }

    private EditText createCell(int row, int col, int cellSize, SudokuCell sudokuCell) {
        EditText cell = new EditText(this);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = cellSize;
        params.height = cellSize;

        int thick = dpToPx(3), thin = dpToPx(1);
        params.setMargins(
                (col % 3 == 0) ? thick : thin,
                (row % 3 == 0) ? thick : thin,
                (col == 8 || (col + 1) % 3 == 0) ? thick : thin,
                (row == 8 || (row + 1) % 3 == 0) ? thick : thin
        );

        cell.setLayoutParams(params);
        cell.setGravity(Gravity.CENTER);
        cell.setTextSize(16);
        cell.setTypeface(Typeface.DEFAULT_BOLD);

        // CHANGED: Enable both keyboard and UI input
        if (sudokuCell.isFixed) {
            cell.setInputType(InputType.TYPE_NULL);
            cell.setEnabled(false);
            cell.setBackgroundResource(R.drawable.bg_fixed_cell);
            cell.setTextColor(Color.WHITE);
        } else {
            // Allow keyboard input for editable cells
            cell.setInputType(InputType.TYPE_CLASS_NUMBER);
            cell.setBackgroundResource(R.drawable.bg_editable_cell);
            cell.setTextColor(Color.WHITE);

            // Set input filter to only allow single digits 1-9
            cell.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});

            setupCellInput(cell, row, col);
        }

        if (sudokuCell.value != 0) {
            cell.setText(String.valueOf(sudokuCell.value));
        }

        // Handle cell selection for both click and focus
        cell.setOnClickListener(v -> {
            if (!sudokuCell.isFixed) {
                selectCell(row, col);
                setupNumberPad(row, col);
            }
        });

        cell.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !sudokuCell.isFixed) {
                selectCell(row, col);
                setupNumberPad(row, col);
            }
        });

        return cell;
    }

    private void setupCellInput(EditText cell, int row, int col) {
        cell.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;

                String input = s.toString().trim();

                // Handle empty input (erase)
                if (input.isEmpty()) {
                    viewModel.setCellValue(row, col, 0);
                    return;
                }

                // Validate input
                if (input.length() > 1) {
                    isUpdating = true;
                    s.clear();
                    isUpdating = false;
                    viewModel.setErrorMessage("Please enter only one digit (1-9)");
                    return;
                }

                char inputChar = input.charAt(0);
                if (!Character.isDigit(inputChar) || inputChar == '0') {
                    isUpdating = true;
                    s.clear();
                    isUpdating = false;
                    viewModel.setErrorMessage("Please enter a valid number (1-9)");
                    return;
                }

                int value = Character.getNumericValue(inputChar);

                // Check if move is valid
                if (!viewModel.isValidMove(row, col, value)) {
                    isUpdating = true;
                    s.clear();
                    isUpdating = false;
                    viewModel.setErrorMessage("Invalid move! This number conflicts with Sudoku rules.");
                    return;
                }

                // Valid input - update the model
                viewModel.setCellValue(row, col, value);

                // Move to next empty cell
                moveToNextCell(row, col);
            }
        });
    }

    private void selectCell(int row, int col) {
        // Clear previous selection
        if (selectedRow >= 0 && selectedCol >= 0 && cellViews[selectedRow][selectedCol] != null) {
            SudokuCell[][] board = viewModel.getBoard().getValue();
            if (board != null && !board[selectedRow][selectedCol].isFixed) {
                cellViews[selectedRow][selectedCol].setBackgroundResource(R.drawable.bg_editable_cell);
                cellViews[selectedRow][selectedCol].setTextColor(Color.WHITE);
            }
        }

        // Set new selection
        selectedRow = row;
        selectedCol = col;
        if (cellViews[row][col] != null) {
            cellViews[row][col].setBackgroundResource(R.drawable.bg_selected_cell);
            cellViews[row][col].setTextColor(Color.BLACK);
        }
    }

    private void setupNumberPad(int row, int col) {
        numberPad.removeAllViews();
        numberPad.setVisibility(View.VISIBLE);
        numberPad.setColumnCount(3);

        int[] validNumbers = viewModel.getValidNumbers(row, col);
        int buttonWidth = (getResources().getDisplayMetrics().widthPixels - dpToPx(64)) / 3;

        for (int num : validNumbers) {
            Button btn = new Button(this);
            btn.setText(String.valueOf(num));
            btn.setTextSize(18);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = buttonWidth;
            params.height = dpToPx(50);
            params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            btn.setLayoutParams(params);

            final int number = num;
            btn.setOnClickListener(v -> {
                if (cellViews[row][col] != null) {
                    cellViews[row][col].setText(String.valueOf(number));
                }

                viewModel.setCellValue(row, col, number);

                numberPad.setVisibility(View.GONE);
                moveToNextCell(row, col);
            });

            numberPad.addView(btn);
        }
    }


    private void moveToNextCell(int currentRow, int currentCol) {
        // Post to next frame to avoid blocking UI
        gridLayout.post(() -> {
            int[] nextCell = viewModel.findNextEmptyCell(currentRow, currentCol);
            if (nextCell[0] != -1) {
                focusCell(nextCell[0], nextCell[1]);
                selectCell(nextCell[0], nextCell[1]);
                // Show number pad for the next cell
                setupNumberPad(nextCell[0], nextCell[1]);
            } else {
                // No more empty cells, hide number pad
                numberPad.setVisibility(View.GONE);
            }
        });
    }

    private void focusCell(int row, int col) {
        if (cellViews[row][col] != null) {
            cellViews[row][col].requestFocus();
            selectCell(row, col);
        }
    }

    private void updateBoardValues(SudokuCell[][] board) {
        // Skip update if board is being rendered for first time
        if (cellViews[0][0] == null) return;

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                EditText cell = cellViews[row][col];
                if (cell != null && cell.isEnabled()) {
                    String currentText = cell.getText().toString();
                    String newValue = board[row][col].value == 0 ? "" : String.valueOf(board[row][col].value);

                    // Update if different and cell is not focused (to avoid interfering with user input)
                    if (!newValue.equals(currentText) && !cell.hasFocus()) {
                        cell.setText(newValue);
                    }
                }
            }
        }
    }

    private void showTimeUpDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Time Over!")
                .setMessage("Time is up! Better luck next time.")
                .setCancelable(false)
                .setPositiveButton("Back to Main", (dialog, which) -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .show();
    }

    private void showWinDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Congratulations!")
                .setMessage("You have successfully completed the Sudoku puzzle!")
                .setCancelable(false)
                .setPositiveButton("Play Again", (dialog, which) -> recreate())
                .setNegativeButton("Main Menu", (dialog, which) -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .show();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewModel != null) {
            viewModel.cleanup();
        }
    }
}