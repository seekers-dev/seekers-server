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

/**
 * The Game class represents a game environment where players, seekers, goals,
 * and camps interact. It manages the game state, updates the positions of game
 * entities, and provides visual rendering.
 * 
 * @author karlz
 */
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

	/**
	 * Constructs a new Game object. Initializes the game environment, creates the
	 * game rendering components, and starts the game timeline.
	 */
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

	/**
	 * Checks if there are open slots for players to join the game.
	 *
	 * @return true if there are open slots, false otherwise
	 */
	public boolean hasOpenSlots() {
		return players.size() < playerCount;
	}

	/**
	 * Adds a new player to the game environment. Creates a camp and a specified
	 * number of seekers for the player.
	 *
	 * @return the newly added player
	 */
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

	/**
	 * Adds a specified number of goals to the game environment at random positions.
	 */
	public void addGoals() {
		for (int i = 0; i < goalCount; i++) {
			new Goal(this, getRandomPosition());
		}
	}

	/**
	 * Returns the rendering component of the game.
	 *
	 * @return the game rendering component
	 */
	public BorderPane getRender() {
		return render;
	}

	/**
	 * Returns the map of helpers associated with the game.
	 *
	 * @return the map of helpers
	 */
	public Map<String, SeekersDispatchHelper> getHelpers() {
		return helpers;
	}

	/**
	 * Returns the list of physical entities in the game.
	 *
	 * @return the list of physical entities
	 */
	public List<Physical> getPhysicals() {
		return physicals;
	}

	/**
	 * Returns the map of seekers in the game.
	 *
	 * @return the map of seekers
	 */
	public ObservableMap<String, Seeker> getSeekers() {
		return seekers;
	}

	/**
	 * Returns the map of players in the game.
	 *
	 * @return the map of players
	 */
	public ObservableMap<String, Player> getPlayers() {
		return players;
	}

	/**
	 * Returns the map of goals in the game.
	 *
	 * @return the map of goals
	 */
	public ObservableMap<String, Goal> getGoals() {
		return goals;
	}

	/**
	 * Returns the map of camps in the game.
	 *
	 * @return the map of camps
	 */
	public ObservableMap<String, Camp> getCamps() {
		return camps;
	}

	/**
	 * Returns the maximum playtime allowed for the game.
	 *
	 * @return the maximum playtime
	 */
	public double getMaxPlaytime() {
		return playtime;
	}

	/**
	 * Returns the passed playtime in the game.
	 *
	 * @return the passed playtime
	 */
	public double getPassedPlaytime() {
		return passed;
	}
}
