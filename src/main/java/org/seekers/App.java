/*
 * Copyright (C) 2022  Seekers Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.seekers;

import org.ini4j.Ini;
import org.seekers.game.StandardMode;
import org.seekers.game.Tournament;
import org.seekers.grpc.SeekersServer;

import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Creates the server and application for the {@code SeekersServer}. Loads and unloads plugins. Loads the  If the stage is closed,
 * the server closes automatically, and vice versa.
 *
 * @author karlz
 */
public class App extends Application {

	private static final Logger logger = LoggerFactory.getLogger(App.class);

	private final @Nonnull Ini config = new Ini();

	/**
	 * Before the server or stage is started, three things must be checked:
	 * <ol>
	 *     <li>Checks for all content folders. If a folder does not exist, it will be created.</li>
	 *     <li>Loads the config.ini file</li>
	 *     <li>Loads all plugins.</li>
	 * </ol>
	 *
	 * @throws IOException if it could not read from the config file
	 */
	@Override
	public void init() throws IOException {
		Path path = Path.of("config.ini");
		if (!Files.exists(path)) {
			Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("config.ini")), path);
		}
		for (String folder : new String[] {"players", "drivers", "results"}) {
			path = Path.of(folder);
			if (!Files.exists(path)) {
				try {
					Files.createDirectory(path);
				} catch (IOException ex) {
					logger.error("Could not find nor create directory", ex);
				}
			}
		}

		config.load(new File("config.ini"));
	}

	@Override
	public void start(Stage stage) throws Exception {
		final SeekersServer server = new SeekersServer(stage, config).setGameMode(new StandardMode())
				.setTournament(new Tournament("players"));
		server.start();
		stage.setOnCloseRequest(c -> {
			logger.info("Try unloading plugins and stopping server on stage close request");
			try {
				server.stop();
			} catch (Exception ex) {
				Thread.currentThread().interrupt();
			}
		});
		stage.setTitle("Seekers");
		stage.setAlwaysOnTop(true);
		stage.setResizable(false);
		stage.show();
	}
}
