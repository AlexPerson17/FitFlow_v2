package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class DashboardScreen extends BaseScreen {

    private Scene scene;

    public DashboardScreen(AppStateManager stateManager) {
        super(stateManager);
        buildScreen();
    }

    private void buildScreen() {
        StackPane root = createRootLayout();

        VBox page = new VBox();
        page.setPrefSize(390, 844);
        page.setMinSize(390, 844);
        page.setMaxSize(390, 844);

        // ── Header (75px) ─────────────────────────────────────────────────
        StackPane header = new StackPane();
        header.setPrefSize(390, 75);
        header.setMinSize(390, 75);
        header.setMaxSize(390, 75);
        header.setBackground(new Background(new BackgroundFill(
                PRIMARY_BLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        VBox titles = new VBox(4);
        titles.setAlignment(Pos.CENTER);
        Label appTitle = new Label("FitFlow");
        appTitle.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: white;");
        Label appSubtitle = new Label("Interactive Workout Assistant");
        appSubtitle.setStyle("-fx-font-size: 12; -fx-text-fill: rgba(255,255,255,0.8);");
        titles.getChildren().addAll(appTitle, appSubtitle);

        Button hamburger = createHeaderHamburgerButton(root, 75);
        header.getChildren().addAll(titles, hamburger);
        StackPane.setAlignment(titles, Pos.CENTER);
        StackPane.setAlignment(hamburger, Pos.TOP_RIGHT);
        StackPane.setMargin(hamburger, new Insets(8, 8, 0, 0));

        // ── White content ──────────────────────────────────────────────────
        VBox content = new VBox(10);
        content.setPadding(new Insets(24));
        VBox.setVgrow(content, Priority.ALWAYS);
        content.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        Label sectionLabel = new Label("NAVIGATE TO");
        sectionLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold;" +
                              " -fx-text-fill: #AAAAAA; -fx-padding: 0 0 4 0;");

        Button profileButton = createNavButton("Profile");
        Button builderButton = createNavButton("Workout Builder");
        Button historyButton = createNavButton("Workout History");

        profileButton.setOnAction(e -> stateManager.showProfileScreen());
        builderButton.setOnAction(e -> stateManager.showRoutineBuilderScreen());
        historyButton.setOnAction(e -> stateManager.showWorkoutHistoryScreen());

        Separator sep = new Separator();
        sep.setPadding(new Insets(6, 0, 6, 0));

        Button logoutButton = createNavButton("Log Out");
        logoutButton.setStyle(logoutButton.getStyle()
                .replace("#2F66B3", "#A81805"));
        logoutButton.setOnMouseEntered(e -> logoutButton.setStyle(logoutButton.getStyle()
                .replace("#A81805", "#8A1404")));
        logoutButton.setOnMouseExited(e -> logoutButton.setStyle(logoutButton.getStyle()
                .replace("#8A1404", "#A81805")));
        logoutButton.setOnAction(e -> stateManager.logOut());

        content.getChildren().addAll(
                sectionLabel, profileButton, builderButton, historyButton,
                sep, logoutButton);

        page.getChildren().addAll(header, content);
        root.getChildren().add(0, page);
        scene = new Scene(root, 390, 844);
    }

    private Button createNavButton(String label) {
        Button button = new Button(label);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(46);
        button.setFont(buttonFont());
        button.setStyle(
                "-fx-background-color: #2F66B3; -fx-text-fill: white;" +
                " -fx-background-radius: 10; -fx-font-size: 14; -fx-font-weight: bold;" +
                " -fx-padding: 10 20 10 20; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;");
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle()
                .replace("#2F66B3", "#3F6FB5")));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle()
                .replace("#3F6FB5", "#2F66B3")));
        return button;
    }

    public Scene getScene() { return scene; }
}
