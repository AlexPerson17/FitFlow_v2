package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class LoginScreen extends BaseScreen {

    private Scene scene;
    private TextField usernameField;
    private PasswordField passwordField;

    public LoginScreen(AppStateManager stateManager) {
        super(stateManager);
        buildScreen();
    }

    private void buildScreen() {
        VBox root = new VBox();
        root.setPrefSize(390, 844);
        root.setMinSize(390, 844);
        root.setMaxSize(390, 844);

        // ── Blue branding section ──────────────────────────────────────────
        VBox brandSection = new VBox(10);
        brandSection.setAlignment(Pos.CENTER);
        brandSection.setPrefHeight(200);
        brandSection.setMinHeight(200);
        brandSection.setMaxHeight(200);
        brandSection.setBackground(new Background(new BackgroundFill(
                PRIMARY_BLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        Label logo = new Label("FF");
        logo.setPrefSize(80, 80);
        logo.setMinSize(80, 80);
        logo.setAlignment(Pos.CENTER);
        logo.setBackground(new Background(new BackgroundFill(
                LOGO_GRAY, new CornerRadii(40), Insets.EMPTY)));
        logo.setStyle("-fx-text-fill: #555; -fx-font-size: 22; -fx-font-weight: bold;");

        Label appTitle = new Label("FitFlow");
        appTitle.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Sign in to continue");
        subtitle.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 13;");

        brandSection.getChildren().addAll(logo, appTitle, subtitle);

        // ── White form section ─────────────────────────────────────────────
        VBox formSection = new VBox(14);
        formSection.setAlignment(Pos.TOP_CENTER);
        formSection.setPadding(new Insets(32, 24, 24, 24));
        VBox.setVgrow(formSection, Priority.ALWAYS);
        formSection.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);
        usernameField.setStyle(
                "-fx-background-color: #F1F1F1; -fx-background-radius: 6;" +
                " -fx-font-size: 13; -fx-pref-height: 40;");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);
        passwordField.setStyle(
                "-fx-background-color: #F1F1F1; -fx-background-radius: 6;" +
                " -fx-font-size: 13; -fx-pref-height: 40;");

        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(300);
        loginButton.setPrefHeight(42);
        loginButton.setFont(buttonFont());
        loginButton.setStyle(
                "-fx-background-color: #2F66B3; -fx-text-fill: white;" +
                " -fx-background-radius: 8; -fx-cursor: hand;");
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(e -> {
            clearNotification();
            stateManager.signInAttempt(usernameField.getText(), passwordField.getText());
        });

        messageLabel = createNotificationLabel();

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label registerLink = new Label("Create Account");
        registerLink.setStyle("-fx-text-fill: #3F6FB5; -fx-cursor: hand; -fx-font-size: 13;");
        registerLink.setOnMouseClicked(e -> stateManager.showSignupScreen());

        formSection.getChildren().addAll(
                usernameField, passwordField, loginButton, messageLabel, spacer, registerLink);

        root.getChildren().addAll(brandSection, formSection);
        scene = new Scene(root, 390, 844);
    }

    public void clearPassword()            { passwordField.setText(""); }
    public void fillUsername(String user)  { usernameField.setText(user); }
    public Scene getScene()                { return scene; }
}
