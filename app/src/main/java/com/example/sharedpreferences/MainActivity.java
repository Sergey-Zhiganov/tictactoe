package com.example.sharedpreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final Button[] buttons = new Button[9];
    private TextView message;
    private TextView currentPlayerView;
    private TextView statistics;
    private TextView games;
    private Button modeButton;

    private String currentPlayer = "X";
    private boolean isXStarting = false;
    private boolean isBotMode = false;
    private int player1Wins = 0;
    private int player2Wins = 0;
    private int totalGames = 0;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "SharedPreferences";
    private static final String PLAYER1_WINS_KEY = "player1Wins";
    private static final String PLAYER2_WINS_KEY = "player2Wins";
    private static final String TOTAL_GAMES_KEY = "totalGames";
    private static final String IS_DARK_THEME = "isDarkTheme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        message = findViewById(R.id.message);
        currentPlayerView = findViewById(R.id.currentPlayer);
        statistics = findViewById(R.id.statistics);
        games = findViewById(R.id.totalGames);
        Button newGameButton = findViewById(R.id.newGame);
        modeButton = findViewById(R.id.modeButton);
        Button themeButton = findViewById(R.id.themeButton);

        for (int i = 0; i < 9; i++) {
            String buttonID = "button" + (i + 1);
            @SuppressLint("DiscouragedApi")
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            buttons[i] = findViewById(resID);
            buttons[i].setOnClickListener(this::buttonClick);
        }

        newGameButton.setOnClickListener(v -> resetGame());
        modeButton.setOnClickListener(v -> toggleMode());
        themeButton.setOnClickListener(v -> toggleTheme());


        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadStatistics();

        updateModeButtonText();
        resetGame();
    }

    private void loadTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkTheme = sharedPreferences.getBoolean(IS_DARK_THEME, false);
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void toggleTheme() {
        boolean isDarkTheme = sharedPreferences.getBoolean(IS_DARK_THEME, false);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            editor.putBoolean(IS_DARK_THEME, false);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            editor.putBoolean(IS_DARK_THEME, true);
        }
        editor.apply();
    }

    private void buttonClick(View view) {
        Button button = (Button) view;
        if (button.getText().toString().isEmpty()) {
            button.setText(currentPlayer);

            if (checkWin()) {
                checkWinner();
            } else if (isDraw()) {
                message.setText("Ничья");
                totalGames++;
                updateStatistics();
            } else {
                switchPlayer();
                if (isBotMode && currentPlayer.equals("O")) {
                    botMove();
                }
            }
        }
    }

    private void switchPlayer() {
        currentPlayer = currentPlayer.equals("X") ? "O" : "X";
        updateCurrentPlayerView();
    }

    private void updateCurrentPlayerView() {
        currentPlayerView.setText("Ход игрока: " + currentPlayer);
    }

    private boolean checkWin() {
        String[][] field = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                field[i][j] = buttons[i * 3 + j].getText().toString();
            }
        }

        for (int i = 0; i < 3; i++) {
            if (field[i][0].equals(currentPlayer) && field[i][1].equals(currentPlayer) && field[i][2].equals(currentPlayer) ||
                    field[0][i].equals(currentPlayer) && field[1][i].equals(currentPlayer) && field[2][i].equals(currentPlayer))
                return true;
        }

        if (field[0][0].equals(currentPlayer) && field[1][1].equals(currentPlayer) && field[2][2].equals(currentPlayer))
            return true;
        return field[0][2].equals(currentPlayer) && field[1][1].equals(currentPlayer) && field[2][0].equals(currentPlayer);
    }

    private boolean isDraw() {
        for (Button button : buttons) {
            if (button.getText().toString().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void checkWinner() {
        if (isBotMode) {
            if (currentPlayer.equals("X")) {
                message.setText("Победил Игрок");
                player1Wins++;
            } else {
                message.setText("Победил Бот");
                player2Wins++;
            }
        } else {
            if (currentPlayer.equals("X")) {
                message.setText("Победил Игрок 1");
                player1Wins++;
            } else {
                message.setText("Победил Игрок 2");
                player2Wins++;
            }
        }
        totalGames++;
        updateStatistics();
        saveStatistics();
        disableButtons();
    }

    private void botMove() {
        Random random = new Random();
        int index;
        do {
            index = random.nextInt(9);
        } while (!buttons[index].getText().toString().isEmpty());

        buttons[index].setText(currentPlayer);

        if (checkWin()) {
            checkWinner();
        } else if (isDraw()) {
            message.setText("Ничья");
            totalGames++;
            updateStatistics();
            saveStatistics();
        } else {
            switchPlayer();
        }
    }

    private void toggleMode() {
        isBotMode = !isBotMode;
        updateModeButtonText();

        resetStatistics();
        resetGame();

        updateStatistics();
        saveStatistics();

        if (isBotMode && currentPlayer.equals("O")) {
            botMove();
        }
    }

    private void resetGame() {
        for (Button button : buttons) {
            button.setText("");
            button.setEnabled(true);
        }
        message.setText("");

        isXStarting = !isXStarting;
        currentPlayer = isXStarting ? "X" : "O";
        updateCurrentPlayerView();
    }

    private void resetStatistics() {
        player1Wins = 0;
        player2Wins = 0;
        totalGames = 0;
    }

    private void updateStatistics() {
        int drawn = totalGames - player1Wins - player2Wins;
        String statisticsText = isBotMode
                ? "Игрок (X): " + player1Wins + " | Бот (O): " + player2Wins + " | Ничьих: " + drawn
                : "Игрок 1 (X): " + player1Wins + " | Игрок 2 (O): " + player2Wins + " | Ничьих: " + drawn;
        statistics.setText(statisticsText);
        games.setText("Всего игр: " + totalGames);
    }

    private void disableButtons() {
        for (Button button : buttons) {
            button.setEnabled(false);
        }
    }

    private void saveStatistics() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PLAYER1_WINS_KEY, player1Wins);
        editor.putInt(PLAYER2_WINS_KEY, player2Wins);
        editor.putInt(TOTAL_GAMES_KEY, totalGames);
        editor.apply();
    }

    private void loadStatistics() {
        player1Wins = sharedPreferences.getInt(PLAYER1_WINS_KEY, 0);
        player2Wins = sharedPreferences.getInt(PLAYER2_WINS_KEY, 0);
        totalGames = sharedPreferences.getInt(TOTAL_GAMES_KEY, 0);
        updateStatistics();
    }

    private void updateModeButtonText() {
        String buttonText = isBotMode ? "Режим: Игра с ботом" : "Режим: 2 игрока";
        modeButton.setText(buttonText);
    }
}
