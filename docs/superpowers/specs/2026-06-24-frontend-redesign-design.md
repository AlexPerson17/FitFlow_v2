# FitFlow Frontend Redesign — Design Spec

**Date:** 2026-06-24  
**Scope:** Frontend only — all backend systems (model, repository, service, util) unchanged.  
**New file:** `CongratulationsScreen.java`  
**Approach:** Screen-by-screen rewrite using the UI Blueprint pixel specs and design system.

---

## 1. Global Design System

### Window (Main.java + every screen)

```java
stage.setWidth(390);
stage.setHeight(844);
stage.setResizable(false);

root.setPrefSize(390, 844);
root.setMaxSize(390, 844);
root.setMinSize(390, 844);
```

All `new Scene(root, W, H)` calls become `new Scene(root, 390, 844)`.  
`RoutineBuilderScreen` and `UserGuidedWorkout` remove `stage.setMaximized(true)`.

### Color Constants — BaseScreen.java

| Constant | Hex | Usage |
|---|---|---|
| `PRIMARY_BLUE` | `#3F6FB5` | Header backgrounds |
| `BUTTON_BLUE` | `#2F66B3` | All primary buttons, borders |
| `CARD_GRAY` | `#F1F1F1` | Card / field backgrounds |
| `WHITE` | `#FFFFFF` | Content area backgrounds |
| `TEXT_BLACK` | `#111111` | Primary text |
| `SUCCESS_GREEN` | `#1EA43A` | Success messages, Done button |
| `LOGOUT_RED` | `#A81805` | Logout button |
| `LOGO_GRAY` | `#D1D1D1` | Logo/avatar placeholder circle |

Replaces all existing `#1E5AA8`, `#002254`, `#B00020` references.

### Typography Scale — BaseScreen.java font helpers

| Method | Size | Weight | Role |
|---|---|---|---|
| `appTitleFont()` | 24px | Bold | "FitFlow" in header |
| `screenTitleFont()` | 30px | Bold | Screen headings |
| `sectionTitleFont()` | 18px | Regular | Sub-sections |
| `bodyFont()` | 13px | Regular | Labels, descriptions |
| `smallLabelFont()` | 11px | Regular | Card sub-labels |
| `buttonFont()` | 14px | Bold | Button text |
| `timerMainFont()` | 54px | Bold | Countdown display |
| `timerSecondaryFont()` | 30px | Regular | Rest/round labels |

### Shared Header — `BaseScreen.createHeader(String screenTitle)`

Used by all post-login screens (Dashboard, Profile, Routine Builder, History).

- Height: 150px, Width: 390px, Background: `PRIMARY_BLUE`
- Hamburger icon: 32×32 at x:8, y:8 (white lines, existing implementation)
- "FitFlow" label: 18px bold, white, centered, top y:8
- `screenTitle` subtitle: 16px bold, white, centered, below app title
- Returns a `VBox` — screens append a tab row if needed

---

## 2. Screen Designs

### 2a. Login Screen (`LoginScreen.java`)

**Layout:** `VBox` root, no card overlay.

- **Blue top section** — `VBox`, height ~200px, background `PRIMARY_BLUE`, centered:
  - Logo circle: 80px diameter, background `LOGO_GRAY`, centered
  - "FitFlow" label: `appTitleFont()`, white
  - "Sign in to continue" subtitle: `bodyFont()`, white at 75% opacity
- **White form section** — `VBox`, `flex:1`, background `WHITE`, padding 24px, spacing 12px:
  - Username `TextField`: maxWidth 300px, background `CARD_GRAY`
  - Password `PasswordField`: maxWidth 300px, background `CARD_GRAY`
  - Login `Button`: prefWidth 300px, background `BUTTON_BLUE`, text white, `buttonFont()`, radius 8px
  - `messageLabel`: error/success notification (existing logic unchanged)
  - "Create Account" `Label`: `bodyFont()`, color `PRIMARY_BLUE`, click → `showSignupScreen()`

No hamburger menu (user not authenticated).  
Scene: `390×844`.

### 2b. Signup Screen (`SignupScreen.java`)

Same blue-top + white-form pattern as Login.

- **Blue top section** — height ~160px:
  - Logo circle: 70px
  - "FitFlow" label: `appTitleFont()`, white
  - "Create Your Account" subtitle: `bodyFont()`, white
- **White form section** — `VBox`, padding 20px, spacing 10px:
  - Username, Password, Confirm Password fields (existing logic unchanged)
  - Show Password `CheckBox`
  - Password requirements `Label`
  - `passwordValidationLabel`, `confirmPasswordValidationLabel`
  - Create Account `Button`: `BUTTON_BLUE`, prefWidth 300px
  - `messageLabel`
  - "Already have an account? Log In" link: `PRIMARY_BLUE`

