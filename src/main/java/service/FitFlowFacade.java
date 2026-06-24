/*
 * File: FitFlowFacade.java
 * Project: FitFlow - Interactive Workout Assistant
 * Course: UMGC CMSC 495
 * Phase: Phase II Source Code
 * Week: 6
 * Version: v0.6.3
 * Author: David Lewis
 * Last Updated: 2026-06-22
 *
 * Purpose:
 * Provides a central integration point between frontend controllers and
 * backend/service logic. Frontend classes should call this facade instead
 * of directly accessing CSV repositories or lower-level backend classes.
 *
 * Dependencies:
 * ServiceResponse, ErrorMessages, ValidationUtil, UserProfile, WorkoutRoutine,
 * WorkoutHistory, ProfileRepository, WorkoutRoutineRepository,
 * WorkoutHistoryRepository, Java Standard Library.
 *
 * Update Notes:
 * Added repository-backed routine/history persistence and current-user
 * profile loading so screens no longer rely on memory-only data or hardcoded
 * profile records.
 *
 */
package service;

import model.RoutineExerciseSelection;
import model.UserProfile;
import model.WorkoutHistory;
import model.WorkoutRoutine;
import model.WorkoutSession;
import repository.ProfileRepository;
import repository.WorkoutHistoryRepository;
import repository.WorkoutRoutineRepository;
import repository.UserAccountRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Main integration facade for FitFlow.
 */
public class FitFlowFacade {
    private final Map<String, UserAccount> usersByUsername;
    private final Map<String, String> activeSessions;
    private final Map<String, List<String>> routinesByUserId;
    private final Map<String, List<String>> historyByUserId;
    private final List<String> exerciseLibrary;
    private final ProfileRepository profileRepository;
    private final WorkoutRoutineRepository workoutRoutineRepository;
    private final WorkoutHistoryRepository workoutHistoryRepository;
    private final TimerService timerService;
    private final UserAccountRepository userAccountRepository;

    /**
     * Creates the facade and loads Phase I starter data.
     */
    public FitFlowFacade() {
        usersByUsername = new HashMap<String, UserAccount>();
        activeSessions = new HashMap<String, String>();
        routinesByUserId = new HashMap<String, List<String>>();
        historyByUserId = new HashMap<String, List<String>>();
        exerciseLibrary = new ArrayList<String>();
        profileRepository = new ProfileRepository();
        workoutRoutineRepository = new WorkoutRoutineRepository();
        workoutHistoryRepository = new WorkoutHistoryRepository();
        timerService = new TimerService();
        userAccountRepository = new UserAccountRepository();

        loadSavedUsers();
        loadStarterExercises();
        createDemoUser();
    }

    /**
     * Registers a new user account after validating basic fields.
     *
     * @param username Desired username.
     * @param password Desired password.
     * @param email User email address.
     * @return ServiceResponse containing the created user ID or an error.
     */
    public ServiceResponse<String> signUp(String username, String password, String email) {
        if (!ValidationUtil.isValidUsername(username)) {
            return ServiceResponse.error(ErrorMessages.USERNAME_REQUIRED, ErrorMessages.CODE_VALIDATION);
        }

        if (!ValidationUtil.isValidPassword(password)) {
            return ServiceResponse.error(ErrorMessages.PASSWORD_TOO_SHORT, ErrorMessages.CODE_VALIDATION);
        }

        if (!ValidationUtil.isValidEmail(email)) {
            return ServiceResponse.error(ErrorMessages.INVALID_EMAIL, ErrorMessages.CODE_VALIDATION);
        }

        String normalizedUsername = username.trim().toLowerCase();

        if (usersByUsername.containsKey(normalizedUsername)) {
            return ServiceResponse.error(ErrorMessages.USER_ALREADY_EXISTS, ErrorMessages.CODE_AUTH);
        }

        String userId = "USER-" + UUID.randomUUID().toString();
        UserAccount account = new UserAccount(userId, normalizedUsername, hashPassword(password), email.trim(), LocalDateTime.now());

        usersByUsername.put(normalizedUsername, account);
        userAccountRepository.saveUser(
                new UserAccountRepository.AccountRecord(
                        userId,
                        normalizedUsername,
                        account.passwordHash,
                        account.email,
                        account.createdAt
                )
        );
        routinesByUserId.put(userId, new ArrayList<String>());
        historyByUserId.put(userId, new ArrayList<String>());

        return ServiceResponse.success(ErrorMessages.SUCCESS_SIGNUP, userId);
    }

