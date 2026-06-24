/*
 * File: IntegrationRegressionTest.java
 * Project: FitFlow - Interactive Workout Assistant
 * Course: UMGC CMSC 495 Computer Science Capstone
 * Phase: Phase II Source Code
 * Version: v0.6.4
 * Author: David Lewis
 * Created: 2026-06-20
 * Last Updated: 2026-06-22
 *
 * Purpose:
 * Runs the integration and regression checks assigned to the team-lead/service
 * area after the routine-save and profile-save wiring updates.
 *
 * What this file tests:
 * - Authentication validation: short username, short password, invalid email,
 *   valid signup, duplicate signup, valid signin, logout, and invalid sessions.
 * - Routine save regression: blank routine names, empty exercise lists, valid
 *   routine save, saved-routine reload, and CSV/repository reload from a new
 *   facade instance.
 * - Profile save regression: incomplete profile data, invalid height/weight,
 *   invalid sessions, valid profile save, and current-user profile loading.
 * - Additional integration behavior: calorie calculation, workout history,
 *   recommendations, and password hashing.
 *
 * Why this file exists:
 * The project design says AppStateManager and the service/facade layer
 * instead of directly writing to CSV files or directly manipulating backend objects. 
 * These tests protect that architecture by proving the service layer accepts 
 * valid requests, blocks invalid requests, returns ServiceResponse objects,
 * and keeps session-based behavior consistent.
 *
 * How to run:
 * This is a plain Java runner with a main method. It does not require JUnit, so
 * it can be compiled and executed quickly from an IDE or command line. Each
 * test section prints PASS/FAIL lines, and the runner throws an exception if
 * any expected result fails.
 *
 * Notes:
 * - Add new checks when AppStateManager or FitFlowFacade gains new service
 *   routes.
 * - If a test fails, fix the source issue first. Do not change the expected
 *   result unless the team intentionally changed the requirement.
 */

import model.RoutineExerciseSelection;
import model.UserProfile;
import model.WorkoutSession;
import service.ErrorMessages;
import service.FitFlowFacade;
import service.ServiceResponse;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * IntegrationRegressionTest is a lightweight regression runner for the current
 * FitFlow service layer.
 *
 * This class intentionally avoids JavaFX dependencies. The goal is to prove that
 * the service path works before the frontend team performs manual UI retesting
 * in IntelliJ. In other words, this file checks the backend/service side of the
 * save buttons, session rules, and validation rules.
 */
public class IntegrationRegressionTest {
    /** Tracks how many expected checks passed during the run. */
    private int passedChecks;

    /** Tracks how many expected checks failed during the run. */
    private int failedChecks;

    /**
     * Starts the test runner.
     *
     * @param args command-line arguments are not used.
     * @throws Exception if reflection or a final test failure occurs.
     */
    public static void main(String[] args) throws Exception {
        IntegrationRegressionTest runner = new IntegrationRegressionTest();
        runner.runAllTests();
    }

    /**
     * Runs every integration/regression section in a predictable order.
     *
     * Calls each test method from authentication through security checks.
     * The order follows the Unit 5 test plan flow: validate login/signup,
     * then test session routing, then routine/profile saves, then regression
     * behavior that depends on a valid session.
     * Each section creates a fresh FitFlowFacade where possible so one
     * test does not hide or cause failures in another section.
     * The final count should show all checks passing and zero failures.
     * If any check fails, the method throws an exception so
     * the issue cannot be missed in the console output.
     */
    private void runAllTests() throws Exception {
        System.out.println("=== FitFlow Integration and Regression Test Runner ===");
        System.out.println("Date: 2026-06-21");
        System.out.println("Focus: service routing, validation, sessions, repository-backed routine/history saves, and current-user profile loading");
        System.out.println();

        testSignupValidationAndDuplicateProtection();
        testSigninLogoutAndInvalidSessionBehavior();
        testRoutineSaveAndReloadForBug03();
        testDetailedRoutineBuilderSettingsArePreserved();
        testProfileSaveValidationAndSuccess();
        testCaloriesHistoryRecommendationsAndSecurityRegression();

        System.out.println();
        System.out.println("=== Final Test Summary ===");
        System.out.println("Passed checks: " + passedChecks);
        System.out.println("Failed checks: " + failedChecks);

        if (failedChecks > 0) {
            throw new IllegalStateException("One or more integration/regression checks failed.");
        }
    }

