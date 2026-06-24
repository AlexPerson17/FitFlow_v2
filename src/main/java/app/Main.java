/*
 * File: Main.java
 * Version: 0.5.2
 * Date last edited: 6/14/2026
 * Author: Alex Ronn
 * File Purpose: Launches the application, starting with the login screen.
 */
package app;

import javafx.application.Application;
import javafx.stage.Stage;
import view.AppStateManager;

public class Main extends Application {

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

    public static void main(String[] args) {
        launch(args);
    }
}