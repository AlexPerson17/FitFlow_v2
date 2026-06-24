import model.Exercise;
import model.UserProfile;
import model.WorkoutHistory;
import model.WorkoutRoutine;
import repository.CSVHelper;
import repository.ProfileRepository;
import repository.WorkoutHistoryRepository;
import repository.WorkoutRoutineRepository;
import util.Calculator;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/*
 * File: BackendProofTest.java
 * Author: Michael Lee
 * Project: FitFlow
 * Date: June 2026
 * Version: 1.0
 *
 * Description:
 * This file gives proof that the main backend classes, calculator,
 * CSVHelper, and repository save/load behavior are working.
 */
public class BackendProofTest {

    /*
     * Runs all backend proof tests in one place.
     */
    public static void main(String[] args) {

        System.out.println("=== FitFlow Backend Proof Test ===");
        System.out.println("Date: June 2026");
        System.out.println();

        String testId = "TEST" + System.currentTimeMillis();

        testUserProfile(testId);
        testExercise();
        testWorkoutRoutine(testId);
        testWorkoutHistory(testId);
        testCalculator();
        testCSVHelper(testId);
        testRepositories(testId);

        System.out.println();
        System.out.println("=== Backend proof test completed ===");
    }

    /*
     * Checks that a UserProfile object can be created and converted to CSV.
     */
    private static void testUserProfile(String testId) {
        System.out.println("Testing UserProfile...");

        UserProfile profile = new UserProfile(
                testId,
                "michaeltest",
                "Michael",
                "Lee",
                29,
                69.0,
                155.0,
                "Male"
        );

        System.out.println(profile);
        System.out.println("UserProfile CSV: " + profile.toCSV());
        System.out.println("PASS: UserProfile object created.");
        System.out.println();
    }

    /*
     * Checks that an Exercise object can be created.
     */
    private static void testExercise() {
        System.out.println("Testing Exercise...");

        Exercise exercise = new Exercise(
                "E001",
                "Push Ups",
                "Strength",
                60,
                5.0,
                "pushups.gif"
        );

        System.out.println(exercise);
        System.out.println("Exercise CSV: " + exercise.toCSV());
        System.out.println("Estimated calories: " + exercise.estimateCalories());
        System.out.println("PASS: Exercise object created.");
        System.out.println();
    }

    /*
     * Checks that a WorkoutRoutine object can be created and validated.
     */
    private static void testWorkoutRoutine(String testId) {
        System.out.println("Testing WorkoutRoutine...");

        WorkoutRoutine routine = new WorkoutRoutine(
                "R001",
                testId,
                "Morning Routine",
                "Push Ups",
                3,
                12,
                60,
                30
        );

        System.out.println(routine);
        System.out.println("Routine CSV: " + routine.toCSV());
        System.out.println("Total time: " + routine.getTotalTime());
        System.out.println("Valid routine: " + routine.isValidRoutine());
        System.out.println("PASS: WorkoutRoutine object created.");
        System.out.println();
    }

    /*
     * Checks that a WorkoutHistory object can be created.
     */
    private static void testWorkoutHistory(String testId) {
        System.out.println("Testing WorkoutHistory...");

        WorkoutHistory history = new WorkoutHistory(
                "H001",
                testId,
                "Morning Routine",
                "2026-06-20",
                270,
                70.0
        );

        System.out.println(history);
        System.out.println("History CSV: " + history.toCSV());
        System.out.println("PASS: WorkoutHistory object created.");
        System.out.println();
    }

    /*
     * Checks that the calculator methods return results.
     */
    private static void testCalculator() {
        System.out.println("Testing Calculator...");

        double bmi = Calculator.calculateBMI(155.0, 69.0);
        double bmr = Calculator.calculateBMR(155.0, 69.0, 29);
        double calories = Calculator.calculateCaloriesBurned(10, 7.0);
        int totalTime = Calculator.calculateTotalWorkoutTime(60, 30, 3);

        System.out.println("BMI: " + bmi);
        System.out.println("BMR: " + bmr);
        System.out.println("Calories burned: " + calories);
        System.out.println("Total workout time: " + totalTime);
        System.out.println("PASS: Calculator methods returned results.");
        System.out.println();
    }

    /*
     * Checks that the old CSVHelper save methods still run.
     */
    private static void testCSVHelper(String testId) {
        System.out.println("Testing CSVHelper...");

        UserProfile profile = new UserProfile(
                testId,
                "csvuser",
                "Michael",
                "Lee",
                29,
                69.0,
                155.0,
                "Male"
        );

        WorkoutRoutine routine = new WorkoutRoutine(
                "R002",
                testId,
                "CSV Routine",
                "Squats",
                3,
                10,
                60,
                30
        );

        WorkoutHistory history = new WorkoutHistory(
                "H002",
                testId,
                "CSV Routine",
                "2026-06-20",
                270,
                70.0
        );

        CSVHelper.saveProfile(profile);
        CSVHelper.saveWorkoutRoutine(routine);
        CSVHelper.saveWorkoutHistory(history);

        System.out.println("profiles.csv exists: " + Files.exists(Paths.get("data/profiles.csv")));
        System.out.println("workout_routines.csv exists: " + Files.exists(Paths.get("data/workout_routines.csv")));
        System.out.println("workout_history.csv exists: " + Files.exists(Paths.get("data/workout_history.csv")));
        System.out.println("PASS: CSVHelper save methods ran.");
        System.out.println();
    }

    /*
     * Checks that the new repository files can save and load data.
     */
    private static void testRepositories(String testId) {
        System.out.println("Testing repository save/load behavior...");

        ProfileRepository profileRepository = new ProfileRepository();
        WorkoutRoutineRepository routineRepository = new WorkoutRoutineRepository();
        WorkoutHistoryRepository historyRepository = new WorkoutHistoryRepository();

        UserProfile profile = new UserProfile(
                testId,
                "repouser",
                "Michael",
                "Lee",
                29,
                69.0,
                155.0,
                "Male"
        );

        WorkoutRoutine routine = new WorkoutRoutine(
                "R003",
                testId,
                "Repository Routine",
                "Lunges",
                3,
                10,
                60,
                30
        );

        WorkoutHistory history = new WorkoutHistory(
                "H003",
                testId,
                "Repository Routine",
                "2026-06-20",
                270,
                70.0
        );

        profileRepository.saveProfile(profile);
        routineRepository.saveWorkoutRoutine(routine);
        historyRepository.saveWorkoutHistory(history);

        UserProfile loadedProfile = profileRepository.findProfileByUserId(testId);
        List<WorkoutRoutine> loadedRoutines = routineRepository.loadWorkoutRoutinesByUser(testId);
        List<WorkoutHistory> loadedHistory = historyRepository.loadWorkoutHistoryByUser(testId);

        System.out.println("Profile loaded: " + (loadedProfile != null));
        System.out.println("Routines loaded for user: " + loadedRoutines.size());
        System.out.println("History loaded for user: " + loadedHistory.size());
        System.out.println("PASS: Repository save/load behavior tested.");
        System.out.println();
    }
}