    /**
     * Tests signup validation and duplicate-account protection.
     *
     * Exercises the signUp method with invalid and valid input.
     * Signup is one of the first service routes the frontend calls. If this
     * fails, the login/signup screens may appear to work visually but still save
     * bad data or allow duplicate accounts.
     * The method calls FitFlowFacade.signUp with controlled test data and
     * checks the ServiceResponse success flag, error code, and returned user ID.
     * Invalid input should return validation errors, valid input should
     * return a user ID, and duplicate usernames should return an auth error.
     * This test uses a unique username for the valid signup path so rerunning
     * the regression test does not fail because an older users.csv file already
     * contains the same test account.
     * Edge cases tested are too-short username, too-short password, invalid email
     * format, duplicate normalized username, and valid password rules.
     */
    private void testSignupValidationAndDuplicateProtection() {
        System.out.println("TC-AUTH-03 to TC-AUTH-08: Signup validation and duplicate protection");
        FitFlowFacade facade = new FitFlowFacade();

        // Username must be at least the required length.
        // Use a valid password here so this check isolates username validation.
        // The facade rejects the request before account creation.
        String validTestPassword = "Password12345";
        String uniqueUsername = "newuser" + System.currentTimeMillis();
        ServiceResponse<String> shortUsername = facade.signUp("ab", validTestPassword, "ab@example.com");
        checkFalse("Short username is rejected", shortUsername.isSuccess());
        checkEquals("Short username returns validation code", ErrorMessages.CODE_VALIDATION, shortUsername.getErrorCode());

        // Password is present but too short for the validation rule.
        // No account should be created with weak test credentials.
        ServiceResponse<String> shortPassword = facade.signUp("validuser", "pass", "valid@example.com");
        checkFalse("Short password is rejected", shortPassword.isSuccess());
        checkEquals("Short password returns validation code", ErrorMessages.CODE_VALIDATION, shortPassword.getErrorCode());

        // Email is missing the required email format.
        // The service returns a validation error instead of accepting bad contact data.
        ServiceResponse<String> invalidEmail = facade.signUp("validuser", validTestPassword, "userexample.com");
        checkFalse("Invalid email is rejected", invalidEmail.isSuccess());
        checkEquals("Invalid email returns validation code", ErrorMessages.CODE_VALIDATION, invalidEmail.getErrorCode());

        // All fields are valid.
        // The account is created and the facade returns a non-blank user ID.
        ServiceResponse<String> validSignup = facade.signUp(uniqueUsername, validTestPassword, uniqueUsername + "@example.com");
        checkTrue("Valid signup creates an account", validSignup.isSuccess());
        checkNotBlank("Valid signup returns a user ID", validSignup.getData());

        // The same username is submitted again after account creation.
        // Duplicate signup fails so users cannot overwrite each other.
        ServiceResponse<String> duplicateSignup = facade.signUp(uniqueUsername, validTestPassword, uniqueUsername + "@example.com");
        checkFalse("Duplicate username is rejected", duplicateSignup.isSuccess());
        checkEquals("Duplicate username returns auth code", ErrorMessages.CODE_AUTH, duplicateSignup.getErrorCode());
        System.out.println();
    }

