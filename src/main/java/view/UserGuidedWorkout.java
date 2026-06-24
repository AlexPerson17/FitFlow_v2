/*
 * File: UserGuidedWorkout.java
 * Original Author: Orange Snaer
 * Version: 7.0
 * Adapted by: Alex Ronn
 * Updated by: David Lewis
 * Date last edited: 6/24/2026
 * File Purpose: Plays the workout exercises chosen by a user. The user can
 *      navigate through the workout using play, pause, and next buttons.
 *      Exercise data is pulled from AppStateManager via the active
 *      WorkoutSession so this screen matches the rest of the project architecture.
 *      Layout: 390x844 mobile-first blueprint (VBox, no BorderPane).
 */

package view;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.RoutineExerciseSelection;
import model.WorkoutSession;
import service.ServiceResponse;

public class UserGuidedWorkout extends BaseScreen {

    /*
     * Lightweight exercise data holder used by this screen.
     *
     * WorkoutSession now stores the detailed Routine Builder selections. This
     * inner class adds image paths for the UI while preserving the selected
     * sets, reps, work seconds, and rest seconds from the builder screen.
     */
    public static class ExerciseData {
        public final String name;
        public final String imagePath;
        public final int sets;
        public final int reps;
        public final int workSeconds;
        public final int restSeconds;

        public ExerciseData(String name, String imagePath,
                            int sets, int reps,
                            int workSeconds, int restSeconds) {
            this.name        = name;
            this.imagePath   = imagePath;
            this.sets        = sets;
            this.reps        = reps;
            this.workSeconds = workSeconds;
            this.restSeconds = restSeconds;
        }
    }

    // UI fields — names must not change: WorkoutTimer accesses these directly.

    public WorkoutTimer    timerManager;
    public ImageView       exerciseImageMain;
    public ProgressBar     progressBar;
    public Label           statusLabel;
    public Label           timerLabel;
    public Label           restLabel;
    public VBox            playingNowCard;
    public VBox            upcomingContainer;
    public Button          playStopButton;

    // Exercise name displayed in the top section.
    private Label exerciseNameLabel;

    public static final int EXERCISE_REST = 60;
    public int     currentSet      = 1;
    public int     currentExercise = 0;
    public boolean workoutRunning  = false;

    // Populated from the active WorkoutSession in show().
    public ExerciseData[] exercises;

    // Stores the routine name from the current session for history records.
    private String activeRoutineName = "Guided Workout";

    // Prevents duplicate history rows if the completion UI is refreshed.
    private boolean historySaved = false;

    // Constructor

    public UserGuidedWorkout(AppStateManager stateManager) {
        super(stateManager);
    }

    // Entry point — called by AppStateManager.showGuidedWorkoutScreen()

    public void show(Stage stage) {

        exercises    = loadExercisesFromSession();
        historySaved = false;

        // If there are no exercises the session is invalid; fall back to
        // dashboard so the user sees a clear error rather than a broken screen.
        if (exercises.length == 0) {
            stateManager.showDashboardScreen();
            return;
        }

        timerManager = new WorkoutTimer(this);

        // ── Root: 390 × 844 VBox ─────────────────────────────────────────────
        VBox root = new VBox();
        root.setPrefSize(390, 844);
        root.setMinSize(390, 844);
        root.setMaxSize(390, 844);
        root.setAlignment(Pos.TOP_CENTER);

        // ── Top section: 490 px, PRIMARY_BLUE ────────────────────────────────
        VBox topSection = buildTopSection();

        // ── Bottom section: 354 px, slightly darker blue ─────────────────────
        VBox bottomSection = buildBottomSection(stage);

        root.getChildren().addAll(topSection, bottomSection);

        // Wrap in StackPane so addNavigationMenu can overlay the hamburger.
        StackPane stackRoot = new StackPane(root);
        addNavigationMenu(stackRoot);

        // Side-panel fields are kept alive (no-op containers) so any remaining
        // references to playingNowCard / upcomingContainer do not NPE.
        playingNowCard     = new VBox();
        upcomingContainer  = new VBox();

        loadNextExerciseImage();
        updateCurrentExercise();

        Scene scene = new Scene(stackRoot, 390, 844);

        stage.setTitle("FitFlow Guided Workout");
        stage.setScene(scene);
        stage.show();
    }

