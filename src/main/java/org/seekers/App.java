package org.seekers;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.seekers.graphic.Camp;
import org.seekers.graphic.Goal;
import org.seekers.graphic.Player;
import org.seekers.graphic.Seeker;
import org.seekers.grpc.CampStatus;
import org.seekers.grpc.Creator;
import org.seekers.grpc.EntityReply;
import org.seekers.grpc.GoalStatus;
import org.seekers.grpc.PlayerReply;
import org.seekers.grpc.PlayerStatus;
import org.seekers.grpc.PropertiesReply;
import org.seekers.grpc.SeekersClient;
import org.seekers.grpc.SeekersServer;
import org.seekers.grpc.SeekerStatus;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class App extends Application {
	private final SeekersServer server = new SeekersServer();
	private final SeekersClient client = new SeekersClient();

	private final Map<String, Creator<?>> creators = Map.of("org.seekers.game.Player", () -> new Player(App.this),
			"org.seekers.game.Seeker", () -> new Seeker(App.this), "org.seekers.game.Goal", () -> new Goal(App.this),
			"org.seekers.game.Camp", () -> new Camp(App.this));

	private final ObservableMap<String, Player> players = FXCollections.observableHashMap();
	private final ObservableMap<String, Seeker> seekers = FXCollections.observableHashMap();
	private final ObservableMap<String, Goal> goals = FXCollections.observableHashMap();
	private final ObservableMap<String, Camp> camps = FXCollections.observableHashMap();

	private final Properties properties = new Properties();

	public String getPropertieAsString(String key) {
		return (String) properties.get(key);
	}

	public Boolean getPropertieAsBoolean(String key) {
		Object val = properties.get(key);

		if (val == null) {
			return null;
		}
		Boolean cast;
		if (val instanceof Boolean) {
			cast = (Boolean) val;
			properties.put(key, cast);
		} else {
			cast = Boolean.parseBoolean((String) val);
		}
		return cast;
	}

	public Double getPropertieAsDouble(String key) {
		Object val = properties.get(key);

		if (val == null) {
			return null;
		}
		Double cast;
		if (val instanceof Double) {
			cast = (Double) val;
			properties.put(key, cast);
		} else {
			cast = Double.valueOf((String) val);
		}
		return cast;
	}

	public Integer getPropertieAsInteger(String key) {
		Object val = properties.get(key);

		if (val == null) {
			return null;
		}
		Integer cast;
		if (val instanceof Integer) {
			cast = (Integer) val;
			properties.put(key, cast);
		} else {
			cast = Integer.valueOf((String) val);
		}
		return cast;
	}

	private MapChangeListener<String, Node> getListener(Collection<Node> to) {
		return new MapChangeListener<>() {
			@Override
			public void onChanged(Change<? extends String, ? extends Node> change) {
				if (change.wasAdded())
					to.add(change.getValueAdded());
				if (change.wasRemoved())
					to.remove(change.getValueRemoved());
			}
		};
	}

	public App() {
		players.addListener(getListener(box.getChildren()));
		seekers.addListener(getListener(group.getChildren()));
		goals.addListener(getListener(group.getChildren()));
		camps.addListener(getListener(group.getChildren()));
	}

	@Override
	public void init() throws Exception {
		PropertiesReply propertiesReply = client.getProperties();
		properties.putAll(propertiesReply.getEntriesMap());

		AnimationTimer timer = new AnimationTimer() {
			final long[] frameTimes = new long[100];
			int frameTimeIndex = 0;
			boolean arrayFilled = false;

			@Override
			public void handle(long now) {
				frameTimes[frameTimeIndex] = now;
				frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length;
				if (frameTimeIndex == 0) {
					arrayFilled = true;
				}
				if (arrayFilled) {
					PlayerReply playersReply = client.getPlayerStatus();
					for (PlayerStatus player : playersReply.getPlayersMap().values()) {
						save(players, player.getId()).switched(player);
					}
					for (CampStatus camp : playersReply.getCampsMap().values()) {
						save(camps, camp.getId()).switched(camp);
					}
					EntityReply entityReply = client.getEntityStatus();
					for (SeekerStatus seeker : entityReply.getSeekersMap().values()) {
						save(seekers, seeker.getSuper().getId()).switched(seeker);
					}
					for (GoalStatus goal : entityReply.getGoalsMap().values()) {
						save(goals, goal.getSuper().getId()).switched(goal);
					}
				}
			}

			@SuppressWarnings("unchecked")
			public <T> T save(Map<String, T> map, String key) {
				T val;
				if (!map.containsKey(key)) {
					val = (T) creators.get(key.substring(0, key.indexOf("@"))).create();
					map.put(key, val);
				} else {
					val = map.get(key);
				}
				return val;
			}
		};
		timer.start();
	}

	private Group group = new Group();
	private VBox box = new VBox(10);

	@Override
	public void start(Stage stage) throws Exception {
		group.getChildren().add(box);

		Scene scene = new Scene(group, 768, 768, true, SceneAntialiasing.BALANCED);
		scene.setFill(Color.gray(.1));
		stage.setOnCloseRequest(c -> {
			try {
				client.stop();
				server.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		stage.setResizable(false);
		stage.setScene(scene);
		stage.show();
	}

	public ObservableMap<String, Goal> getGoals() {
		return goals;
	}

	public ObservableMap<String, Seeker> getSeekers() {
		return seekers;
	}

	public ObservableMap<String, Player> getPlayers() {
		return players;
	}

	public ObservableMap<String, Camp> getCamps() {
		return camps;
	}
}
