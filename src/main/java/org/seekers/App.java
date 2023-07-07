package org.seekers;

import org.seekers.grpc.SeekerProperties;
import org.seekers.grpc.SeekersServer;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		final SeekersServer server = new SeekersServer(stage);
		stage.setOnCloseRequest(c -> {
			try {
				server.stop();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
				Thread.currentThread().interrupt();
			}
		});
		stage.setWidth(SeekerProperties.getDefault().getMapWidth());
		stage.setHeight(SeekerProperties.getDefault().getMapHeight());
		stage.setTitle("Seekers");
		stage.setAlwaysOnTop(true);
		stage.setResizable(false);
		stage.show();
	}
}