    // ── Top section builder ────────────────────────────────────────────────────

    /*
     * Top section (490 px): a 140 px white header band showing "WORK",
     * then the remaining 350 px for the exercise image + name on a blue field.
     */
    private VBox buildTopSection() {

        VBox topSection = new VBox();
        topSection.setPrefHeight(490);
        topSection.setMinHeight(490);
        topSection.setMaxHeight(490);
        topSection.setAlignment(Pos.TOP_CENTER);
        topSection.setBackground(new Background(new BackgroundFill(
                PRIMARY_BLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        // ── 140 px white header band ──────────────────────────────────────────
        VBox headerBand = new VBox();
        headerBand.setPrefHeight(140);
        headerBand.setMinHeight(140);
        headerBand.setMaxHeight(140);
        headerBand.setAlignment(Pos.CENTER);
        headerBand.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        // restLabel doubles as the phase header ("WORK" / "REST" / "WORKOUT COMPLETE")
        // WorkoutTimer sets restLabel.setText() — keep it here in the header.
        restLabel = new Label("WORK");
        restLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        restLabel.setTextFill(Color.web("#2962BD"));
        restLabel.setAlignment(Pos.CENTER);

        // statusLabel sits below the phase word (save confirmations, errors, etc.)
        statusLabel = new Label();
        statusLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        statusLabel.setTextFill(Color.web("#555555"));
        statusLabel.setAlignment(Pos.CENTER);

        headerBand.getChildren().addAll(restLabel, statusLabel);

        // ── Image + exercise name (remaining ~350 px of top section) ──────────
        VBox imageArea = new VBox(8);
        imageArea.setAlignment(Pos.CENTER);
        VBox.setVgrow(imageArea, Priority.ALWAYS);

        exerciseImageMain = new ImageView();
        exerciseImageMain.setFitWidth(300);
        exerciseImageMain.setFitHeight(260);
        exerciseImageMain.setPreserveRatio(true);

        exerciseNameLabel = new Label();
        exerciseNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        exerciseNameLabel.setTextFill(Color.WHITE);
        exerciseNameLabel.setAlignment(Pos.CENTER);

        imageArea.getChildren().addAll(exerciseImageMain, exerciseNameLabel);

        topSection.getChildren().addAll(headerBand, imageArea);
        return topSection;
    }

    // ── Bottom section builder ─────────────────────────────────────────────────

    /*
     * Bottom section (354 px): countdown timer, elapsed/progress row, controls.
     */
    private VBox buildBottomSection(Stage stage) {

        VBox bottomSection = new VBox(14);
        bottomSection.setPrefHeight(354);
        bottomSection.setMinHeight(354);
        bottomSection.setMaxHeight(354);
        bottomSection.setAlignment(Pos.CENTER);
        bottomSection.setPadding(new Insets(20, 20, 20, 20));
        bottomSection.setBackground(new Background(new BackgroundFill(
                Color.web("#2F66B3"), CornerRadii.EMPTY, Insets.EMPTY)));

        // ── Large countdown timer label (54 px) ───────────────────────────────
        timerLabel = new Label("00:00");
        timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 54));
        timerLabel.setTextFill(Color.WHITE);
        timerLabel.setAlignment(Pos.CENTER);

        // ── Progress bar (elapsed visual) ─────────────────────────────────────
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(330);
        progressBar.setPrefHeight(10);

        // ── Controls: Cancel | Play/Pause | Next ──────────────────────────────
        HBox controls = buildControls(stage);

        bottomSection.getChildren().addAll(timerLabel, progressBar, controls);
        return bottomSection;
    }

    // ── Controls row ──────────────────────────────────────────────────────────

    private HBox buildControls(Stage stage) {

        HBox controls = new HBox(20);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(10, 0, 0, 0));

        // Cancel button navigates back to dashboard.
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefSize(80, 44);
        cancelButton.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        cancelButton.setStyle(
                "-fx-background-color: #E53935; -fx-text-fill: white;" +
                "-fx-background-radius: 8;");
        cancelButton.setOnAction(e -> {
            if (timerManager != null) {
                timerManager.stopWorkout();
            }
            stateManager.showDashboardScreen();
        });

        // Play / Pause button — WorkoutTimer updates its graphic directly.
        playStopButton = new Button();
        playStopButton.setGraphic(IconImage("/Icons/play.png"));
        playStopButton.setPrefSize(70, 70);
        playStopButton.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(50), Insets.EMPTY)));
        playStopButton.setOnAction(e -> {
            if (workoutRunning) {
                timerManager.stopWorkout();
                stateManager.pauseGuidedWorkout();
            } else {
                timerManager.startWorkout();
                stateManager.resumeGuidedWorkout();
            }
        });

        // Next button skips to the next exercise via the backend.
        Button nextButton = new Button("Next");
        nextButton.setPrefSize(80, 44);
        nextButton.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        nextButton.setStyle(
                "-fx-background-color: white; -fx-text-fill: #2F66B3;" +
                "-fx-background-radius: 8;");
        nextButton.setOnAction(e -> {
            if (currentExercise < exercises.length - 1) {
                stateManager.skipGuidedWorkoutStep();
                currentExercise++;
                updateCurrentExercise();
            }
        });

        controls.getChildren().addAll(cancelButton, playStopButton, nextButton);
        return controls;
    }

    // Session data loading

    /*
     * Builds the ExerciseData array from the active WorkoutSession.
     *
     * The session holds the detailed exercise settings passed from
     * RoutineBuilderScreen. This method converts those model objects into
     * ExerciseData objects and adds the correct image paths for the UI. This is
     * what keeps changed sets, reps, and rest values from resetting to defaults.
     */
    private ExerciseData[] loadExercisesFromSession() {

        ServiceResponse<WorkoutSession> response =
                stateManager.getCurrentWorkoutSession();

        if (!response.isSuccess() || response.getData() == null) {
            return new ExerciseData[0];
        }

        WorkoutSession session = response.getData();
        activeRoutineName = session.getRoutineName();
        List<RoutineExerciseSelection> selections = session.getRoutineExercises();

        if (selections == null || selections.isEmpty()) {
            return new ExerciseData[0];
        }

        List<ExerciseData> list = new ArrayList<>();

        for (RoutineExerciseSelection selection : selections) {
            String name = selection.getExerciseName();
            list.add(new ExerciseData(
                    name,
                    resolveImagePath(name),
                    selection.getSets(),
                    selection.getReps(),
                    selection.getWorkSeconds(),
                    selection.getRestSeconds()
            ));
        }

        return list.toArray(new ExerciseData[0]);
    }

    // Returns the resource image path for a given exercise name.
    private String resolveImagePath(String exerciseName) {
        switch (exerciseName) {
            case "Push-ups":       return "/Images/pushup.png";
            case "Plank":          return "/Images/pushup.png";
            case "Sit-ups":        return "/Images/situp.png";
            case "Squats":         return "/Images/squat.png";
            case "Dumbbell Curls": return "/Images/dumbbell_curl.png";
            default:               return "/Images/snail.jpg";
        }
    }

    // Returns the resource gif path for a given exercise name.
    private String resolveGifPath(String exerciseName) {
        switch (exerciseName) {
            case "Push-ups":       return "/Images/pushup.gif";
            case "Plank":          return "/Images/pushup.png";
            case "Sit-ups":        return "/Images/situp.gif";
            case "Squats":         return "/Images/squat.gif";
            case "Dumbbell Curls": return "/Images/dumbbell_curl.gif";
            default:               return "/Images/snail.jpg";
        }
    }

    // Returns the workout category for a given exercise name.
    private String workoutCategory(String exerciseName) {
        switch (exerciseName) {
            case "Push-ups":       return "Strength";
            case "Plank":          return "Core";
            case "Sit-ups":        return "Core";
            case "Squats":         return "Legs";
            case "Dumbbell Curls": return "Arms";
            default:               return "Unknown";
        }
    }

    // Image loading

    // Loads the image for the current exercise into the main display area.
    public void loadNextExerciseImage() {
        try {
            String path = resolveGifPath(exercises[currentExercise].name);
            InputStream stream = getClass().getResourceAsStream(path);
            if (stream != null) {
                exerciseImageMain.setImage(new Image(stream));
            }
        } catch (Exception e) {
            System.out.println("Image failed to load: "
                    + exercises[currentExercise].name);
        }
    }

    // Creates an ImageView for a button icon.
    public ImageView IconImage(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) return new ImageView();
        ImageView icon = new ImageView(new Image(stream));
        icon.setFitWidth(35);
        icon.setFitHeight(35);
        icon.setPreserveRatio(true);
        return icon;
    }

    // State update helpers

    /*
     * Refreshes the "Playing Now" card.
     * In the new layout this is a no-op for the side panel (removed), but
     * WorkoutTimer calls workout.updatePlayingNow() so the method must exist.
     */
    public void updatePlayingNow() {
        // No side panel in the new layout; exercise name label is updated by
        // updateCurrentExercise() instead.
    }

    // Refreshes the queue view (no-op in the new layout; kept for compatibility).
    public void updateQueue() {
        // No side panel in the new layout.
    }

    // Resets the REST label to its default "WORK" style.
    public void RestLabelStyle() {
        restLabel.setTextFill(Color.web("#2962BD"));
        restLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
    }

    /*
     * Resets all UI state for the newly selected exercise.
     * Called when the user presses next, and after each exercise
     * transition inside WorkoutTimer.
     */
    public void updateCurrentExercise() {
        currentSet     = 1;
        workoutRunning = false;

        ExerciseData exercise = exercises[currentExercise];

        timerManager.reset();

        timerLabel.setText(String.format(
                "%02d:%02d",
                exercise.workSeconds / 60,
                exercise.workSeconds % 60));

        progressBar.setProgress(0);

        restLabel.setVisible(true);
        restLabel.setText("WORK");
        RestLabelStyle();

        if (exerciseNameLabel != null) {
            exerciseNameLabel.setText(exercise.name
                    + "  •  " + workoutCategory(exercise.name)
                    + "   Set " + currentSet + " / " + exercise.sets);
        }

        loadNextExerciseImage();

        playStopButton.setGraphic(IconImage("/Icons/play.png"));
    }

    /*
     * Saves the workout after the visible guided workout timer completes.
     *
     * This is needed because the JavaFX timer controls the on-screen workout
     * flow. The guard flag prevents repeated completion events from adding
     * duplicates.
     */
    public void saveCompletedWorkoutToHistory() {
        if (historySaved) {
            return;
        }

        historySaved = true;

        int durationSeconds = calculatePlannedWorkoutDurationSeconds();
        String summary = buildWorkoutHistorySummary();

        ServiceResponse<Boolean> response =
                stateManager.saveCompletedWorkout(summary, durationSeconds);

        if (statusLabel != null) {
            if (response.isSuccess()) {
                statusLabel.setText("Workout saved to history.");
            } else {
                statusLabel.setText("Workout complete, but history was not saved: "
                        + response.getMessage());
            }
        }
    }

    /*
     * Calculates the planned workout time displayed by the guided workout UI.
     * This includes each exercise set, short rests between sets, and long rests
     * between different exercises.
     */
    public int calculatePlannedWorkoutDurationSeconds() {
        int total = 0;

        for (int i = 0; i < exercises.length; i++) {
            ExerciseData exercise = exercises[i];

            total += exercise.sets * exercise.workSeconds;

            if (exercise.sets > 1) {
                total += (exercise.sets - 1) * exercise.restSeconds;
            }

            if (i < exercises.length - 1) {
                total += EXERCISE_REST;
            }
        }

        return total;
    }

    /*
     * Builds a short readable summary for the workout history CSV/UI row.
     */
    private String buildWorkoutHistorySummary() {
        return activeRoutineName + " completed with "
                + exercises.length + " exercise(s)";
    }

    /*
     * Refreshes the workout queue (kept for WorkoutTimer compatibility).
     */
    public void updateSetProgress() {
        // No side panel in the new layout.
    }

    // ── Legacy layout methods kept as stubs so any external callers compile ───

    /*
     * createWorkoutSection is no longer called by show() in the new layout but
     * is kept to avoid breaking any subclass or test that references it.
     */
    public VBox createWorkoutSection() {
        return new VBox();
    }

    /*
     * buildSidePanel is no longer called by show() in the new layout but
     * is kept to avoid breaking any subclass or test that references it.
     */
    public VBox buildSidePanel() {
        return new VBox();
    }

    /*
     * WorkoutImageCard is no longer called by show() in the new layout but
     * is kept so any callers outside this class continue to compile.
     */
    public VBox WorkoutImageCard(ExerciseData exercise,
                                  String category,
                                  String reps,
                                  String duration) {
        return new VBox();
    }
}
