package org.seekers.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

/**
 * The SeekersPythonClient class is a client for running and monitoring the
 * Seekers game client written in Python.
 */
public class SeekersClient {

	private static final @Nonnull Logger logger = LoggerFactory.getLogger(SeekersClient.class);

	private static final @Nonnull String PYTHON_FOLDER = SeekersConfig.getConfig().getProjectPythonFolder();
	private static final @Nonnull String PYTHON_BINARY = SeekersConfig.getConfig().getProjectPythonBinary();

	private final @Nonnull ProcessBuilder builder;

	/**
	 * Initializes the SeekersPythonClient with the specified Python file.
	 *
	 * @param path The path to the Python file.
	 */
	public SeekersClient(String path) throws IOException {
		builder = new ProcessBuilder(PYTHON_FOLDER.concat(PYTHON_BINARY), PYTHON_FOLDER.concat(
				"run_client.py"), path);
		builder.redirectErrorStream(true);

		File log = new File(path + ".log");
		if (!log.exists() && !log.createNewFile()) { // create log file
			logger.error("Could not create log file {}", log.getAbsolutePath());
		}
		builder.redirectOutput(log);
		start();
	}

	private @Nullable Process process;

	/**
	 * Starts the Python client process.
	 *
	 * @throws IOException if an I/O error occurs during process startup.
	 */
	public void start() throws IOException {
		process = builder.start();
		logger.info("Client started");
	}

	/**
	 * Stops the Python client process if it is running.
	 */
	public void stop() {
		if (process != null) {
			process.destroy();
		} else {
			logger.error("Process is null!");
		}
		logger.info("Client stopped");
	}
}
