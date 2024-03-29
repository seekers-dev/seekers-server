package org.seekers;

import javafx.application.Application;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Launcher {
	public static void main(String[] args) {
		Application.launch(App.class, args);
	}
}
