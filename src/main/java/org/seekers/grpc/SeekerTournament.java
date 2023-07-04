package org.seekers.grpc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.util.Pair;

public class SeekerTournament implements Iterator<Pair<String, String>> {

	private final @Nonnull Queue<Pair<String, String>> matches = new LinkedList<>();
	private final @Nonnull Map<String, PlayerCard> cards = new HashMap<>();
	private final @Nonnull List<PlayerCard> tops = new ArrayList<>();

	public SeekerTournament() {
		File folder = new File(SeekerProperties.getDefault().getProjectPathToAis());
		String[] files = folder.list((File dir, String name) -> name.endsWith(".py"));
		for (int p = 0, size = files.length; p < size; p++) {
			for (int m = p + 1; m < size; m++) {
				matches.add(new Pair<>(files[p], files[m]));
			}
		}
	}

	public PlayerCard getPlayerCard(String name) {
		PlayerCard card;
		if (!cards.containsKey(name)) {
			card = new PlayerCard(name);
			cards.put(name, card);
			tops.add(card);
		} else {
			card = cards.get(name);
		}
		return card;
	}

	public Map<String, PlayerCard> getPlayerCards() {
		return cards;
	}

	@Nonnull
	public List<PlayerCard> getTopPlayers() {
		tops.sort(null);
		return tops;
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

	public static class PlayerCard implements Comparable<PlayerCard> {
		private int wins;
		private int draws;
		private int losses;

		private final String name;

		public PlayerCard(String name) {
			this.name = name;
		}

		public int compared(double value) {
			if (value < 0)
				return -1;
			if (value > 0)
				return 1;
			return 0;
		}

		@Override
		public int compareTo(PlayerCard o) {
			return compared((o.wins + 0.5 * o.draws) - (wins + 0.5 * draws));
		}

		@Override
		public boolean equals(@Nullable Object obj) {
			if (obj == null || obj instanceof PlayerCard) {
				return false;
			}
			PlayerCard card = (PlayerCard) obj;
			return name.contentEquals(card.name) && wins == card.wins && draws == card.draws && losses == card.losses;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 31 * hash + name.hashCode();
			hash = 31 * hash + wins;
			hash = 31 * hash + draws;
			hash = 31 * hash + losses;
			return hash;
		}

		@Override
		public String toString() {
			return name + " (" + wins + '-' + draws + '-' + losses + ')';
		}

		public String getName() {
			return name;
		}

		public int getWins() {
			return wins;
		}

		public void setWins(int wins) {
			this.wins = wins;
		}

		public int getDraws() {
			return draws;
		}

		public void setDraws(int draws) {
			this.draws = draws;
		}

		public int getLosses() {
			return losses;
		}

		public void setLosses(int losses) {
			this.losses = losses;
		}
	}
}
