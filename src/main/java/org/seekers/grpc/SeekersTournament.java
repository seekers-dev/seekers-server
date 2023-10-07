package org.seekers.grpc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.Gson;

/**
 * Represents a Seekers Tournament.
 */
public class SeekersTournament implements Serializable, Iterator<SeekersTournament.Match> {

	private static final Gson gson = new Gson();

	private final @Nonnull List<Match> matches = new LinkedList<>();
	private final @Nonnull Map<String, Participant> participants = new HashMap<>();

	/**
	 * Constructs a SeekersTournament object and initializes the matches queue.
	 */
	public SeekersTournament() {
		File folder = new File(SeekersProperties.getDefault().getProjectPathToAis());
		String[] files = folder.list((File dir, String name) -> name.startsWith("ai") && name.endsWith(".py"));
		Objects.requireNonNull(files);
		for (int p = 0, size = files.length; p < size; p++) {
			for (int m = p + 1; m < size; m++) {
				matches.add(new Match(List.of(folder + "/" + files[p], folder + "/" + files[m])));
			}
		}
	}

	public void save() {
		try (FileOutputStream stream = new FileOutputStream(hashCode() + ".json")) {
			stream.write(gson.toJson(this).getBytes());
		} catch (IOException e) {
            throw new SeekersException(e);
        }
    }

	/**
	 * Gets the Participant object for the given name. If no Participant is present, a
	 * new will be created.
	 *
	 * @param name the name of the player
	 * @return the Participant object
	 */
	public Participant getPlayerCard(String name) {
		Participant card;
		if (!participants.containsKey(name)) {
			card = new Participant(name);
			participants.put(name, card);
		} else {
			card = participants.get(name);
		}
		return card;
	}

	/**
	 * Gets the list of top players.
	 *
	 * @return the list of top players
	 */
	@Nonnull
	public List<Participant> getTopPlayers() {
        List<Participant> tops = new ArrayList<>(participants.values());
		tops.sort(null);
		return tops;
	}

	public Match getCurrentMatch() {
		if (index == 0)
			throw new NoSuchElementException();
		return matches.get(index - 1);
	}

	/**
	 * Checks if there are more matches available.
	 *
	 * @return true if there are more matches, false otherwise
	 */
	@Override
	public boolean hasNext() {
		return index < matches.size();
	}

	private transient int index = 0;

	/**
	 * Gets the next match.
	 *
	 * @return the next match as a Pair of player names
	 * @throws NoSuchElementException if there are no more matches left
	 */
	@Override
	public Match next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		return matches.get(index++);
	}

	public static class Match implements Serializable {
		private final Map<String, Integer> members = new LinkedHashMap<>();

		@SuppressWarnings("unused")
		private boolean isOver;

		public Match(Iterable<String> participants) {
			for (String participant : participants) {
				members.put(participant, 0);
			}
		}

		public void markAsOver() {
			this.isOver = true;
		}

		public boolean isOver() {
			return isOver;
		}

		public Map<String, Integer> getMembers() {
			return members;
		}
	}

	/**
	 * Represents a player card with win, draw, and loss statistics.
	 */
	public static class Participant implements Comparable<Participant> {
		private int wins;
		private int draws;
		private int losses;

		private final String name;

		/**
		 * Constructs a Participant object with the given name.
		 *
		 * @param name the name of the player
		 */
		public Participant(String name) {
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
		public int compareTo(Participant o) {
			return compared((o.wins + 0.5 * o.draws) - (wins + 0.5 * draws));
		}

		@Override
		public boolean equals(@Nullable Object obj) {
			if (!(obj instanceof Participant)) {
				return false;
			}
			Participant card = (Participant) obj;
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
