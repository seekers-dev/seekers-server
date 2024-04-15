package org.seekers;

import javafx.application.Application;

/**
 * Launcher instance for the application. This class exists to prevent possible future breaking changes. With this
 * class, the main method is always located inside the same class, independently of JavaFX and gRPC.
 *
 * @author karlz
 */
public class Launcher {
    /**
     * Launches the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
