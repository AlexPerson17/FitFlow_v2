# FitFlow Frontend Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign all 8 JavaFX screens to match the UI Blueprint (390×844 fixed window, #3F6FB5 color system, header+content layout) and add a new CongratulationsScreen.

**Architecture:** Each screen is a full rewrite of its `buildScreen()` method using constants and helpers centralised in `BaseScreen`. The root layout shifts from "card on blue background" to a VBox with a blue header bar and white content area. AppStateManager gains one new navigation method; all backend layers are untouched.

**Tech Stack:** Java 17+, JavaFX 17+, existing Maven/IDE build, existing CSV-backed service layer.

---

## File Map

| File | Change |
|---|---|
| `view/BaseScreen.java` | Add color constants, font helpers, `createHeaderHamburgerButton()` |
| `app/Main.java` | Set stage 390×844, not resizable |
| `view/LoginScreen.java` | Rewrite `buildScreen()` |
| `view/SignupScreen.java` | Rewrite `buildScreen()` |
| `view/DashboardScreen.java` | Rewrite `buildScreen()` |
| `view/ProfileScreen.java` | Rewrite `buildScreen()` |
| `view/WorkoutHistoryScreen.java` | Rewrite `buildScreen()` |
| `view/CongratulationsScreen.java` | **New file** |
| `view/AppStateManager.java` | Add `showCongratulationsScreen(int, int)` |
| `view/WorkoutTimer.java` | Update `setDone()` to navigate to Congratulations |
| `view/RoutineBuilderScreen.java` | Rewrite `show()` + all helpers |
| `view/UserGuidedWorkout.java` | Rewrite `show()` + all helpers |

**Run command:** Launch `app.Main` from your IDE, or if using Maven: `mvn javafx:run`  
**Test command:** Run `src/test/java/IntegrationRegressionTest.java` and `BackendProofTest.java` from IDE after each task.

---

## Task 1: Update BaseScreen.java — Design System Foundation

**Files:**
- Modify: `src/main/java/view/BaseScreen.java`

- [ ] **Step 1: Add color constants and font helpers**

Replace the existing color constant block and add font helpers. Open `BaseScreen.java` and replace the three existing color constants (`PRIMARY_BLUE`, `ACCENT_BLUE`, `ERROR_RED`) and add the following at the top of the class body:

```java
// Color palette from UI Blueprint
protected static final Color PRIMARY_BLUE   = Color.web("#3F6FB5");
protected static final Color BUTTON_BLUE    = Color.web("#2F66B3");
protected static final Color CARD_GRAY      = Color.web("#F1F1F1");
protected static final Color TEXT_BLACK     = Color.web("#111111");
protected static final Color SUCCESS_GREEN  = Color.web("#1EA43A");
protected static final Color LOGOUT_RED     = Color.web("#A81805");
protected static final Color LOGO_GRAY      = Color.web("#D1D1D1");

// Font helpers
protected static Font appTitleFont()     { return Font.font("Segoe UI", FontWeight.BOLD, 18); }
protected static Font screenTitleFont()  { return Font.font("Segoe UI", FontWeight.BOLD, 16); }
protected static Font bodyFont()         { return Font.font("Segoe UI", 13); }
protected static Font smallLabelFont()   { return Font.font("Segoe UI", 11); }
protected static Font buttonFont()       { return Font.font("Segoe UI", FontWeight.BOLD, 14); }
protected static Font timerMainFont()    { return Font.font("Segoe UI", FontWeight.BOLD, 54); }
protected static Font timerSecondFont()  { return Font.font("Segoe UI", 30); }
```

- [ ] **Step 2: Add `createHeaderHamburgerButton()` method**

Add this method to `BaseScreen`. It wires the navigation overlay to the StackPane root and returns the button for embedding in the screen's header:

```java
/**
 * Adds the navigation dropdown overlay to root and returns the hamburger
 * button. Callers embed the button inside their header StackPane.
 * The overlay is positioned just below the header (top-right).
 */
protected Button createHeaderHamburgerButton(StackPane root, double headerHeight) {
    navigationMenu = createNavigationMenu();

    Button hamburger = createHamburgerButton();
    hamburger.setOnAction(e ->
            navigationMenu.setVisible(!navigationMenu.isVisible()));

    root.getChildren().add(navigationMenu);
    StackPane.setAlignment(navigationMenu, Pos.TOP_RIGHT);
    StackPane.setMargin(navigationMenu,
            new Insets(headerHeight + 4, 8, 0, 0));

    return hamburger;
}
```

- [ ] **Step 3: Update button colors in `styleNavigationButton` and `createHamburgerButton`**

In `styleNavigationButton`, change `-fx-background-color: #1E5AA8` to `-fx-background-color: #2F66B3`.

In `createNavigationMenu`, change the logout line from:
```java
logoutButton.setStyle(logoutButton.getStyle().replace("#1E5AA8", "#B00020"));
```
to:
```java
logoutButton.setStyle(logoutButton.getStyle().replace("#2F66B3", "#A81805"));
```

In `createHamburgerButton`, change `-fx-background-color: #002254` to `-fx-background-color: #2F66B3`.

- [ ] **Step 4: Update `createRootLayout()` to 390×844**

Replace the body of `createRootLayout()`:

```java
protected StackPane createRootLayout() {
    StackPane root = new StackPane();
    root.setAlignment(Pos.TOP_LEFT);
    root.setPrefSize(390, 844);
    root.setMinSize(390, 844);
    root.setMaxSize(390, 844);
    return root;
}
```

- [ ] **Step 5: Verify the file compiles**

Build the project in your IDE (Build → Rebuild Project). Expected: zero compile errors. No runtime test yet — no screen calls the new methods until later tasks.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/view/BaseScreen.java
git commit -m "style: add UI Blueprint color constants, font helpers, and header nav helper to BaseScreen"
```

---

## Task 2: Update Main.java — Stage Sizing

**Files:**
- Modify: `src/main/java/app/Main.java`

- [ ] **Step 1: Set stage to 390×844 fixed**

Replace the body of `start()`:

```java
@Override
public void start(Stage primaryStage) {
    primaryStage.setWidth(390);
    primaryStage.setHeight(844);
    primaryStage.setResizable(false);

    AppStateManager stateManager = new AppStateManager(primaryStage);
    primaryStage.setTitle("FitFlow");
    stateManager.showLoginScreen();
    primaryStage.show();
}
```

- [ ] **Step 2: Launch the app**

Run `app.Main` from your IDE. Expected: a 390×844 non-resizable window opens. It will still show the old blue-card Login screen — that is fine for now.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/app/Main.java
git commit -m "style: set stage to 390x844 fixed per UI Blueprint"
```

---

## Task 3: Rewrite LoginScreen.java

**Files:**
- Modify: `src/main/java/view/LoginScreen.java`

- [ ] **Step 1: Replace `buildScreen()` with the new layout**

The screen uses a `VBox` root (no card overlay). Blue top for branding, white bottom for the form.

Replace the entire `buildScreen()` method and update the `scene` declaration imports. The full file becomes:

