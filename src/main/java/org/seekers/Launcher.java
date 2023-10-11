package org.seekers;

import javafx.application.Application;

import java.io.IOException;

public class Launcher {
	public static void main(String[] args) throws IOException {
		Startup.check();
		Application.launch(App.class, args);
	}
}
