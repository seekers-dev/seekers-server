package org.seekers;

import org.pf4j.JarPluginManager;
import org.pf4j.PluginManager;
import org.seekers.grpc.SeekersServer;

import javafx.application.Application;
import javafx.stage.Stage;
import org.seekers.plugin.LanguageLoader;
import org.seekers.plugin.SeekersExtension;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class App extends Application {

	private final @Nonnull List<LanguageLoader> loaders = new ArrayList<>();

	@Override
	public void init() {
		PluginManager manager = new JarPluginManager();
		manager.loadPlugins();
		manager.startPlugins();

		for (SeekersExtension extension : manager.getExtensions(SeekersExtension.class)) {
			extension.addLanguageLoaders(loaders);
		}

		manager.stopPlugins();
	}

	@Override
	public void start(Stage stage) throws Exception {
		final SeekersServer server = new SeekersServer(stage);
		server.getLoaders().addAll(loaders);
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
