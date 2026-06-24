/*
 * File: ProfileScreen.java
 * Version: 0.6.1
 * Date last edited: 6/20/2026
 * Author: Alex Ronn
 * Modified by: David Lewis
 * File Purpose: This class builds the profile screen
 * 		and allows the user to edit values.
 * Update Notes: Connected the Save button to the AppStateManager profile-save
 * bridge and added clear status feedback so profile updates no longer use the
 * placeholder save path.
 */

package view;

import java.text.DecimalFormat;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.UserProfile;
import service.ServiceResponse;
import util.Calculator;

public class ProfileScreen extends BaseScreen {

    private Scene scene;
    
    private UserProfile profile;

    private TextField firstNameField;
    private TextField lastNameField;
    private TextField ageField;
    private TextField weightField;
    private TextField heightField;

    private RadioButton maleButton;
    private RadioButton femaleButton;
    private RadioButton otherButton;

    private Label bmiLabel;
    private Label saveStatusLabel;

    public ProfileScreen(
            AppStateManager stateManager,
            UserProfile profile) {

        super(stateManager);

        this.profile = profile;

        buildScreen();
    }

    private void buildScreen() {
        StackPane root = createRootLayout();

        VBox page = new VBox();
        page.setPrefSize(390, 844);
        page.setMinSize(390, 844);
        page.setMaxSize(390, 844);

        // ── Header (75px) ─────────────────────────────────────────────────
        StackPane header = new StackPane();
        header.setPrefSize(390, 75);
        header.setMinSize(390, 75);
        header.setMaxSize(390, 75);
        header.setBackground(new Background(new BackgroundFill(
                PRIMARY_BLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        VBox titles = new VBox(3);
        titles.setAlignment(Pos.CENTER);
        Label appTitle = new Label("FitFlow");
        appTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: white;");
        Label screenTitle = new Label("Your Profile");
        screenTitle.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: white;");
        titles.getChildren().addAll(appTitle, screenTitle);

        Button hamburger = createHeaderHamburgerButton(root, 75);
        header.getChildren().addAll(titles, hamburger);
        StackPane.setAlignment(titles, Pos.CENTER);
        StackPane.setAlignment(hamburger, Pos.TOP_RIGHT);
        StackPane.setMargin(hamburger, new Insets(8, 8, 0, 0));

        // ── Scrollable white content ───────────────────────────────────────
        VBox formBox = new VBox(10);
        formBox.setPadding(new Insets(20, 20, 20, 20));
        formBox.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        String fieldStyle = "-fx-background-color: #F1F1F1; -fx-background-radius: 6;" +
                            " -fx-font-size: 13; -fx-pref-height: 36;";

        // Avatar / BMI row
        Label avatarCircle = new Label();
        avatarCircle.setPrefSize(52, 52);
        avatarCircle.setMinSize(52, 52);
        avatarCircle.setBackground(new Background(new BackgroundFill(
                LOGO_GRAY, new CornerRadii(26), Insets.EMPTY)));

        bmiLabel = new Label("---");
        bmiLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #3F6FB5;");

        Label bmiHeader = new Label("BMI");
        bmiHeader.setStyle("-fx-font-size: 11; -fx-text-fill: #888;");

        VBox bmiBox = new VBox(2, bmiHeader, bmiLabel);

        HBox avatarRow = new HBox(12, avatarCircle, bmiBox);
        avatarRow.setAlignment(Pos.CENTER_LEFT);
        avatarRow.setPadding(new Insets(0, 0, 8, 0));

        // Name fields
        firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        firstNameField.setStyle(fieldStyle);

        lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        lastNameField.setStyle(fieldStyle);

        HBox nameRow = new HBox(10, firstNameField, lastNameField);
        HBox.setHgrow(firstNameField, Priority.ALWAYS);
        HBox.setHgrow(lastNameField, Priority.ALWAYS);

        // Age / Weight fields
        ageField = new TextField();
        ageField.setPromptText("Age");
        ageField.setStyle(fieldStyle);

        weightField = new TextField();
        weightField.setPromptText("Weight (lbs)");
        weightField.setStyle(fieldStyle);
        weightField.textProperty().addListener((obs, o, n) -> updateBMILabel());

        HBox ageWeightRow = new HBox(10, ageField, weightField);
        HBox.setHgrow(ageField, Priority.ALWAYS);
        HBox.setHgrow(weightField, Priority.ALWAYS);

        // Height field
        heightField = new TextField();
        heightField.setPromptText("Height (in)");
        heightField.setStyle(fieldStyle);
        heightField.textProperty().addListener((obs, o, n) -> updateBMILabel());

        // Gender
        Label genderHeader = new Label("Gender");
        genderHeader.setStyle("-fx-font-size: 11; -fx-text-fill: #888;");

        ToggleGroup genderGroup = new ToggleGroup();
        maleButton   = new RadioButton("Male");
        femaleButton = new RadioButton("Female");
        otherButton  = new RadioButton("Other");
        maleButton.setToggleGroup(genderGroup);
        femaleButton.setToggleGroup(genderGroup);
        otherButton.setToggleGroup(genderGroup);

        HBox genderBox = new HBox(16, maleButton, femaleButton, otherButton);
        genderBox.setAlignment(Pos.CENTER_LEFT);

        saveStatusLabel = new Label("");
        saveStatusLabel.setWrapText(true);
        saveStatusLabel.setStyle("-fx-font-size: 12;");

        Button saveButton = new Button("Save Profile");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setPrefHeight(42);
        saveButton.setStyle("-fx-background-color: #2F66B3; -fx-text-fill: white;" +
                            " -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
        saveButton.setOnAction(e -> saveProfileChanges());

        formBox.getChildren().addAll(
                avatarRow, nameRow, ageWeightRow, heightField,
                genderHeader, genderBox, saveStatusLabel, saveButton);

        ScrollPane scrollPane = new ScrollPane(formBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white; -fx-background: white;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        page.getChildren().addAll(header, scrollPane);
        root.getChildren().add(0, page);
        scene = new Scene(root, 390, 844);

        loadProfileData();
    }

    /*
     * Saves the values currently entered on the profile screen.
     *
     * Builds a UserProfile object from the visible form fields.
     * The screen needs to send real profile data to AppStateManager
     * instead of printing the old placeholder message.
     * This method parses number fields safely, sends the profile through
     * stateManager.saveProfile, updates the local profile only on success, and
     * displays the ServiceResponse message for the user.
     */
    private void saveProfileChanges() {
        try {
            UserProfile dataToSave = new UserProfile(
                    profile.getUserId(),
                    profile.getUsername(),
                    firstNameField.getText(),
                    lastNameField.getText(),
                    Integer.parseInt(ageField.getText()),
                    Double.parseDouble(heightField.getText()),
                    Double.parseDouble(weightField.getText()),
                    getSelectedGender());

            ServiceResponse<Boolean> attempt =
                    stateManager.saveProfile(dataToSave);

            if (attempt.isSuccess()) {
                profile = dataToSave;
                updateBMILabel();
                showSaveSuccess(attempt.getMessage());
            } else {
                showSaveError(attempt.getMessage());
            }
        } catch (NumberFormatException exception) {
            showSaveError("Age, height, and weight must be valid numbers.");
        }
    }

    // Shows a success message below the form controls.
    private void showSaveSuccess(String message) {
        saveStatusLabel.setText(message);
        saveStatusLabel.setStyle("-fx-text-fill: #1E5AA8; -fx-font-weight: bold;");
    }

    // Shows an error message below the form controls.
    private void showSaveError(String message) {
        saveStatusLabel.setText(message);
        saveStatusLabel.setStyle("-fx-text-fill: #B00020; -fx-font-weight: bold;");
    }

    private void loadProfileData() {

        if (profile == null) {
            return;
        }

        firstNameField.setText(
                profile.getFirstName()
        );

        lastNameField.setText(
                profile.getLastName()
        );

        ageField.setText(
                String.valueOf(
                        profile.getAge()
                )
        );

        heightField.setText(
                String.valueOf(
                        profile.getHeight()
                )
        );

        weightField.setText(
                String.valueOf(
                        profile.getWeight()
                )
        );

        switch (profile.getGender()) {

            case "Male":
                maleButton.setSelected(true);
                break;

            case "Female":
                femaleButton.setSelected(true);
                break;

            default:
                otherButton.setSelected(true);
        }

        updateBMILabel();
    }
    
    private String getSelectedGender() {
    	if (maleButton.isSelected())
    		return "Male";
    	else if (femaleButton.isSelected())
    		return "Female";
    	return "Other";
    }
    
    public void updateBMILabel() {
        try {
            double weight = Double.parseDouble(weightField.getText());
            double height = Double.parseDouble(heightField.getText());
            if (height <= 0) return;
            DecimalFormat df = new DecimalFormat("#.##");
            bmiLabel.setText(df.format(Calculator.calculateBMI(weight, height)));
        } catch (NumberFormatException e) {
            bmiLabel.setText("---");
        }
    }
    
    public Scene getScene() {
        return scene;
    }
}