```java
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
import javafx.scene.text.FontWeight;

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
        appTitle.setFont(appTitleFont());
        appTitle.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Sign in to continue");
        subtitle.setFont(bodyFont());
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
        registerLink.setFont(bodyFont());
        registerLink.setStyle("-fx-text-fill: #3F6FB5; -fx-cursor: hand;");
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
```

- [ ] **Step 2: Launch the app and verify Login screen**

Run `app.Main`. Expected:
- Window is 390×844, not resizable
- Blue top area (~200px) with "FF" circle, "FitFlow" title, "Sign in to continue"
- White bottom with username field, password field, Login button, "Create Account" link at bottom
- Login button works (try logging in with a test account if data exists)

- [ ] **Step 3: Commit**

```bash
git add src/main/java/view/LoginScreen.java
git commit -m "style: rewrite LoginScreen to blueprint blue-top + white-form layout"
```

---

## Task 4: Rewrite SignupScreen.java

**Files:**
- Modify: `src/main/java/view/SignupScreen.java`

- [ ] **Step 1: Replace `buildScreen()` — same blue-top + white-form pattern**

All validation logic (`validatePasswordLive()`, `clearPasswords()`) stays untouched. Only the layout changes.

Replace the entire file:

```java
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
            passwordField.setVisible(!show);          passwordField.setManaged(!show);
            confirmPasswordField.setVisible(!show);   confirmPasswordField.setManaged(!show);
            passwordVisibleField.setVisible(show);    passwordVisibleField.setManaged(show);
            confirmPasswordVisibleField.setVisible(show); confirmPasswordVisibleField.setManaged(show);
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

    public void clearUsername() { usernameField.setText(""); }

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
```

- [ ] **Step 2: Navigate to Signup and verify**

Launch the app, click "Create Account" on the Login screen. Expected:
- Same blue branding top as Login
- Scrollable white form with all fields
- Live password validation still works as you type
- "Already have an account? Log In" link returns to Login

- [ ] **Step 3: Commit**

```bash
git add src/main/java/view/SignupScreen.java
git commit -m "style: rewrite SignupScreen to blueprint layout, preserve all validation logic"
```

---

## Task 5: Rewrite DashboardScreen.java

**Files:**
- Modify: `src/main/java/view/DashboardScreen.java`

- [ ] **Step 1: Replace `buildScreen()`**

```java
package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class DashboardScreen extends BaseScreen {

    private Scene scene;

    public DashboardScreen(AppStateManager stateManager) {
        super(stateManager);
        buildScreen();
    }

    private void buildScreen() {
        StackPane root = createRootLayout();

        VBox page = new VBox();
        page.setPrefSize(390, 844);

        // ── Header (75px) ─────────────────────────────────────────────────
        StackPane header = new StackPane();
        header.setPrefSize(390, 75);
        header.setMinSize(390, 75);
        header.setMaxSize(390, 75);
        header.setBackground(new Background(new BackgroundFill(
                PRIMARY_BLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        VBox titles = new VBox(4);
        titles.setAlignment(Pos.CENTER);
        Label appTitle = new Label("FitFlow");
        appTitle.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: white;");
        Label appSubtitle = new Label("Interactive Workout Assistant");
        appSubtitle.setStyle("-fx-font-size: 12; -fx-text-fill: rgba(255,255,255,0.8);");
        titles.getChildren().addAll(appTitle, appSubtitle);

        Button hamburger = createHeaderHamburgerButton(root, 75);
        header.getChildren().addAll(titles, hamburger);
        StackPane.setAlignment(hamburger, Pos.TOP_RIGHT);
        StackPane.setMargin(hamburger, new Insets(8, 8, 0, 0));

        // ── White content ──────────────────────────────────────────────────
        VBox content = new VBox(10);
        content.setPadding(new Insets(24));
        VBox.setVgrow(content, Priority.ALWAYS);
        content.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        Label sectionLabel = new Label("NAVIGATE TO");
        sectionLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold;" +
                              " -fx-text-fill: #AAAAAA; -fx-padding: 0 0 4 0;");

        Button profileButton = createNavButton("Profile");
        Button builderButton = createNavButton("Workout Builder");
        Button historyButton = createNavButton("Workout History");

        profileButton.setOnAction(e -> stateManager.showProfileScreen());
        builderButton.setOnAction(e -> stateManager.showRoutineBuilderScreen());
        historyButton.setOnAction(e -> stateManager.showWorkoutHistoryScreen());

        Separator sep = new Separator();
        sep.setPadding(new Insets(6, 0, 6, 0));

        Button logoutButton = createNavButton("Log Out");
        logoutButton.setStyle(logoutButton.getStyle()
                .replace("#2F66B3", "#A81805"));
        logoutButton.setOnAction(e -> stateManager.logOut());

        content.getChildren().addAll(
                sectionLabel, profileButton, builderButton, historyButton,
                sep, logoutButton);

        page.getChildren().addAll(header, content);
        root.getChildren().add(0, page);
        scene = new Scene(root, 390, 844);
    }

    private Button createNavButton(String label) {
        Button button = new Button(label);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(46);
        button.setFont(buttonFont());
        button.setStyle(
                "-fx-background-color: #2F66B3; -fx-text-fill: white;" +
                " -fx-background-radius: 10; -fx-font-size: 14; -fx-font-weight: bold;" +
                " -fx-padding: 10 20 10 20; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;");
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle()
                .replace("#2F66B3", "#3F6FB5")));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle()
                .replace("#3F6FB5", "#2F66B3")));
        return button;
    }

    public Scene getScene() { return scene; }
}
```

- [ ] **Step 2: Verify Dashboard**

Log in with a test account. Expected:
- 75px blue header with "FitFlow" and subtitle, hamburger top-right
- White area with "NAVIGATE TO" label and three blue nav buttons
- Separator and red Log Out button
- Hamburger opens the nav overlay dropdown

- [ ] **Step 3: Commit**

```bash
git add src/main/java/view/DashboardScreen.java
git commit -m "style: rewrite DashboardScreen to blueprint header + nav layout"
```

---

## Task 6: Rewrite ProfileScreen.java

**Files:**
- Modify: `src/main/java/view/ProfileScreen.java`

- [ ] **Step 1: Replace `buildScreen()`**

All business logic methods (`saveProfileChanges`, `loadProfileData`, `updateBMILabel`, `getSelectedGender`) stay **unchanged**. Only the layout is replaced.

Replace the `buildScreen()` method (lines 60–287):

