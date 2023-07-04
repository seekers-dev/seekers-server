package org.seekers;

import org.seekers.grpc.SeekersServer;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
	private final SeekersServer server = new SeekersServer();

	@Override
	public void start(Stage stage) throws Exception {
		stage.setOnCloseRequest(c -> {
			try {
				server.stop();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
				Thread.currentThread().interrupt();
			}
		});
		stage.setScene(server.getGame());
		stage.setAlwaysOnTop(true);
		stage.setResizable(false);
		stage.show();
	}
}
