package com.example.sudokumaster.model;

public class SudokuCell {
    public int value;
    public boolean isFixed;

    public SudokuCell(int value, boolean isFixed) {
        this.value = value;
        this.isFixed = isFixed;
    }
}