```java
private void buildScreen() {
    StackPane root = createRootLayout();

    VBox page = new VBox();
    page.setPrefSize(390, 844);

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
    StackPane.setAlignment(hamburger, Pos.TOP_RIGHT);
    StackPane.setMargin(hamburger, new Insets(8, 8, 0, 0));

    // ── Scrollable white content ───────────────────────────────────────
    VBox formBox = new VBox(10);
    formBox.setPadding(new Insets(20, 20, 20, 20));
    formBox.setBackground(new Background(new BackgroundFill(
            Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

    String fieldStyle = "-fx-background-color: #F1F1F1; -fx-background-radius: 6;" +
                        " -fx-font-size: 13; -fx-pref-height: 36;";

    // Avatar row
    Label avatarCircle = new Label();
    avatarCircle.setPrefSize(52, 52);
    avatarCircle.setMinSize(52, 52);
    avatarCircle.setBackground(new Background(new BackgroundFill(
            LOGO_GRAY, new CornerRadii(26), Insets.EMPTY)));

    bmiLabel = new Label("---");
    bmiLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #3F6FB5;");

    VBox bmiBox = new VBox(2, new Label("BMI"), bmiLabel);
    ((Label) bmiBox.getChildren().get(0)).setStyle("-fx-font-size: 11; -fx-text-fill: #888;");

    HBox avatarRow = new HBox(12, avatarCircle, bmiBox);
    avatarRow.setAlignment(Pos.CENTER_LEFT);
    avatarRow.setPadding(new Insets(0, 0, 8, 0));

    // Fields
    firstNameField = new TextField();
    firstNameField.setPromptText("First Name");
    firstNameField.setStyle(fieldStyle);

    lastNameField = new TextField();
    lastNameField.setPromptText("Last Name");
    lastNameField.setStyle(fieldStyle);

    HBox nameRow = new HBox(10, firstNameField, lastNameField);
    HBox.setHgrow(firstNameField, Priority.ALWAYS);
    HBox.setHgrow(lastNameField, Priority.ALWAYS);

    ageField = new TextField();
    ageField.setPromptText("Age");
    ageField.setStyle(fieldStyle);

    weightField = new TextField();
    weightField.setPromptText("Weight (lbs)");
    weightField.setStyle(fieldStyle);

    HBox ageWeightRow = new HBox(10, ageField, weightField);
    HBox.setHgrow(ageField, Priority.ALWAYS);
    HBox.setHgrow(weightField, Priority.ALWAYS);

    heightField = new TextField();
    heightField.setPromptText("Height (in)");
    heightField.setStyle(fieldStyle);

    // Gender
    Label genderLabel = new Label("Gender");
    genderLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #888;");
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
            genderLabel, genderBox, saveStatusLabel, saveButton);

    ScrollPane scrollPane = new ScrollPane(formBox);
    scrollPane.setFitToWidth(true);
    scrollPane.setStyle("-fx-background-color: white; -fx-background: white;");
    VBox.setVgrow(scrollPane, Priority.ALWAYS);

    page.getChildren().addAll(header, scrollPane);
    root.getChildren().add(0, page);
    scene = new Scene(root, 390, 844);
}
```

Also remove the old `HBox topSection`, `VBox profileSection`, `VBox logoSection` variable declarations from the top of `buildScreen()` — they're not used any more.

- [ ] **Step 2: Verify Profile screen**

Navigate to Profile from the Dashboard. Expected:
- 75px blue header with hamburger
- Avatar circle + BMI display
- All form fields pre-populated from the loaded profile
- Save button persists changes and shows success/error message

- [ ] **Step 3: Commit**

```bash
git add src/main/java/view/ProfileScreen.java
git commit -m "style: rewrite ProfileScreen to blueprint header + compact form layout"
```

---

## Task 7: Rewrite WorkoutHistoryScreen.java

**Files:**
- Modify: `src/main/java/view/WorkoutHistoryScreen.java`

- [ ] **Step 1: Replace `buildScreen()`**

`getWorkoutHistory()` and `buildHistoryRow()` parsing logic are unchanged; only the layout is rebuilt.

Replace the entire file:

```java
package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import java.util.List;

public class WorkoutHistoryScreen extends BaseScreen {

    private Scene scene;

    public WorkoutHistoryScreen(AppStateManager stateManager) {
        super(stateManager);
        buildScreen();
    }

    private void buildScreen() {
        StackPane root = createRootLayout();

        VBox page = new VBox();
        page.setPrefSize(390, 844);

        // ── Header (140px) ─────────────────────────────────────────────────
        StackPane header = new StackPane();
        header.setPrefSize(390, 140);
        header.setMinSize(390, 140);
        header.setMaxSize(390, 140);
        header.setBackground(new Background(new BackgroundFill(
                PRIMARY_BLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        Label historyTitle = new Label("History");
        historyTitle.setStyle("-fx-font-size: 34; -fx-text-fill: white; -fx-font-weight: bold;");

        Button hamburger = createHeaderHamburgerButton(root, 140);
        header.getChildren().addAll(historyTitle, hamburger);
        StackPane.setAlignment(historyTitle, Pos.CENTER);
        StackPane.setAlignment(hamburger, Pos.TOP_LEFT);
        StackPane.setMargin(hamburger, new Insets(32, 0, 0, 24));

        // ── Card list ─────────────────────────────────────────────────────
        VBox cardList = new VBox(24);
        cardList.setPadding(new Insets(36, 25, 24, 25));
        cardList.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        List<String> entries = stateManager.getWorkoutHistory();

        if (entries == null || entries.isEmpty()) {
            Label empty = new Label("No workout history found.");
            empty.setFont(bodyFont());
            empty.setTextFill(Color.GRAY);
            cardList.getChildren().add(empty);
            cardList.setAlignment(Pos.TOP_CENTER);
        } else {
            for (String entry : entries) {
                cardList.getChildren().add(buildHistoryCard(entry));
            }
        }

        ScrollPane scroll = new ScrollPane(cardList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: white; -fx-background: white;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        page.getChildren().addAll(header, scroll);
        root.getChildren().add(0, page);
        scene = new Scene(root, 390, 844);
    }

    private VBox buildHistoryCard(String entry) {
        VBox card = new VBox(8);
        card.setPrefWidth(340);
        card.setMaxWidth(340);
        card.setPadding(new Insets(10));
        card.setBackground(new Background(new BackgroundFill(
                CARD_GRAY, new CornerRadii(8), Insets.EMPTY)));
        card.setStyle("-fx-border-color: #2F66B3; -fx-border-radius: 8; -fx-border-width: 1;");

        String[] parts = entry.split(" \\| ");
        if (parts.length < 4) {
            card.getChildren().add(new Label(entry));
            return card;
        }

        String date     = parts[0].trim();
        String routine  = parts[1].trim();
        String duration = parts[2].trim();
        String calories = parts[3].trim();

        // Top row: date | name centered | Load button
        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #555;");

        Label nameLabel = new Label(routine);
        nameLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #333;");

        Button loadBtn = new Button("Load");
        loadBtn.setPrefSize(60, 26);
        loadBtn.setStyle("-fx-background-color: #2F66B3; -fx-text-fill: white;" +
                         " -fx-font-size: 11; -fx-font-weight: bold;" +
                         " -fx-background-radius: 4; -fx-cursor: hand;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topRow = new HBox(8, dateLabel, spacer, nameLabel, spacer, loadBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Stats row: 5 equal columns
        HBox statsRow = new HBox();
        statsRow.getChildren().addAll(
                statCell(duration,  "Duration"),
                statCell(calories,  "Calories"),
                statCell("—",       "Exercises"),
                statCell("—",       "Rest"),
                statCell("—",       "Rounds"));

        card.getChildren().addAll(topRow, statsRow);
        return card;
    }

    private VBox statCell(String value, String label) {
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #222;");
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 9; -fx-text-fill: #888;");
        VBox cell = new VBox(2, v, l);
        cell.setAlignment(Pos.CENTER);
        HBox.setHgrow(cell, Priority.ALWAYS);
        return cell;
    }

    public Scene getScene() { return scene; }
}
```

