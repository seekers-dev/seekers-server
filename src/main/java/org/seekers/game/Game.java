/*
 * Copyright (C) 2022  Seekers Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.seekers.game;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import org.seekers.grpc.service.CommandResponse;
import org.seekers.plugin.GameMap;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.*;

/**
 * The Game class represents a game environment where players, seekers, goals,
 * and camps interact. It manages the game state, updates the positions of game
 * entities, and provides visual rendering.
 *
 * @author karlz
 * @author joendter
 */
public class Game extends Scene {

    // Game objects
    private final @Nonnull List<Entity> entities = new ArrayList<>();
    private GameMap gameMap;
    private long tick = 0;

    // Cached types for gRPC fast access
    private final @Nonnull List<Player> players = new ArrayList<>();
    private final @Nonnull List<Seeker> seekers = new ArrayList<>();
    private final @Nonnull List<Goal> goals = new ArrayList<>();
    private final @Nonnull List<Camp> camps = new ArrayList<>();

    // Graphics
    private final @Nonnull BooleanProperty finished = new SimpleBooleanProperty(false);
    private final @Nonnull Label time = new Label();
    private final @Nonnull VBox info = new VBox();
    private final @Nonnull Group front = new Group();
    private final @Nonnull Group back = new Group();

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
        this.gameProperties = gameProperties;
        this.campProperties = campProperties;
        this.seekerProperties = seekerProperties;
        this.goalProperties = goalProperties;

        time.setFont(Font.font("Ubuntu", 14));
        time.setTextFill(Color.WHITESMOKE);

        parent.setTop(getInfo());
        parent.getChildren().addAll(getBack(), getFront());
        parent.setBottom(time);
        parent.setBackground(new Background(new BackgroundFill(Color.gray(.1), null, null)));

        Timeline timeline = getTimeline();
        timeline.play();
    }

    /**
     * Properties for all global config attributes.
     */
    public static class Properties {
        private static final String SECTION = "global";

        /**
         * Creates a new properties instance from the ini object.
         *
         * @param ini the ini object that holds the data of {@code config.ini}
         */
        public Properties(Ini ini) {
            playtime = ini.fetch(SECTION, "playtime", int.class);
            tickDuration = ini.fetch(SECTION, "tick-duration", double.class);
            players = ini.fetch(SECTION, "players", int.class);
            seekers = ini.fetch(SECTION, "seekers", int.class);
            goals = ini.fetch(SECTION, "goals", int.class);
            width = ini.fetch("map", "width", double.class);
            height = ini.fetch("map", "height", int.class);
        }

        // Global properties
        final int playtime;
        final double tickDuration;
        final int players;
        final int seekers;
        final int goals;

        // Map properties
        final double width;
        final double height;
    }

    /**
     * Creates and returns the new timeline. The Timeline runs and updates the game. It will update every entity every
     * 10 milliseconds. Entities are only updated if all players have joined the game and there is still playtime
     * remaining. Resetting the game will not reset the timeline.
     *
     * @return the created timeline object
     */
    private Timeline getTimeline() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(getGameProperties().tickDuration), e -> {
            if (hasOpenSlots())
                return;
            if (tick > gameProperties.playtime) {
                finished.set(true);
                return;
            }
            for (Entity entity : List.copyOf(getEntities())) {
                entity.update();
            }
            time.setText("[ " + (++tick) + " ]");
        }));
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        return timeline;
    }

    /**
     * Resets the game. This will:
     * <ol>
     *     <li>Destroy all entities</li>
     *     <li>Clears the scene</li>
     *     <li>Resets all changed properties</li>
     * </ol>
     */
    public synchronized void reset() {
        // Destroy entities and clear cache
        entities.clear();
        players.clear();
        seekers.clear();
        camps.clear();

        // Clear scene content
        getBack().getChildren().clear();
        getFront().getChildren().clear();
        getInfo().getChildren().clear();

        // Add goals back
        for (Goal goal : goals) {
            getFront().getChildren().add(goal);
            getEntities().add(goal);
        }

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
        int sum = 0;
        for (Player player : players) {
            tournament.getResults().computeIfAbsent(player.getName(), n -> new ArrayList<>());
            sum += player.getScore();
        }
        for (Player player : players) {
            tournament.getResults().get(player.getName()).add(100 * (sum == 0 ? 1 / players.size() : player.getScore() / sum));
        }
    }

    /**
     * @return the current status of the game
     */
    public synchronized CommandResponse.Builder getCommandResponse() {
        return CommandResponse.newBuilder().addAllPlayers(Corresponding.transform(getPlayers()))
            .addAllCamps(Corresponding.transform(getCamps()))
            .addAllSeekers(Seeker.transform(getSeekers()))
            .addAllGoals(Goal.transform(getGoals()))
            .setPassedPlaytime(getPassedPlaytime());
    }

    /**
     * @return the list of entities
     */
    @Nonnull
    public List<Entity> getEntities() {
        return entities;
    }

    /**
     * @return the list of seekers
     */
    @Nonnull
	public List<Seeker> getSeekers() {
        return seekers;
    }

    /**
     * @return the list of players
     */
    @Nonnull
	public List<Player> getPlayers() {
        return players;
    }

    /**
     * @return the list of goals
     */
    @Nonnull
	public List<Goal> getGoals() {
        return goals;
    }

    /**
     * @return the list of camps
     */
    @Nonnull
	public List<Camp> getCamps() {
        return camps;
    }

    @CheckReturnValue
    public GameMap getGameMap() {
        return gameMap;
    }

    public void setGameMap(@Nonnull GameMap gameMap) {
        this.gameMap = gameMap;
    }

    @Nonnull
    public BooleanProperty finishedProperty() {
        return finished;
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
    public long getPassedPlaytime() {
        return tick;
    }

    @Nonnull
    public Properties getGameProperties() {
        return gameProperties;
    }

    @Nonnull
    public Seeker.Properties getSeekerProperties() {
        return seekerProperties;
    }

    @Nonnull
    public Goal.Properties getGoalProperties() {
        return goalProperties;
    }

    @Nonnull
    public Camp.Properties getCampProperties() {
        return campProperties;
    }
}
