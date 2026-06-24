/*
 * File: Exercise.java
 * Author: Michael Lee
 * Course: CMSC 495
 * Project: FitFlow
 * Date: June 8, 2026
 * Version: 1.1
 * Description:
 * This file stores the information for one exercise in FitFlow.
 * It keeps track of the exercise ID, name, category, duration,
 * calories burned per minute, and the image/gif file connected to it.
 */

package model;

public class Exercise {

    // Unique ID for the exercise
    private String exerciseId;

    // Name of the exercise, like Push Ups or Squats
    private String name;

    // Category of the exercise, like Strength or Core
    private String category;

    // Default exercise duration in seconds
    private int duration;

    // Estimated calories burned per minute for this exercise
    private double caloriesPerMinute;

    // Image or gif file used for the exercise visual
    private String imageFile;

    /*
     * This constructor creates a new exercise.
     * It stores the exercise details so FitFlow can use them in workouts.
     */
    public Exercise(String exerciseId, String name, String category, int duration, double caloriesPerMinute, String imageFile) {
        this.exerciseId = exerciseId;
        this.name = name;
        this.category = category;
        this.duration = duration;
        this.caloriesPerMinute = caloriesPerMinute;
        this.imageFile = imageFile;
    }

    /*
     * Returns the exercise ID.
     */
    public String getExerciseId() {
        return exerciseId;
    }

    /*
     * Returns the exercise name.
     */
    public String getName() {
        return name;
    }

    /*
     * Returns the exercise category.
     */
    public String getCategory() {
        return category;
    }

    /*
     * Returns the exercise duration in seconds.
     */
    public int getDuration() {
        return duration;
    }

    /*
     * Returns the estimated calories burned per minute.
     */
    public double getCaloriesPerMinute() {
        return caloriesPerMinute;
    }

    /*
     * Returns the image or gif file name for this exercise.
     */
    public String getImageFile() {
        return imageFile;
    }

    /*
     * Estimates calories burned for this exercise.
     * This is just a basic estimate using duration and calories per minute.
     */
    public double estimateCalories() {
        return (duration / 60.0) * caloriesPerMinute;
    }

    /*
     * Converts the exercise data into CSV format.
     * This makes it easier to save or organize exercise data in exercises.csv.
     */
    public String toCSV() {
        return exerciseId + "," + name + "," + category + "," + duration + "," + caloriesPerMinute + "," + imageFile;
    }

    /*
     * Returns the exercise information in a simple readable format.
     * This is mostly used for printing test results in the console.
     */
    @Override
    public String toString() {
        return name + " (" + category + ") - " + duration + " seconds";
    }
}