- [ ] **Step 2: Verify History screen**

Navigate to Workout History. Expected:
- 140px blue header with "History" centered and hamburger top-left
- Cards styled with Card Gray background and blue border
- Each card shows date, routine name, Load button, stats row

- [ ] **Step 3: Commit**

```bash
git add src/main/java/view/WorkoutHistoryScreen.java
git commit -m "style: rewrite WorkoutHistoryScreen to blueprint 140px header + card list"
```

---

## Task 8: Create CongratulationsScreen.java

**Files:**
- Create: `src/main/java/view/CongratulationsScreen.java`

- [ ] **Step 1: Create the new file**

```java
package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import util.Calculator;

public class CongratulationsScreen extends BaseScreen {

    private Scene scene;
    private final int durationSeconds;
    private final int exerciseCount;

    public CongratulationsScreen(AppStateManager stateManager,
                                  int durationSeconds,
                                  int exerciseCount) {
        super(stateManager);
        this.durationSeconds = durationSeconds;
        this.exerciseCount   = exerciseCount;
        buildScreen();
    }

    private void buildScreen() {
        VBox root = new VBox();
        root.setPrefSize(390, 844);
        root.setMinSize(390, 844);
        root.setMaxSize(390, 844);

        // ── Blue top section (330px) ───────────────────────────────────────
        VBox topSection = new VBox(10);
        topSection.setAlignment(Pos.TOP_CENTER);
        topSection.setPrefHeight(330);
        topSection.setMinHeight(330);
        topSection.setMaxHeight(330);
        topSection.setPadding(new Insets(28, 20, 20, 20));
        topSection.setBackground(new Background(new BackgroundFill(
                PRIMARY_BLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        Label congratsLabel = new Label("Congratulations!");
        congratsLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: white;");

        // Trophy circle (150×150)
        Label trophyCircle = new Label("🏆");
        trophyCircle.setPrefSize(150, 150);
        trophyCircle.setMinSize(150, 150);
        trophyCircle.setAlignment(Pos.CENTER);
        trophyCircle.setStyle("-fx-font-size: 60;");
        trophyCircle.setBackground(new Background(new BackgroundFill(
                Color.rgb(255, 255, 255, 0.2), new CornerRadii(75), Insets.EMPTY)));

        Label completeLabel = new Label("Workout Complete");
        completeLabel.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: white;");

        TextField workoutNameField = new TextField();
        workoutNameField.setPromptText("Name this workout...");
        workoutNameField.setPrefWidth(180);
        workoutNameField.setPrefHeight(32);
        workoutNameField.setMaxWidth(180);
        workoutNameField.setStyle("-fx-background-color: rgba(255,255,255,0.25);" +
                                   " -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.6);" +
                                   " -fx-background-radius: 6; -fx-font-size: 13;");

        topSection.getChildren().addAll(
                congratsLabel, trophyCircle, completeLabel, workoutNameField);

        // ── White bottom section (514px) ──────────────────────────────────
        VBox bottomSection = new VBox(20);
        bottomSection.setAlignment(Pos.TOP_CENTER);
        bottomSection.setPadding(new Insets(50, 20, 30, 20));
        VBox.setVgrow(bottomSection, Priority.ALWAYS);
        bottomSection.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        // Stats 2×2 grid
        int durationMinutes = durationSeconds / 60;
        int calories = (int) Calculator.calculateCaloriesBurned(durationMinutes, 8.0);
        String durationText = String.format("%d:%02d", durationSeconds / 60, durationSeconds % 60);

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(32);
        statsGrid.setVgap(34);
        statsGrid.setAlignment(Pos.CENTER);

        statsGrid.add(statCard(durationText,             "Duration"),          0, 0);
        statsGrid.add(statCard(String.valueOf(exerciseCount), "Exercises"),     1, 0);
        statsGrid.add(statCard(String.valueOf(calories) + " cal", "Calories"),  0, 1);
        statsGrid.add(statCard("—",                      "Total Workouts"),     1, 1);

        // Buttons
        Button showHistoryBtn = new Button("Show History");
        showHistoryBtn.setPrefSize(145, 56);
        showHistoryBtn.setStyle("-fx-background-color: #2F66B3; -fx-text-fill: white;" +
                                " -fx-font-size: 15; -fx-font-weight: bold;" +
                                " -fx-background-radius: 18; -fx-cursor: hand;");
        showHistoryBtn.setOnAction(e -> stateManager.showWorkoutHistoryScreen());

        Button doneBtn = new Button("Done");
        doneBtn.setPrefSize(145, 56);
        doneBtn.setStyle("-fx-background-color: #1EA43A; -fx-text-fill: white;" +
                         " -fx-font-size: 15; -fx-font-weight: bold;" +
                         " -fx-background-radius: 18; -fx-cursor: hand;");
        doneBtn.setOnAction(e -> stateManager.showRoutineBuilderScreen());

        HBox buttonRow = new HBox(18, showHistoryBtn, doneBtn);
        buttonRow.setAlignment(Pos.CENTER);

        bottomSection.getChildren().addAll(statsGrid, buttonRow);
        root.getChildren().addAll(topSection, bottomSection);
        scene = new Scene(root, 390, 844);
    }

    private VBox statCard(String value, String label) {
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: #111;");
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 12; -fx-text-fill: #888;");
        VBox card = new VBox(4, v, l);
        card.setPrefSize(145, 86);
        card.setMinSize(145, 86);
        card.setAlignment(Pos.CENTER);
        card.setBackground(new Background(new BackgroundFill(
                CARD_GRAY, new CornerRadii(8), Insets.EMPTY)));
        return card;
    }

    public Scene getScene() { return scene; }
}
```

- [ ] **Step 2: Compile**

Build the project. The new file compiles but isn't navigated to yet. Expected: zero errors.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/view/CongratulationsScreen.java
git commit -m "feat: add CongratulationsScreen with stats grid and Show History / Done buttons"
```

---

## Task 9: Update AppStateManager.java — Add Navigation to Congratulations

**Files:**
- Modify: `src/main/java/view/AppStateManager.java`

- [ ] **Step 1: Add `showCongratulationsScreen()` method**

Add the following method to `AppStateManager` after `showGuidedWorkoutScreen()` (after line 142):

```java
/**
 * Navigates to the Congratulations screen shown when a workout completes.
 * Called by WorkoutTimer after saveCompletedWorkoutToHistory() succeeds.
 *
 * @param durationSeconds planned workout duration in seconds
 * @param exerciseCount   number of exercises in the completed workout
 */
public void showCongratulationsScreen(int durationSeconds, int exerciseCount) {
    CongratulationsScreen screen =
            new CongratulationsScreen(this, durationSeconds, exerciseCount);
    primaryStage.setScene(screen.getScene());
}
```

- [ ] **Step 2: Compile**

Build the project. Expected: zero errors.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/view/AppStateManager.java
git commit -m "feat: add showCongratulationsScreen navigation to AppStateManager"
```

