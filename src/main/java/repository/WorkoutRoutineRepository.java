/*
 * File: WorkoutRoutineRepository.java
 * Author: Michael Lee
 * Course: CMSC 495
 * Project: FitFlow
 * Date: June 2026
 * Version: 1.1
 *
 * Description:
 * This file saves and loads workout routine data from workout_routines.csv.
 */

package repository;

import model.WorkoutRoutine;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WorkoutRoutineRepository {

    private static final String DATA_FOLDER = "data";
    private static final String FILE_PATH = DATA_FOLDER + "/workout_routines.csv";
    private static final String HEADER = "routineId,userId,routineName,exerciseName,sets,reps,duration,restTime";

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
     * Makes sure the workout routine CSV file exists.
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
            System.out.println("Could not create routine file: " + e.getMessage());
        }
    }

    /*
     * Saves one workout routine to the CSV file.
     */
    public void saveWorkoutRoutine(WorkoutRoutine routine) {
        makeSureFileExists();

        try {
            FileWriter writer = new FileWriter(FILE_PATH, true);
            writer.write(routine.toCSV() + "\n");
            writer.close();

            System.out.println("Workout routine saved to CSV.");
        } catch (IOException e) {
            System.out.println("Error saving workout routine: " + e.getMessage());
        }
    }

    /*
     * Loads all saved workout routines.
     */
    public List<WorkoutRoutine> loadAllWorkoutRoutines() {
        makeSureFileExists();

        List<WorkoutRoutine> routineList = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(Paths.get(FILE_PATH));

            for (String line : lines) {
                if (line.trim().isEmpty() || line.equals(HEADER)) {
                    continue;
                }

                WorkoutRoutine routine = parseWorkoutRoutine(line);

                if (routine != null) {
                    routineList.add(routine);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading workout routines: " + e.getMessage());
        }

        return routineList;
    }

    /*
     * Gets workout routines for one user.
     */
    public List<WorkoutRoutine> loadWorkoutRoutinesByUser(String userId) {
        List<WorkoutRoutine> allRoutines = loadAllWorkoutRoutines();
        List<WorkoutRoutine> userRoutines = new ArrayList<>();

        for (WorkoutRoutine routine : allRoutines) {
            if (routine.getUserId().equals(userId)) {
                userRoutines.add(routine);
            }
        }

        return userRoutines;
    }

    /*
     * Turns one CSV line back into a WorkoutRoutine object.
     */
    private WorkoutRoutine parseWorkoutRoutine(String line) {
        try {
            String[] parts = line.split(",", -1);

            if (parts.length < 8) {
                return null;
            }

            String routineId = parts[0];
            String userId = parts[1];
            String routineName = parts[2];
            String exerciseName = parts[3];
            int sets = Integer.parseInt(parts[4]);
            int reps = Integer.parseInt(parts[5]);
            int duration = Integer.parseInt(parts[6]);
            int restTime = Integer.parseInt(parts[7]);

            return new WorkoutRoutine(
                    routineId,
                    userId,
                    routineName,
                    exerciseName,
                    sets,
                    reps,
                    duration,
                    restTime
            );
        } catch (Exception e) {
            System.out.println("Error reading workout routine line: " + e.getMessage());
            return null;
        }
    }
}
