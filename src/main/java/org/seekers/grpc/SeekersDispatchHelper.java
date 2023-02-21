package org.seekers.grpc;

import static org.seekers.grpc.Corresponding.transform;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.seekers.game.Camp;
import org.seekers.game.Game;
import org.seekers.game.Goal;
import org.seekers.game.Player;
import org.seekers.game.Seeker;

import com.karlz.exchange.ExchangeHelper.DispatchHelper;

public class SeekersDispatchHelper implements DispatchHelper<Corresponding<?>, StatusReply> {
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
	public StatusReply associated() {
		@SuppressWarnings("unchecked")
		StatusReply reply = StatusReply.newBuilder().addAllPlayers(transform(players)).addAllCamps(transform(camps))
				.addAllSeekers((Collection<StatusReply.Seeker>) (Collection<?>) transform(seekers))
				.addAllGoals((Collection<StatusReply.Goal>) (Collection<?>) transform(goals))
				.setPassedPlaytime(game.getPassedPlaytime()).build();
		clean();
		return reply;
	}

	public void init(Game game) {
		players.addAll(game.getPlayers());
		seekers.addAll(game.getSeekers());
		goals.addAll(game.getGoals());
		camps.addAll(game.getCamps());
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