---

## Task 10: Update WorkoutTimer.java — Navigate on Completion

**Files:**
- Modify: `src/main/java/view/WorkoutTimer.java`

- [ ] **Step 1: Update `setDone()` to call `showCongratulationsScreen()`**

In `WorkoutTimer.java`, find the `setDone()` method. Replace the block that shows the inline "WORKOUT COMPLETE" banner (everything after `workout.saveCompletedWorkoutToHistory()`) with a navigation call.

Current end of `setDone()`:
```java
workout.restLabel.setVisible(true);
workout.restLabel.setText("WORKOUT COMPLETE");

workout.restLabel.setBackground(new Background(new BackgroundFill(
        Color.web("#4CAF50"), new CornerRadii(10), Insets.EMPTY)));
workout.restLabel.setPadding(new Insets(12, 25, 12, 25));
workout.restLabel.setTextFill(Color.WHITE);
workout.restLabel.setFont(Font.font("System", FontWeight.BOLD, 32));

// Persist the completed workout so WorkoutHistoryScreen can display it.
workout.saveCompletedWorkoutToHistory();
```

Replace with:
```java
// Save history first, then navigate to Congratulations screen.
workout.saveCompletedWorkoutToHistory();

int duration      = workout.calculatePlannedWorkoutDurationSeconds();
int exerciseCount = workout.exercises.length;
workout.stateManager.showCongratulationsScreen(duration, exerciseCount);
```

- [ ] **Step 2: Make `calculatePlannedWorkoutDurationSeconds()` accessible**

In `UserGuidedWorkout.java`, change the visibility of `calculatePlannedWorkoutDurationSeconds()` from `private` to `public`:

```java
// Before:
private int calculatePlannedWorkoutDurationSeconds() {

// After:
public int calculatePlannedWorkoutDurationSeconds() {
```

- [ ] **Step 3: Compile and do an end-to-end workout test**

Run the app. Start a short workout in the Routine Builder (e.g., 1 exercise, 5 seconds work time). Let the timer run to completion. Expected:
- Timer counts down to zero
- History is saved (check WorkoutHistory screen)
- App navigates to CongratulationsScreen showing duration, exercise count, calories
- "Done" button returns to Routine Builder
- "Show History" button shows history

- [ ] **Step 4: Run regression tests**

Run `IntegrationRegressionTest.java` and `BackendProofTest.java` from your IDE. Expected: all tests pass (no backend changes were made).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/view/WorkoutTimer.java src/main/java/view/UserGuidedWorkout.java
git commit -m "feat: navigate to CongratulationsScreen when workout completes instead of inline banner"
```

---

## Task 11: Rewrite RoutineBuilderScreen.java

**Files:**
- Modify: `src/main/java/view/RoutineBuilderScreen.java`

- [ ] **Step 1: Replace the entire file**

All backend interactions (`getSelectedRoutineSelections`, `saveRoutineWithDetails`, `startGuidedWorkoutWithDetails`) stay **unchanged**. The layout is rewritten for 390×844 with the two-column blueprint design.

The `show(Stage)` method is kept so `AppStateManager.showRoutineBuilderScreen()` requires no change.

```java
package view;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.RoutineExerciseSelection;
import service.ServiceResponse;

public class RoutineBuilderScreen extends BaseScreen {

    private static final String ICONS  = "/Icons";
    private static final String IMAGES = "/Images";

    // Save overlay fields
    private StackPane saveRoutineOverlay;
    private TextField routineNameField;
    private Label     routineNameMessageLabel;

    // Routine panel for selected exercises
    private VBox routinePanel;

    public RoutineBuilderScreen(AppStateManager stateManager) {
        super(stateManager);
    }

    public void show(Stage stage) {
        StackPane root = createRootLayout();

        VBox page = new VBox();
        page.setPrefSize(390, 844);

        // ── Header (150px) with tab row ───────────────────────────────────
        StackPane header = buildHeader(root);

        // ── Body: two-column HBox ──────────────────────────────────────────
        HBox body = new HBox(10);
        body.setPadding(new Insets(8));
        VBox.setVgrow(body, Priority.ALWAYS);

        // Left: exercise library (178px, scrollable)
        VBox exerciseLibrary = buildExerciseLibrary();
        ScrollPane libScroll = new ScrollPane(exerciseLibrary);
        libScroll.setFitToWidth(true);
        libScroll.setPrefWidth(178);
        libScroll.setMinWidth(178);
        libScroll.setMaxWidth(178);
        libScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        libScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Right: routine settings panel (178px)
        VBox settingsPanel = buildSettingsPanel(stage);
        settingsPanel.setPrefWidth(178);
        settingsPanel.setMinWidth(178);
        settingsPanel.setMaxWidth(178);

        body.getChildren().addAll(libScroll, settingsPanel);
        page.getChildren().addAll(header, body);

        root.getChildren().add(0, page);

        // Save overlay
        saveRoutineOverlay = createSaveRoutineOverlay();
        root.getChildren().add(saveRoutineOverlay);

        Scene scene = new Scene(root, 390, 844);
        stage.setScene(scene);
        stage.setMaximized(false);
        stage.show();
    }

