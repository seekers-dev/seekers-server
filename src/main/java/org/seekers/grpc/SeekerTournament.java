package org.seekers.grpc;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

import javafx.util.Pair;

public class SeekerTournament implements Iterator<Pair<String, String>> {

	private final Queue<Pair<String, String>> matches = new LinkedList<>();

	public SeekerTournament() {
		File folder = new File(SeekerProperties.getDefault().getProjectPathToAis());
		String[] files = folder.list((File dir, String name) -> name.endsWith(".py"));
		for (int p = 0, size = files.length; p < size; p++) {
			for (int m = p + 1; m < size; m++) {
				System.out.println(files[p] + " vs. " + files[m]);
				matches.add(new Pair<>(files[p], files[m]));
			}
		}
	}

	@Override
	public boolean hasNext() {
		return !matches.isEmpty();
	}

	@Override
	public Pair<String, String> next() {
		if (!hasNext())
			throw new NoSuchElementException();
		return matches.poll();
	}
}
