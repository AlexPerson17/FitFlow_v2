/*
 * File: RoutineBuilderScreen.java
 * Version: 0.7.0
 * Date last edited: 6/24/2026
 * Original Author: Orange Snaer
 * Adapted by: Alex Ronn
 * Modified by: David Lewis
 * File Purpose: This class builds the workout routine builder screen.
 * Update Notes: Rewrote to 390x844 fixed two-column blueprint layout.
 */

package view;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.RoutineExerciseSelection;
import service.ServiceResponse;

public class RoutineBuilderScreen extends BaseScreen {

    private static final String ICONS  = "/Icons";
    private static final String IMAGES = "/Images";

    private StackPane saveRoutineOverlay;
    private TextField routineNameField;
    private Label     routineNameMessageLabel;
    private VBox      routinePanel;

    public RoutineBuilderScreen(AppStateManager stateManager) {
        super(stateManager);
    }

    public void show(Stage stage) {
        StackPane root = createRootLayout();

        VBox page = new VBox();
        page.setPrefSize(390, 844);
        page.setMinSize(390, 844);
        page.setMaxSize(390, 844);

        // ── Header (150px) with tab row ───────────────────────────────────
        StackPane header = buildHeader(root);

        // ── Body: two-column HBox ──────────────────────────────────────────
        HBox body = new HBox(10);
        body.setPadding(new Insets(8));
        VBox.setVgrow(body, Priority.ALWAYS);

        VBox exerciseLibrary = buildExerciseLibrary();
        ScrollPane libScroll = new ScrollPane(exerciseLibrary);
        libScroll.setFitToWidth(true);
        libScroll.setPrefWidth(178);
        libScroll.setMinWidth(178);
        libScroll.setMaxWidth(178);
        libScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        libScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox settingsPanel = buildSettingsPanel(stage);
        settingsPanel.setPrefWidth(178);
        settingsPanel.setMinWidth(178);
        settingsPanel.setMaxWidth(178);

        body.getChildren().addAll(libScroll, settingsPanel);
        page.getChildren().addAll(header, body);
        root.getChildren().add(0, page);

        saveRoutineOverlay = createSaveRoutineOverlay();
        root.getChildren().add(saveRoutineOverlay);

        Scene scene = new Scene(root, 390, 844);
        stage.setScene(scene);
        stage.setMaximized(false);
        stage.show();
    }

