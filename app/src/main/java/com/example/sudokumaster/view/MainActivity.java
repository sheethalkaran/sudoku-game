package com.example.sudokumaster.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sudokumaster.R;

public class MainActivity extends AppCompatActivity {

    Button easyBtn, mediumBtn, hardBtn, playBtn;
    int selectedDifficulty = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        easyBtn = findViewById(R.id.btn_easy);
        mediumBtn = findViewById(R.id.btn_medium);
        hardBtn = findViewById(R.id.btn_hard);
        playBtn = findViewById(R.id.btn_play);

        // Set level button click listeners
        easyBtn.setOnClickListener(v -> {
            selectedDifficulty = 1;
            highlightSelectedButton(easyBtn);
        });

        mediumBtn.setOnClickListener(v -> {
            selectedDifficulty = 2;
            highlightSelectedButton(mediumBtn);
        });

        hardBtn.setOnClickListener(v -> {
            selectedDifficulty = 3;
            highlightSelectedButton(hardBtn);
        });

        // Play button starts the game only if difficulty is selected
        playBtn.setOnClickListener(v -> {
            if (selectedDifficulty > 0) {
                openGame(selectedDifficulty);
            }
        });
    }

    private void openGame(int difficulty) {
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra("difficulty", difficulty);
        startActivity(intent);
    }

    private void highlightSelectedButton(Button selectedButton) {
        // Reset all buttons to dimmed state
        easyBtn.setAlpha(0.5f);
        mediumBtn.setAlpha(0.5f);
        hardBtn.setAlpha(0.5f);

        // Highlight selected
        selectedButton.setAlpha(1.0f);
    }
}