    /**
     * Signs in a user and returns a temporary session token.
     *
     * @param username Username entered by the user.
     * @param password Password entered by the user.
     * @return ServiceResponse containing a session token or an error.
     */
    public ServiceResponse<String> signIn(String username, String password) {
        if (ValidationUtil.isBlank(username)) {
            return ServiceResponse.error(ErrorMessages.USERNAME_REQUIRED, ErrorMessages.CODE_VALIDATION);
        }

        if (ValidationUtil.isBlank(password)) {
            return ServiceResponse.error(ErrorMessages.PASSWORD_REQUIRED, ErrorMessages.CODE_VALIDATION);
        }

        String normalizedUsername = username.trim().toLowerCase();
        UserAccount account = usersByUsername.get(normalizedUsername);

        if (account == null || !account.passwordHash.equals(hashPassword(password))) {
            return ServiceResponse.error(ErrorMessages.INVALID_LOGIN, ErrorMessages.CODE_AUTH);
        }

        String sessionToken = UUID.randomUUID().toString();
        activeSessions.put(sessionToken, account.userId);

        return ServiceResponse.success(ErrorMessages.SUCCESS_LOGIN, sessionToken);
    }

    /**
     * Ends a valid session token.
     *
     * @param sessionToken Active session token.
     * @return ServiceResponse explaining whether logout completed.
     */
    public ServiceResponse<Boolean> logout(String sessionToken) {
        if (!isValidSession(sessionToken)) {
            return ServiceResponse.error(ErrorMessages.SESSION_EXPIRED, ErrorMessages.CODE_SESSION);
        }

        activeSessions.remove(sessionToken);
        return ServiceResponse.success(ErrorMessages.SUCCESS_LOGOUT, Boolean.TRUE);
    }

    /**
     * Loads the starter exercise library.
     *
     * @param sessionToken Active session token.
     * @return ServiceResponse containing the exercise names.
     */
    public ServiceResponse<List<String>> getWorkouts(String sessionToken) {
        if (!isValidSession(sessionToken)) {
            return ServiceResponse.error(ErrorMessages.SESSION_EXPIRED, ErrorMessages.CODE_SESSION);
        }

        return ServiceResponse.success(ErrorMessages.SUCCESS_WORKOUTS_LOADED, new ArrayList<String>(exerciseLibrary));
    }

    /**
     * Saves a simple workout routine for the signed-in user.
     *
     * @param sessionToken Active session token.
     * @param routineName Name of the workout routine.
     * @param exerciseNames Selected exercise names.
     * @return ServiceResponse confirming the routine save operation.
     */
    public ServiceResponse<Boolean> saveWorkout(String sessionToken, String routineName, List<String> exerciseNames) {
        if (!isValidSession(sessionToken)) {
            return ServiceResponse.error(ErrorMessages.SESSION_EXPIRED, ErrorMessages.CODE_SESSION);
        }

        if (!ValidationUtil.isValidRoutineName(routineName)) {
            return ServiceResponse.error(ErrorMessages.ROUTINE_NAME_REQUIRED, ErrorMessages.CODE_VALIDATION);
        }

        if (!ValidationUtil.hasSelectedExercises(exerciseNames)) {
            return ServiceResponse.error(ErrorMessages.EXERCISE_LIST_REQUIRED, ErrorMessages.CODE_VALIDATION);
        }

        String userId = activeSessions.get(sessionToken);
        List<String> userRoutines = routinesByUserId.get(userId);

        if (userRoutines == null) {
            userRoutines = new ArrayList<String>();
            routinesByUserId.put(userId, userRoutines);
        }

        /*
         * The routine builder currently sends a routine name and a list of
         * selected exercise names. The repository stores one CSV row per
         * exercise because WorkoutRoutine represents one exercise entry inside
         * a routine. This keeps the UI simple while still making the save
         * durable in data/workout_routines.csv. Default sets/reps/duration/rest
         * values are used until the UI passes those detailed values separately.
         */
        String routineId = "ROUTINE-" + UUID.randomUUID().toString();
        for (String exerciseName : exerciseNames) {
            if (!ValidationUtil.isBlank(exerciseName)) {
                WorkoutRoutine routine = new WorkoutRoutine(
                        routineId,
                        userId,
                        routineName.trim(),
                        exerciseName.trim(),
                        1,
                        0,
                        60,
                        30
                );
                workoutRoutineRepository.saveWorkoutRoutine(routine);
            }
        }

        String routineRecord = routineName.trim() + " -> " + exerciseNames.toString();
        userRoutines.add(routineRecord);

        return ServiceResponse.success(ErrorMessages.SUCCESS_ROUTINE_SAVED, Boolean.TRUE);
    }


