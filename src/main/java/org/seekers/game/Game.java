package org.seekers.game;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point2D;
import javafx.scene.Group;
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
import org.ini4j.Ini;
import org.seekers.grpc.Corresponding;
import org.seekers.plugin.Tournament;
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

    // Game objects
    private final @Nonnull List<Entity> entities = new ArrayList<>();
    private final @Nonnull Set<Player> players = new HashSet<>();
    private final @Nonnull Set<Seeker> seekers = new HashSet<>();
    private final @Nonnull Set<Goal> goals = new HashSet<>();
    private final @Nonnull Set<Camp> camps = new HashSet<>();
    private final @Nonnull Set<Animation> animations = new HashSet<>();
    private int tick = 0;

    // Graphics
    private final @Nonnull BooleanProperty finished = new SimpleBooleanProperty(false);
    private final @Nonnull Label time = new Label();
    private final @Nonnull VBox info = new VBox();
    private final @Nonnull Group front = new Group();
    private final @Nonnull Group back = new Group();
    private final @Nonnull BorderPane render;

    // Properties
    private final @Nonnull Properties gameProperties;
    private final @Nonnull Camp.Properties campProperties;
    private final @Nonnull Seeker.Properties seekerProperties;
    private final @Nonnull Goal.Properties goalProperties;

    /**
     * Constructs a new Game object. Initializes the game environment, creates the
     * game rendering components, and starts the game timeline.
     */
    public Game(@Nonnull BorderPane parent, @Nonnull Game.Properties gameProperties, @Nonnull Camp.Properties campProperties,
                @Nonnull Seeker.Properties seekerProperties, @Nonnull Goal.Properties goalProperties) {
        super(parent, gameProperties.width, gameProperties.height, true, SceneAntialiasing.BALANCED);
        this.render = parent;
        this.gameProperties = gameProperties;
        this.campProperties = campProperties;
        this.seekerProperties = seekerProperties;
        this.goalProperties = goalProperties;

        time.setFont(Font.font("Ubuntu", 14));
        time.setTextFill(Color.WHITESMOKE);

        render.setTop(getInfo());
        render.getChildren().addAll(getBack(), getFront());
        render.setBottom(time);
        render.setBackground(new Background(new BackgroundFill(Color.gray(.1), null, null)));

        Timeline timeline = getTimeline();
        timeline.play();
        addGoals();
    }

    public static class Properties {
        private static final String SECTION = "global";

        public Properties(Ini ini) {
            autoPlay = ini.fetch(SECTION, "auto-play", boolean.class);
            playtime = ini.fetch(SECTION, "playtime", int.class);
            players = ini.fetch(SECTION, "players", int.class);
            seekers = ini.fetch(SECTION, "seekers", int.class);
            goals = ini.fetch(SECTION, "goals", int.class);
            width = ini.fetch("map", "width", double.class);
            height = ini.fetch("map", "height", int.class);
        }

        // Global properties
        private final boolean autoPlay;
        private final int playtime;
        private final int players;
        private final int seekers;
        private final int goals;

        // Map properties
        private final double width;
        private final double height;
    }

    private Timeline getTimeline() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(10.0), e -> {
            if (hasOpenSlots())
                return;
            if (tick > gameProperties.playtime) {
                finished.set(true);
                return;
            }
            for (Entity entity : List.copyOf(getEntities())) {
                entity.update();
                if (gameProperties.autoPlay && (entity instanceof Seeker)) {
                    ((Seeker) entity).setAutoCommands();
                }
            }
            time.setText("[ " + (++tick) + " ]");
        }));
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        return timeline;
    }

    public void reset() {
        // Destroy entities
        for (Player player : List.copyOf(players)) player.destroy();
        for (Seeker seeker : List.copyOf(seekers)) seeker.destroy();
        for (Camp camp : List.copyOf(camps)) camp.destroy();
        for (Animation animation : List.copyOf(animations)) animation.destroy();

        // Reset property
        finished.set(false);
        tick = 0;
    }

    /**
     * Checks if there are open slots for players to join the game.
     *
     * @return true if there are open slots, false otherwise
     */
    public boolean hasOpenSlots() {
        return players.size() < gameProperties.players;
    }

    public void addToTournament(Tournament tournament) {
        for (Player player : players) {
            tournament.getResults().get(player.getName()).add(player.getScore());
        }
    }

    /**
     * Adds a new player to the game environment. Creates a camp and a specified
     * number of seekers for the player.
     *
     * @return the newly added player
     */
    public Player addPlayer() {
        Player player = new Player(this);
        Camp camp = new Camp(player, campProperties);
        camp.setPosition(new Point2D(gameProperties.width * (players.size() - 0.5) / gameProperties.players,
                gameProperties.height * 0.5));
        for (int s = 0; s < gameProperties.seekers; s++) {
            Seeker seeker = new Seeker(player, seekerProperties);
            seeker.setPosition(getRandomPosition());
        }
        return player;
    }

    /**
     * Adds a specified number of goals to the game environment at random positions.
     */
    public void addGoals() {
        for (int i = 0; i < gameProperties.goals; i++) {
            Goal goal = new Goal(this, goalProperties);
            goal.setPosition(getRandomPosition());
        }
    }

    public synchronized StatusResponse getStatusResponse() {
        return StatusResponse.newBuilder().addAllPlayers(Corresponding.transform(getPlayers()))
                .addAllCamps(Corresponding.transform(getCamps()))
                .addAllSeekers(Seeker.transform(getSeekers()))
                .addAllGoals(Goal.transform(getGoals()))
                .setPassedPlaytime(getPassedPlaytime()).build();
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
     * @return the set of seekers
     */
    @Nonnull
	public Set<Seeker> getSeekers() {
        return seekers;
    }

    /**
     * @return the set of players
     */
    @Nonnull
	public Set<Player> getPlayers() {
        return players;
    }

    /**
     * @return the set of goals
     */
    @Nonnull
	public Set<Goal> getGoals() {
        return goals;
    }

    /**
     * @return the set of camps
     */
    @Nonnull
	public Set<Camp> getCamps() {
        return camps;
    }

    @Nonnull
    public Set<Animation> getAnimations() {
        return animations;
    }

    @Nonnull
    public BooleanProperty finishedProperty() {
        return finished;
    }

    /**
     * @return the game rendering component
     */
    @Nonnull
    public BorderPane getRender() {
        return render;
    }

    @Nonnull
    public VBox getInfo() {
        return info;
    }

    @Nonnull
    public Group getBack() {
        return back;
    }

    @Nonnull
    public Group getFront() {
        return front;
    }

    /**
     * @return the passed playtime
     */
    public double getPassedPlaytime() {
        return tick;
    }
}
