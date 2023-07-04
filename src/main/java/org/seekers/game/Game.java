package org.seekers.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.seekers.grpc.SeekerProperties;
import org.seekers.grpc.SeekerTournament;
import org.seekers.grpc.SeekerTournament.PlayerCard;
import org.seekers.grpc.net.StatusResponse;

import io.scvis.geometry.Vector2D;
import io.scvis.observable.WrappedObject;
import io.scvis.proto.Corresponding;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.util.Pair;

/**
 * The Game class represents a game environment where players, seekers, goals,
 * and camps interact. It manages the game state, updates the positions of game
 * entities, and provides visual rendering.
 * 
 * @author karlz
 */
public class Game extends Scene implements TorusMap {

	private final static Logger logger = Logger.getLogger(Game.class.getSimpleName());

	private final @Nonnull List<Physical> physicals = new ArrayList<>();

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

	private final BooleanProperty finished = new SimpleBooleanProperty(false);

	private final @Nonnull Label time = new Label();

	@Nonnull
	private final Timeline timeline = new Timeline(new KeyFrame(Duration.millis(10.0), e -> {
		if (hasOpenSlots())
			return;
		if (passed > playtime) {
			finished.set(true);
			return;
		}
		for (int i = 0; i < physicals.size(); i++) {
			Physical physical = physicals.get(i);
			physical.update(speed);
			if (autoPlay && (physical instanceof Seeker)) {
				((Seeker) physical).setAutoCommands();
			}
		}
		passed += speed;
		time.setText("[ " + passed + " ]");
	}));

	private final @Nonnull BorderPane render;

	/**
	 * Constructs a new Game object. Initializes the game environment, creates the
	 * game rendering components, and starts the game timeline.
	 */
	public Game(@Nonnull BorderPane parent, double width, double height) {
		super(parent, width, height);
		this.render = parent;
		this.width = width;
		this.height = height;

		VBox info = new VBox();
		render.setTop(info);

		Group front = new Group();
		Group back = new Group();
		render.getChildren().addAll(back, front);

		camps.addListener(getListener(back.getChildren()));
		seekers.addListener(getListener(front.getChildren()));
		goals.addListener(getListener(front.getChildren()));
		players.addListener(getListener(info.getChildren()));

		time.setFont(Font.font("Ubuntu", 14));
		time.setTextFill(Color.WHITESMOKE);
		render.setBottom(time);

		render.setBackground(new Background(new BackgroundFill(Color.gray(.1), null, null)));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();

		addGoals();
	}

	private static <T extends WrappedObject> MapChangeListener<String, T> getListener(Collection<Node> coll) {
		return e -> Platform.runLater(() -> coll.add((Node) e.getValueAdded().get()));
	}

	/**
	 * Checks if there are open slots for players to join the game.
	 *
	 * @return true if there are open slots, false otherwise
	 */
	public boolean hasOpenSlots() {
		return players.size() < playerCount;
	}

	public void addToTournament(SeekerTournament tournament) {
		List<Pair<String, Integer>> scores = new ArrayList<>();
		for (Player player : players.values()) {
			scores.add(new Pair<>(player.getName(), player.getScore()));
		}
		scores.sort((a, b) -> b.getValue() - a.getValue());
		if (scores.size() == 1) {
			PlayerCard card = tournament.getPlayerCard(scores.get(0).getKey());
			card.setWins(card.getWins() + 1);
			logger.info(card + " got a pass");
		} else if (scores.size() >= 2) {
			PlayerCard card0 = tournament.getPlayerCard(scores.get(0).getKey());
			PlayerCard card1 = tournament.getPlayerCard(scores.get(1).getKey());
			if (scores.get(0).getValue().equals(scores.get(1).getValue())) {
				card0.setDraws(card0.getDraws() + 1);
				card1.setDraws(card1.getDraws() + 1);
				logger.info(card0 + " {" + scores.get(0).getValue() + "} and " + card1 + " {" + scores.get(1).getValue()
						+ "} ended with a draw");
			} else {
				card0.setWins(card0.getWins() + 1);
				card1.setLosses(card1.getLosses() + 1);
				logger.info(card0 + " {" + scores.get(0).getValue() + "} won against " + card1 + " {"
						+ scores.get(1).getValue() + "}");

			}
		}
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
		new Camp(player, new Vector2D(width / max * (cur + 0.5), height * 0.5));
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

	public StatusResponse getStatusResponse() {
		@SuppressWarnings("unchecked")
		StatusResponse reply = StatusResponse.newBuilder()
				.addAllPlayers((Collection<org.seekers.grpc.game.Player>) (Collection<?>) Corresponding
						.transform(getPlayers().values()))
				.addAllCamps((Collection<org.seekers.grpc.game.Camp>) (Collection<?>) Corresponding
						.transform(getCamps().values()))
				.addAllSeekers((Collection<org.seekers.grpc.game.Seeker>) (Collection<?>) Corresponding
						.transform(getSeekers().values()))
				.addAllGoals((Collection<org.seekers.grpc.game.Goal>) (Collection<?>) Corresponding
						.transform(getGoals().values()))
				.setPassedPlaytime(getPassedPlaytime()).build();
		return reply;
	}

	public BooleanProperty finishedProperty() {
		return finished;
	}

	/**
	 * Returns the rendering component of the game.
	 *
	 * @return the game rendering component
	 */
	@Nonnull
	public BorderPane getRender() {
		return render;
	}

	/**
	 * Returns the list of physical entities in the game.
	 *
	 * @return the list of physical entities
	 */
	@Nonnull
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
