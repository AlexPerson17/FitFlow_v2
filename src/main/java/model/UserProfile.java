/*
 * File: UserProfile.java
 * Author: Michael Lee
 * Contributors: Alex Ronn, Travisha Mills
 * Course: CMSC 495
 * Project: FitFlow
 * Date: June 2026
 * Version: 1.1
 *
 * Description:
 * This file stores profile information for one FitFlow user.
 */

package model;

public class UserProfile {

    // Basic user profile details
    private String userId;
    private String username;
    private String firstName;
    private String lastName;
    private int age;
    private double height;
    private double weight;
    private String gender;

    /*
     * Creates a user profile with the main profile details.
     */
    public UserProfile(
            String userId,
            String username,
            String firstName,
            String lastName,
            int age,
            double height,
            double weight,
            String gender) {

        this.userId = userId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.gender = gender;
    }

    /*
     * Gets the user's ID.
     */
    public String getUserId() {
        return userId;
    }

    /*
     * Gets the username.
     */
    public String getUsername() {
        return username;
    }

    /*
     * Gets the first name.
     */
    public String getFirstName() {
        return firstName;
    }

    /*
     * Gets the last name.
     */
    public String getLastName() {
        return lastName;
    }

    /*
     * Gets the user's age.
     */
    public int getAge() {
        return age;
    }

    /*
     * Gets the user's height.
     */
    public double getHeight() {
        return height;
    }

    /*
     * Gets the user's weight.
     */
    public double getWeight() {
        return weight;
    }

    /*
     * Gets the user's gender.
     */
    public String getGender() {
        return gender;
    }

    /*
     * Updates the first name.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /*
     * Updates the last name.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /*
     * Updates the age.
     */
    public void setAge(int age) {
        this.age = age;
    }

    /*
     * Updates the height.
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /*
     * Updates the weight.
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /*
     * Updates the gender.
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /*
     * Turns the profile into one CSV row.
     */
    public String toCSV() {
        return userId + "," +
                username + "," +
                firstName + "," +
                lastName + "," +
                age + "," +
                height + "," +
                weight + "," +
                gender;
    }

    /*
     * Shows the profile in a simple readable way.
     */
    @Override
    public String toString() {
        return "User: " + username +
                "\nFirst Name: " + firstName +
                "\nLast Name: " + lastName +
                "\nAge: " + age +
                "\nHeight: " + height +
                "\nWeight: " + weight +
                "\nGender: " + gender;
    }
}
