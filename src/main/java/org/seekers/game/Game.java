package org.seekers.game;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.*;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.util.Pair;
import org.seekers.grpc.Corresponding;
import org.seekers.grpc.SeekersConfig;
import org.seekers.grpc.SeekersTournament;
import org.seekers.grpc.net.StatusResponse;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * The Game class represents a game environment where players, seekers, goals,
 * and camps interact. It manages the game state, updates the positions of game
 * entities, and provides visual rendering.
 *
 * @author karlz
 */
public class Game extends Scene implements TorusMap {

    private static final double GLOBAL_SPEED = SeekersConfig.getConfig().getGlobalSpeed();
    private static final double GLOBAL_PLAYTIME = SeekersConfig.getConfig().getGlobalPlaytime();
    private static final int GLOBAL_PLAYERS = SeekersConfig.getConfig().getGlobalPlayers();
    private static final int GLOBAL_SEEKERS = SeekersConfig.getConfig().getGlobalSeekers();
    private static final int GLOBAL_GOALS = SeekersConfig.getConfig().getGlobalGoals();
    private static final boolean GLOBAL_AUTO_PLAY = SeekersConfig.getConfig().getGlobalAutoPlay();

    private final @Nonnull List<Entity> entities = new ArrayList<>();
    private final @Nonnull ObservableList<Player> players = FXCollections.observableArrayList();
    private final @Nonnull ObservableList<Seeker> seekers = FXCollections.observableArrayList();
    private final @Nonnull ObservableList<Goal> goals = FXCollections.observableArrayList();
    private final @Nonnull ObservableList<Camp> camps = FXCollections.observableArrayList();
    private final @Nonnull ObservableSet<Animation> animations = FXCollections.observableSet();
    private final @Nonnull BooleanProperty finished = new SimpleBooleanProperty(false);
    private final @Nonnull Label time = new Label();
    private final @Nonnull BorderPane render;

    private double passed = 0.0;
	private final double width;
	private final double height;

    /**
     * Constructs a new Game object. Initializes the game environment, creates the
     * game rendering components, and starts the game timeline.
     */
    public Game(@Nonnull BorderPane parent, double width, double height) {
        super(parent, width, height, true, SceneAntialiasing.BALANCED);
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
        animations.addListener((SetChangeListener.Change<? extends Node> change) -> Platform.runLater(() -> {
            if (change.wasAdded()) {
                back.getChildren().add(change.getElementAdded());
            } else if (change.wasRemoved()) {
                back.getChildren().remove(change.getElementRemoved());
            }
        }));

        time.setFont(Font.font("Ubuntu", 14));
        time.setTextFill(Color.WHITESMOKE);
        render.setBottom(time);

        render.setBackground(new Background(new BackgroundFill(Color.gray(.1), null, null)));
        Timeline timeline = getTimeline();
        timeline.play();

        addGoals();
    }

    private static <T extends Node> ListChangeListener<T> getListener(Collection<Node> coll) {
        return e -> Platform.runLater(() -> {
            e.next();
            if (e.wasAdded()) {
                for (int index = 0; index < e.getAddedSize(); index++) {
                    coll.add(e.getAddedSubList().get(index));
                }
            }
        });
    }

    private Timeline getTimeline() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(10.0), e -> {
            if (hasOpenSlots())
                return;
            if (passed > GLOBAL_PLAYTIME) {
                finished.set(true);
                return;
            }
            int last = entities.size();
            for (int i = 0; i < last; i++) {
                int now = entities.size();
                if (now < last) {
                    i--;
                }
                last = now;
                Entity entity = entities.get(i);
                entity.update();
                if (GLOBAL_AUTO_PLAY && (entity instanceof Seeker)) {
                    ((Seeker) entity).setAutoCommands();
                }
            }
            passed += GLOBAL_SPEED;
            time.setText("[ " + passed + " ]");
        }));
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        return timeline;
    }

    /**
     * Checks if there are open slots for players to join the game.
     *
     * @return true if there are open slots, false otherwise
     */
    public boolean hasOpenSlots() {
        return players.size() < GLOBAL_PLAYERS;
    }

    public void addToTournament(SeekersTournament tournament) {
        List<Pair<String, Integer>> scores = new ArrayList<>();
        for (Player player : players) {
            scores.add(new Pair<>(player.getName(), player.getScore()));
            tournament.getCurrentMatch().getMembers().put(player.getName() + ".py", player.getScore());
        }
        tournament.getCurrentMatch().markAsOver();
        scores.sort((a, b) -> b.getValue() - a.getValue());
        if (scores.size() == 1) {
            SeekersTournament.Participant card = tournament.getPlayerCard(scores.get(0).getKey());
            card.setWins(card.getWins() + 1);
        } else if (scores.size() >= 2) {
            SeekersTournament.Participant card0 = tournament.getPlayerCard(scores.get(0).getKey());
            SeekersTournament.Participant card1 = tournament.getPlayerCard(scores.get(1).getKey());
            if (scores.get(0).getValue().equals(scores.get(1).getValue())) {
                card0.setDraws(card0.getDraws() + 1);
                card1.setDraws(card1.getDraws() + 1);
            } else {
                card0.setWins(card0.getWins() + 1);
                card1.setLosses(card1.getLosses() + 1);
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
        Player player = new Player(this);
        new Camp(player, new Point2D(width / GLOBAL_PLAYERS * (cur + 0.5), height * 0.5));
        for (int s = 0; s < GLOBAL_SEEKERS; s++) {
            new Seeker(player, getRandomPosition());
        }

        return player;
    }

    /**
     * Adds a specified number of goals to the game environment at random positions.
     */
    public void addGoals() {
        for (int i = 0; i < GLOBAL_GOALS; i++) {
            new Goal(this, getRandomPosition());
        }
    }

    public synchronized StatusResponse getStatusResponse() {
        return StatusResponse.newBuilder().addAllPlayers(Corresponding.transform(getPlayers()))
                .addAllCamps(Corresponding.transform(getCamps()))
                .addAllSeekers(Seeker.transform(getSeekers()))
                .addAllGoals(Goal.transform(getGoals()))
                .setPassedPlaytime(getPassedPlaytime()).build();
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
    public List<Entity> getEntities() {
        return entities;
    }

    /**
     * Returns the list of seekers in the game.
     *
     * @return the list of seekers
     */
    @Nonnull
	public ObservableList<Seeker> getSeekers() {
        return seekers;
    }

    /**
     * Returns the list of players in the game.
     *
     * @return the list of players
     */
    @Nonnull
	public ObservableList<Player> getPlayers() {
        return players;
    }

    /**
     * Returns the list of goals in the game.
     *
     * @return the list of goals
     */
    @Nonnull
	public ObservableList<Goal> getGoals() {
        return goals;
    }

    /**
     * Returns the list of camps in the game.
     *
     * @return the list of camps
     */
    @Nonnull
	public ObservableList<Camp> getCamps() {
        return camps;
    }

    @Nonnull
	public ObservableSet<Animation> getAnimations() {
        return animations;
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
