package org.seekers;

import javafx.application.Application;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Launcher {
	public static void main(String[] args) {
		ArgumentParser parser = ArgumentParsers.newFor("seekers-service").build();
		parser.addArgument("--port").type(int.class).help("sets the port").setDefault(7777);
		parser.addArgument("--mode").type(String.class).help("the game mode").setDefault("standard");
		parser.addArgument("locations").type(String.class).help("client locations").nargs("+");
		try {
			@SuppressWarnings("unused") // TODO use args
			Namespace res = parser.parseArgs(args);

			Application.launch(App.class, args);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
		}
	}
}
