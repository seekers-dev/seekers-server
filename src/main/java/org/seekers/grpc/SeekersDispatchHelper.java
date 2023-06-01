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

public class SeekersDispatchHelper implements DispatchHelper<Corresponding<?>, StatusResponse> {
	private final Set<Player> players = new HashSet<>();
	private final Set<Seeker> seekers = new HashSet<>();
	private final Set<Goal> goals = new HashSet<>();
	private final Set<Camp> camps = new HashSet<>();

	private final Game game;

	public SeekersDispatchHelper(Game game) {
		this.game = game;
		init(game);
	}

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

	public void init(Game game) {
		players.addAll(game.getPlayers().values());
		seekers.addAll(game.getSeekers().values());
		goals.addAll(game.getGoals().values());
		camps.addAll(game.getCamps().values());
	}

	public void clean() {
		players.clear();
		seekers.clear();
		goals.clear();
		camps.clear();
	}

	@Override
	public void push(Collection<Corresponding<?>> collection, Corresponding<?> element) {
		collection.add(element);
	}

	public Set<Player> getPlayers() {
		return players;
	}

	public Set<Seeker> getSeekers() {
		return seekers;
	}

	public Set<Goal> getGoals() {
		return goals;
	}

	public Set<Camp> getCamps() {
		return camps;
	}
}
