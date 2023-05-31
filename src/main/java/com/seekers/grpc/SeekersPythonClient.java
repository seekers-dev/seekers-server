package com.seekers.grpc;

import java.io.File;
import java.io.IOException;

public class SeekersPythonClient {

	private static final String EXEC_CLIENT = "target/seekers-py/run_clients.py";

	private ProcessBuilder builder;

	public SeekersPythonClient(String file) {
		this.builder = new ProcessBuilder("python3", EXEC_CLIENT, file);
		File log = new File(file + ".log");

		builder.redirectErrorStream(true);

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

	public void start() throws IOException {
		builder.start();
	}

	public void stop() {
		if (process != null)
			process.destroy();
	}
}
