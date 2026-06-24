/*
 * File: WorkoutSession.java
 * Editor: Travisha Mills
 * Updated: David Lewis 
 * Project: FitFlow
 * Date: June 22, 2026
 * Version: 6.1
 * Description:
 * Represents an active guided workout session and tracks timer state.
 */

package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WorkoutSession {

    private String sessionId;
    private String routineName;
    private List<String> exercises;
    private List<RoutineExerciseSelection> routineExercises;

    private int currentExerciseIndex;
    private int exerciseDurationSeconds;
    private int restDurationSeconds;
    private int remainingSeconds;
    private int totalElapsedSeconds;

    private boolean active;
    private boolean paused;
    private boolean resting;
    private boolean completed;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public WorkoutSession(String routineName, List<String> exercises,
                          int exerciseDurationSeconds, int restDurationSeconds) {

        this(
                routineName,
                buildDefaultSelections(exercises, exerciseDurationSeconds, restDurationSeconds),
                exerciseDurationSeconds,
                restDurationSeconds,
                true
        );
    }

    /*
     * Creates a guided workout session using the detailed routine settings
     * selected in RoutineBuilderScreen. This constructor preserves the user's
     * chosen sets, reps, and rest values instead of rebuilding each exercise
     * with defaults when the guided workout opens.
     */
    public WorkoutSession(String routineName,
                          List<RoutineExerciseSelection> routineExercises,
                          int exerciseDurationSeconds,
                          int restDurationSeconds,
                          boolean detailedSelections) {

        this.sessionId = "SESSION-" + UUID.randomUUID();
        this.routineName = routineName;
        this.routineExercises = new ArrayList<RoutineExerciseSelection>();
        this.exercises = new ArrayList<String>();

        if (routineExercises != null) {
            for (RoutineExerciseSelection selection : routineExercises) {
                if (selection != null && selection.isValidSelection()) {
                    this.routineExercises.add(selection);
                    this.exercises.add(selection.getExerciseName());
                }
            }
        }

        this.currentExerciseIndex = 0;
        this.exerciseDurationSeconds = exerciseDurationSeconds;
        this.restDurationSeconds = restDurationSeconds;
        this.remainingSeconds = exerciseDurationSeconds;
        this.totalElapsedSeconds = 0;

        this.active = true;
        this.paused = false;
        this.resting = false;
        this.completed = false;

        this.startedAt = LocalDateTime.now();
    }

    /*
     * Converts the older exercise-name-only start path into detailed selections.
     * This keeps existing tests and service calls working while allowing the new
     * Routine Builder path to pass real per-exercise settings.
     */
    private static List<RoutineExerciseSelection> buildDefaultSelections(List<String> exercises,
                                                                         int exerciseDurationSeconds,
                                                                         int restDurationSeconds) {
        List<RoutineExerciseSelection> selections = new ArrayList<RoutineExerciseSelection>();

        if (exercises == null) {
            return selections;
        }

        for (String exercise : exercises) {
            selections.add(new RoutineExerciseSelection(
                    exercise,
                    3,
                    10,
                    exerciseDurationSeconds,
                    restDurationSeconds
            ));
        }

        return selections;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getRoutineName() {
        return routineName;
    }

    public List<String> getExercises() {
        return exercises;
    }

    public List<RoutineExerciseSelection> getRoutineExercises() {
        return routineExercises;
    }

    public String getCurrentExercise() {
        // Returns the current exercise name or blank if the list is invalid.
        if (exercises == null || exercises.isEmpty() || currentExerciseIndex >= exercises.size()) {
            return "";
        }

        return exercises.get(currentExerciseIndex);
    }

    public int getCurrentExerciseIndex() {
        return currentExerciseIndex;
    }

    public int getExerciseDurationSeconds() {
        return exerciseDurationSeconds;
    }

    public int getRestDurationSeconds() {
        return restDurationSeconds;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public int getTotalElapsedSeconds() {
        return totalElapsedSeconds;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isResting() {
        return resting;
    }

    public boolean isCompleted() {
        return completed;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void pause() {
        // Pauses the workout only if it is active.
        if (active && !completed) {
            paused = true;
        }
    }

    public void resume() {
        // Resumes the workout only if it is active.
        if (active && !completed) {
            paused = false;
        }
    }

    public void reset() {
        // Resets the workout back to the first exercise.
        currentExerciseIndex = 0;
        remainingSeconds = exerciseDurationSeconds;
        totalElapsedSeconds = 0;
        active = true;
        paused = false;
        resting = false;
        completed = false;
        completedAt = null;
    }

    public void tick() {
        // Decreases the timer by one second when the workout is running.
        if (!active || paused || completed) {
            return;
        }

        if (remainingSeconds > 0) {
            remainingSeconds--;
            totalElapsedSeconds++;
        }

        if (remainingSeconds == 0) {
            moveToNextState();
        }
    }

    public void skip() {
        // Skips the current exercise or rest period.
        if (!active || completed) {
            return;
        }

        moveToNextExercise();
    }

    private void moveToNextState() {
        // Starts rest if available, otherwise moves to the next exercise.
        if (!resting && restDurationSeconds > 0 && currentExerciseIndex < exercises.size() - 1) {
            resting = true;
            remainingSeconds = restDurationSeconds;
        } else {
            moveToNextExercise();
        }
    }

    private void moveToNextExercise() {
        // Moves to the next exercise or completes the workout.
        resting = false;

        if (currentExerciseIndex < exercises.size() - 1) {
            currentExerciseIndex++;
            remainingSeconds = exerciseDurationSeconds;
        } else {
            complete();
        }
    }

    private void complete() {
        // Marks the workout as completed.
        active = false;
        paused = false;
        resting = false;
        completed = true;
        remainingSeconds = 0;
        completedAt = LocalDateTime.now();
    }

    public String getStatusText() {
        // Returns a short label for the current workout state.
        if (completed) {
            return "Workout Complete";
        }

        if (paused) {
            return "Paused";
        }

        if (resting) {
            return "Rest";
        }

        return "Exercise";
    }

    public String getCompletionSummary() {
        // Creates a short summary for workout history.
        return routineName + " completed with " + exercises.size()
                + " exercises in " + totalElapsedSeconds + " seconds.";
    }
}
