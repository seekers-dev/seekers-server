package org.seekers;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.seekers.grpc.SeekersServer;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		Parameters parameters = getParameters();
		String[] args = parameters.getRaw().toArray(new String[0]);
		ArgumentParser parser = ArgumentParsers.newFor("seekers-server").build();
		parser.addArgument("--port").type(int.class).help("sets the port").setDefault(7777);
		parser.addArgument("--mode").type(String.class).help("the game mode").setDefault("standard");
		// parser.addArgument("locations").type(String.class).help("client locations").nargs("+");
		try {
			@SuppressWarnings("unused") // TODO use args
			Namespace res = parser.parseArgs(args);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
		}

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