    /**
     * Tests session creation, valid session use, logout, and invalidated session
     * protection.
     *
     * Signs in with the demo user, loads workouts, logs out, and tries to
     * reuse the old token.
     * The UI depends on a session token after login. Logout must also stop
     * that same token from being reused.
     * The method calls signIn, getWorkouts, logout, then getWorkouts again
     * with the same token.
     * The first getWorkouts call succeeds, logout succeeds, and the
     * second getWorkouts call fails with a session error.
     * Edge case tested stale session tokens are blocked after logout.
     */
    private void testSigninLogoutAndInvalidSessionBehavior() {
        System.out.println("TC-AUTH-09, TC-AUTH-10, TC-INT-01, TC-INT-04: Signin, logout, and session behavior");
        FitFlowFacade facade = new FitFlowFacade();

        // The built-in demo account signs in successfully.
        // A session token is returned for later service calls.
        ServiceResponse<String> signin = facade.signIn("demo", "password");
        checkTrue("Valid demo signin succeeds", signin.isSuccess());
        checkNotBlank("Signin returns a session token", signin.getData());

        String sessionToken = signin.getData();

        // A valid token can load the starter exercise library.
        // Five starter exercises are present based on the current source baseline.
        ServiceResponse<List<String>> workoutLoad = facade.getWorkouts(sessionToken);
        checkTrue("Valid session loads starter workouts", workoutLoad.isSuccess());
        checkEquals("Starter library contains five exercises", 5, workoutLoad.getData().size());

        // Logout removes the session from the active session map.
        // Logout returns success for the active token.
        ServiceResponse<Boolean> logout = facade.logout(sessionToken);
        checkTrue("Logout succeeds for active session", logout.isSuccess());

        // The same token should not work after logout.
        // The facade returns a session error instead of loading workouts.
        ServiceResponse<List<String>> afterLogout = facade.getWorkouts(sessionToken);
        checkFalse("Logged-out session cannot load workouts", afterLogout.isSuccess());
        checkEquals("Logged-out session returns session error", ErrorMessages.CODE_SESSION, afterLogout.getErrorCode());
        System.out.println();
    }

    /**
     * Retests the service side of BUG-03 and routine save behavior.
     *
     * Checks invalid routine input, valid routine saving, and saved routine
     * reloading.
     * The RoutineBuilderScreen save button was changed to send selected
     * exercises through AppStateManager and FitFlowFacade. This test proves the
     * receiving service method handles those requests correctly.
     * The method signs in, sends invalid save requests, sends a valid save
     * request, then reloads saved routines using the same session.
     * Blank names and empty exercise lists fail; valid selected
     * exercises save and reload.
     * Edge cases tested whitespace-only routine name and empty selected list.
     */
    private void testRoutineSaveAndReloadForBug03() {
        System.out.println("BUG-03, TC-RB-06, TC-INT-05: Routine save and reload regression");
        FitFlowFacade facade = new FitFlowFacade();
        String sessionToken = signInAndReturnToken(facade);

        // A routine with spaces only is not a real routine name.
        // Validation blocks the save before anything is added to memory.
        ServiceResponse<Boolean> blankName = facade.saveWorkout(sessionToken, "   ", Arrays.asList("Push-ups"));
        checkFalse("Blank routine name is rejected", blankName.isSuccess());
        checkEquals("Blank routine name returns validation code", ErrorMessages.CODE_VALIDATION, blankName.getErrorCode());

        // The user clicked save without adding any exercises.
        // The service rejects the request so an empty workout is not saved.
        ServiceResponse<Boolean> noExercises = facade.saveWorkout(sessionToken, "Morning Starter", Arrays.asList());
        checkFalse("Routine with no exercises is rejected", noExercises.isSuccess());
        checkEquals("No-exercise routine returns validation code", ErrorMessages.CODE_VALIDATION, noExercises.getErrorCode());

        // A named routine with selected exercises is valid.
        // The facade returns true so the UI can display a save-success message.
        ServiceResponse<Boolean> saveRoutine = facade.saveWorkout(
                sessionToken,
                "Morning Starter",
                Arrays.asList("Push-ups", "Squats", "Plank")
        );
        checkTrue("Routine save succeeds with name and selected exercises", saveRoutine.isSuccess());
        checkEquals("Routine save returns true", Boolean.TRUE, saveRoutine.getData());

        // Saved data should be available through the same facade after save.
        // The saved routine list contains the routine name and selected exercise text.
        ServiceResponse<List<String>> savedRoutines = facade.getSavedRoutines(sessionToken);
        checkTrue("Saved routines reload succeeds", savedRoutines.isSuccess());
        checkTrue("Saved routine list includes Morning Starter", savedRoutines.getData().toString().contains("Morning Starter"));
        checkTrue("Saved routine list includes selected exercise", savedRoutines.getData().toString().contains("Squats"));

        // A new facade instance simulates closing/reopening the service layer.
        // If the routine was only stored in memory, this check would fail.
        // Passing this proves saveWorkout wrote routine rows into the repository/CSV path.
        FitFlowFacade restartedFacade = new FitFlowFacade();
        String restartedSession = signInAndReturnToken(restartedFacade);
        ServiceResponse<List<String>> persistedRoutines = restartedFacade.getSavedRoutines(restartedSession);
        checkTrue("Repository-backed routine reload succeeds after facade restart", persistedRoutines.isSuccess());
        checkTrue("CSV routine reload still includes Morning Starter", persistedRoutines.getData().toString().contains("Morning Starter"));
        checkTrue("CSV routine reload still includes selected exercise", persistedRoutines.getData().toString().contains("Squats"));
        System.out.println();
    }

