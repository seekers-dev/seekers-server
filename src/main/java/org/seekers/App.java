package org.seekers;

import org.seekers.game.Game;
import org.seekers.grpc.SeekersJavaClient;
import org.seekers.grpc.SeekersServer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
	private final SeekersServer server = new SeekersServer();

	private final SeekersJavaClient client0 = new SeekersJavaClient();
	private final SeekersJavaClient client1 = new SeekersJavaClient();

//	private final SeekersPythonClient client0 = new SeekersPythonClient("target/seekers-py/examples/ai-decide.py");
//	private final SeekersPythonClient client1 = new SeekersPythonClient("target/seekers-py/examples/ai-magnet.py");

	@Override
	public void start(Stage stage) throws Exception {
		stage.setOnCloseRequest(c -> {
			try {
				client0.stop();
				client1.stop();
				server.stop();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		});
		Game game = server.getGame();

		stage.setScene(new Scene(game.getRender(), game.getWidth(), game.getHeight()));
		stage.setAlwaysOnTop(true);
		stage.setResizable(false);
		stage.show();
	}
}
