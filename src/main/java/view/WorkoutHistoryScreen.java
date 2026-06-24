package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.util.List;

public class WorkoutHistoryScreen extends BaseScreen {

    private Scene scene;

    public WorkoutHistoryScreen(AppStateManager stateManager) {
        super(stateManager);
        buildScreen();
    }

    private void buildScreen() {
        StackPane root = createRootLayout();

        VBox page = new VBox();
        page.setPrefSize(390, 844);
        page.setMinSize(390, 844);
        page.setMaxSize(390, 844);

        // ── Header (140px) ────────────────────────────────────────────────
        StackPane header = new StackPane();
        header.setPrefSize(390, 140);
        header.setMinSize(390, 140);
        header.setMaxSize(390, 140);
        header.setBackground(new Background(new BackgroundFill(
                PRIMARY_BLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        Label historyTitle = new Label("History");
        historyTitle.setStyle("-fx-font-size: 34; -fx-text-fill: white; -fx-font-weight: bold;");

        Button hamburger = createHeaderHamburgerButton(root, 140);
        header.getChildren().addAll(historyTitle, hamburger);
        StackPane.setAlignment(historyTitle, Pos.CENTER);
        StackPane.setAlignment(hamburger, Pos.TOP_LEFT);
        StackPane.setMargin(hamburger, new Insets(32, 0, 0, 24));

        // ── Card list ─────────────────────────────────────────────────────
        VBox cardList = new VBox(24);
        cardList.setPadding(new Insets(36, 25, 24, 25));
        cardList.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        List<String> entries = stateManager.getWorkoutHistory();

        if (entries == null || entries.isEmpty()) {
            Label empty = new Label("No workout history found.");
            empty.setStyle("-fx-font-size: 13; -fx-text-fill: #888;");
            cardList.getChildren().add(empty);
            cardList.setAlignment(Pos.TOP_CENTER);
        } else {
            for (String entry : entries) {
                cardList.getChildren().add(buildHistoryCard(entry));
            }
        }

        ScrollPane scroll = new ScrollPane(cardList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: white; -fx-background: white;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        page.getChildren().addAll(header, scroll);
        root.getChildren().add(0, page);
        scene = new Scene(root, 390, 844);
    }

    private VBox buildHistoryCard(String entry) {
        VBox card = new VBox(8);
        card.setPrefWidth(340);
        card.setMinWidth(340);
        card.setMaxWidth(340);
        card.setPadding(new Insets(10));
        card.setBackground(new Background(new BackgroundFill(
                CARD_GRAY, new CornerRadii(8), Insets.EMPTY)));
        card.setStyle("-fx-border-color: #2F66B3; -fx-border-radius: 8; -fx-border-width: 1;");

        String[] parts = entry.split(" \\| ");
        if (parts.length < 4) {
            card.getChildren().add(new Label(entry));
            return card;
        }

        String date     = parts[0].trim();
        String routine  = parts[1].trim();
        String duration = parts[2].trim();
        String calories = parts[3].trim();

        // Top row: date | name | Load button
        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #555;");

        Label nameLabel = new Label(routine);
        nameLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #333; -fx-font-weight: bold;");

        Button loadBtn = new Button("Load");
        loadBtn.setPrefSize(60, 26);
        loadBtn.setStyle("-fx-background-color: #2F66B3; -fx-text-fill: white;" +
                         " -fx-font-size: 11; -fx-font-weight: bold;" +
                         " -fx-background-radius: 4; -fx-cursor: hand;");

        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        HBox topRow = new HBox(8, dateLabel, leftSpacer, nameLabel, rightSpacer, loadBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Stats row: 5 equal columns
        HBox statsRow = new HBox();
        statsRow.getChildren().addAll(
                statCell(duration, "Duration"),
                statCell(calories, "Calories"),
                statCell("—", "Exercises"),
                statCell("—", "Rest"),
                statCell("—", "Rounds"));

        card.getChildren().addAll(topRow, statsRow);
        return card;
    }

    private VBox statCell(String value, String label) {
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #222;");
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 9; -fx-text-fill: #888;");
        VBox cell = new VBox(2, v, l);
        cell.setAlignment(Pos.CENTER);
        HBox.setHgrow(cell, Priority.ALWAYS);
        return cell;
    }

    public Scene getScene() { return scene; }
}