All existing validation logic (`validatePasswordLive()`, `clearPasswords()`, etc.) unchanged.  
Scene: `390×844`.

### 2c. Dashboard Screen (`DashboardScreen.java`)

- **Blue header** — 75px, `PRIMARY_BLUE`, hamburger top-right:
  - "FitFlow" label: `appTitleFont()`, white, centered
  - "Interactive Workout Assistant" subtitle: `bodyFont()`, white at 80% opacity
- **White content** — `VBox`, padding 24px, spacing 10px:
  - "NAVIGATE TO" section label: 11px bold, color `#AAAAAA`, uppercase
  - Profile `Button`, Workout Builder `Button`, Workout History `Button`: `createNavButton()` style
  - `Separator`
  - Log Out `Button`: `LOGOUT_RED`

`createNavButton()` updated: background `BUTTON_BLUE` (was `#1E5AA8`), prefHeight 46px, `buttonFont()`.  
Scene: `390×844`.

### 2d. Profile Screen (`ProfileScreen.java`)

- **Blue header** — 75px, `PRIMARY_BLUE`:
  - Hamburger top-right
  - "FitFlow" centered, `appTitleFont()`, white
  - "Your Profile" subtitle: 16px bold, white
- **White content** — `ScrollPane` wrapping `VBox`, padding 16px, spacing 8px:
  - Avatar row: `HBox` with 52px `LOGO_GRAY` circle + username label + BMI value
  - First Name, Last Name fields: `CARD_GRAY` background
  - Age, Weight row: `HBox` with two equal fields
  - Height field
  - Gender `HBox`: Male / Female / Other `RadioButton`s
  - `saveStatusLabel`
  - Save `Button`: `BUTTON_BLUE`, full width

`saveProfileChanges()`, `loadProfileData()`, `updateBMILabel()` logic unchanged.  
Scene: `390×844`.

### 2e. Routine Builder Screen (`RoutineBuilderScreen.java`)

Follows the blueprint exactly.

- **Blue header** — 150px, `PRIMARY_BLUE`:
  - Hamburger: 32×32, x:8, y:8
  - "FitFlow" 18px bold + "Routine Builder" 16px bold, centered
  - Tab row at y:105, height 40px:
    - "Add Exercise" tab: x:20, width:150, 14px, white
    - "Routine Settings" tab: x:215, width:150, 14px, white
    - Active tab: 2px white `Rectangle` underline at bottom of tab; inactive tab text at 65% opacity white
- **Main content** — `HBox`, y:150, height:694px, padding 8px, gap 10px:
  - **Left — Exercise Column** (178px wide, scrollable `ScrollPane`):
    - Exercise cards: 168×48px, gap 8px, background `CARD_GRAY`
    - Each card: 42×42 image + 90px text area + 20×20 `+` button (right)
    - Exercise name font: 10px
  - **Right — Routine Settings** (178px wide):
    - Timer display: 28px, centered, height area 70px
    - Setting cards (Sets, Reps, Rest, Work Duration): 160×50px, gap 10px, background `CARD_GRAY`, border `BUTTON_BLUE`
    - Inside each card: `−` button 18×18, value label 11px bold, `+` button 18×18, category label 9px
    - Start button: 80×32px, 12px bold, bottom-right
    - Save button: same row as Start

Remove `stage.setMaximized(true)`. Scene: `390×844` fixed.  
All existing `getSelectedRoutineSelections()`, `saveRoutineWithDetails()`, `startWorkout()` logic unchanged.

### 2f. Workout Timer / Guided Workout (`UserGuidedWorkout.java`)

Follows the blueprint exactly. Layout is a `VBox` root (not `BorderPane`), no side panel.

- **Top section** — 490px, background `PRIMARY_BLUE`:
  - White "work header" sub-section — 140px, `WHITE`, width 390px:
    - Work/Rest phase title: 30px bold, `TEXT_BLACK`, centered, y:16
    - "Exercise X of Y": 24px, x:24, y:70
    - "Round X of Y": 24px, x:24, y:110
  - PNG area — 350px height, centered:
    - Exercise GIF/image: max 330×300px, centered, `preserveRatio`
- **Bottom section** — 354px, background `PRIMARY_BLUE`:
  - REST label: 36px bold, white, centered, y:28 from section top
  - Main countdown: `timerMainFont()` (54px bold), white, centered, y:90
  - Small elapsed timer + clock icon (28×28): 32px, centered, y:145
  - Controls row at y:245:
    - Cancel "✕": 32px bold, white, x:32 from left
    - Play/Pause circle: 76×76, center x:195, `WHITE` background
    - Next circle: 76×76, center x:315, semi-transparent white

