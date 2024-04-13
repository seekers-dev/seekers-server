package org.seekers.plugin;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Represents a Seekers Tournament.
 */
public class Tournament implements Serializable {

	private static final Logger logger = LoggerFactory.getLogger(Tournament.class);
	private static final Gson gson = new Gson();

	private final @Nonnull List<List<String>> matches = new LinkedList<>();
	private final @Nonnull Map<String, List<Integer>> results = new HashMap<>();

	public Tournament(String path) {
		File folder = new File(path);
		String[] files = folder.list((File dir, String name) -> name.startsWith("ai") && !name.endsWith(".log"));
		if (files != null) {
			for (int p = 0, size = files.length; p < size; p++) {
				for (int m = p + 1; m < size; m++) {
					matches.add(List.of(folder + "/" + files[p], folder + "/" + files[m]));
				}
			}
		} else {
			logger.error("No AIs found in folder, maybe folder or files are missing?");
		}
	}

	public void save() throws IOException {
		File file = new File("results");
		if (!file.exists() && !file.mkdir()) {
			logger.error("Failed to create results folder");
		}
		try (FileOutputStream stream = new FileOutputStream("results/" + hashCode() + ".json")) {
			stream.write(gson.toJson(this).getBytes());
		}
    }

	@Nonnull
	public List<List<String>> getMatches() {
		return matches;
	}

	@Nonnull
	public Map<String, List<Integer>> getResults() {
		return results;
	}
}