    private StackPane buildHeader(StackPane root) {
        StackPane header = new StackPane();
        header.setPrefSize(390, 150);
        header.setMinSize(390, 150);
        header.setMaxSize(390, 150);
        header.setBackground(new Background(new BackgroundFill(
                PRIMARY_BLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        VBox titles = new VBox(4);
        titles.setAlignment(Pos.CENTER);
        titles.setPadding(new Insets(0, 0, 35, 0));

        Label appTitle = new Label("FitFlow");
        appTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: white;");
        Label screenSubtitle = new Label("Routine Builder");
        screenSubtitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: white;");
        titles.getChildren().addAll(appTitle, screenSubtitle);

        HBox tabRow = new HBox();
        tabRow.setPrefHeight(40);
        tabRow.setAlignment(Pos.BOTTOM_LEFT);
        tabRow.setPadding(new Insets(0, 0, 0, 20));
        tabRow.getChildren().addAll(buildTab("Add Exercise", true),
                                    buildTab("Routine Settings", false));

        Button hamburger = createHeaderHamburgerButton(root, 150);

        header.getChildren().addAll(titles, tabRow, hamburger);
        StackPane.setAlignment(titles,    Pos.CENTER);
        StackPane.setAlignment(tabRow,    Pos.BOTTOM_LEFT);
        StackPane.setAlignment(hamburger, Pos.TOP_RIGHT);
        StackPane.setMargin(hamburger, new Insets(8, 8, 0, 0));

        return header;
    }

    private Label buildTab(String text, boolean active) {
        Label tab = new Label(text);
        tab.setPrefWidth(150);
        tab.setPrefHeight(40);
        tab.setAlignment(Pos.BOTTOM_CENTER);
        tab.setPadding(new Insets(0, 0, 4, 0));
        if (active) {
            tab.setStyle("-fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold;" +
                         " -fx-border-color: transparent transparent white transparent;" +
                         " -fx-border-width: 0 0 2 0;");
        } else {
            tab.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 13;");
        }
        return tab;
    }

    private VBox buildExerciseLibrary() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(4, 4, 4, 0));

        Label title = new Label("Exercises");
        title.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #888;");
        box.getChildren().add(title);

        for (String exercise : stateManager.getExercises()) {
            box.getChildren().add(buildExerciseCard(exercise));
        }
        return box;
    }

    private HBox buildExerciseCard(String exerciseName) {
        HBox card = new HBox(6);
        card.setPrefHeight(48);
        card.setMaxHeight(48);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(4, 6, 4, 6));
        card.setBackground(new Background(new BackgroundFill(
                CARD_GRAY, new CornerRadii(6), Insets.EMPTY)));

        ImageView img = loadIcon(resolveImagePath(exerciseName), 42);

        Label name = new Label(exerciseName);
        name.setStyle("-fx-font-size: 9;");
        name.setPrefWidth(70);
        name.setWrapText(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button plusBtn = new Button("+");
        plusBtn.setStyle("-fx-background-color: #2F66B3; -fx-text-fill: white;" +
                         " -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold;");
        plusBtn.setPrefSize(22, 22);
        plusBtn.setOnAction(e -> addExerciseToRoutine(exerciseName));

        card.getChildren().addAll(img, name, spacer, plusBtn);
        return card;
    }

    private VBox buildSettingsPanel(Stage stage) {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(4, 0, 4, 4));

        Label title = new Label("Routine");
        title.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #888;");

        routinePanel = new VBox(6);
        routinePanel.setPadding(new Insets(4, 0, 0, 0));
        ScrollPane routineScroll = new ScrollPane(routinePanel);
        routineScroll.setFitToWidth(true);
        routineScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        routineScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(routineScroll, Priority.ALWAYS);

        Button saveBtn  = new Button("Save");
        Button startBtn = new Button("Start");
        saveBtn.setStyle("-fx-background-color: #2F66B3; -fx-text-fill: white;" +
                         " -fx-font-size: 11; -fx-font-weight: bold;" +
                         " -fx-background-radius: 5; -fx-cursor: hand;");
        startBtn.setStyle("-fx-background-color: #1EA43A; -fx-text-fill: white;" +
                          " -fx-font-size: 11; -fx-font-weight: bold;" +
                          " -fx-background-radius: 5; -fx-cursor: hand;");
        saveBtn.setPrefHeight(30);
        startBtn.setPrefHeight(30);
        HBox.setHgrow(saveBtn,  Priority.ALWAYS);
        HBox.setHgrow(startBtn, Priority.ALWAYS);

        saveBtn.setOnAction(e -> {
            routineNameField.clear();
            routineNameMessageLabel.setText("");
            saveRoutineOverlay.setVisible(true);
        });
        startBtn.setOnAction(e -> startWorkout(stage));

        HBox bottomRow = new HBox(6, saveBtn, startBtn);
        bottomRow.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(title, routineScroll, bottomRow);
        return panel;
    }

    private void addExerciseToRoutine(String exerciseName) {
        RoutineExerciseSelection selection =
                new RoutineExerciseSelection(exerciseName, 3, 10, 60, 30);
        routinePanel.getChildren().add(buildRoutineCard(exerciseName, selection));
    }

