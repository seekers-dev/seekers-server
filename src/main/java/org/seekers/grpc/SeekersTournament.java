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

/**
 * Represents a Seekers Tournament.
 */
public class SeekersTournament implements Iterator<Pair<String, String>> {

	private final @Nonnull Queue<Pair<String, String>> matches = new LinkedList<>();
	private final @Nonnull Map<String, PlayerCard> cards = new HashMap<>();
	private final @Nonnull List<PlayerCard> tops = new ArrayList<>();

	/**
	 * Constructs a SeekersTournament object and initializes the matches queue.
	 */
	public SeekersTournament() {
		File folder = new File(SeekersProperties.getDefault().getProjectPathToAis());
		String[] files = folder.list((File dir, String name) -> name.startsWith("ai") && name.endsWith(".py"));
		for (int p = 0, size = files.length; p < size; p++) {
			for (int m = p + 1; m < size; m++) {
				matches.add(new Pair<>(files[p], files[m]));
			}
		}
	}

	/**
	 * Gets the PlayerCard object for the given name. If no PlayeCard is present, a
	 * new will be created.
	 *
	 * @param name the name of the player
	 * @return the PlayerCard object
	 */
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

	/**
	 * Gets the map of player cards.
	 *
	 * @return the map of player cards
	 */
	@Nonnull
	public Map<String, PlayerCard> getPlayerCards() {
		return cards;
	}

	/**
	 * Gets the list of top players.
	 *
	 * @return the list of top players
	 */
	@Nonnull
	public List<PlayerCard> getTopPlayers() {
		tops.sort(null);
		return tops;
	}

	/**
	 * Checks if there are more matches available.
	 *
	 * @return true if there are more matches, false otherwise
	 */
	@Override
	public boolean hasNext() {
		return !matches.isEmpty();
	}

	/**
	 * Gets the next match.
	 *
	 * @return the next match as a Pair of player names
	 * @throws NoSuchElementException if there are no more matches left
	 */
	@Override
	public Pair<String, String> next() {
		if (!hasNext())
			throw new NoSuchElementException();
		return matches.poll();
	}

	/**
	 * Represents a player card with win, draw, and loss statistics.
	 */
	public static class PlayerCard implements Comparable<PlayerCard> {
		private int wins;
		private int draws;
		private int losses;

		private final String name;

		/**
		 * Constructs a PlayerCard object with the given name.
		 *
		 * @param name the name of the player
		 */
		public PlayerCard(String name) {
			this.name = name;
		}

		/**
		 * Compares the player card to another player card based on their win, draw, and
		 * loss statistics total value.
		 *
		 * @param value the value to compare against
		 * @return -1 if the value is less, 0 if the value is equal, 1 if the value is
		 *         greater
		 */
		public int compared(double value) {
			if (value < 0)
				return -1;
			if (value > 0)
				return 1;
			return 0;
		}

		/**
		 * Compares the player card to another player card based on their win, draw, and
		 * loss statistics. A win counts as one point, a draw as a half point and a loss
		 * as no point.
		 * <p>
		 * <code>value = wins + 0.5 * draws</code>
		 *
		 * @param o the player card to compare
		 * @return -1 if the other player card has a higher score, 0 if they have equal
		 *         scores, 1 if the other player card has a lower score
		 */
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

		/**
		 * Gets the name of the player.
		 *
		 * @return the player name
		 */
		public String getName() {
			return name;
		}

		/**
		 * Gets the number of wins.
		 *
		 * @return the number of wins
		 */
		public int getWins() {
			return wins;
		}

		/**
		 * Sets the number of wins.
		 *
		 * @param wins the number of wins
		 */
		public void setWins(int wins) {
			this.wins = wins;
		}

		/**
		 * Gets the number of draws.
		 *
		 * @return the number of draws
		 */
		public int getDraws() {
			return draws;
		}

		/**
		 * Sets the number of draws.
		 *
		 * @param draws the number of draws
		 */
		public void setDraws(int draws) {
			this.draws = draws;
		}

		/**
		 * Gets the number of losses.
		 *
		 * @return the number of losses
		 */
		public int getLosses() {
			return losses;
		}

		/**
		 * Sets the number of losses.
		 *
		 * @param losses the number of losses
		 */
		public void setLosses(int losses) {
			this.losses = losses;
		}
	}
}
