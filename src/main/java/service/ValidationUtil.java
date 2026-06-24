/*
 * File: ValidationUtil.java
 * Project: FitFlow - Interactive Workout Assistant
 * Course: UMGC CMSC 495
 * Phase: Phase I Source Code
 * Week: 4
 * Version: v0.4.03
 * Author: David Lewis
 * Last Updated: 2026-06-07
 *
 * Purpose:
 * Provides shared validation helper methods for usernames, passwords,
 * email addresses, profile values, workout names, and exercise selections.
 *
 * Dependencies:
 * Java Standard Library only.
 */
package service;

import java.util.List;


/**
 * Shared validation methods for FitFlow Phase I source code.
 */
public final class ValidationUtil {
    private ValidationUtil() {
        // Utility class should not be instantiated.
    }

    /**
     * Checks whether a text value is null, empty, or only whitespace.
     *
     * @param value Text value to evaluate.
     * @return true if the value is missing; false otherwise.
     */
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Validates a username.
     *
     * @param username Username entered by the user.
     * @return true if username is usable; false otherwise.
     */
    public static boolean isValidUsername(String username) {
        return !isBlank(username) && username.trim().length() >= 3;
    }

    /**
     * Validates a password for Phase I requirements.
     *
     * @param password Password entered by the user.
     * @return true if password meets the minimum length requirement and number.
     */
    public static boolean isValidPassword(String password) {
        // Password must be at least 12 characters and contain one number.
        return password != null
                && password.length() >= 12
                && password.matches(".*\\d.*");
    }

    /**
     * Validates a basic email format.
     *
     * @param email Email entered by the user.
     * @return true if the email contains basic expected email characters.
     */
    public static boolean isValidEmail(String email) {
        if (isBlank(email)) {
            return false;
        }

        String trimmedEmail = email.trim();
        return trimmedEmail.contains("@")
                && trimmedEmail.contains(".")
                && trimmedEmail.indexOf("@") > 0
                && trimmedEmail.lastIndexOf(".") > trimmedEmail.indexOf("@") + 1;
    }

    /**
     * Validates height and weight values for profile and calculation features.
     *
     * @param height User height value.
     * @param weight User weight value.
     * @return true if both values are greater than zero.
     */
    public static boolean isValidHeightWeight(double height, double weight) {
        return height > 0 && weight > 0;
    }

    /**
     * Validates a workout routine name.
     *
     * @param routineName Name entered for the workout routine.
     * @return true if the name is not blank.
     */
    public static boolean isValidRoutineName(String routineName) {
        return !isBlank(routineName);
    }

    /**
     * Validates whether a workout routine includes at least one exercise.
     *
     * @param exerciseNames List of selected exercise names.
     * @return true if the list exists and contains at least one entry.
     */
    public static boolean hasSelectedExercises(List<String> exerciseNames) {
        return exerciseNames != null && !exerciseNames.isEmpty();
    }

    /**
     * Validates a workout duration.
     *
     * @param durationSeconds Workout duration in seconds.
     * @return true if duration is greater than zero.
     */
    public static boolean isValidDuration(int durationSeconds) {
        return durationSeconds > 0;
    }
}