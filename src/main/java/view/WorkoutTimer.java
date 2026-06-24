/*
 * File: WorkoutTimer.java
 * Original Author: Orange Snaer
 * Version: 6.4
 * Adapted by: Alex Ronn
 * Updated by: David Lewis 
 * Date last edited: 6/22/2026
 * File Purpose: Sets up timers for the guided workout screen so the user
 *      knows when to work and when to rest. Handles set transitions, rest
 *      periods, long breaks between exercises, and workout completion.
 */

package view;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class WorkoutTimer {

    private final UserGuidedWorkout workout;

    private enum TimerMode { TIME_DURATION, REST, LONG_BREAK }

    private int       remainingTime;
    private TimerMode currentMode;
    private Timeline  timeline;
    private Timeline  progressBarFlow;

    public WorkoutTimer(UserGuidedWorkout workout) {
        this.workout = workout;
    }

    // Public controls — called by UserGuidedWorkout button actions

    // Starts a new workout or resumes a paused one.
    public void startWorkout() {
        if (timeline != null && timeline.getStatus() == Animation.Status.PAUSED) {
            resumeWorkout();
            return;
        }
        startWorkoutSet();
    }

    // Pauses the current timer and updates the play button icon.
    public void stopWorkout() {
        workout.workoutRunning = false;

        if (timeline != null) {
            timeline.pause();
        }

        if (progressBarFlow != null) {
            progressBarFlow.pause();
        }

        workout.playStopButton.setGraphic(workout.IconImage("/Icons/play.png"));
    }

    // Resumes a paused timer and updates the play button icon.
    public void resumeWorkout() {
        workout.workoutRunning = true;

        if (timeline != null) {
            timeline.play();
        }

        if (progressBarFlow != null) {
            progressBarFlow.play();
        }

        workout.playStopButton.setGraphic(workout.IconImage("/Icons/pause.jpg"));
    }

    // Phase starters

    // Starts (or restarts) the work phase for the current exercise set.
    public void startWorkoutSet() {
        UserGuidedWorkout.ExerciseData exercise =
                workout.exercises[workout.currentExercise];

        workout.updatePlayingNow();
        workout.restLabel.setVisible(false);

        currentMode   = TimerMode.TIME_DURATION;
        remainingTime = exercise.workSeconds;

        startTimer(exercise.workSeconds);

        workout.workoutRunning = true;
        workout.playStopButton.setGraphic(workout.IconImage("/Icons/pause.jpg"));
    }

    // Starts the short rest period between sets of the same exercise.
    public void startRestPeriod() {
        UserGuidedWorkout.ExerciseData exercise =
                workout.exercises[workout.currentExercise];

        workout.restLabel.setVisible(true);
        workout.restLabel.setText("REST");

        currentMode   = TimerMode.REST;
        remainingTime = exercise.restSeconds;

        startTimer(exercise.restSeconds);
    }

    // Starts the long break between different exercises.
    public void startLongRest() {
        workout.restLabel.setVisible(true);
        workout.restLabel.setText("NEXT WORKOUT REST");

        currentMode   = TimerMode.LONG_BREAK;
        remainingTime = workout.EXERCISE_REST;

        startTimer(workout.EXERCISE_REST);
    }

    // Timer engine

    private void startTimer(int totalSeconds) {
        stopTimers();
        updateTimerLabel();

        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> timerCountdown()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        workout.progressBar.setProgress(0);

        progressBarFlow = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(workout.progressBar.progressProperty(), 0)),
                new KeyFrame(Duration.seconds(totalSeconds),
                        new KeyValue(workout.progressBar.progressProperty(), 1))
        );

        progressBarFlow.play();
    }

    private void updateTimerLabel() {
        workout.timerLabel.setText(String.format(
                "%02d:%02d",
                remainingTime / 60,
                remainingTime % 60));
    }

    // Ticks the countdown by one second and triggers the next phase when done.
    private void timerCountdown() {
        remainingTime--;
        updateTimerLabel();

        if (remainingTime > 0) {
            return;
        }

        stopTimers();

        switch (currentMode) {
            case TIME_DURATION: setDone();        break;
            case REST:          finishRest();      break;
            case LONG_BREAK:    finishLongBreak(); break;
        }
    }

    // Phase transitions

    // Called when a work set finishes. Starts rest, long break, or completes.
    private void setDone() {
        UserGuidedWorkout.ExerciseData ex =
                workout.exercises[workout.currentExercise];

        if (workout.currentSet < ex.sets) {
            startRestPeriod();
            return;
        }

        workout.currentSet = 1;

        if (workout.currentExercise < workout.exercises.length - 1) {
            startLongRest();
            return;
        }

        // All exercises finished — save history and navigate to congratulations.
        workout.workoutRunning = false;
        workout.playStopButton.setGraphic(workout.IconImage("/Icons/play.png"));

        // Persist the completed workout so WorkoutHistoryScreen can display it.
        workout.saveCompletedWorkoutToHistory();

        int duration      = workout.calculatePlannedWorkoutDurationSeconds();
        int exerciseCount = workout.exercises.length;
        workout.stateManager.showCongratulationsScreen(duration, exerciseCount);
    }

    // Called when a rest period finishes. Increments the set and restarts.
    private void finishRest() {
        workout.currentSet++;
        startWorkoutSet();

        // Reset and restart ensures the timer display is clean for the new set.
        workout.timerManager.stopWorkout();
        workout.timerManager.reset();
        workout.timerManager.startWorkoutSet();
    }

    // Called when the long break finishes. Advances to the next exercise.
    private void finishLongBreak() {
        workout.restLabel.setVisible(false);
        workout.currentExercise++;
        workout.currentSet = 1;

        if (workout.currentExercise < workout.exercises.length) {
            workout.updateCurrentExercise();
            startWorkoutSet();
            return;
        }

        workout.workoutRunning = false;
        stopTimers();
    }

    // Reset

    // Restores the timer to its default display state.
    public void reset() {
        stopTimers();

        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }

        if (progressBarFlow != null) {
            progressBarFlow.stop();
            progressBarFlow = null;
        }

        remainingTime = 0;
        currentMode   = null;

        workout.workoutRunning = false;
        workout.progressBar.setProgress(0);
        workout.timerLabel.setText("00:00");
        workout.restLabel.setVisible(false);

        workout.playStopButton.setGraphic(workout.IconImage("/Icons/play.png"));
    }

    private void stopTimers() {
        if (timeline != null) {
            timeline.stop();
        }

        if (progressBarFlow != null) {
            progressBarFlow.stop();
        }
    }
}
