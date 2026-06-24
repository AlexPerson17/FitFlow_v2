/*
 * File: AppStateManager.java
 * Version: 0.6.4
 * Date last edited: 6/22/2026
 * Author: Alex Ronn
 * Modified by: David Lewis
 * File Purpose: Manages swapping between different pages in the application.
 * Update Notes: Added routine-save routing and profile-save routing so JavaFX
 * screens send save requests through AppStateManager before reaching
 * FitFlowFacade. This keeps the view layer separate from the service layer and
 * matches the team UML flow. Updated profile loading so the profile screen now
 * asks FitFlowFacade for the signed-in user's profile instead of creating a
 * hardcoded John Smith test profile.
 */

package view;

import java.util.List;

import javafx.stage.Stage;
import model.RoutineExerciseSelection;
import model.UserProfile;
import model.WorkoutSession;
import service.*;

public class AppStateManager {

    private final Stage primaryStage;
    private LoginScreen loginScreen;
    private SignupScreen signupScreen; 
    private ProfileScreen profileScreen;
    private FitFlowFacade facade;
    private String sessionToken;

    public AppStateManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
        facade = new FitFlowFacade();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Shows the login screen.
     */
    public void showLoginScreen() {

    	if (loginScreen == null)
    		loginScreen = new LoginScreen(this);
    	else
    		loginScreen.clearPassword();

        primaryStage.setScene(
                loginScreen.getScene()
        );
    }

    /**
     * Shows the signup screen.
     */
    public void showSignupScreen() {

    	if (signupScreen == null)
    		signupScreen = new SignupScreen(this);
    	else
    		signupScreen.clearPasswords();

        primaryStage.setScene(
                signupScreen.getScene()
        );
    }
    
    /**
     * Shows the Dashboard screen.
     */
    public void showDashboardScreen() {

    	DashboardScreen dashboardScreen =
                new DashboardScreen(this);

        primaryStage.setScene(
        		dashboardScreen.getScene()
        );
    }
    
    /**
     * Shows the routine builder screen.
     */
    public void showRoutineBuilderScreen() {

    	RoutineBuilderScreen routineBuilderScreen =
                new RoutineBuilderScreen(this);

    	routineBuilderScreen.show(primaryStage);
    }
    
    /**
     * Shows the workout history screen.
     */
    public void showWorkoutHistoryScreen() {
        WorkoutHistoryScreen historyScreen = new WorkoutHistoryScreen(this);
        primaryStage.setScene(historyScreen.getScene());
    }
    
    /**
     * Shows the profile screen for the signed-in user.
     *
     * The older Phase I version created a hardcoded John Smith profile just to
     * prove the screen could open. That was useful for early UI testing, but it
     * did not match the real login flow. This version asks FitFlowFacade for the
     * profile tied to the current session token, then builds the screen from
     * that returned UserProfile object.
     */
    public void showProfileScreen() {
        ServiceResponse<UserProfile> profileLoad = facade.getCurrentUserProfile(sessionToken);

        if (profileLoad.isSuccess()) {
            profileScreen = new ProfileScreen(this, profileLoad.getData());
        } else {
            showLoginScreen();
            loginScreen.showError(profileLoad.getMessage());
            return;
        }

        primaryStage.setScene(
        		profileScreen.getScene()
        );
    }
    
    /**
     * Shows the guided workout screen for the active session.
     *
     * The screen reads the current WorkoutSession through
     * getCurrentWorkoutSession(), so startGuidedWorkout() must have been
     * called before navigating here. RoutineBuilderScreen already does this
     * via startWorkout() → stateManager.startGuidedWorkout().
     */
    public void showGuidedWorkoutScreen() {
        UserGuidedWorkout guidedWorkout = new UserGuidedWorkout(this);
        guidedWorkout.show(primaryStage);
    }

    /**
     * Navigates to the Congratulations screen shown when a workout completes.
     * Called by WorkoutTimer after saveCompletedWorkoutToHistory() succeeds.
     *
     * @param durationSeconds total workout duration in seconds
     * @param exerciseCount   number of exercises in the completed workout
     */
    public void showCongratulationsScreen(int durationSeconds, int exerciseCount) {
        CongratulationsScreen screen =
                new CongratulationsScreen(this, durationSeconds, exerciseCount);
        primaryStage.setScene(screen.getScene());
    }

    /*
     * Handles attempts to log in
     */
    public void signInAttempt(String username, String password) {
    	ServiceResponse<String> attempt = facade.signIn(username, password);
    	if (attempt.isSuccess()) {
    		sessionToken = attempt.getData();
    		showDashboardScreen();
    	} else {
    		String reason = attempt.getMessage();
    		loginScreen.showError(reason);
    	}
    }
    
    /*
     * Handles logging out
     */
    public void logOut() {
    	ServiceResponse<Boolean> attempt = facade.logout(sessionToken);
    	if (attempt.isSuccess()) {
    		loginScreen.showSuccess(attempt.getMessage());
    		showLoginScreen();
    	} else {
    		loginScreen.showError(attempt.getMessage());
    		showLoginScreen();
    	}
    }
    
    /*
     * Handles attempts to sign up
     */
    public void signUpAttempt(String username, String password, String email) {
    	ServiceResponse<String> attempt = facade.signUp(username, password, email);
    	if (attempt.isSuccess()) {
    		showLoginScreen();
    		loginScreen.showSuccess(attempt.getMessage());
    		loginScreen.fillUsername(username);
    	} else {
    		String reason = attempt.getMessage();
    		signupScreen.showError(reason);
    	}
    }
    