    /**
     * Tests the detailed Routine Builder path for sets, reps, and rest values.
     *
     * This protects the UI fix where RoutineBuilderScreen now stores a
     * RoutineExerciseSelection object for each added exercise. The service path
     * must preserve those values inside WorkoutSession so UserGuidedWorkout can
     * display them instead of rebuilding defaults.
     */
    private void testDetailedRoutineBuilderSettingsArePreserved() {
        System.out.println("TC-RB-09 and guided workout regression: builder values are preserved");
        FitFlowFacade facade = new FitFlowFacade();
        String sessionToken = signInAndReturnToken(facade);

        List<RoutineExerciseSelection> selections = Arrays.asList(
                new RoutineExerciseSelection("Push-ups", 5, 12, 60, 45),
                new RoutineExerciseSelection("Sit-ups", 4, 20, 60, 15)
        );

        ServiceResponse<Boolean> saveDetailedRoutine = facade.saveWorkoutWithDetails(
                sessionToken,
                "Custom Builder Routine",
                selections
        );
        checkTrue("Detailed routine save succeeds", saveDetailedRoutine.isSuccess());

        ServiceResponse<WorkoutSession> startDetailedWorkout = facade.startGuidedWorkoutWithDetails(
                sessionToken,
                "Custom Builder Routine",
                selections
        );
        checkTrue("Detailed guided workout starts", startDetailedWorkout.isSuccess());

        WorkoutSession session = startDetailedWorkout.getData();
        checkEquals("First exercise keeps edited sets", 5, session.getRoutineExercises().get(0).getSets());
        checkEquals("First exercise keeps edited reps", 12, session.getRoutineExercises().get(0).getReps());
        checkEquals("First exercise keeps edited rest", 45, session.getRoutineExercises().get(0).getRestSeconds());
        checkEquals("Second exercise keeps edited sets", 4, session.getRoutineExercises().get(1).getSets());
        checkEquals("Second exercise keeps edited reps", 20, session.getRoutineExercises().get(1).getReps());
        checkEquals("Second exercise keeps edited rest", 15, session.getRoutineExercises().get(1).getRestSeconds());
        System.out.println();
    }

