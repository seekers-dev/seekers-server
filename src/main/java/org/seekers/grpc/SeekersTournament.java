package org.seekers.grpc;

import java.util.ArrayList;
import java.util.List;

/**
 * Checkout <a href="https://strategy.channelfireball.com/mtg/channelmagic-articles/understanding-standings-part-i-tournament-structure-the-basics/" >Magic Tournaments<a/>
 */
public class SeekersTournament {
	public static class PlayerStat implements Comparable<PlayerStat> {
		private final List<PlayerStat> opponents = new ArrayList<>();

		private int victories;
		private int defeats;
		private int draws;

		/**
		 * Points scored in all matches
		 */
		private int scores;

		/**
		 * Points in the tournaments. Every victory gives 3 points and every draw 1
		 * point.
		 */
		private int points;

		/**
		 * Opponent Match Win Percentage
		 */
		private double omw;

		/**
		 * Game Win Percentage
		 */
		private double gw;

		/**
		 * Average Opponent Score Points4
		 */
		private double osp;

		/**
		 * Calculates the total points based on victories and draws. Points formula: 3
		 * points for a victory, 1 point for a draw.
		 */
		private void calcPoints() {
			this.points = victories * 3 + draws;
		}

		/**
		 * Calculates the opponent match win percentage. Sum up the victories of all
		 * opponents and divide by the total number of matches.
		 */
		private void calcOmw() {
			double total = 0;
			for (int index = 0; index < opponents.size(); index++) {
				total += opponents.get(index).victories;
			}
			this.omw = total / opponents.size();
		}

		/**
		 * Calculates the game win percentage. Divide the number of victories by the
		 * total number of matches.
		 */
		private void calcGw() {
			this.gw = victories / opponents.size();
		}

		/**
		 * Calculates the average opponent score points. Sum up the scores of all
		 * opponents and divide by the total number of matches.
		 */
		private void calcOsp() {
			int total = 0;
			for (int index = 0; index < opponents.size(); index++) {
				total += opponents.get(index).scores;
			}
			this.osp = total / opponents.size();
		}

		@Override
		public int compareTo(PlayerStat another) {
			int diffPoints = points - another.points;
			if (diffPoints != 0) {
				return Integer.signum(diffPoints);
			} else {
				double diffOmw = omw - another.omw;
				if (diffOmw != 0) {
					return (int) Math.signum(diffOmw);
				} else {
					double diffGw = gw - another.gw;
					if (diffGw != 0) {
						return (int) Math.signum(diffGw);
					} else {
						double diffOsp = osp - another.osp;
						return (int) Math.signum(diffOsp);
					}
				}
			}
		}

		/**
		 * Get the list of opponents played by the player.
		 * 
		 * @return List of opponents.
		 */
		public List<PlayerStat> getOpponents() {
			return opponents;
		}

		public int getVictories() {
			return victories;
		}

		public void setVictories(int victories) {
			this.victories = victories;
		}

		public int getDraws() {
			return draws;
		}

		public void setDraws(int draws) {
			this.draws = draws;
		}

		public int getDefeats() {
			return defeats;
		}

		public void setDefeats(int defeats) {
			this.defeats = defeats;
		}

		public double getOpponentMatchWinPercentage() {
			return omw;
		}

		public double getGameWinPercentage() {
			return gw;
		}

		public double getOpponentScorePoints() {
			return osp;
		}
	}

	private final List<PlayerStat> players = new ArrayList<>();

	public SeekersTournament() {

	}

	public void end() {
		// Split own and opponent scores
		for (PlayerStat player : players) {
			player.calcPoints();
			player.calcGw();
		}
		for (PlayerStat player : players) {
			player.calcOmw();
			player.calcOsp();
		}
		players.sort(null); // Use comparable
	}

	public List<PlayerStat> getPlayers() {
		return players;
	}
}
