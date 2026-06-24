# FitFlow Capstone - Phase II Source Code

FitFlow is a Java/JavaFX workout assistant for creating routines, saving profile data, tracking workout history, and running service-layer validation and integration checks.

## Project Structure

- `src/main/java/app` - JavaFX application entry point.
- `src/main/java/view` - JavaFX screens and navigation.
- `src/main/java/service` - Facade, validation, timer, responses, and service messages.
- `src/main/java/model` - Domain objects such as profiles, exercises, routines, history, and sessions.
- `src/main/java/repository` - CSV-backed persistence helpers.
- `src/main/java/util` - Calculation helpers.
- `src/main/resources` - Icons and image assets.
- `src/test/java` - Java test runners.
- `docs` - Test evidence, change notes, and defect updates.

## Requirements

- Java JDK 26
- JavaFX configured in IntelliJ IDEA for running the UI.
- Windows 11 was used as the main test environment.

## Run the JavaFX App

Open the project in IntelliJ IDEA and run:

src/main/java/app/Main.java

Make sure JavaFX is configured in the IDE before running the UI.

## Run the Integration Regression Test

From the project root, compile and run the non-JavaFX service test:

powershell

mkdir out
javac -d out (Get-ChildItem -Recurse src/main/java/model,src/main/java/repository,src/main/java/service,src/main/java/util,src/test/java -Filter *.java).FullName
java -cp out IntegrationRegressionTest


Expected result:


Passed checks: 78
Failed checks: 0


## Current Test Evidence

The latest integration regression output is saved at:

docs/test_evidence/IntegrationRegressionTest_2026-06-22_output.txt


## Notes

The integration test confirms signup validation, duplicate account handling, session routing, routine persistence, profile saving/loading, workout history persistence, calorie calculation, recommendations, and password hashing behavior.

Manual JavaFX screenshot evidence should still be added by the frontend/UI owners for login, signup, dashboard, profile, routine builder, workout history, and UI graphics checks.
