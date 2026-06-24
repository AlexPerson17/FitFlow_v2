/*
 * File: WorkoutHistoryRepository.java
 * Author: Michael Lee
 * Course: CMSC 495
 * Project: FitFlow
 * Date: June 2026
 * Version: 1.1
 *
 * Description:
 * This file saves and loads workout history data from workout_history.csv.
 */

package repository;

import model.WorkoutHistory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WorkoutHistoryRepository {

    private static final String DATA_FOLDER = "data";
    private static final String FILE_PATH = DATA_FOLDER + "/workout_history.csv";
    private static final String HEADER = "historyId,userId,routineName,completedDate,duration,estimatedCalories";

    /*
     * Makes sure the data folder exists.
     */
    private void makeSureDataFolderExists() {
        try {
            Files.createDirectories(Paths.get(DATA_FOLDER));
        } catch (IOException e) {
            System.out.println("Could not create data folder: " + e.getMessage());
        }
    }

    /*
     * Makes sure the workout history CSV file exists.
     */
    private void makeSureFileExists() {
        makeSureDataFolderExists();

        try {
            if (!Files.exists(Paths.get(FILE_PATH)) || Files.size(Paths.get(FILE_PATH)) == 0) {
                FileWriter writer = new FileWriter(FILE_PATH);
                writer.write(HEADER + "\n");
                writer.close();
            }
        } catch (IOException e) {
            System.out.println("Could not create workout history file: " + e.getMessage());
        }
    }

    /*
     * Saves one completed workout to the CSV file.
     */
    public void saveWorkoutHistory(WorkoutHistory history) {
        makeSureFileExists();

        try {
            FileWriter writer = new FileWriter(FILE_PATH, true);
            writer.write(history.toCSV() + "\n");
            writer.close();

            System.out.println("Workout history saved to CSV.");
        } catch (IOException e) {
            System.out.println("Error saving workout history: " + e.getMessage());
        }
    }

    /*
     * Loads all saved workout history records.
     */
    public List<WorkoutHistory> loadAllWorkoutHistory() {
        makeSureFileExists();

        List<WorkoutHistory> historyList = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(Paths.get(FILE_PATH));

            for (String line : lines) {
                if (line.trim().isEmpty() || line.equals(HEADER)) {
                    continue;
                }

                WorkoutHistory history = parseWorkoutHistory(line);

                if (history != null) {
                    historyList.add(history);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading workout history: " + e.getMessage());
        }

        return historyList;
    }

    /*
     * Gets workout history for one user.
     */
    public List<WorkoutHistory> loadWorkoutHistoryByUser(String userId) {
        List<WorkoutHistory> allHistory = loadAllWorkoutHistory();
        List<WorkoutHistory> userHistory = new ArrayList<>();

        for (WorkoutHistory history : allHistory) {
            if (history.getUserId().equals(userId)) {
                userHistory.add(history);
            }
        }

        return userHistory;
    }

    /*
     * Turns one CSV line back into a WorkoutHistory object.
     */
    private WorkoutHistory parseWorkoutHistory(String line) {
        try {
            String[] parts = line.split(",", -1);

            if (parts.length < 6) {
                return null;
            }

            String historyId = parts[0];
            String userId = parts[1];
            String routineName = parts[2];
            String completedDate = parts[3];
            int duration = Integer.parseInt(parts[4]);
            double estimatedCalories = Double.parseDouble(parts[5]);

            return new WorkoutHistory(
                    historyId,
                    userId,
                    routineName,
                    completedDate,
                    duration,
                    estimatedCalories
            );
        } catch (Exception e) {
            System.out.println("Error reading workout history line: " + e.getMessage());
            return null;
        }
    }
}