    /**
     * Tests the profile-save service route added for the profile screen.
     *
     * Sends invalid and valid UserProfile objects through saveProfile.
     * AppStateManager.saveProfile replaced a placeholder. This test proves
     * the new service route validates profile input and returns clear responses.
     * The method signs in, attempts invalid saves, attempts an invalid
     * session save, and then saves a valid profile.
     * Incomplete/invalid profile values fail; invalid session fails;
     * Valid profile data saves successfully.
     * Edge cases tested are blank first name, zero height, and fake session token.
     */
    private void testProfileSaveValidationAndSuccess() {
        System.out.println("TC-PROF-03, TC-PROF-04, profile save regression");
        FitFlowFacade facade = new FitFlowFacade();
        String sessionToken = signInAndReturnToken(facade);

        // First name is blank.
        // Incomplete profile data returns a validation error.
        UserProfile invalidProfile = new UserProfile("USER-DEMO", "demo", "", "Lewis", 27, 70, 180, "Male");
        ServiceResponse<Boolean> invalidProfileResult = facade.saveProfile(sessionToken, invalidProfile);
        checkFalse("Incomplete profile is rejected", invalidProfileResult.isSuccess());
        checkEquals("Incomplete profile returns validation code", ErrorMessages.CODE_VALIDATION, invalidProfileResult.getErrorCode());

        // Height is zero, which would break fitness calculations later.
        // Invalid height/weight validation blocks the save.
        UserProfile badHeightProfile = new UserProfile("USER-DEMO", "demo", "David", "Lewis", 27, 0, 180, "Male");
        ServiceResponse<Boolean> badHeightResult = facade.saveProfile(sessionToken, badHeightProfile);
        checkFalse("Profile with invalid height is rejected", badHeightResult.isSuccess());
        checkEquals("Invalid height profile returns validation code", ErrorMessages.CODE_VALIDATION, badHeightResult.getErrorCode());

        // Profile is valid, but the session token is not.
        // Session validation runs before saving profile data.
        UserProfile validProfileForInvalidSession = new UserProfile("USER-DEMO", "demo", "David", "Lewis", 27, 70, 180, "Male");
        ServiceResponse<Boolean> invalidSessionResult = facade.saveProfile("fake-session-token", validProfileForInvalidSession);
        checkFalse("Profile save with fake session is rejected", invalidSessionResult.isSuccess());
        checkEquals("Fake session profile save returns session code", ErrorMessages.CODE_SESSION, invalidSessionResult.getErrorCode());

        // Complete profile data and a valid session.
        // The profile saves through the service/repository flow and returns true.
        UserProfile validProfile = new UserProfile("USER-DEMO", "demo", "David", "Lewis", 27, 70, 180, "Male");
        ServiceResponse<Boolean> validProfileResult = facade.saveProfile(sessionToken, validProfile);
        checkTrue("Valid profile save succeeds", validProfileResult.isSuccess());
        checkEquals("Valid profile save returns true", Boolean.TRUE, validProfileResult.getData());

        // Load the current signed-in profile through the facade.
        // This protects the AppStateManager profile screen flow because it now
        // depends on getCurrentUserProfile instead of a hardcoded John Smith user.
        ServiceResponse<UserProfile> loadedProfile = facade.getCurrentUserProfile(sessionToken);
        checkTrue("Current user profile load succeeds", loadedProfile.isSuccess());
        checkEquals("Loaded profile belongs to demo user", "USER-DEMO", loadedProfile.getData().getUserId());
        checkEquals("Loaded profile uses saved first name", "David", loadedProfile.getData().getFirstName());
        checkFalse("Loaded profile is not hardcoded John Smith", "John".equals(loadedProfile.getData().getFirstName())
                && "Smith".equals(loadedProfile.getData().getLastName()));

        // A new facade instance should still load the profile from CSV.
        // This proves the profile screen can use durable repository data after restart.
        FitFlowFacade restartedFacade = new FitFlowFacade();
        String restartedSession = signInAndReturnToken(restartedFacade);
        ServiceResponse<UserProfile> persistedProfile = restartedFacade.getCurrentUserProfile(restartedSession);
        checkTrue("Repository-backed profile load succeeds after facade restart", persistedProfile.isSuccess());
        checkEquals("Persisted profile keeps saved first name", "David", persistedProfile.getData().getFirstName());
        System.out.println();
    }