    private StackPane buildHeader(StackPane root) {
        StackPane header = new StackPane();
        header.setPrefSize(390, 150);
        header.setMinSize(390, 150);
        header.setMaxSize(390, 150);
        header.setBackground(new Background(new BackgroundFill(
                PRIMARY_BLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        VBox titles = new VBox(4);
        titles.setAlignment(Pos.CENTER);
        titles.setPadding(new Insets(0, 0, 35, 0)); // leave room for tab row at bottom

        Label appTitle = new Label("FitFlow");
        appTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: white;");
        Label screenSubtitle = new Label("Routine Builder");
        screenSubtitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: white;");
        titles.getChildren().addAll(appTitle, screenSubtitle);

        // Tab row at y:105 from top of header
        HBox tabRow = new HBox();
        tabRow.setPrefHeight(40);
        tabRow.setAlignment(Pos.BOTTOM_LEFT);
        tabRow.setPadding(new Insets(0, 0, 0, 20));

        Label addTab = buildTab("Add Exercise", true);
        Label settingsTab = buildTab("Routine Settings", false);
        tabRow.getChildren().addAll(addTab, settingsTab);

        Button hamburger = createHeaderHamburgerButton(root, 150);

        header.getChildren().addAll(titles, tabRow, hamburger);
        StackPane.setAlignment(titles,   Pos.CENTER);
        StackPane.setAlignment(tabRow,   Pos.BOTTOM_LEFT);
        StackPane.setAlignment(hamburger, Pos.TOP_RIGHT);
        StackPane.setMargin(hamburger, new Insets(8, 8, 0, 0));

        return header;
    }

    private Label buildTab(String text, boolean active) {
        Label tab = new Label(text);
        tab.setPrefWidth(150);
        tab.setPrefHeight(40);
        tab.setAlignment(Pos.BOTTOM_CENTER);
        tab.setPadding(new Insets(0, 0, 4, 0));
        if (active) {
            tab.setStyle("-fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold;" +
                         " -fx-border-color: transparent transparent white transparent;" +
                         " -fx-border-width: 0 0 2 0;");
        } else {
            tab.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 13;");
        }
        return tab;
    }

    // ── Left panel: exercise library ──────────────────────────────────────

    private VBox buildExerciseLibrary() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(4, 4, 4, 0));

        Label title = new Label("Exercises");
        title.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #888;");
        box.getChildren().add(title);

        for (String exercise : stateManager.getExercises()) {
            box.getChildren().add(buildExerciseCard(exercise));
        }
        return box;
    }

    private HBox buildExerciseCard(String exerciseName) {
        HBox card = new HBox(6);
        card.setPrefHeight(48);
        card.setMaxHeight(48);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(4, 6, 4, 6));
        card.setBackground(new Background(new BackgroundFill(
                CARD_GRAY, new CornerRadii(6), Insets.EMPTY)));

        ImageView img = loadIcon(resolveImagePath(exerciseName), 42);
        img.setFitWidth(42);
        img.setFitHeight(42);

        Label name = new Label(exerciseName);
        name.setStyle("-fx-font-size: 9;");
        name.setPrefWidth(70);
        name.setWrapText(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button plusBtn = new Button();
        plusBtn.setGraphic(loadIcon(ICONS + "/plus.png", 14));
        plusBtn.setStyle("-fx-background-color: #2F66B3; -fx-background-radius: 4; -fx-cursor: hand;");
        plusBtn.setPrefSize(20, 20);
        plusBtn.setOnAction(e -> addExerciseToRoutine(exerciseName));

        card.getChildren().addAll(img, name, spacer, plusBtn);
        return card;
    }

    // ── Right panel: routine settings ─────────────────────────────────────

    private VBox buildSettingsPanel(Stage stage) {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(4, 0, 4, 4));

        Label title = new Label("Routine");
        title.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #888;");

        // Timer display (70px area)
        Label timerDisplay = new Label("00:30");
        timerDisplay.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #111;");
        timerDisplay.setAlignment(Pos.CENTER);
        timerDisplay.setPrefWidth(Double.MAX_VALUE);
        timerDisplay.setPrefHeight(70);

        // Routine scroll panel
        routinePanel = new VBox(6);
        routinePanel.setPadding(new Insets(4, 0, 0, 0));
        ScrollPane routineScroll = new ScrollPane(routinePanel);
        routineScroll.setFitToWidth(true);
        routineScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        routineScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(routineScroll, Priority.ALWAYS);

        // Bottom buttons
        Button saveBtn  = new Button("Save");
        Button startBtn = new Button("Start");
        saveBtn.setStyle("-fx-background-color: #2F66B3; -fx-text-fill: white;" +
                         " -fx-font-size: 11; -fx-font-weight: bold;" +
                         " -fx-background-radius: 5; -fx-cursor: hand;");
        startBtn.setStyle("-fx-background-color: #1EA43A; -fx-text-fill: white;" +
                          " -fx-font-size: 11; -fx-font-weight: bold;" +
                          " -fx-background-radius: 5; -fx-cursor: hand;");
        saveBtn.setPrefHeight(30);
        startBtn.setPrefHeight(30);
        HBox.setHgrow(saveBtn,  Priority.ALWAYS);
        HBox.setHgrow(startBtn, Priority.ALWAYS);

        saveBtn.setOnAction(e -> {
            routineNameField.clear();
            routineNameMessageLabel.setText("");
            saveRoutineOverlay.setVisible(true);
        });
        startBtn.setOnAction(e -> startWorkout(stage));

        HBox bottomRow = new HBox(6, saveBtn, startBtn);
        bottomRow.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(title, timerDisplay, routineScroll, bottomRow);
        return panel;
    }

    private void addExerciseToRoutine(String exerciseName) {
        RoutineExerciseSelection selection =
                new RoutineExerciseSelection(exerciseName, 3, 10, 60, 30);

        VBox card = buildRoutineCard(exerciseName, selection);
        routinePanel.getChildren().add(card);
    }

