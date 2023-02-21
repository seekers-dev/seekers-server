package com.seekers;

import com.seekers.grpc.SeekersClient;
import com.seekers.grpc.SeekersServer;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
	private final SeekersServer server = new SeekersServer();
	private final SeekersClient client = new SeekersClient();

	@Override
	public void start(Stage stage) throws Exception {
		stage.setOnCloseRequest(c -> {
			try {
				client.stop();
				server.stop();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
		stage.setScene(client.getGame());
		stage.setResizable(false);
		stage.show();
	}
}