    /**
     * Tests additional integration behavior that can be affected by service-layer
     * changes.
     *
     * Checks calories, history save/load, recommendations, and password
     * hashing.
     * Regression testing should confirm that new routine/profile save work
     * did not break other facade responsibilities.
     * The method uses a valid session for calculation/history/recommendation
     * calls and uses reflection only for the private password-hash inspection.
     * valid operations succeed, invalid duration fails, history reloads,
     * recommendation text is returned, and the password is not stored as raw text.
     * Edge cases are zero workout duration and raw-password storage.
     */
    private void testCaloriesHistoryRecommendationsAndSecurityRegression() throws Exception {
        System.out.println("TC-CALC, TC-HIST, TC-SEC-01: Integration regression checks");
        FitFlowFacade facade = new FitFlowFacade();
        String sessionToken = signInAndReturnToken(facade);

        // Duration is zero seconds.
        // Calorie calculation should fail instead of returning a misleading estimate.
        ServiceResponse<Double> badCalories = facade.calculateCalories(sessionToken, 7.0, 0);
        checkFalse("Invalid calorie duration is rejected", badCalories.isSuccess());

        // 7 calories per minute for 1800 seconds equals 30 minutes * 7.
        // The estimate should be 210.0 calories.
        ServiceResponse<Double> calories = facade.calculateCalories(sessionToken, 7.0, 1800);
        checkTrue("Valid calorie calculation succeeds", calories.isSuccess());
        checkEquals("Calories calculation returns expected estimate", 210.0, calories.getData());

        // Save a completed workout summary to the user's history list.
        // The save call succeeds and returns true.
        ServiceResponse<Boolean> historySave = facade.saveWorkoutHistory(sessionToken, "Morning Starter completed", 1800);
        checkTrue("Workout history save succeeds", historySave.isSuccess());
        checkEquals("Workout history save returns true", Boolean.TRUE, historySave.getData());

        // Saved history should reload for the same signed-in user.
        // The history list contains the summary just saved.
        ServiceResponse<List<String>> historyLoad = facade.getWorkoutHistory(sessionToken);
        checkTrue("Workout history reload succeeds", historyLoad.isSuccess());
        checkTrue("Workout history contains saved summary", historyLoad.getData().toString().contains("Morning Starter completed"));

        // Restart the facade and reload the demo user's history.
        // If history was only stored in memory, this restarted instance would
        // have an empty history list. Passing proves CSV/repository persistence.
        FitFlowFacade restartedFacade = new FitFlowFacade();
        String restartedSession = signInAndReturnToken(restartedFacade);
        ServiceResponse<List<String>> persistedHistory = restartedFacade.getWorkoutHistory(restartedSession);
        checkTrue("Repository-backed workout history reload succeeds after facade restart", persistedHistory.isSuccess());
        checkTrue("CSV history reload contains saved summary", persistedHistory.getData().toString().contains("Morning Starter completed"));

        // Recommendation requests should return usable workout guidance.
        // Strength recommendation includes a push-focused suggestion in current baseline.
        ServiceResponse<String> recommendation = facade.getRecommendations(sessionToken, "strength");
        checkTrue("Recommendation request succeeds", recommendation.isSuccess());
        checkTrue("Strength recommendation includes a workout suggestion", recommendation.getData().toLowerCase().contains("push"));

        // Inspect private demo account only to confirm raw password is not stored.
        // Stored value is a long hash and does not equal the original password text.
        checkPasswordIsNotStoredAsRawText(facade);
        System.out.println();
    }

    /**
     * Signs in with the demo account and returns the active session token.
     *
     * Centralizes repeated signin steps used by several tests.
     * Multiple tests require a valid session before calling service methods.
     * Calls facade.signIn("demo", "password") and validates the returned token.
     * the demo account exists in the current facade baseline.
     * if the demo account is removed or the password changes,
     * the helper fails immediately and makes the broken setup obvious.
     *
     * @param facade service facade under test.
     * @return active session token for the demo user.
     */
    private String signInAndReturnToken(FitFlowFacade facade) {
        ServiceResponse<String> signin = facade.signIn("demo", "password");
        checkTrue("Helper signin succeeds", signin.isSuccess());
        checkNotBlank("Helper signin returns session token", signin.getData());
        return signin.getData();
    }

