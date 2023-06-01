package org.seekers.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.seekers.grpc.SeekerProperties;
import org.seekers.grpc.SeekersDispatchHelper;

import io.scvis.geometry.Vector2D;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableMap;
import javafx.scene.Group;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class Game extends TorusMap {

	private final Map<String, SeekersDispatchHelper> helpers = new HashMap<>();

	private final List<Physical> physicals = new ArrayList<>();

	private final ObservableMap<String, Player> players = FXCollections.observableHashMap();
	private final ObservableMap<String, Seeker> seekers = FXCollections.observableHashMap();
	private final ObservableMap<String, Goal> goals = FXCollections.observableHashMap();
	private final ObservableMap<String, Camp> camps = FXCollections.observableHashMap();

	private double width = SeekerProperties.getDefault().getMapWidth();
	private double height = SeekerProperties.getDefault().getMapHeight();
	private double speed = SeekerProperties.getDefault().getGlobalSpeed();
	private double passed = 0.0;
	private double playtime = SeekerProperties.getDefault().getGlobalPlaytime();
	private int playerCount = SeekerProperties.getDefault().getGlobalPlayers();
	private int seekerCount = SeekerProperties.getDefault().getGlobalSeekers();
	private int goalCount = SeekerProperties.getDefault().getGlobalGoals();
	private boolean autoPlay = SeekerProperties.getDefault().getGlobalAutoPlay();

	private final Timeline timeline = new Timeline(new KeyFrame(Duration.millis(10.0), e -> {
		if (hasOpenSlots())
			return;
		for (Physical physical : physicals) {
			physical.update(speed);
			if (autoPlay && (physical instanceof Seeker)) {
				((Seeker) physical).setAutoCommands();
			}
		}
	}));

	private final BorderPane render = new BorderPane();

	public Game() {
		VBox info = new VBox();

		Group front = new Group();
		Group back = new Group();

		camps.addListener((Change<? extends String, ? extends Camp> e) -> back.getChildren()
				.add(e.getValueAdded().getMirror().getReflection()));
		seekers.addListener((Change<? extends String, ? extends Seeker> e) -> front.getChildren()
				.add(e.getValueAdded().getMirror().getReflection()));
		goals.addListener((Change<? extends String, ? extends Goal> e) -> front.getChildren()
				.add(e.getValueAdded().getMirror().getReflection()));
		players.addListener((Change<? extends String, ? extends Player> e) -> info.getChildren()
				.add(e.getValueAdded().getMirror().getReflection()));

		render.setBackground(new Background(new BackgroundFill(Color.gray(.1), null, null)));
		render.getChildren().addAll(back, front);
		render.setTop(info);

		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.play();

		addGoals();
	}

	public boolean hasOpenSlots() {
		return players.size() < playerCount;
	}

	public Player addPlayer() {
		int cur = players.size();
		int max = playerCount;

		Player player = new Player(this);
		player.setCamp(new Camp(player, new Vector2D(width / max * (cur + 0.5), height * 0.5)));
		for (int s = 0; s < seekerCount; s++) {
			new Seeker(player, getRandomPosition());
		}

		return player;
	}

	public void addGoals() {
		for (int i = 0; i < goalCount; i++) {
			new Goal(this, getRandomPosition());
		}
	}

	public BorderPane getRender() {
		return render;
	}

	public Map<String, SeekersDispatchHelper> getHelpers() {
		return helpers;
	}

	public List<Physical> getPhysicals() {
		return physicals;
	}

	public ObservableMap<String, Seeker> getSeekers() {
		return seekers;
	}

	public ObservableMap<String, Player> getPlayers() {
		return players;
	}

	public ObservableMap<String, Goal> getGoals() {
		return goals;
	}

	public ObservableMap<String, Camp> getCamps() {
		return camps;
	}

	public double getMaxPlaytime() {
		return playtime;
	}

	public double getPassedPlaytime() {
		return passed;
	}
}
