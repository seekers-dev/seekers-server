package org.seekers.grpc;

import java.io.File;
import java.io.IOException;

/**
 * The SeekersPythonClient class is a client for running and monitoring the
 * Seekers game client written in Python.
 */
public class SeekersPythonClient {

	private ProcessBuilder builder;

	/**
	 * Initializes the SeekersPythonClient with the specified Python file.
	 *
	 * @param file The path to the Python file.
	 */
	public SeekersPythonClient(String file) {
		String path = SeekerProperties.getDefault().getProjectPathToAis() + file;
		builder = new ProcessBuilder("python3", SeekerProperties.getDefault().getProjectPathToExec(), path);
		builder.redirectErrorStream(true);

		File log = new File(path + ".log");
		try {
			if (!log.exists()) {
				log.createNewFile();
			}
			builder.redirectOutput(log);
			start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Process process;

	/**
	 * Starts the Python client process.
	 *
	 * @throws IOException if an I/O error occurs during process startup.
	 */
	public void start() throws IOException {
		builder.start();
	}

	/**
	 * Stops the Python client process if it is running.
	 */
	public void stop() {
		if (process != null)
			process.destroy();
	}
}