    /*
     * Saves a workout routine with the detailed settings selected in the
     * Routine Builder UI.
     *
     * Saves each selected exercise with its own sets, reps, duration, and
     * rest value.
     * The older saveWorkout method only received exercise names and stored
     * default values. This method preserves the user's changed builder values.
     * RoutineBuilderScreen passes RoutineExerciseSelection objects through
     * AppStateManager. The facade validates them, saves one WorkoutRoutine row
     * per exercise, and still stores a readable in-memory display record.
     */
    public ServiceResponse<Boolean> saveWorkoutWithDetails(String sessionToken,
                                                           String routineName,
                                                           List<RoutineExerciseSelection> selectedExercises) {
        if (!isValidSession(sessionToken)) {
            return ServiceResponse.error(ErrorMessages.SESSION_EXPIRED, ErrorMessages.CODE_SESSION);
        }

        if (!ValidationUtil.isValidRoutineName(routineName)) {
            return ServiceResponse.error(ErrorMessages.ROUTINE_NAME_REQUIRED, ErrorMessages.CODE_VALIDATION);
        }

        if (selectedExercises == null || selectedExercises.isEmpty()) {
            return ServiceResponse.error(ErrorMessages.EXERCISE_LIST_REQUIRED, ErrorMessages.CODE_VALIDATION);
        }

        String userId = activeSessions.get(sessionToken);
        String routineId = "ROUTINE-" + UUID.randomUUID().toString();
        List<String> displayParts = new ArrayList<String>();

        for (RoutineExerciseSelection selection : selectedExercises) {
            if (selection == null || !selection.isValidSelection()) {
                return ServiceResponse.error("Routine exercise settings are invalid.", ErrorMessages.CODE_VALIDATION);
            }

            WorkoutRoutine routine = new WorkoutRoutine(
                    routineId,
                    userId,
                    routineName.trim(),
                    selection.getExerciseName().trim(),
                    selection.getSets(),
                    selection.getReps(),
                    selection.getWorkSeconds(),
                    selection.getRestSeconds()
            );

            workoutRoutineRepository.saveWorkoutRoutine(routine);
            displayParts.add(selection.toString());
        }

        List<String> userRoutines = routinesByUserId.get(userId);
        if (userRoutines == null) {
            userRoutines = new ArrayList<String>();
            routinesByUserId.put(userId, userRoutines);
        }

        userRoutines.add(routineName.trim() + " -> " + displayParts.toString());
        return ServiceResponse.success(ErrorMessages.SUCCESS_ROUTINE_SAVED, Boolean.TRUE);
    }

    /**
     * Loads saved workout routines for the active user.
     *
     * @param sessionToken Active session token.
     * @return ServiceResponse containing saved routine records.
     */
    public ServiceResponse<List<String>> getSavedRoutines(String sessionToken) {
        if (!isValidSession(sessionToken)) {
            return ServiceResponse.error(ErrorMessages.SESSION_EXPIRED, ErrorMessages.CODE_SESSION);
        }

        String userId = activeSessions.get(sessionToken);
        List<String> routines = new ArrayList<String>();

        /*
         * Load durable CSV records first so saved routines are still available
         * after a facade restart. The in-memory list is only kept as a fallback
         * for older Phase I test paths that saved simple display strings.
         */
        List<WorkoutRoutine> savedRoutineRows = workoutRoutineRepository.loadWorkoutRoutinesByUser(userId);
        for (WorkoutRoutine routine : savedRoutineRows) {
            routines.add(routine.getRoutineName() + " -> " + routine.getExerciseName());
        }

        List<String> memoryRoutines = routinesByUserId.get(userId);
        if (memoryRoutines != null) {
            for (String routine : memoryRoutines) {
                if (!routines.contains(routine)) {
                    routines.add(routine);
                }
            }
        }

        return ServiceResponse.success("Saved workout routines loaded successfully.", routines);
    }

