package org.seekers.grpc;

import java.util.Collection;

import org.seekers.game.Camp;
import org.seekers.game.Game;
import org.seekers.game.Goal;
import org.seekers.game.Player;
import org.seekers.game.Seeker;
import org.seekers.grpc.net.StatusResponse;

import io.scvis.proto.Corresponding;

/**
 * The SeekersDispatchHelper class is responsible for managing the
 * synchronization and exchange of game data between the server and clients in
 * the Seekers game.
 * 
 * @author karlz
 */
public class SeekersDispatchHelper implements Corresponding<StatusResponse> {

	private final Game game;

	/**
	 * Constructs a new SeekersDispatchHelper instance.
	 *
	 * @param game The game instance to associate with this helper.
	 */
	public SeekersDispatchHelper(Game game) {
		this.game = game;
	}

	/**
	 * Retrieves the associated StatusResponse containing the transformed game data.
	 * This method is called by the server to obtain the current game state to be
	 * sent to the clients.
	 *
	 * @return The StatusResponse containing the transformed game data.
	 */
	@Override
	public StatusResponse associated() {
		@SuppressWarnings("unchecked")
		StatusResponse reply = StatusResponse.newBuilder().addAllPlayers(
				(Collection<org.seekers.grpc.game.Player>) (Collection<?>) Corresponding.transform(getPlayers()))
				.addAllCamps(
						(Collection<org.seekers.grpc.game.Camp>) (Collection<?>) Corresponding.transform(getCamps()))
				.addAllSeekers((Collection<org.seekers.grpc.game.Seeker>) (Collection<?>) Corresponding
						.transform(getSeekers()))
				.addAllGoals(
						(Collection<org.seekers.grpc.game.Goal>) (Collection<?>) Corresponding.transform(getGoals()))
				.setPassedPlaytime(game.getPassedPlaytime()).build();
		return reply;
	}

	/**
	 * Retrieves the set of players in the game.
	 *
	 * @return The set of players.
	 */
	public Collection<Player> getPlayers() {
		return game.getPlayers().values();
	}

	/**
	 * Retrieves the set of seekers in the game.
	 *
	 * @return The set of seekers.
	 */
	public Collection<Seeker> getSeekers() {
		return game.getSeekers().values();
	}

	/**
	 * Retrieves the set of goals in the game.
	 *
	 * @return The set of goals.
	 */
	public Collection<Goal> getGoals() {
		return game.getGoals().values();
	}

	/**
	 * Retrieves the set of camps in the game.
	 *
	 * @return The set of camps.
	 */
	public Collection<Camp> getCamps() {
		return game.getCamps().values();
	}
}
