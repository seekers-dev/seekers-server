package org.seekers;

import org.ini4j.Ini;
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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates the server and application for the {@code SeekersServer}.
 *
 * @author karlz
 */
public class App extends Application {

	private static final Logger logger = LoggerFactory.getLogger(App.class);

	private final @Nonnull List<LanguageLoader> loaders = new ArrayList<>();
	private final @Nonnull Ini config = new Ini();

	/**
	 * Checks for all content folders. If a folder does not exist, it will be created. Loads all plugins.
	 */
	@Override
	public void init() throws IOException {
		config.load(new File("config.ini"));

		for (String folder : List.of("dist", "players", "plugins", "results")) {
			Path path = Path.of(folder);
			if (!Files.exists(path)) {
				try {
					Files.createDirectory(path);
				} catch (IOException ex) {
					logger.error("Could not create directory", ex);
				}
			}
		}

		PluginManager manager = new DefaultPluginManager();
		manager.loadPlugins();
		manager.startPlugins();

		var extensions = manager.getExtensions(SeekersExtension.class);
		logger.info("Found following extensions: {}", extensions);
		for (SeekersExtension extension : extensions) {
			extension.setup(config.get(manager.whichPlugin(extension.getClass()).getPluginId()));
			extension.addLanguageLoaders(loaders);
		}

		manager.stopPlugins();
		manager.unloadPlugins();
	}

	@Override
	public void start(Stage stage) throws Exception {
		final SeekersServer server = new SeekersServer(stage, config, loaders);
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
