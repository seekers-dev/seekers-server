package org.seekers;

import org.seekers.grpc.SeekersServer;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		final SeekersServer server = new SeekersServer(stage, 7777);
		stage.setOnCloseRequest(c -> {
			try {
				server.stop();
			} catch (Exception ex) {
				Thread.currentThread().interrupt();
			}
		});
		stage.setScene(server.getGame());
		stage.setTitle("Seekers");
		stage.setAlwaysOnTop(true);
		stage.setResizable(false);
		stage.show();
	}
}
