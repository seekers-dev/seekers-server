package org.seekers;

import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.seekers.grpc.SeekersServer;

import javafx.application.Application;
import javafx.stage.Stage;
import org.seekers.plugin.LanguageLoader;
import org.seekers.plugin.SeekersExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class App extends Application {

	private static final Logger logger = LoggerFactory.getLogger(App.class);

	private final @Nonnull List<LanguageLoader> loaders = new ArrayList<>();

	@Override
	public void init() {
		PluginManager manager = new DefaultPluginManager();
		manager.loadPlugins();
		manager.startPlugins();

		var extensions = manager.getExtensions(SeekersExtension.class);
		logger.info("Found following extensions: {}", extensions);
		for (SeekersExtension extension : extensions) {
			extension.addLanguageLoaders(loaders);
		}
	}

	@Override
	public void start(Stage stage) throws Exception {
		final SeekersServer server = new SeekersServer(stage, loaders);
		stage.setOnCloseRequest(c -> {
			logger.info("Try stopping server on stage close request");
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
