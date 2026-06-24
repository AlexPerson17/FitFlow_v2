/*
 * File: Calculator.java
 * Author: Michael Lee
 * Course: CMSC 495
 * Project: FitFlow
 * Date: June 2026
 * Version: 1.1
 *
 * Description:
 * This file handles simple fitness calculations for FitFlow.
 */

package util;

public class Calculator {

    /*
     * Calculates BMI using weight and height.
     */
    public static double calculateBMI(double weight, double height) {
        if (height <= 0) {
            return 0;
        }

        return 703 * weight / (height * height);
    }

    /*
     * Gives a basic BMR estimate.
     */
    public static double calculateBMR(double weight, double height, int age) {
        if (weight <= 0 || height <= 0 || age <= 0) {
            return 0;
        }

        return 10 * weight + 6.25 * height - 5 * age + 5;
    }

    /*
     * Estimates calories burned during a workout.
     */
    public static double calculateCaloriesBurned(int durationMinutes, double caloriesPerMinute) {
        if (durationMinutes <= 0 || caloriesPerMinute <= 0) {
            return 0;
        }

        return durationMinutes * caloriesPerMinute;
    }

    /*
     * Calculates the full workout time.
     */
    public static int calculateTotalWorkoutTime(int duration, int restTime, int sets) {
        if (duration <= 0 || restTime < 0 || sets <= 0) {
            return 0;
        }

        return (duration + restTime) * sets;
    }
}
