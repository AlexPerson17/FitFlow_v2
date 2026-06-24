/*
 * File: WorkoutRoutine.java
 * Author: Michael Lee
 * Course: CMSC 495
 * Project: FitFlow
 * Date: June 2026
 * Version: 1.1
 *
 * Description:
 * This file stores workout routine information for FitFlow.
 */

package model;

public class WorkoutRoutine {

    // Basic workout routine details
    private String routineId;
    private String userId;
    private String routineName;
    private String exerciseName;
    private int sets;
    private int reps;
    private int duration;
    private int restTime;

    /*
     * Creates a workout routine with the main routine details.
     */
    public WorkoutRoutine(String routineId, String userId, String routineName, String exerciseName,
                          int sets, int reps, int duration, int restTime) {
        this.routineId = routineId;
        this.userId = userId;
        this.routineName = routineName;
        this.exerciseName = exerciseName;
        this.sets = sets;
        this.reps = reps;
        this.duration = duration;
        this.restTime = restTime;
    }

    /*
     * Gets the routine ID.
     */
    public String getRoutineId() {
        return routineId;
    }

    /*
     * Gets the user ID.
     */
    public String getUserId() {
        return userId;
    }

    /*
     * Gets the routine name.
     */
    public String getRoutineName() {
        return routineName;
    }

    /*
     * Gets the exercise name.
     */
    public String getExerciseName() {
        return exerciseName;
    }

    /*
     * Gets the number of sets.
     */
    public int getSets() {
        return sets;
    }

    /*
     * Gets the number of reps.
     */
    public int getReps() {
        return reps;
    }

    /*
     * Gets the exercise duration.
     */
    public int getDuration() {
        return duration;
    }

    /*
     * Gets the rest time.
     */
    public int getRestTime() {
        return restTime;
    }

    /*
     * Calculates the full routine time.
     */
    public int getTotalTime() {
        return (duration + restTime) * sets;
    }

    /*
     * Checks that the routine has the basic needed information.
     */
    public boolean isValidRoutine() {
        return routineName != null && !routineName.isEmpty()
                && exerciseName != null && !exerciseName.isEmpty()
                && sets > 0
                && reps >= 0
                && duration > 0
                && restTime >= 0;
    }

    /*
     * Turns the routine into one CSV row.
     */
    public String toCSV() {
        return routineId + "," + userId + "," + routineName + "," + exerciseName + ","
                + sets + "," + reps + "," + duration + "," + restTime;
    }

    /*
     * Shows the routine in a simple readable way.
     */
    @Override
    public String toString() {
        return "Routine: " + routineName +
                "\nExercise: " + exerciseName +
                "\nSets: " + sets +
                "\nReps: " + reps +
                "\nDuration: " + duration + " seconds" +
                "\nRest Time: " + restTime + " seconds";
    }
}
