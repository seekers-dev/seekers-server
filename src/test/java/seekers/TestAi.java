package seekers;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class TestAi {
	@Test
	void runtime() throws IOException {
		String[] input = { "python", "src/test/resources/test-ai.py" };
		Runtime.getRuntime().exec(input);
	}

	@Test
	void process() throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder("python", "src/test/resources/test-ai.py");
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();
		process.getInputStream().transferTo(System.out);
	}
}
