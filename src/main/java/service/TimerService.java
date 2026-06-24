/*
 * File: TimerService.java
 * Editor: Travisha Mills
 * Updated: David Lewis
 * Project: FitFlow
 * Date: June 22, 2026
 * Version: 6.1
 * Description:
 * Handles guided workout timer logic for start, pause, resume, reset,
 * skip, rest periods, and workout completion.
 */

package service;

import model.RoutineExerciseSelection;
import model.WorkoutSession;

import java.util.List;

public class TimerService {

    private WorkoutSession currentSession;

    public ServiceResponse<WorkoutSession> startWorkout(String routineName,
                                                        List<String> exercises,
                                                        int exerciseDurationSeconds,
                                                        int restDurationSeconds) {
        // Existing name-only path used by older tests and simple service calls.
        if (ValidationUtil.isBlank(routineName)) {
            return ServiceResponse.error("Routine name is required.", ErrorMessages.CODE_VALIDATION);
        }

        if (!ValidationUtil.hasSelectedExercises(exercises)) {
            return ServiceResponse.error(ErrorMessages.EXERCISE_LIST_REQUIRED, ErrorMessages.CODE_VALIDATION);
        }

        if (!ValidationUtil.isValidDuration(exerciseDurationSeconds)) {
            return ServiceResponse.error(ErrorMessages.INVALID_DURATION, ErrorMessages.CODE_VALIDATION);
        }

        if (restDurationSeconds < 0) {
            return ServiceResponse.error("Rest time cannot be negative.", ErrorMessages.CODE_VALIDATION);
        }

        currentSession = new WorkoutSession(
                routineName,
                exercises,
                exerciseDurationSeconds,
                restDurationSeconds
        );

        return ServiceResponse.success("Workout started successfully.", currentSession);
    }

    /*
     * Starts a guided workout using the detailed settings from Routine Builder.
     *
     * Accepts one RoutineExerciseSelection per selected routine item.
     * The UI can change sets, reps, and rest values, so the timer must
     *      receive those values instead of rebuilding defaults.
     * Valid selections are placed into WorkoutSession, which the guided
     *      workout screen reads when building its queue.
     */
    public ServiceResponse<WorkoutSession> startWorkoutWithSelections(String routineName,
                                                                      List<RoutineExerciseSelection> routineExercises) {
        if (ValidationUtil.isBlank(routineName)) {
            return ServiceResponse.error("Routine name is required.", ErrorMessages.CODE_VALIDATION);
        }

        if (routineExercises == null || routineExercises.isEmpty()) {
            return ServiceResponse.error(ErrorMessages.EXERCISE_LIST_REQUIRED, ErrorMessages.CODE_VALIDATION);
        }

        for (RoutineExerciseSelection selection : routineExercises) {
            if (selection == null || !selection.isValidSelection()) {
                return ServiceResponse.error("Routine exercise settings are invalid.", ErrorMessages.CODE_VALIDATION);
            }
        }

        currentSession = new WorkoutSession(
                routineName,
                routineExercises,
                routineExercises.get(0).getWorkSeconds(),
                routineExercises.get(0).getRestSeconds(),
                true
        );

        return ServiceResponse.success("Workout started successfully.", currentSession);
    }

    public ServiceResponse<WorkoutSession> pauseWorkout() {
        // Pauses the current workout session.
        if (!hasActiveSession()) {
            return ServiceResponse.error("No active workout session found.", ErrorMessages.CODE_VALIDATION);
        }

        currentSession.pause();

        return ServiceResponse.success("Workout paused.", currentSession);
    }

    public ServiceResponse<WorkoutSession> resumeWorkout() {
        // Resumes the current workout session.
        if (!hasActiveSession()) {
            return ServiceResponse.error("No active workout session found.", ErrorMessages.CODE_VALIDATION);
        }

        currentSession.resume();

        return ServiceResponse.success("Workout resumed.", currentSession);
    }

    public ServiceResponse<WorkoutSession> resetWorkout() {
        // Resets the current workout session to the beginning.
        if (currentSession == null) {
            return ServiceResponse.error("No workout session found.", ErrorMessages.CODE_VALIDATION);
        }

        currentSession.reset();

        return ServiceResponse.success("Workout reset.", currentSession);
    }

    public ServiceResponse<WorkoutSession> skipCurrentStep() {
        // Skips the current exercise or rest period.
        if (!hasActiveSession()) {
            return ServiceResponse.error("No active workout session found.", ErrorMessages.CODE_VALIDATION);
        }

        currentSession.skip();

        if (currentSession.isCompleted()) {
            return ServiceResponse.success("Workout completed.", currentSession);
        }

        return ServiceResponse.success("Skipped to next step.", currentSession);
    }

    public ServiceResponse<WorkoutSession> tickWorkout() {
        // Moves the workout timer forward by one second.
        if (!hasActiveSession()) {
            return ServiceResponse.error("No active workout session found.", ErrorMessages.CODE_VALIDATION);
        }

        currentSession.tick();

        if (currentSession.isCompleted()) {
            return ServiceResponse.success("Workout completed.", currentSession);
        }

        return ServiceResponse.success("Timer updated.", currentSession);
    }

    public ServiceResponse<WorkoutSession> getCurrentSession() {
        // Returns the current workout session.
        if (currentSession == null) {
            return ServiceResponse.error("No workout session found.", ErrorMessages.CODE_VALIDATION);
        }

        return ServiceResponse.success("Workout session loaded.", currentSession);
    }

    public ServiceResponse<String> getCompletionSummary() {
        // Returns the summary after a workout is completed.
        if (currentSession == null || !currentSession.isCompleted()) {
            return ServiceResponse.error("Workout is not complete yet.", ErrorMessages.CODE_VALIDATION);
        }

        return ServiceResponse.success("Workout summary created.", currentSession.getCompletionSummary());
    }

    private boolean hasActiveSession() {
        // Checks that a workout exists and has not finished.
        return currentSession != null && !currentSession.isCompleted();
    }
}
