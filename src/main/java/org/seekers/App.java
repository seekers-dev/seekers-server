package org.seekers;

import org.seekers.game.Game;
import org.seekers.grpc.SeekersClient;
import org.seekers.grpc.SeekersServer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
	private final SeekersServer server = new SeekersServer();

	private final SeekersClient client0 = new SeekersClient("ai-decide.py");
	private final SeekersClient client1 = new SeekersClient("ai-magnet.py");

	@Override
	public void start(Stage stage) throws Exception {
		stage.setOnCloseRequest(c -> {
			try {
				client0.stop();
				client1.stop();
				server.stop();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
				Thread.currentThread().interrupt();
			}
		});
		Game game = server.getGame();

		stage.setScene(new Scene(game.getRender(), game.getWidth(), game.getHeight()));
		stage.setAlwaysOnTop(true);
		stage.setResizable(false);
		stage.show();
	}
}
