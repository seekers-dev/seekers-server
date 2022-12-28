package org.seekers.grpc;

import java.util.Map;

import org.seekers.graphic.Camp;
import org.seekers.graphic.Game;
import org.seekers.graphic.Goal;
import org.seekers.graphic.Player;
import org.seekers.graphic.Seeker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class GetHelper implements Switching<StatusReply> {
	private final ObservableMap<String, Player> players = FXCollections.observableHashMap();
	private final ObservableMap<String, Seeker> seekers = FXCollections.observableHashMap();
	private final ObservableMap<String, Goal> goals = FXCollections.observableHashMap();
	private final ObservableMap<String, Camp> camps = FXCollections.observableHashMap();

	private final Game game;

	private final Map<String, Creator<?>> creators = Map.of("Player", () -> new Player(getGame()), "Seeker",
			() -> new Seeker(getGame()), "Goal", () -> new Goal(getGame()), "Camp", () -> new Camp(getGame()));

	public GetHelper(Game game) {
		this.game = game;
	}

	@SuppressWarnings("unchecked")
	public <T> T save(Map<String, T> map, String id, String type) {
		T val = map.get(id);
		if (val == null) {
			val = (T) creators.get(type).create();
			map.put(id, val);
		}
		return val;
	}

	@Override
	public void switched(StatusReply associated) {
		for (StatusReply.Player player : associated.getPlayersList()) {
			save(players, player.getId(), "Player").switched(player);
		}
		for (StatusReply.Camp camp : associated.getCampsList()) {
			save(camps, camp.getId(), "Camp").switched(camp);
		}
		for (StatusReply.Seeker seeker : associated.getSeekersList()) {
			save(seekers, seeker.getSuper().getId(), "Seeker").switched(seeker);
		}
		for (StatusReply.Goal goal : associated.getGoalsList()) {
			save(goals, goal.getSuper().getId(), "Goal").switched(goal);
		}
	}

	public Game getGame() {
		return game;
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