    /**
     * Saves updated profile data for the signed-in user.
     *
     * Accepts a UserProfile object from AppStateManager and saves it
     * through the repository helper.
     * The frontend should not write directly to CSV files or bypass the
     * service layer. This method keeps profile persistence aligned with the
     * project architecture.
     * The method checks the session token, validates required profile
     * fields, then asks CSVHelper to append the profile record to profiles.csv.
     *
     * @param sessionToken Active session token.
     * @param profileData Updated profile information from the profile screen.
     * @return ServiceResponse confirming whether the profile save completed.
     */
    public ServiceResponse<Boolean> saveProfile(String sessionToken, UserProfile profileData) {
        if (!isValidSession(sessionToken)) {
            return ServiceResponse.error(ErrorMessages.SESSION_EXPIRED, ErrorMessages.CODE_SESSION);
        }

        if (profileData == null
                || ValidationUtil.isBlank(profileData.getFirstName())
                || ValidationUtil.isBlank(profileData.getLastName())
                || profileData.getAge() <= 0
                || !ValidationUtil.isValidHeightWeight(profileData.getHeight(), profileData.getWeight())) {
            return ServiceResponse.error(ErrorMessages.INVALID_PROFILE, ErrorMessages.CODE_VALIDATION);
        }

        String userId = activeSessions.get(sessionToken);
        UserAccount account = findAccountByUserId(userId);
        String username = account == null ? profileData.getUsername() : account.username;

        /*
         * Save a profile tied to the active session instead of trusting a user ID
         * that came from the screen. This prevents one logged-in user from
         * accidentally saving profile data under another user's ID.
         */
        UserProfile sessionProfile = new UserProfile(
                userId,
                username,
                profileData.getFirstName(),
                profileData.getLastName(),
                profileData.getAge(),
                profileData.getHeight(),
                profileData.getWeight(),
                profileData.getGender()
        );

        profileRepository.saveProfile(sessionProfile);
        return ServiceResponse.success(ErrorMessages.SUCCESS_PROFILE_SAVED, Boolean.TRUE);
    }

    /**
     * Loads the profile for the currently signed-in user.
     *
     * If a saved CSV profile exists, that profile is returned. If this is the
     * user's first visit to the profile page, the facade creates a safe starter
     * profile from the active account instead of letting AppStateManager use a
     * hardcoded test user.
     *
     * @param sessionToken Active session token.
     * @return ServiceResponse containing the current user's profile.
     */
    public ServiceResponse<UserProfile> getCurrentUserProfile(String sessionToken) {
        if (!isValidSession(sessionToken)) {
            return ServiceResponse.error(ErrorMessages.SESSION_EXPIRED, ErrorMessages.CODE_SESSION);
        }

        String userId = activeSessions.get(sessionToken);
        UserProfile savedProfile = profileRepository.findProfileByUserId(userId);

        if (savedProfile != null) {
            return ServiceResponse.success("Profile loaded successfully.", savedProfile);
        }

        UserAccount account = findAccountByUserId(userId);
        String username = account == null ? "user" : account.username;

        UserProfile starterProfile = new UserProfile(
                userId,
                username,
                "",
                "",
                18,
                70,
                180,
                ""
        );

        return ServiceResponse.success("Starter profile loaded successfully.", starterProfile);
    }

    /**
     * Saves a completed workout history record for the active user.
     *
     * @param sessionToken Active session token.
     * @param workoutSummary Short summary of the completed workout.
     * @param durationSeconds Total workout duration in seconds.
     * @return ServiceResponse confirming whether history was saved.
     */
    public ServiceResponse<Boolean> saveWorkoutHistory(String sessionToken, String workoutSummary, int durationSeconds) {
        if (!isValidSession(sessionToken)) {
            return ServiceResponse.error(ErrorMessages.SESSION_EXPIRED, ErrorMessages.CODE_SESSION);
        }

        if (ValidationUtil.isBlank(workoutSummary)) {
            return ServiceResponse.error("Workout history summary is required.", ErrorMessages.CODE_VALIDATION);
        }

        if (!ValidationUtil.isValidDuration(durationSeconds)) {
            return ServiceResponse.error(ErrorMessages.INVALID_DURATION, ErrorMessages.CODE_VALIDATION);
        }

        String userId = activeSessions.get(sessionToken);
        List<String> userHistory = historyByUserId.get(userId);

        if (userHistory == null) {
            userHistory = new ArrayList<String>();
            historyByUserId.put(userId, userHistory);
        }

        String completedDate = LocalDateTime.now().toString();
        double estimatedCalories = (durationSeconds / 60.0) * 7.0;
        WorkoutHistory historyRecord = new WorkoutHistory(
                "HISTORY-" + UUID.randomUUID().toString(),
                userId,
                workoutSummary.trim(),
                completedDate,
                durationSeconds,
                estimatedCalories
        );

        workoutHistoryRepository.saveWorkoutHistory(historyRecord);
        userHistory.add(completedDate + " | " + workoutSummary.trim() + " | " + durationSeconds + " seconds");
        return ServiceResponse.success("Workout history saved successfully.", Boolean.TRUE);
    }