    /*
     * Gets the exercise names that should be displayed in the routine builder.
     *
     * Requests the exercise list from FitFlowFacade.
     * Keeps the JavaFX screen from building its own workout data.
     * Sends the current session token to the service layer and returns
     * only the list data when the request succeeds.
     */
    public List<String> getExercises() {
    	ServiceResponse<List<String>> attempt = facade.getWorkouts(sessionToken);
    	if (attempt.isSuccess()) {
    		return attempt.getData();
    	}
    	return List.of();
    }
    
    /*
     * Gets the list of the names of workouts that have been completed.
     *
     * Requests the workout list from FitFlowFacade.
     * Sends the current session token to the service layer and returns
     * only the list data when the request succeeds.
     */
    public List<String> getWorkoutHistory() {
        ServiceResponse<List<String>> attempt = facade.getWorkoutHistory(sessionToken);
        if (attempt.isSuccess()) {
            return attempt.getData();
        }
        return List.of();
    }
    
    /*
     * Sends a routine save request from the UI to the service facade.
     *
     * Receives a routine name and selected exercise names from the
     * routine builder screen.
     * The view package should not call FitFlowFacade directly.
     * AppStateManager passes the current session token and routine data
     * to FitFlowFacade.saveWorkout, then returns the ServiceResponse so the
     * screen can display the real success or error message.
     */
    public ServiceResponse<Boolean> saveRoutine(String routineName, List<String> exerciseNames) {
        return facade.saveWorkout(sessionToken, routineName, exerciseNames);
    }


    /*
     * Saves a routine with the user's selected sets, reps, duration, and rest.
     * RoutineBuilderScreen calls this detailed path when the user changes the
     * builder controls so those values are not lost before repository saving.
     */
    public ServiceResponse<Boolean> saveRoutineWithDetails(String routineName,
                                                           List<RoutineExerciseSelection> selectedExercises) {
        return facade.saveWorkoutWithDetails(sessionToken, routineName, selectedExercises);
    }
    
    /*
     * Sends a profile save request from the UI to the service facade.
     *
     * Receives updated profile data from ProfileScreen.
     * ProfileScreen should not save directly to CSV or call backend files
     * because the UI layer should stay separate from service/data logic.
     * AppStateManager passes the active session token and UserProfile
     * object to FitFlowFacade.saveProfile, then returns the ServiceResponse so
     * ProfileScreen can show a clear success or error message.
     */
    public ServiceResponse<Boolean> saveProfile(UserProfile profileData) {
        return facade.saveProfile(sessionToken, profileData);
    }

    // Start workout
    public ServiceResponse<WorkoutSession> startGuidedWorkout(String routineName,
                                                              List<String> exercises,
                                                              int exerciseDuration,
                                                              int restDuration) {

        // Sends workout start request to the backend timer service.
        return facade.startGuidedWorkout(
                sessionToken,
                routineName,
                exercises,
                exerciseDuration,
                restDuration
        );
    }


    /*
     * Starts a guided workout with the detailed values from Routine Builder.
     * This is the path used by the Start Exercise button after the user changes
     * sets, reps, or rest values in the builder UI.
     */
    public ServiceResponse<WorkoutSession> startGuidedWorkoutWithDetails(String routineName,
                                                                         List<RoutineExerciseSelection> selectedExercises) {
        return facade.startGuidedWorkoutWithDetails(
                sessionToken,
                routineName,
                selectedExercises
        );
    }

    // Pause Workout
    public ServiceResponse<WorkoutSession> pauseGuidedWorkout() {

        // Sends pause request to the backend timer service.
        return facade.pauseGuidedWorkout(sessionToken);
    }

    // Resume Workout
    public ServiceResponse<WorkoutSession> resumeGuidedWorkout() {

        // Sends resume request to the backend timer service.
        return facade.resumeGuidedWorkout(sessionToken);
    }

    // Reset Workout
    public ServiceResponse<WorkoutSession> resetGuidedWorkout() {

        // Sends reset request to the backend timer service.
        return facade.resetGuidedWorkout(sessionToken);
    }

    // Skip Exercise
    public ServiceResponse<WorkoutSession> skipGuidedWorkoutStep() {

        // Sends skip request to move to next exercise.
        return facade.skipGuidedWorkoutStep(sessionToken);
    }

    //Timer Tick
    public ServiceResponse<WorkoutSession> tickGuidedWorkout() {

        // Updates the timer by one second.
        return facade.tickGuidedWorkout(sessionToken);
    }

    //Current Session
    public ServiceResponse<WorkoutSession> getCurrentWorkoutSession() {

        // Returns the current guided workout state.
        return facade.getCurrentWorkoutSession(sessionToken);
    }

    //Save Completed Workout
    public ServiceResponse<Boolean> saveCompletedWorkout() {

        // Saves completed guided workout to workout history.
        return facade.saveCompletedGuidedWorkout(sessionToken);
    }

    /*
     * Saves a completed guided workout directly from the JavaFX timer screen.
     *
     * The guided workout UI uses its own visual countdown timer. Because that
     * screen was not sending UI tick back to TimerService, the backend
     * WorkoutSession was not marked complete. This bridge lets WorkoutTimer save the completed
     * workout summary through the normal FitFlowFacade.saveWorkoutHistory path
     * as soon as the user-facing workout finishes.
     */
    public ServiceResponse<Boolean> saveCompletedWorkout(String workoutSummary, int durationSeconds) {
        return facade.saveWorkoutHistory(sessionToken, workoutSummary, durationSeconds);
    }
}
