package org.seekers.grpc;

import java.util.Map;

import org.seekers.graphic.Camp;
import org.seekers.graphic.Game;
import org.seekers.graphic.Goal;
import org.seekers.graphic.Player;
import org.seekers.graphic.Seeker;

import com.karlz.entity.Creator;
import com.karlz.exchange.ExchangeHelper.StoreHelper;
import com.karlz.exchange.Reference;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class GetHelper implements StoreHelper<Reference<?>, StatusReply> {
	private final ObservableMap<String, Player> players = FXCollections.observableHashMap();
	private final ObservableMap<String, Seeker> seekers = FXCollections.observableHashMap();
	private final ObservableMap<String, Goal> goals = FXCollections.observableHashMap();
	private final ObservableMap<String, Camp> camps = FXCollections.observableHashMap();

	private final Game game;

	private final Map<String, Creator<? extends Reference<?>>> creators = Map.of( //
			"Player", () -> new Player(getGame()), //
			"Seeker", () -> new Seeker(getGame()), //
			"Goal", () -> new Goal(getGame()), //
			"Camp", () -> new Camp(getGame()) //
	);

	public GetHelper(Game game) {
		this.game = game;
	}

	@Override
	public Reference<?> save(Map<String, Reference<?>> map, String id, String type) {
		Reference<?> val = map.get(id);
		if (val == null) {
			val = creators.get(type).create();
			map.put(id, val);
		}
		return val;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(StatusReply associated) {
		for (StatusReply.Player player : associated.getPlayersList()) {
			((Player) save((Map<String, Reference<?>>) (Map<String, ?>) players, player.getId(), "Player"))
					.update(player);
		}
		for (StatusReply.Camp camp : associated.getCampsList()) {
			((Camp) save((Map<String, Reference<?>>) (Map<String, ?>) camps, camp.getId(), "Camp")).update(camp);
		}
		for (StatusReply.Seeker seeker : associated.getSeekersList()) {
			((Seeker) save((Map<String, Reference<?>>) (Map<String, ?>) seekers, seeker.getSuper().getId(), "Seeker"))
					.update(seeker);
		}
		for (StatusReply.Goal goal : associated.getGoalsList()) {
			((Goal) save((Map<String, Reference<?>>) (Map<String, ?>) goals, goal.getSuper().getId(), "Goal"))
					.update(goal);
		}
	}

	public Game getGame() {
		return game;
	}

	@Override
	public Map<String, Map<String, ? extends Reference<?>>> getContainer() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Creator<? extends Reference<?>>> getCreators() {
		return creators;
	}

	public ObservableMap<String, Player> getPlayers() {
		return players;
	}

	public ObservableMap<String, Camp> getCamps() {
		return camps;
	}

	public ObservableMap<String, Seeker> getSeekers() {
		return seekers;
	}

	public ObservableMap<String, Goal> getGoals() {
		return goals;
	}
}