    /**
     * Loads workout history for the active user.
     *
     * @param sessionToken Active session token.
     * @return ServiceResponse containing workout history records.
     */
    public ServiceResponse<List<String>> getWorkoutHistory(String sessionToken) {
        if (!isValidSession(sessionToken)) {
            return ServiceResponse.error(ErrorMessages.SESSION_EXPIRED, ErrorMessages.CODE_SESSION);
        }

        String userId = activeSessions.get(sessionToken);
        List<String> history = new ArrayList<String>();

        /*
         * Load from the repository first so history survives beyond the current
         * facade instance. In-memory history remains a short-term fallback for
         * current-session display strings and older test paths.
         */
        List<WorkoutHistory> savedHistoryRows = workoutHistoryRepository.loadWorkoutHistoryByUser(userId);
        for (WorkoutHistory savedHistory : savedHistoryRows) {
            history.add(savedHistory.getCompletedDate()
                    + " | " + savedHistory.getRoutineName()
                    + " | " + savedHistory.getDuration()
                    + " seconds | "
                    + savedHistory.getEstimatedCalories()
                    + " calories");
        }

        List<String> memoryHistory = historyByUserId.get(userId);
        if (memoryHistory != null) {
            for (String entry : memoryHistory) {
                if (!history.contains(entry)) {
                    history.add(entry);
                }
            }
        }

        return ServiceResponse.success(ErrorMessages.SUCCESS_HISTORY_LOADED, history);
    }

    /**
     * Calculates estimated calories burned using a simple Phase I formula.
     *
     * @param sessionToken Active session token.
     * @param caloriesPerMinute Estimated calories burned per minute.
     * @param durationSeconds Exercise duration in seconds.
     * @return ServiceResponse containing the estimated calories burned.
     */
    public ServiceResponse<Double> calculateCalories(String sessionToken, double caloriesPerMinute, int durationSeconds) {
        if (!isValidSession(sessionToken)) {
            return ServiceResponse.error(ErrorMessages.SESSION_EXPIRED, ErrorMessages.CODE_SESSION);
        }

        if (caloriesPerMinute <= 0 || !ValidationUtil.isValidDuration(durationSeconds)) {
            return ServiceResponse.error(ErrorMessages.INVALID_DURATION, ErrorMessages.CODE_CALCULATION);
        }

        double minutes = durationSeconds / 60.0;
        double estimatedCalories = caloriesPerMinute * minutes;

        return ServiceResponse.success(ErrorMessages.SUCCESS_CALORIES_CALCULATED, estimatedCalories);
    }

    /**
     * Creates a basic workout recommendation using the user's stated goal.
     *
     * @param sessionToken Active session token.
     * @param fitnessGoal User's fitness goal.
     * @return ServiceResponse containing a recommendation sentence.
     */
    public ServiceResponse<String> getRecommendations(String sessionToken, String fitnessGoal) {
        if (!isValidSession(sessionToken)) {
            return ServiceResponse.error(ErrorMessages.SESSION_EXPIRED, ErrorMessages.CODE_SESSION);
        }

        if (ValidationUtil.isBlank(fitnessGoal)) {
            return ServiceResponse.success(ErrorMessages.SUCCESS_RECOMMENDATION_CREATED,
                    "Start with a balanced beginner routine using push-ups, squats, and planks.");
        }

        String goal = fitnessGoal.trim().toLowerCase();

        if (goal.contains("weight") || goal.contains("fat")) {
            return ServiceResponse.success(ErrorMessages.SUCCESS_RECOMMENDATION_CREATED,
                    "Recommended: use shorter rest periods and include squats, sit-ups, and push-ups.");
        }

        if (goal.contains("strength") || goal.contains("muscle")) {
            return ServiceResponse.success(ErrorMessages.SUCCESS_RECOMMENDATION_CREATED,
                    "Recommended: focus on push-ups, squats, and dumbbell curls with steady rest periods.");
        }

        if (goal.contains("core")) {
            return ServiceResponse.success(ErrorMessages.SUCCESS_RECOMMENDATION_CREATED,
                    "Recommended: focus on planks, sit-ups, and controlled bodyweight movements.");
        }

        return ServiceResponse.success(ErrorMessages.SUCCESS_RECOMMENDATION_CREATED,
                "Recommended: follow a balanced routine using all five starter exercises.");
    }

