/*
 * File: WorkoutHistory.java
 * Author: Michael Lee
 * Course: CMSC 495
 * Project: FitFlow
 * Date: June 2026
 * Version: 1.1
 *
 * Description:
 * This file stores one completed workout record for FitFlow.
 */

package model;

public class WorkoutHistory {

    // Basic workout history details
    private String historyId;
    private String userId;
    private String routineName;
    private String completedDate;
    private int duration;
    private double estimatedCalories;

    /*
     * Creates a completed workout history record.
     */
    public WorkoutHistory(String historyId, String userId, String routineName,
                          String completedDate, int duration, double estimatedCalories) {
        this.historyId = historyId;
        this.userId = userId;
        this.routineName = routineName;
        this.completedDate = completedDate;
        this.duration = duration;
        this.estimatedCalories = estimatedCalories;
    }

    /*
     * Gets the history ID.
     */
    public String getHistoryId() {
        return historyId;
    }

    /*
     * Gets the user ID.
     */
    public String getUserId() {
        return userId;
    }

    /*
     * Gets the completed routine name.
     */
    public String getRoutineName() {
        return routineName;
    }

    /*
     * Gets the completed date.
     */
    public String getCompletedDate() {
        return completedDate;
    }

    /*
     * Gets the workout duration.
     */
    public int getDuration() {
        return duration;
    }

    /*
     * Gets the estimated calories burned.
     */
    public double getEstimatedCalories() {
        return estimatedCalories;
    }

    /*
     * Turns the workout history into one CSV row.
     */
    public String toCSV() {
        return historyId + "," + userId + "," + routineName + "," + completedDate + ","
                + duration + "," + estimatedCalories;
    }

    /*
     * Shows the workout history in a simple readable way.
     */
    @Override
    public String toString() {
        return "Workout History" +
                "\nRoutine: " + routineName +
                "\nDate Completed: " + completedDate +
                "\nDuration: " + duration + " seconds" +
                "\nEstimated Calories: " + estimatedCalories;
    }
}
