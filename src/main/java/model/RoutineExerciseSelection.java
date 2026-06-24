/*
 * File: RoutineExerciseSelection.java
 * Project: FitFlow
 * Date: 6/22/2026
 * Version: 6.1
 * Author: David Lewis
 *
 * Description:
 * Stores the exercise settings selected in the Routine Builder UI.
 *
 * Holds the exercise name plus the user's selected sets, reps, work duration,
 * and rest time.
 *
 * RoutineBuilderScreen previously stored only the exercise name on each JavaFX
 * panel. When the guided workout opened, the app rebuilt every exercise with
 * default sets, reps, and rest values. This model gives the UI a clean object
 * to pass through AppStateManager, FitFlowFacade, TimerService, and
 * WorkoutSession so the user's selected values are preserved.
 *
 * RoutineBuilderScreen creates one RoutineExerciseSelection per added exercise.
 * The plus/minus controls update the object. The selected objects are then
 * passed into the guided workout session and repository save path.
 */

package model;

public class RoutineExerciseSelection {

    private String exerciseName;
    private int sets;
    private int reps;
    private int workSeconds;
    private int restSeconds;

    public RoutineExerciseSelection(String exerciseName,
                                    int sets,
                                    int reps,
                                    int workSeconds,
                                    int restSeconds) {
        this.exerciseName = exerciseName;
        this.sets = sets;
        this.reps = reps;
        this.workSeconds = workSeconds;
        this.restSeconds = restSeconds;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = Math.max(0, sets);
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = Math.max(0, reps);
    }

    public int getWorkSeconds() {
        return workSeconds;
    }

    public void setWorkSeconds(int workSeconds) {
        this.workSeconds = Math.max(1, workSeconds);
    }

    public int getRestSeconds() {
        return restSeconds;
    }

    public void setRestSeconds(int restSeconds) {
        this.restSeconds = Math.max(0, restSeconds);
    }

    public boolean isValidSelection() {
        return exerciseName != null
                && !exerciseName.trim().isEmpty()
                && sets > 0
                && reps >= 0
                && workSeconds > 0
                && restSeconds >= 0;
    }

    @Override
    public String toString() {
        return exerciseName
                + " (sets=" + sets
                + ", reps=" + reps
                + ", duration=" + workSeconds
                + ", rest=" + restSeconds + ")";
    }
}