    public ServiceResponse<WorkoutSession> startGuidedWorkout(String sessionToken,
                                                              String routineName,
                                                              List<String> exercises,
                                                              int exerciseDurationSeconds,
                                                              int restDurationSeconds) {
        // Checks the login session before starting a guided workout.
        if (!isValidSession(sessionToken)) {
            return ServiceResponse.error(ErrorMessages.SESSION_EXPIRED, ErrorMessages.CODE_SESSION);
        }

        return timerService.startWorkout(routineName, exercises, exerciseDurationSeconds, restDurationSeconds);
    }

    /*
     * Starts a guided workout using the detailed Routine Builder settings.
     * This preserves changed sets, reps, and rest values in the guided workout
     * UI instead of falling back to default values.
     */
    public ServiceResponse<WorkoutSession> startGuidedWorkoutWithDetails(String sessionToken,
                                                                         String routineName,
                                                                         List<RoutineExerciseSelection> selectedExercises) {
        if (!isValidSession(sessionToken)) {
            return ServiceResponse.error(ErrorMessages.SESSION_EXPIRED, ErrorMessages.CODE_SESSION);
        }

        return timerService.startWorkoutWithSelections(routineName, selectedExercises);
    }

    public ServiceResponse<WorkoutSession> pauseGuidedWorkout(String sessionToken) {
        // Checks the login session before pausing the workout.
        if (!isValidSession(sessionToken)) {
            return ServiceResponse.error(ErrorMessages.SESSION_EXPIRED, ErrorMessages.CODE_SESSION);
        }

        return timerService.pauseWorkout();
    }

    public ServiceResponse<WorkoutSession> resumeGuidedWorkout(String sessionToken) {
        // Checks the login session before resuming the workout.
        if (!isValidSession(sessionToken)) {
            return ServiceResponse.error(ErrorMessages.SESSION_EXPIRED, ErrorMessages.CODE_SESSION);
        }

        return timerService.resumeWorkout();
    }

    public ServiceResponse<WorkoutSession> resetGuidedWorkout(String sessionToken) {
        // Checks the login session before resetting the workout.
        if (!isValidSession(sessionToken)) {
            return ServiceResponse.error(ErrorMessages.SESSION_EXPIRED, ErrorMessages.CODE_SESSION);
        }

        return timerService.resetWorkout();
    }

    public ServiceResponse<WorkoutSession> skipGuidedWorkoutStep(String sessionToken) {
        // Checks the login session before skipping the current step.
        if (!isValidSession(sessionToken)) {
            return ServiceResponse.error(ErrorMessages.SESSION_EXPIRED, ErrorMessages.CODE_SESSION);
        }

        return timerService.skipCurrentStep();
    }

    public ServiceResponse<WorkoutSession> tickGuidedWorkout(String sessionToken) {
        // Checks the login session before updating the timer.
        if (!isValidSession(sessionToken)) {
            return ServiceResponse.error(ErrorMessages.SESSION_EXPIRED, ErrorMessages.CODE_SESSION);
        }

        return timerService.tickWorkout();
    }

    public ServiceResponse<WorkoutSession> getCurrentWorkoutSession(String sessionToken) {
        // Checks the login session before returning workout timer state.
        if (!isValidSession(sessionToken)) {
            return ServiceResponse.error(ErrorMessages.SESSION_EXPIRED, ErrorMessages.CODE_SESSION);
        }

        return timerService.getCurrentSession();
    }

