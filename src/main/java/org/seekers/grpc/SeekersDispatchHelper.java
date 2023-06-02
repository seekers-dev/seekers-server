package org.seekers.grpc;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.seekers.game.Camp;
import org.seekers.game.Game;
import org.seekers.game.Goal;
import org.seekers.game.Player;
import org.seekers.game.Seeker;
import org.seekers.grpc.net.StatusResponse;

import io.scvis.proto.Corresponding;
import io.scvis.proto.ExchangeHelper.DispatchHelper;

/**
 * The SeekersDispatchHelper class is responsible for managing the
 * synchronization and exchange of game data between the server and clients in
 * the Seekers game.
 * 
 * @author karlz
 */
public class SeekersDispatchHelper implements DispatchHelper<Corresponding<?>, StatusResponse> {
	private final Set<Player> players = new HashSet<>();
	private final Set<Seeker> seekers = new HashSet<>();
	private final Set<Goal> goals = new HashSet<>();
	private final Set<Camp> camps = new HashSet<>();

	private final Game game;

	/**
	 * Constructs a new SeekersDispatchHelper instance.
	 *
	 * @param game The game instance to associate with this helper.
	 */
	public SeekersDispatchHelper(Game game) {
		this.game = game;
		init(game);
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
		StatusResponse reply = StatusResponse.newBuilder()
				.addAllPlayers(
						(Collection<org.seekers.grpc.game.Player>) (Collection<?>) Corresponding.transform(players))
				.addAllCamps((Collection<org.seekers.grpc.game.Camp>) (Collection<?>) Corresponding.transform(camps))
				.addAllSeekers(
						(Collection<org.seekers.grpc.game.Seeker>) (Collection<?>) Corresponding.transform(seekers))
				.addAllGoals((Collection<org.seekers.grpc.game.Goal>) (Collection<?>) Corresponding.transform(goals))
				.setPassedPlaytime(game.getPassedPlaytime()).build();
		clean();
		return reply;
	}

	/**
	 * Initializes the sets of players, seekers, goals, and camps with the
	 * corresponding entities from the game.
	 *
	 * @param game The game instance from which to initialize the entities.
	 */
	public void init(Game game) {
		players.addAll(game.getPlayers().values());
		seekers.addAll(game.getSeekers().values());
		goals.addAll(game.getGoals().values());
		camps.addAll(game.getCamps().values());
	}

	/**
	 * Clears the sets of players, seekers, goals, and camps. This method is called
	 * after the associated StatusResponse is sent to clients to prepare for the
	 * next synchronization.
	 */
	public void clean() {
		players.clear();
		seekers.clear();
		goals.clear();
		camps.clear();
	}

	/**
	 * Adds an element to the given collection.
	 *
	 * @param collection The collection to which the element is added.
	 * @param element    The element to be added.
	 */
	@Override
	public void push(Collection<Corresponding<?>> collection, Corresponding<?> element) {
		collection.add(element);
	}

	/**
	 * Retrieves the set of players in the game.
	 *
	 * @return The set of players.
	 */
	public Set<Player> getPlayers() {
		return players;
	}

	/**
	 * Retrieves the set of seekers in the game.
	 *
	 * @return The set of seekers.
	 */
	public Set<Seeker> getSeekers() {
		return seekers;
	}

	/**
	 * Retrieves the set of goals in the game.
	 *
	 * @return The set of goals.
	 */
	public Set<Goal> getGoals() {
		return goals;
	}

	/**
	 * Retrieves the set of camps in the game.
	 *
	 * @return The set of camps.
	 */
	public Set<Camp> getCamps() {
		return camps;
	}
}
