/*
 * File: ErrorMessages.java
 * Project: FitFlow - Interactive Workout Assistant
 * Course: UMGC CMSC 495
 * Phase: Phase II Source Code
 * Week: 6
 * Version: v0.6.01
 * Author: David Lewis
 * Last Updated: 2026-06-20
 *
 * Purpose:
 * Centralizes common success and error messages so frontend and backend
 * code use the same wording for validation, security, data loading, and
 * integration failures.
 *
 * Dependencies:
 * Java Standard Library only.
 */

/**
 * Stores shared FitFlow messages and error codes.
 */

package service;

public final class ErrorMessages {
    private ErrorMessages() {
        // Utility class should not be instantiated.
    }

    public static final String SUCCESS_SIGNUP = "Account created successfully.";
    public static final String SUCCESS_LOGIN = "Login successful.";
    public static final String SUCCESS_LOGOUT = "Logout successful.";
    public static final String SUCCESS_WORKOUTS_LOADED = "Exercise library loaded successfully.";
    public static final String SUCCESS_ROUTINE_SAVED = "Workout routine saved successfully.";
    public static final String SUCCESS_PROFILE_SAVED = "Profile saved successfully.";
    public static final String SUCCESS_HISTORY_LOADED = "Workout history loaded successfully.";
    public static final String SUCCESS_CALORIES_CALCULATED = "Calories calculated successfully.";
    public static final String SUCCESS_RECOMMENDATION_CREATED = "Workout recommendation created successfully.";

    public static final String USERNAME_REQUIRED = "Username must be at least 3 characters.";
    public static final String PASSWORD_REQUIRED = "Password is required.";
    public static final String EMAIL_REQUIRED = "Email is required.";
    public static final String INVALID_EMAIL = "Email must contain @ and a valid domain.";
    public static final String PASSWORD_TOO_SHORT = "Password must be at least 12 characters and include at least 1 number.";
    public static final String INVALID_LOGIN = "Invalid username or password.";
    public static final String USER_ALREADY_EXISTS = "A user with this username already exists.";
    public static final String SESSION_EXPIRED = "Session expired or invalid. Please log in again.";
    public static final String ROUTINE_NAME_REQUIRED = "Workout routine name is required.";
    public static final String EXERCISE_LIST_REQUIRED = "At least one exercise must be selected.";
    public static final String INVALID_DURATION = "Workout duration must be greater than zero.";
    public static final String INVALID_HEIGHT_WEIGHT = "Height and weight must be greater than zero.";
    public static final String INVALID_PROFILE = "Profile information is incomplete or invalid.";
    public static final String DATA_LOAD_FAILURE = "Data could not be loaded. Please try again.";
    public static final String DATA_SAVE_FAILURE = "Data could not be saved. Please try again.";

    public static final String CODE_VALIDATION = "VALIDATION_ERROR";
    public static final String CODE_AUTH = "AUTH_ERROR";
    public static final String CODE_SESSION = "SESSION_ERROR";
    public static final String CODE_DATA = "DATA_ERROR";
    public static final String CODE_CALCULATION = "CALCULATION_ERROR";
}


