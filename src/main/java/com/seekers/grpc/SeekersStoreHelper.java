package com.seekers.grpc;

import java.util.Map;
import java.util.function.Supplier;

import com.seekers.graphic.Camp;
import com.seekers.graphic.Game;
import com.seekers.graphic.Goal;
import com.seekers.graphic.Player;
import com.seekers.graphic.Seeker;
import com.seekers.grpc.game.StatusResponse;

import io.scvis.proto.ExchangeHelper.StoreHelper;
import io.scvis.proto.Reference;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class SeekersStoreHelper implements StoreHelper<Reference<?>, StatusResponse> {
	private final ObservableMap<String, Player> players = FXCollections.observableHashMap();
	private final ObservableMap<String, Seeker> seekers = FXCollections.observableHashMap();
	private final ObservableMap<String, Goal> goals = FXCollections.observableHashMap();
	private final ObservableMap<String, Camp> camps = FXCollections.observableHashMap();

	private final Game game;

	private final Map<String, Supplier<? extends Reference<?>>> creators = Map.of( //
			"Player", () -> new Player(getGame()), //
			"Seeker", () -> new Seeker(getGame()), //
			"Goal", () -> new Goal(getGame()), //
			"Camp", () -> new Camp(getGame()) //
	);

	public SeekersStoreHelper(Game game) {
		this.game = game;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(StatusResponse associated) {
		for (com.seekers.grpc.game.Player player : associated.getPlayersList()) {
			((Player) save((Map<String, Reference<?>>) (Map<String, ?>) players, player.getId(), "Player"))
					.update(player);
		}
		for (com.seekers.grpc.game.Camp camp : associated.getCampsList()) {
			((Camp) save((Map<String, Reference<?>>) (Map<String, ?>) camps, camp.getId(), "Camp")).update(camp);
		}
		for (com.seekers.grpc.game.Seeker seeker : associated.getSeekersList()) {
			((Seeker) save((Map<String, Reference<?>>) (Map<String, ?>) seekers, seeker.getSuper().getId(), "Seeker"))
					.update(seeker);
		}
		for (com.seekers.grpc.game.Goal goal : associated.getGoalsList()) {
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
	public Map<String, Supplier<? extends Reference<?>>> getCreators() {
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