Side panel (`buildSidePanel()`, `upcomingContainer`, `playingNowCard`) removed — no room at 390px.  
Workout queue info displayed inline in work header instead (exercise name, set progress).  
All timer logic (`WorkoutTimer.java`), session loading, history saving unchanged.  
Scene: `390×844` fixed.

### 2g. Congratulations Screen (`CongratulationsScreen.java`) — NEW

New file in `view/` package.

- **Blue top section** — 330px, `PRIMARY_BLUE`:
  - "Congratulations!" label: 28px bold, white, centered, y:28
  - Symbol circle: 150×150, centered, y:90 — green checkmark or trophy icon using `LOGO_GRAY` background
  - "Workout Complete" label: 26px bold, white, centered, y:250
  - Workout name `TextField`: 180×32px, centered, y:292, 14px, `CARD_GRAY` background
- **White bottom section** — 514px, `WHITE`:
  - Stats `GridPane`: 2 columns × 2 rows
    - Card width: 145px, height: 86px, gap H:32px, V:34px, top margin: 50px
    - Background: `CARD_GRAY`
    - Value label: 26px bold, `TEXT_BLACK`
    - Category label: 12px, `#888888`
    - Stats: Duration, Exercises Completed, Calories Burned, Total Workouts
  - Two buttons (y:420 from section top):
    - "Show History": 145×56px, radius 18px, 20px, `BUTTON_BLUE`, x:36
    - "Done": 145×56px, radius 18px, 20px, `SUCCESS_GREEN`, x:209

Constructor receives duration (seconds) and exercise count from `AppStateManager`.  
Calories estimated via `Calculator.calculateCaloriesBurned(durationMinutes, 8.0)` (8 cal/min is a reasonable moderate-intensity estimate; `durationMinutes` = duration seconds ÷ 60).  
"Done" → `stateManager.showRoutineBuilderScreen()`.  
"Show History" → `stateManager.showWorkoutHistoryScreen()`.

### 2h. Workout History Screen (`WorkoutHistoryScreen.java`)

- **Blue header** — 140px, `PRIMARY_BLUE`:
  - Hamburger icon: 42×42, x:24, y:32
  - "History" title: 34px, white, centered, y:40
- **Scrollable content** — starts y:140, background `WHITE`:
  - Padding top: 36px, cards centered
  - Each card: width 340px, height 120px, gap 24px
  - Card background: `CARD_GRAY`, border: `BUTTON_BLUE`, padding 10px
  - Top row: date (left, x:12), routine name (center), Load button (right)
  - Load button: 60×26px, 12px, `BUTTON_BLUE`
  - Stats row: 5 equal columns — duration, exercises, rest, rounds, calories

Existing `buildHistoryRow()` logic adapted; `getWorkoutHistory()` call unchanged.  
Scene: `390×844`.

---

## 3. AppStateManager Changes

Add one navigation method:

```java
// durationSeconds: from UserGuidedWorkout.calculatePlannedWorkoutDurationSeconds()
// exerciseCount: exercises.length from the active session
public void showCongratulationsScreen(int durationSeconds, int exerciseCount) {
    // create CongratulationsScreen(this, durationSeconds, exerciseCount), set scene
}
```

`WorkoutTimer.setDone()` calls `stateManager.showCongratulationsScreen(durationSeconds, exerciseCount)` instead of the inline "WORKOUT COMPLETE" banner. It already calls `workout.saveCompletedWorkoutToHistory()` — that call stays in place before the navigation.

No changes to any existing `AppStateManager` methods or the service/repository layers they call.

---

## 4. Files Changed Summary

| File | Change type |
|---|---|
| `view/BaseScreen.java` | Modify — add constants, font helpers, `createHeader()` |
| `view/LoginScreen.java` | Rewrite |
| `view/SignupScreen.java` | Rewrite |
| `view/DashboardScreen.java` | Rewrite |
| `view/ProfileScreen.java` | Rewrite |
| `view/RoutineBuilderScreen.java` | Rewrite |
| `view/UserGuidedWorkout.java` | Rewrite |
| `view/WorkoutHistoryScreen.java` | Rewrite |
| `view/WorkoutTimer.java` | Modify — `setDone()` calls `showCongratulationsScreen()` |
| `view/AppStateManager.java` | Modify — add `showCongratulationsScreen()` |
| `view/CongratulationsScreen.java` | **New file** |
| `Main.java` | Modify — stage sizing |

**Backend untouched:** all `model/`, `repository/`, `service/`, `util/` packages.
