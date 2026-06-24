/*
 * File: CSVHelper.java
 * Author: Michael Lee
 * Course: CMSC 495
 * Project: FitFlow
 * Date: June 2026
 * Version: 1.1
 *
 * Description:
 * This file handles simple CSV saving for the backend.
 */

package repository;

import model.UserProfile;
import model.WorkoutRoutine;
import model.WorkoutHistory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CSVHelper {

    /*
     * Makes sure the data folder exists before saving files.
     */
    private static void makeSureDataFolderExists() {
        try {
            Files.createDirectories(Paths.get("data"));
        } catch (IOException e) {
            System.out.println("Could not create data folder: " + e.getMessage());
        }
    }

    /*
     * Saves one user profile to profiles.csv.
     */
    public static void saveProfile(UserProfile profile) {
        makeSureDataFolderExists();

        try {
            FileWriter writer = new FileWriter("data/profiles.csv", true);
            writer.write(profile.toCSV() + "\n");
            writer.close();

            System.out.println("Profile saved to CSV.");
        } catch (IOException e) {
            System.out.println("Error saving profile: " + e.getMessage());
        }
    }

    /*
     * Saves one workout routine to workout_routines.csv.
     */
    public static void saveWorkoutRoutine(WorkoutRoutine routine) {
        makeSureDataFolderExists();

        try {
            FileWriter writer = new FileWriter("data/workout_routines.csv", true);
            writer.write(routine.toCSV() + "\n");
            writer.close();

            System.out.println("Workout routine saved to CSV.");
        } catch (IOException e) {
            System.out.println("Error saving workout routine: " + e.getMessage());
        }
    }

    /*
     * Saves one completed workout to workout_history.csv.
     */
    public static void saveWorkoutHistory(WorkoutHistory history) {
        makeSureDataFolderExists();

        try {
            FileWriter writer = new FileWriter("data/workout_history.csv", true);
            writer.write(history.toCSV() + "\n");
            writer.close();

            System.out.println("Workout history saved to CSV.");
        } catch (IOException e) {
            System.out.println("Error saving workout history: " + e.getMessage());
        }
    }
}
