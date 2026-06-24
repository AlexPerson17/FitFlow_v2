package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class SignupScreen extends BaseScreen {

    private Scene scene;
    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField passwordVisibleField;
    private TextField confirmPasswordVisibleField;
    private Label passwordValidationLabel;
    private Label confirmPasswordValidationLabel;

    public SignupScreen(AppStateManager stateManager) {
        super(stateManager);
        buildScreen();
    }

    private void buildScreen() {
        VBox root = new VBox();
        root.setPrefSize(390, 844);
        root.setMinSize(390, 844);
        root.setMaxSize(390, 844);

        // ── Blue branding section ──────────────────────────────────────────
        VBox brandSection = new VBox(8);
        brandSection.setAlignment(Pos.CENTER);
        brandSection.setPrefHeight(170);
        brandSection.setMinHeight(170);
        brandSection.setMaxHeight(170);
        brandSection.setBackground(new Background(new BackgroundFill(
                PRIMARY_BLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        Label logo = new Label("FF");
        logo.setPrefSize(66, 66);
        logo.setMinSize(66, 66);
        logo.setAlignment(Pos.CENTER);
        logo.setBackground(new Background(new BackgroundFill(
                LOGO_GRAY, new CornerRadii(33), Insets.EMPTY)));
        logo.setStyle("-fx-text-fill: #555; -fx-font-size: 18; -fx-font-weight: bold;");

        Label appTitle = new Label("FitFlow");
        appTitle.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Create Your Account");
        subtitle.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 13;");

        brandSection.getChildren().addAll(logo, appTitle, subtitle);

        // ── White form section ─────────────────────────────────────────────
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        scroll.setStyle("-fx-background-color: white; -fx-background: white;");

        VBox formSection = new VBox(10);
        formSection.setAlignment(Pos.TOP_CENTER);
        formSection.setPadding(new Insets(24, 24, 24, 24));
        formSection.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        String fieldStyle = "-fx-background-color: #F1F1F1; -fx-background-radius: 6;" +
                            " -fx-font-size: 13; -fx-pref-height: 38;";

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);
        usernameField.setStyle(fieldStyle);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);
        passwordField.setStyle(fieldStyle);

        passwordVisibleField = new TextField();
        passwordVisibleField.setPromptText("Password");
        passwordVisibleField.setMaxWidth(300);
        passwordVisibleField.setStyle(fieldStyle);
        passwordVisibleField.setVisible(false);
        passwordVisibleField.setManaged(false);
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setMaxWidth(300);
        confirmPasswordField.setStyle(fieldStyle);

        confirmPasswordVisibleField = new TextField();
        confirmPasswordVisibleField.setPromptText("Confirm Password");
        confirmPasswordVisibleField.setMaxWidth(300);
        confirmPasswordVisibleField.setStyle(fieldStyle);
        confirmPasswordVisibleField.setVisible(false);
        confirmPasswordVisibleField.setManaged(false);
        confirmPasswordVisibleField.textProperty().bindBidirectional(
                confirmPasswordField.textProperty());

        passwordField.textProperty().addListener((obs, o, n) -> validatePasswordLive());
        confirmPasswordField.textProperty().addListener((obs, o, n) -> validatePasswordLive());

        CheckBox showPasswordCheckBox = new CheckBox("Show Password");
        showPasswordCheckBox.setOnAction(e -> {
            boolean show = showPasswordCheckBox.isSelected();
            passwordField.setVisible(!show);
            passwordField.setManaged(!show);
            confirmPasswordField.setVisible(!show);
            confirmPasswordField.setManaged(!show);
            passwordVisibleField.setVisible(show);
            passwordVisibleField.setManaged(show);
            confirmPasswordVisibleField.setVisible(show);
            confirmPasswordVisibleField.setManaged(show);
        });

        Label passwordRequirements = new Label(
                "Password Requirements:\n• At least 12 characters\n• At least 1 number");
        passwordRequirements.setStyle("-fx-font-size: 11; -fx-text-fill: #777;");
        passwordRequirements.setWrapText(true);
        passwordRequirements.setMaxWidth(300);

        passwordValidationLabel = new Label();
        passwordValidationLabel.setWrapText(true);
        passwordValidationLabel.setMaxWidth(300);

        confirmPasswordValidationLabel = new Label();
        confirmPasswordValidationLabel.setWrapText(true);
        confirmPasswordValidationLabel.setMaxWidth(300);

        Button createAccountButton = new Button("Create Account");
        createAccountButton.setMaxWidth(300);
        createAccountButton.setPrefHeight(42);
        createAccountButton.setStyle(
                "-fx-background-color: #2F66B3; -fx-text-fill: white;" +
                " -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
        createAccountButton.setDefaultButton(true);
        createAccountButton.setOnAction(e -> {
            clearNotification();
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirm  = confirmPasswordField.getText();
            final String TEST_EMAIL = "test@test.com";
            if (!password.isEmpty() && password.equals(confirm)) {
                stateManager.signUpAttempt(username, password, TEST_EMAIL);
            } else {
                showError("Passwords do not match.");
            }
        });

        messageLabel = createNotificationLabel();

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label loginLink = new Label("Already have an account? Log In");
        loginLink.setStyle("-fx-text-fill: #3F6FB5; -fx-cursor: hand; -fx-font-size: 13;");
        loginLink.setOnMouseClicked(e -> stateManager.showLoginScreen());

        formSection.getChildren().addAll(
                usernameField, passwordField, passwordVisibleField,
                confirmPasswordField, confirmPasswordVisibleField,
                showPasswordCheckBox, passwordRequirements,
                passwordValidationLabel, confirmPasswordValidationLabel,
                createAccountButton, messageLabel, spacer, loginLink);

        scroll.setContent(formSection);
        root.getChildren().addAll(brandSection, scroll);
        scene = new Scene(root, 390, 844);
    }

    private void validatePasswordLive() {
        String password = passwordField.getText();
        String confirm  = confirmPasswordField.getText();
        boolean valid   = password.length() >= 12 && password.matches(".*\\d.*");

        if (password.isEmpty()) {
            passwordValidationLabel.setText("");
        } else if (valid) {
            passwordValidationLabel.setText("Password meets requirements.");
            passwordValidationLabel.setTextFill(Color.GREEN);
        } else {
            passwordValidationLabel.setText("Password must be 12+ characters and include 1 number.");
            passwordValidationLabel.setTextFill(Color.RED);
        }

        if (confirm.isEmpty()) {
            confirmPasswordValidationLabel.setText("");
        } else if (confirm.equals(password)) {
            confirmPasswordValidationLabel.setText("Passwords match.");
            confirmPasswordValidationLabel.setTextFill(Color.GREEN);
        } else {
            confirmPasswordValidationLabel.setText("Passwords do not match.");
            confirmPasswordValidationLabel.setTextFill(Color.RED);
        }
    }

    public void clearPasswords() {
        usernameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        passwordValidationLabel.setText("");
        confirmPasswordValidationLabel.setText("");
        clearNotification();
    }

    public Scene getScene() { return scene; }
}