    /**
     * Confirms that the demo password is stored as a hash and not plain text.
     *
     * Uses reflection to read the private usersByUsername map and the
     * private passwordHash field from the demo account.
     * FitFlowFacade intentionally hides account internals. Reflection is
     * used only in this test so the production class can remain encapsulated.
     * The method reads the stored value and checks length plus direct text
     * comparison.
     * The stored password value should be at least SHA-256 hash length
     * and should not equal "password".
     * Edge case tested accidental raw password storage during future edits.
     *
     * @param facade service facade containing the demo account.
     * @throws Exception if reflection access fails because the implementation changed.
     */
    private void checkPasswordIsNotStoredAsRawText(FitFlowFacade facade) throws Exception {
        Field usersField = FitFlowFacade.class.getDeclaredField("usersByUsername");
        usersField.setAccessible(true);
        Map<?, ?> users = (Map<?, ?>) usersField.get(facade);
        Object demoAccount = users.get("demo");

        Field passwordHashField = demoAccount.getClass().getDeclaredField("passwordHash");
        passwordHashField.setAccessible(true);
        String storedPasswordValue = (String) passwordHashField.get(demoAccount);

        checkTrue("Stored password value is hashed length", storedPasswordValue.length() >= 64);
        checkFalse("Stored password value is not raw text", "password".equals(storedPasswordValue));
    }

    /**
     * Records a passing check when the condition is true.
     *
     * Standardizes true-condition assertions for this plain Java runner.
     * Using one helper keeps output consistent and avoids duplicated PASS/FAIL logic.
     * A true condition calls pass; a false condition calls fail.
     * Every call should describe the requirement being checked.
     *
     * @param label human-readable check description.
     * @param condition result being verified.
     */
    private void checkTrue(String label, boolean condition) {
        if (condition) {
            pass(label);
        } else {
            fail(label);
        }
    }

    /**
     * Records a passing check when the condition is false.
     *
     * Convenience wrapper for negative assertions.
     * Many validation tests expect a request to fail safely.
     * Inverts the condition and passes it to checkTrue.
     * Failed service calls should use this helper when failure is correct.
     *
     * @param label human-readable check description.
     * @param condition result expected to be false.
     */
    private void checkFalse(String label, boolean condition) {
        checkTrue(label, !condition);
    }

    /**
     * Compares expected and actual values.
     *
     * Checks exact equality for strings, integers, booleans, and doubles.
     * Some tests need more than success/failure; they need to confirm exact
     * error codes, counts, or calculated values.
     * Uses null-safe equals and prints both values if the check fails.
     * Actual values should match the requirement documented by the test label.
     *
     * @param label human-readable check description.
     * @param expected required value.
     * @param actual value returned by the code under test.
     */
    private void checkEquals(String label, Object expected, Object actual) {
        if (expected == null ? actual == null : expected.equals(actual)) {
            pass(label);
        } else {
            fail(label + " | expected=" + expected + ", actual=" + actual);
        }
    }

    /**
     * Confirms that a string exists and contains non-space characters.
     *
     * Validates returned IDs and session tokens.
     * A success response is not enough if the returned token or ID is blank.
     * Checks for not-null and at least one non-whitespace character.
     * user IDs and session tokens should be usable strings.
     *
     * @param label human-readable check description.
     * @param value string returned by the service layer.
     */
    private void checkNotBlank(String label, String value) {
        checkTrue(label, value != null && !value.trim().isEmpty());
    }

    /**
     * Records one successful check and prints the result.
     *
     * @param label human-readable check description.
     */
    private void pass(String label) {
        passedChecks++;
        System.out.println("PASS - " + label);
    }

    /**
     * Records one failed check and prints the result.
     *
     * @param label human-readable check description.
     */
    private void fail(String label) {
        failedChecks++;
        System.out.println("FAIL - " + label);
    }
}