    private VBox buildRoutineCard(String exerciseName, RoutineExerciseSelection selection) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(6));
        card.setBackground(new Background(new BackgroundFill(
                CARD_GRAY, new CornerRadii(6), Insets.EMPTY)));
        card.setStyle("-fx-border-color: #2F66B3; -fx-border-radius: 6; -fx-border-width: 1;");
        card.setUserData(selection);

        Label nameLabel = new Label(exerciseName);
        nameLabel.setStyle("-fx-font-size: 10; -fx-font-weight: bold;");

        HBox controls = new HBox(4);
        controls.getChildren().addAll(
                buildSettingControl("Sets",  3,  selection),
                buildSettingControl("Reps",  10, selection),
                buildSettingControl("Rest",  30, selection));

        Button deleteBtn = new Button("✕");
        deleteBtn.setStyle("-fx-background-color: #A81805; -fx-text-fill: white;" +
                           " -fx-font-size: 9; -fx-background-radius: 3; -fx-cursor: hand;");
        deleteBtn.setPrefSize(18, 18);
        deleteBtn.setOnAction(e -> routinePanel.getChildren().remove(card));

        HBox topRow = new HBox(4, nameLabel);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topRow.getChildren().addAll(spacer, deleteBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(topRow, controls);
        return card;
    }

    private VBox buildSettingControl(String labelText, int initial,
                                     RoutineExerciseSelection selection) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 8; -fx-text-fill: #666;");

        Label value = new Label(String.valueOf(initial));
        value.setStyle("-fx-font-size: 10; -fx-font-weight: bold;");

        Button minus = new Button("−");
        Button plus  = new Button("+");
        String btnStyle = "-fx-background-color: #2F66B3; -fx-text-fill: white;" +
                          " -fx-font-size: 9; -fx-background-radius: 3; -fx-cursor: hand;" +
                          " -fx-min-width: 16; -fx-min-height: 16;";
        minus.setStyle(btnStyle);
        plus.setStyle(btnStyle);

        minus.setOnAction(e -> {
            int v = Integer.parseInt(value.getText());
            if (v > 0) {
                value.setText(String.valueOf(v - 1));
                updateSelectionValue(selection, labelText, v - 1);
            }
        });
        plus.setOnAction(e -> {
            int v = Integer.parseInt(value.getText()) + 1;
            value.setText(String.valueOf(v));
            updateSelectionValue(selection, labelText, v);
        });

        HBox row = new HBox(2, minus, value, plus);
        row.setAlignment(Pos.CENTER);
        row.setBackground(new Background(new BackgroundFill(
                Color.web("#D8E8FF"), new CornerRadii(4), Insets.EMPTY)));
        row.setPadding(new Insets(2));

        VBox box = new VBox(2, label, row);
        box.setAlignment(Pos.CENTER);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private void updateSelectionValue(RoutineExerciseSelection sel, String field, int val) {
        if (sel == null || field == null) return;
        switch (field) {
            case "Sets": sel.setSets(val);        break;
            case "Reps": sel.setReps(val);        break;
            case "Rest": sel.setRestSeconds(val); break;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private ImageView loadIcon(String path, int size) {
        try {
            ImageView iv = new ImageView(
                    new Image(getClass().getResourceAsStream(path)));
            iv.setFitWidth(size);
            iv.setFitHeight(size);
            iv.setPreserveRatio(true);
            return iv;
        } catch (Exception e) {
            return new ImageView();
        }
    }

    private String resolveImagePath(String name) {
        switch (name) {
            case "Push-ups":       return IMAGES + "/pushup.png";
            case "Plank":          return IMAGES + "/pushup.png";
            case "Sit-ups":        return IMAGES + "/situp.png";
            case "Squats":         return IMAGES + "/squat.png";
            case "Dumbbell Curls": return IMAGES + "/dumbbell_curl.png";
            default:               return IMAGES + "/snail.jpg";
        }
    }

    private void startWorkout(Stage stage) {
        List<RoutineExerciseSelection> selected = getSelectedRoutineSelections();
        ServiceResponse<?> response = stateManager.startGuidedWorkoutWithDetails(
                "Guided Workout", selected);
        if (!response.isSuccess()) {
            routineNameMessageLabel.setText(response.getMessage());
            routineNameMessageLabel.setTextFill(Color.RED);
            saveRoutineOverlay.setVisible(true);
            return;
        }
        stateManager.showGuidedWorkoutScreen();
    }

    private List<RoutineExerciseSelection> getSelectedRoutineSelections() {
        List<RoutineExerciseSelection> list = new ArrayList<>();
        for (Node node : routinePanel.getChildren()) {
            Object ud = node.getUserData();
            if (ud instanceof RoutineExerciseSelection)
                list.add((RoutineExerciseSelection) ud);
        }
        return list;
    }

    private StackPane createSaveRoutineOverlay() {
        StackPane overlay = createOverlay();

        StackPane card = createCard(300, 220);
        VBox content = createCardContent();

        Label title = new Label("Save Routine");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        routineNameField = new TextField();
        routineNameField.setPromptText("Routine Name");
        routineNameField.setMaxWidth(230);

        routineNameMessageLabel = new Label();
        routineNameMessageLabel.setWrapText(true);

        Button saveBtn  = new Button("Save");
        Button closeBtn = new Button("Close");

        saveBtn.setStyle("-fx-background-color: #2F66B3; -fx-text-fill: white;" +
                         " -fx-background-radius: 6; -fx-cursor: hand;");
        closeBtn.setStyle("-fx-background-color: #888; -fx-text-fill: white;" +
                          " -fx-background-radius: 6; -fx-cursor: hand;");

        saveBtn.setOnAction(e -> {
            String name = routineNameField.getText().trim();
            if (name.isEmpty()) {
                routineNameMessageLabel.setText("Please enter a routine name.");
                routineNameMessageLabel.setTextFill(Color.RED);
                return;
            }
            ServiceResponse<Boolean> response = stateManager.saveRoutineWithDetails(
                    name, getSelectedRoutineSelections());
            routineNameMessageLabel.setText(response.getMessage());
            routineNameMessageLabel.setTextFill(response.isSuccess() ? Color.GREEN : Color.RED);
        });

        closeBtn.setOnAction(e -> {
            routineNameField.clear();
            routineNameMessageLabel.setText("");
            overlay.setVisible(false);
        });

        HBox btnRow = new HBox(10, saveBtn, closeBtn);
        btnRow.setAlignment(Pos.CENTER);

        content.getChildren().addAll(
                title, new Label("Enter a name:"), routineNameField,
                routineNameMessageLabel, btnRow);
        card.getChildren().add(content);
        overlay.getChildren().add(card);
        return overlay;
    }
}
```

- [ ] **Step 2: Verify Routine Builder**

Navigate to Workout Builder. Expected:
- 390×844 window (not maximized)
- 150px blue header with "FitFlow / Routine Builder" and tab row
- Two-column body: left exercise list cards with + buttons, right routine settings
- Clicking + adds a card to the right panel with Sets/Reps/Rest controls
- Save opens the overlay; Start navigates to the workout timer

- [ ] **Step 3: Commit**

```bash
git add src/main/java/view/RoutineBuilderScreen.java
git commit -m "style: rewrite RoutineBuilderScreen to 390x844 two-column blueprint layout"
```

---

## Task 12: Rewrite UserGuidedWorkout.java

**Files:**
- Modify: `src/main/java/view/UserGuidedWorkout.java`

- [ ] **Step 1: Replace `show()` and all layout builders**

All timer logic (`WorkoutTimer`), session loading (`loadExercisesFromSession`), image resolution, history saving, and set/exercise progression stay **unchanged**. Only the visual layout changes. The side panel is removed (no room at 390px); queue info is shown in the work header.

Replace the `show()` method and all layout methods, keeping the non-layout methods intact:

```java
// Replace show() method:
public void show(Stage stage) {
    exercises    = loadExercisesFromSession();
    historySaved = false;

    if (exercises.length == 0) {
        stateManager.showDashboardScreen();
        return;
    }

    StackPane root = createRootLayout();
    timerManager  = new WorkoutTimer(this);

    VBox page = new VBox();
    page.setPrefSize(390, 844);

    // Top section (490px): white work header + exercise image
    VBox topSection = buildTopSection();

    // Bottom section (354px): blue controls
    VBox bottomSection = buildBottomSection(stage);

    page.getChildren().addAll(topSection, bottomSection);
    root.getChildren().add(0, page);
    addNavigationMenu(root);

    updateCurrentExercise();

    Scene scene = new Scene(root, 390, 844);
    stage.setTitle("FitFlow Guided Workout");
    stage.setScene(scene);
    stage.setMaximized(false);
    stage.show();
}

private VBox buildTopSection() {
    VBox top = new VBox();
    top.setPrefHeight(490);
    top.setMinHeight(490);
    top.setMaxHeight(490);

    // White work header (140px)
    VBox workHeader = new VBox(4);
    workHeader.setPrefHeight(140);
    workHeader.setMinHeight(140);
    workHeader.setMaxHeight(140);
    workHeader.setPadding(new Insets(16, 24, 10, 24));
    workHeader.setBackground(new Background(new BackgroundFill(
            Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

    restLabel = new Label("WORK");
    restLabel.setStyle("-fx-font-size: 30; -fx-font-weight: bold; -fx-text-fill: #111;");
    restLabel.setAlignment(Pos.CENTER);
    restLabel.setMaxWidth(Double.MAX_VALUE);
    restLabel.setVisible(true);

    statusLabel = new Label("Exercise 1 of " + exercises.length);
    statusLabel.setStyle("-fx-font-size: 24; -fx-text-fill: #555;");

    Label roundLabel = new Label("Round 1 of 1");
    roundLabel.setStyle("-fx-font-size: 24; -fx-text-fill: #555;");

    workHeader.getChildren().addAll(restLabel, statusLabel, roundLabel);

    // Exercise image area (350px)
    StackPane imageArea = new StackPane();
    imageArea.setPrefHeight(350);
    VBox.setVgrow(imageArea, Priority.ALWAYS);
    imageArea.setBackground(new Background(new BackgroundFill(
            PRIMARY_BLUE, CornerRadii.EMPTY, Insets.EMPTY)));

    exerciseImageMain = new ImageView();
    exerciseImageMain.setFitWidth(330);
    exerciseImageMain.setFitHeight(300);
    exerciseImageMain.setPreserveRatio(true);
    imageArea.getChildren().add(exerciseImageMain);

    top.getChildren().addAll(workHeader, imageArea);
    return top;
}

private VBox buildBottomSection(Stage stage) {
    VBox bottom = new VBox();
    bottom.setPrefHeight(354);
    bottom.setMinHeight(354);
    bottom.setMaxHeight(354);
    bottom.setAlignment(Pos.TOP_CENTER);
    bottom.setPadding(new Insets(28, 20, 20, 20));
    bottom.setBackground(new Background(new BackgroundFill(
            PRIMARY_BLUE, CornerRadii.EMPTY, Insets.EMPTY)));

    // REST/phase label (visible during rest, hidden during work)
    Label phaseLabel = new Label("REST");
    phaseLabel.setStyle("-fx-font-size: 36; -fx-font-weight: bold; -fx-text-fill: white;");
    phaseLabel.setVisible(false);

    // Main countdown timer
    timerLabel = new Label("00:00");
    timerLabel.setFont(timerMainFont());
    timerLabel.setStyle("-fx-font-size: 54; -fx-font-weight: bold; -fx-text-fill: white;");
    timerLabel.setAlignment(Pos.CENTER);
    timerLabel.setMaxWidth(Double.MAX_VALUE);

    // Progress bar
    progressBar = new ProgressBar(0);
    progressBar.setPrefWidth(350);
    progressBar.setPrefHeight(8);

    // Small elapsed timer
    Label elapsedIcon = new Label("🕐");
    elapsedIcon.setStyle("-fx-font-size: 22;");
    Label elapsedLabel = new Label("00:00");
    elapsedLabel.setStyle("-fx-font-size: 30; -fx-text-fill: rgba(255,255,255,0.85);");
    HBox elapsedRow = new HBox(8, elapsedIcon, elapsedLabel);
    elapsedRow.setAlignment(Pos.CENTER);

    // Control buttons
    playStopButton = new Button();
    playStopButton.setGraphic(IconImage("/Icons/play.png"));
    playStopButton.setPrefSize(76, 76);
    playStopButton.setBackground(new Background(new BackgroundFill(
            Color.WHITE, new CornerRadii(38), Insets.EMPTY)));
    playStopButton.setStyle("-fx-cursor: hand;");
    playStopButton.setOnAction(e -> {
        if (workoutRunning) {
            timerManager.stopWorkout();
            stateManager.pauseGuidedWorkout();
        } else {
            timerManager.startWorkout();
            stateManager.resumeGuidedWorkout();
        }
    });

    Button cancelBtn = new Button("✕");
    cancelBtn.setStyle("-fx-font-size: 28; -fx-font-weight: bold;" +
                       " -fx-text-fill: rgba(255,255,255,0.8);" +
                       " -fx-background-color: transparent; -fx-cursor: hand;");
    cancelBtn.setOnAction(e -> stateManager.showRoutineBuilderScreen());

    Button nextBtn = new Button();
    nextBtn.setGraphic(IconImage("/Icons/next.png"));
    nextBtn.setPrefSize(52, 52);
    nextBtn.setBackground(new Background(new BackgroundFill(
            Color.rgb(255, 255, 255, 0.2), new CornerRadii(26), Insets.EMPTY)));
    nextBtn.setStyle("-fx-cursor: hand;");
    nextBtn.setOnAction(e -> {
        if (currentExercise < exercises.length - 1) {
            stateManager.skipGuidedWorkoutStep();
            currentExercise++;
            updateCurrentExercise();
        }
    });

    HBox controlRow = new HBox();
    controlRow.setAlignment(Pos.CENTER);
    controlRow.setPrefWidth(390);

    Region leftSpacer = new Region();
    Region rightSpacer = new Region();
    HBox.setHgrow(leftSpacer, Priority.ALWAYS);
    HBox.setHgrow(rightSpacer, Priority.ALWAYS);

    controlRow.getChildren().addAll(cancelBtn, leftSpacer, playStopButton, rightSpacer, nextBtn);

    VBox.setMargin(controlRow, new Insets(30, 0, 0, 0));

    bottom.getChildren().addAll(
            timerLabel, progressBar, elapsedRow, controlRow);

    // Store phaseLabel reference in restLabel field so WorkoutTimer can update it
    this.restLabel = phaseLabel;
    bottom.getChildren().add(0, phaseLabel);

    return bottom;
}
```

Also update `updateCurrentExercise()` to update `statusLabel` with the new inline display:

```java
public void updateCurrentExercise() {
    currentSet     = 1;
    workoutRunning = false;
    ExerciseData exercise = exercises[currentExercise];
    timerManager.reset();
    timerLabel.setText(String.format("%02d:%02d",
            exercise.workSeconds / 60, exercise.workSeconds % 60));
    progressBar.setProgress(0);
    restLabel.setVisible(false);
    restLabel.setText("REST");
    RestLabelStyle();
    if (statusLabel != null) {
        statusLabel.setText("Exercise " + (currentExercise + 1) + " of " + exercises.length);
    }
    playStopButton.setGraphic(IconImage("/Icons/play.png"));
}
```

Remove the `buildSidePanel()`, `WorkoutImageCard()`, `updateQueue()`, `updatePlayingNow()` methods — they are no longer used in the 390px layout.

- [ ] **Step 2: Verify the Guided Workout screen**

Start a workout from the Routine Builder. Expected:
- 390×844 window
- White header showing "WORK", "Exercise 1 of N", "Round 1 of 1"
- Exercise GIF in the blue image area
- Blue bottom area with countdown, progress bar, cancel/play/next controls
- Timer counts down, transitions between work and rest
- On completion, navigates to CongratulationsScreen

- [ ] **Step 3: Run full regression**

Run both `IntegrationRegressionTest.java` and `BackendProofTest.java`. Expected: all pass.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/view/UserGuidedWorkout.java
git commit -m "style: rewrite UserGuidedWorkout to 390x844 blueprint layout, remove side panel"
```

---

## Done

All 12 tasks complete. The app is 390×844, follows the UI Blueprint color system and layout patterns, and includes the new CongratulationsScreen. Backend is untouched.

**Verify end-to-end one final time:**
1. Launch app → Login screen (blue top, white form)
2. Create account → Signup screen
3. Log in → Dashboard (blue header, nav buttons)
4. Navigate to Profile → compact form, save works
5. Navigate to Workout Builder → two-column layout, add exercises, start workout
6. Workout timer runs → completion navigates to Congratulations
7. View History → card list with Load buttons
8. Hamburger menu works on all post-login screens

Run `IntegrationRegressionTest.java` and `BackendProofTest.java` one final time — all should pass.
