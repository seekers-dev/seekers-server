package org.seekers.grpc;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * The SeekersPythonClient class is a client for running and monitoring the
 * Seekers game client written in Python.
 */
public class SeekersClient {

	private static final Logger logger = Logger.getLogger(SeekersClient.class.getSimpleName());

	private final ProcessBuilder builder;

	/**
	 * Initializes the SeekersPythonClient with the specified Python file.
	 *
	 * @param path The path to the Python file.
	 */
	public SeekersClient(String path) {
		builder = new ProcessBuilder(
				SeekersProperties.getDefault().getProjectExecCommand().concat(" " + path).split(" "));
		builder.redirectErrorStream(true);

		File log = new File(path + ".log");
		try {
			if (!log.exists() && !log.createNewFile()) { // create log file
				logger.warning("Could not create log file: " + log.getAbsolutePath());
			}
			builder.redirectOutput(log);
			start();
		} catch (IOException e) {
			throw  new SeekersException(e);
		}
	}

	private Process process;

	/**
	 * Starts the Python client process.
	 *
	 * @throws IOException if an I/O error occurs during process startup.
	 */
	public void start() throws IOException {
		process = builder.start();
		logger.info("Client stated");
	}

	/**
	 * Stops the Python client process if it is running.
	 */
	public void stop() {
		if (process != null) {
			process.destroy();
		} else {
			logger.warning("Process is null!");
		}
		logger.info("Client stopped");
	}
}
