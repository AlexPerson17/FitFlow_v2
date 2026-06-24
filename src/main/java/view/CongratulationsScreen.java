package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import util.Calculator;

public class CongratulationsScreen extends BaseScreen {

    private Scene scene;
    private final int durationSeconds;
    private final int exerciseCount;

    public CongratulationsScreen(AppStateManager stateManager,
                                  int durationSeconds,
                                  int exerciseCount) {
        super(stateManager);
        this.durationSeconds = durationSeconds;
        this.exerciseCount   = exerciseCount;
        buildScreen();
    }

    private void buildScreen() {
        VBox root = new VBox();
        root.setPrefSize(390, 844);
        root.setMinSize(390, 844);
        root.setMaxSize(390, 844);

        // ── Blue top section (330px) ───────────────────────────────────────
        VBox topSection = new VBox(10);
        topSection.setAlignment(Pos.TOP_CENTER);
        topSection.setPrefHeight(330);
        topSection.setMinHeight(330);
        topSection.setMaxHeight(330);
        topSection.setPadding(new Insets(28, 20, 20, 20));
        topSection.setBackground(new Background(new BackgroundFill(
                PRIMARY_BLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        Label congratsLabel = new Label("Congratulations!");
        congratsLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: white;");

        Label trophyCircle = new Label("🏆");
        trophyCircle.setPrefSize(150, 150);
        trophyCircle.setMinSize(150, 150);
        trophyCircle.setAlignment(Pos.CENTER);
        trophyCircle.setStyle("-fx-font-size: 60;");
        trophyCircle.setBackground(new Background(new BackgroundFill(
                Color.rgb(255, 255, 255, 0.2), new CornerRadii(75), Insets.EMPTY)));

        Label completeLabel = new Label("Workout Complete");
        completeLabel.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: white;");

        topSection.getChildren().addAll(congratsLabel, trophyCircle, completeLabel);

        // ── White bottom section ──────────────────────────────────────────
        VBox bottomSection = new VBox(20);
        bottomSection.setAlignment(Pos.TOP_CENTER);
        bottomSection.setPadding(new Insets(50, 20, 30, 20));
        VBox.setVgrow(bottomSection, Priority.ALWAYS);
        bottomSection.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        // Stats 2x2 grid
        int durationMinutes = durationSeconds / 60;
        int calories = (int) Calculator.calculateCaloriesBurned(durationMinutes, 8.0);
        String durationText = String.format("%d:%02d", durationMinutes, durationSeconds % 60);

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(32);
        statsGrid.setVgap(34);
        statsGrid.setAlignment(Pos.CENTER);

        statsGrid.add(statCard(durationText,                   "Duration"),       0, 0);
        statsGrid.add(statCard(String.valueOf(exerciseCount),  "Exercises"),      1, 0);
        statsGrid.add(statCard(calories + " cal",              "Calories"),       0, 1);
        statsGrid.add(statCard("—",                       "Total Workouts"), 1, 1);

        // Buttons
        Button showHistoryBtn = new Button("Show History");
        showHistoryBtn.setPrefSize(145, 56);
        showHistoryBtn.setStyle("-fx-background-color: #2F66B3; -fx-text-fill: white;" +
                                " -fx-font-size: 15; -fx-font-weight: bold;" +
                                " -fx-background-radius: 18; -fx-cursor: hand;");
        showHistoryBtn.setOnAction(e -> stateManager.showWorkoutHistoryScreen());

        Button doneBtn = new Button("Done");
        doneBtn.setPrefSize(145, 56);
        doneBtn.setStyle("-fx-background-color: #1EA43A; -fx-text-fill: white;" +
                         " -fx-font-size: 15; -fx-font-weight: bold;" +
                         " -fx-background-radius: 18; -fx-cursor: hand;");
        doneBtn.setOnAction(e -> stateManager.showRoutineBuilderScreen());

        HBox buttonRow = new HBox(18, showHistoryBtn, doneBtn);
        buttonRow.setAlignment(Pos.CENTER);

        bottomSection.getChildren().addAll(statsGrid, buttonRow);
        root.getChildren().addAll(topSection, bottomSection);
        scene = new Scene(root, 390, 844);
    }

    private VBox statCard(String value, String label) {
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: #111;");
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 12; -fx-text-fill: #888;");
        VBox card = new VBox(4, v, l);
        card.setPrefSize(145, 86);
        card.setMinSize(145, 86);
        card.setAlignment(Pos.CENTER);
        card.setBackground(new Background(new BackgroundFill(
                CARD_GRAY, new CornerRadii(8), Insets.EMPTY)));
        return card;
    }

    public Scene getScene() { return scene; }
}