    public ServiceResponse<Boolean> saveCompletedGuidedWorkout(String sessionToken) {
        // Saves completed guided workout results into workout history.
        if (!isValidSession(sessionToken)) {
            return ServiceResponse.error(ErrorMessages.SESSION_EXPIRED, ErrorMessages.CODE_SESSION);
        }

        ServiceResponse<WorkoutSession> sessionResponse = timerService.getCurrentSession();

        if (!sessionResponse.isSuccess()) {
            return ServiceResponse.error(sessionResponse.getMessage(), sessionResponse.getErrorCode());
        }

        WorkoutSession session = sessionResponse.getData();

        if (!session.isCompleted()) {
            return ServiceResponse.error("Workout must be complete before saving history.", ErrorMessages.CODE_VALIDATION);
        }

        return saveWorkoutHistory(
                sessionToken,
                session.getCompletionSummary(),
                session.getTotalElapsedSeconds()
        );
    }

    /**
     * Checks whether a session token exists.
     *
     * @param sessionToken Session token to validate.
     * @return true if the session is active.
     */
    public boolean isValidSession(String sessionToken) {
        return sessionToken != null && activeSessions.containsKey(sessionToken);
    }

    /**
     * Loads the five starter exercises discussed in the project scope.
     */
    private void loadStarterExercises() {
        exerciseLibrary.add("Push-ups");
        exerciseLibrary.add("Sit-ups");
        exerciseLibrary.add("Plank");
        exerciseLibrary.add("Squats");
        exerciseLibrary.add("Dumbbell Curls");
    }

    /**
     * Creates a demo user for quick integration testing.
     */
    private void createDemoUser() {
        UserAccount demo = new UserAccount("USER-DEMO", "demo", hashPassword("password"),
                "demo@fitflow.local", LocalDateTime.now());

        usersByUsername.put("demo", demo);
        routinesByUserId.put("USER-DEMO", new ArrayList<String>());
        historyByUserId.put("USER-DEMO", new ArrayList<String>());
    }

    private void loadSavedUsers() {
        // Loads saved CSV users so login still works after closing the app.
        List<UserAccountRepository.AccountRecord> savedUsers =
                userAccountRepository.loadUsers();

        for (UserAccountRepository.AccountRecord savedUser : savedUsers) {
            UserAccount account = new UserAccount(
                    savedUser.userId,
                    savedUser.username,
                    savedUser.passwordHash,
                    savedUser.email,
                    savedUser.createdAt
            );

            usersByUsername.put(savedUser.username.toLowerCase(), account);
            routinesByUserId.put(savedUser.userId, new ArrayList<String>());
            historyByUserId.put(savedUser.userId, new ArrayList<String>());
        }
    }

    /**
     * Finds an internal account record by user ID.
     *
     * Session maps store user IDs, while account records are keyed by username.
     * This helper bridges those two maps when the facade needs the current
     * username for profile loading or profile saving.
     *
     * @param userId user ID from the active session map.
     * @return matching UserAccount, or null when no account is found.
     */
    private UserAccount findAccountByUserId(String userId) {
        for (UserAccount account : usersByUsername.values()) {
            if (account.userId.equals(userId)) {
                return account;
            }
        }

        return null;
    }

    /**
     * Hashes a password using SHA-256 for Phase I demonstration purposes.
     *
     * @param password Raw password entered by the user.
     * @return Hashed password string.
     */
    private String hashPassword(String password) {
        if (password == null) {
            return "";
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexValue = new StringBuilder();

            for (int index = 0; index < hashBytes.length; index++) {
                String hex = Integer.toHexString(0xff & hashBytes[index]);

                if (hex.length() == 1) {
                    hexValue.append('0');
                }

                hexValue.append(hex);
            }

            return hexValue.toString();
        } catch (NoSuchAlgorithmException exception) {
            return "";
        }
    }

    /**
     * Minimal internal account record used for Phase I integration testing.
     */
    private static class UserAccount {
        private final String userId;
        private final String username;
        private final String passwordHash;
        private final String email;
        private final LocalDateTime createdAt;

        private UserAccount(String userId, String username, String passwordHash, String email, LocalDateTime createdAt) {
            this.userId = userId;
            this.username = username;
            this.passwordHash = passwordHash;
            this.email = email;
            this.createdAt = createdAt;
        }

        /**
         * Formats account data for debugging without exposing password details.
         *
         * @return Safe account summary.
         */
        @Override
        public String toString() {
            return "UserAccount{" +
                    "userId='" + userId + '\'' +
                    ", username='" + username + '\'' +
                    ", email='" + email + '\'' +
                    ", createdAt=" + createdAt +
                    '}';
        }
    }
}