    private VBox buildRoutineCard(String exerciseName, RoutineExerciseSelection selection) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(6));
        card.setBackground(new Background(new BackgroundFill(
                CARD_GRAY, new CornerRadii(6), Insets.EMPTY)));
        card.setStyle("-fx-border-color: #2F66B3; -fx-border-radius: 6; -fx-border-width: 1;");
        card.setUserData(selection);

        Label nameLabel = new Label(exerciseName);
        nameLabel.setStyle("-fx-font-size: 10; -fx-font-weight: bold;");

        Button deleteBtn = new Button("✕");
        deleteBtn.setStyle("-fx-background-color: #A81805; -fx-text-fill: white;" +
                           " -fx-font-size: 9; -fx-background-radius: 3; -fx-cursor: hand;");
        deleteBtn.setPrefSize(18, 18);
        deleteBtn.setOnAction(e -> routinePanel.getChildren().remove(card));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topRow = new HBox(4, nameLabel, spacer, deleteBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);

        HBox controls = new HBox(4);
        controls.getChildren().addAll(
                buildSettingControl("Sets", selection.getSets(),        selection),
                buildSettingControl("Reps", selection.getReps(),        selection),
                buildSettingControl("Rest", selection.getRestSeconds(), selection));

        card.getChildren().addAll(topRow, controls);
        return card;
    }

    private VBox buildSettingControl(String labelText, int initial,
                                     RoutineExerciseSelection selection) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 8; -fx-text-fill: #666;");

        Label value = new Label(String.valueOf(initial));
        value.setStyle("-fx-font-size: 10; -fx-font-weight: bold;");

        Button minus = new Button("−");
        Button plus  = new Button("+");
        String btnStyle = "-fx-background-color: #2F66B3; -fx-text-fill: white;" +
                          " -fx-font-size: 9; -fx-background-radius: 3; -fx-cursor: hand;" +
                          " -fx-min-width: 16; -fx-min-height: 16;";
        minus.setStyle(btnStyle);
        plus.setStyle(btnStyle);

        minus.setOnAction(e -> {
            int v = Integer.parseInt(value.getText());
            if (v > 0) {
                value.setText(String.valueOf(v - 1));
                updateSelectionValue(selection, labelText, v - 1);
            }
        });
        plus.setOnAction(e -> {
            int v = Integer.parseInt(value.getText()) + 1;
            value.setText(String.valueOf(v));
            updateSelectionValue(selection, labelText, v);
        });

        HBox row = new HBox(2, minus, value, plus);
        row.setAlignment(Pos.CENTER);
        row.setBackground(new Background(new BackgroundFill(
                Color.web("#D8E8FF"), new CornerRadii(4), Insets.EMPTY)));
        row.setPadding(new Insets(2));

        VBox box = new VBox(2, label, row);
        box.setAlignment(Pos.CENTER);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private void updateSelectionValue(RoutineExerciseSelection sel, String field, int val) {
        if (sel == null || field == null) return;
        switch (field) {
            case "Sets": sel.setSets(val);        break;
            case "Reps": sel.setReps(val);        break;
            case "Rest": sel.setRestSeconds(val); break;
        }
    }

    private List<RoutineExerciseSelection> getSelectedRoutineSelections() {
        List<RoutineExerciseSelection> list = new ArrayList<>();
        for (Node node : routinePanel.getChildren()) {
            Object ud = node.getUserData();
            if (ud instanceof RoutineExerciseSelection)
                list.add((RoutineExerciseSelection) ud);
        }
        return list;
    }

    private void startWorkout(Stage stage) {
        List<RoutineExerciseSelection> selected = getSelectedRoutineSelections();
        ServiceResponse<?> response = stateManager.startGuidedWorkoutWithDetails(
                "Guided Workout", selected);
        if (!response.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Cannot Start Workout");
            alert.setContentText(response.getMessage());
            alert.showAndWait();
            return;
        }
        stateManager.showGuidedWorkoutScreen();
    }

    private StackPane createSaveRoutineOverlay() {
        StackPane overlay = createOverlay();

        StackPane card = createCard(300, 220);
        VBox content = createCardContent();

        Label overlayTitle = new Label("Save Routine");
        overlayTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        routineNameField = new TextField();
        routineNameField.setPromptText("Routine Name");
        routineNameField.setMaxWidth(230);

        routineNameMessageLabel = new Label();
        routineNameMessageLabel.setWrapText(true);

        Button saveBtn  = new Button("Save");
        Button closeBtn = new Button("Close");

        saveBtn.setStyle("-fx-background-color: #2F66B3; -fx-text-fill: white;" +
                         " -fx-background-radius: 6; -fx-cursor: hand;");
        closeBtn.setStyle("-fx-background-color: #888; -fx-text-fill: white;" +
                          " -fx-background-radius: 6; -fx-cursor: hand;");

        saveBtn.setOnAction(e -> {
            String name = routineNameField.getText().trim();
            if (name.isEmpty()) {
                routineNameMessageLabel.setText("Please enter a routine name.");
                routineNameMessageLabel.setTextFill(Color.RED);
                return;
            }
            ServiceResponse<Boolean> response = stateManager.saveRoutineWithDetails(
                    name, getSelectedRoutineSelections());
            routineNameMessageLabel.setText(response.getMessage());
            routineNameMessageLabel.setTextFill(response.isSuccess() ? Color.GREEN : Color.RED);
        });

        closeBtn.setOnAction(e -> {
            routineNameField.clear();
            routineNameMessageLabel.setText("");
            overlay.setVisible(false);
        });

        HBox btnRow = new HBox(10, saveBtn, closeBtn);
        btnRow.setAlignment(Pos.CENTER);

        content.getChildren().addAll(overlayTitle,
                new Label("Enter a name:"),
                routineNameField, routineNameMessageLabel, btnRow);
        card.getChildren().add(content);
        overlay.getChildren().add(card);
        return overlay;
    }

    private ImageView loadIcon(String path, int size) {
        try {
            ImageView iv = new ImageView(
                    new Image(getClass().getResourceAsStream(path)));
            iv.setFitWidth(size);
            iv.setFitHeight(size);
            iv.setPreserveRatio(true);
            return iv;
        } catch (Exception e) {
            return new ImageView();
        }
    }

    private String resolveImagePath(String name) {
        switch (name) {
            case "Push-ups":       return IMAGES + "/pushup.png";
            case "Plank":          return IMAGES + "/pushup.png";
            case "Sit-ups":        return IMAGES + "/situp.png";
            case "Squats":         return IMAGES + "/squat.png";
            case "Dumbbell Curls": return IMAGES + "/dumbbell_curl.png";
            default:               return IMAGES + "/snail.jpg";
        }
    }
}
