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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.grpc.stub.StreamObserver;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.seekers.Launcher;
import org.seekers.grpc.Corresponding;
import org.seekers.grpc.game.PlayerOuterClass;
import org.seekers.grpc.service.CommandResponse;

/**
 * The Player class represents a player in the game.
 * 
 * @author karlz
game.getFront().getChildren().add(this);
 */
public class Player extends Label implements Entity, Corresponding<PlayerOuterClass.Player> {

	private static final @Nonnull Random rand = new Random();

	private final @Nonnull Game game;
	private final @Nonnull Map<String, Seeker> seekers = new LinkedHashMap<>();

	private @Nullable Camp camp;
	private @Nonnull Color color;
	private @Nonnull String name;
	private int score;

	private @CheckForNull StreamObserver<CommandResponse> observer;

	/**
	 * Constructs a new instance of the Player class.
	 *
	 * @param game The Game object associated with the Player object.
	 */
	public Player(@Nonnull Game game) {
		this.game = game;
		this.name = "Player " + hashCode();
		this.color = new Color((rand.nextDouble() + 1) / 3, (rand.nextDouble() + 1) / 3, (rand.nextDouble() + 1) / 3,
				1.0);
		setFont(Font.loadFont(Launcher.class.getResourceAsStream("PixelFont.otf"), 24.0));
		setTextFill(color);
		getGame().getEntities().add(this);
		getGame().getInfo().getChildren().add(this);
		getGame().getPlayers().add(this);
	}

	@Override
	public void update() {
		if (observer != null) {
			observer.onNext(getGame().getCommandResponse().build());
			observer.onCompleted();
			observer = null;
		}
	}

	private void updateText() {
		setText(String.format("%4d %s", getScore(), getName()));
	}

	/**
	 * Gets the map of Seekers associated with the Player.
	 *
	 * @return The map of Seekers associated with the Player.
	 */
	@Nonnull
	public Map<String, Seeker> getSeekers() {
		return seekers;
	}

	@Nullable
	private String id;

	@Nonnull
	@Override
	public String getIdentifier() {
		if (id == null)
			id = Integer.toHexString(hashCode());
		return id;
	}

	/**
	 * Gets the Game object associated with the Player.
	 *
	 * @return The Game object associated with the Player.
	 */
	@Nonnull
	public Game getGame() {
		return game;
	}

	/**
	 * Gets the Camp associated with the Player.
	 *
	 * @return The Camp associated with the Player.
	 */
	@Nullable
	public Camp getCamp() {
		return camp;
	}

	/**
	 * Sets the Camp associated with the Player.
	 *
	 * @param camp The Camp to set.
	 */
	public void setCamp(@Nonnull Camp camp) {
		this.camp = camp;
	}

	/**
	 * Gets the name of the Player.
	 *
	 * @return The name of the Player.
	 */
	@Nonnull
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the Player.
	 *
	 * @param name The name to set.
	 */
	public void setName(@Nonnull String name) {
		this.name = name;
		updateText();
	}

	/**
	 * Gets the color of the Player.
	 *
	 * @return The color of the Player.
	 */
	@Nonnull
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the color of the Player.
	 *
	 * @param color The color to set.
	 */
	public void setColor(@Nonnull Color color) {
		this.color = new Color(Math.max(0.25, color.getRed()), Math.max(0.25, color.getBlue()), Math.max(0.25,
							color.getGreen()), 1.0);
		for (Seeker seeker : seekers.values()) {
			seeker.setColor(this.color);
		}
		setTextFill(this.color);

		if (camp != null) {
			camp.setStroke(this.color);
		}
	}

	/**
	 * Gets the score of the Player.
	 *
	 * @return The score of the Player.
	 */
	public int getScore() {
		return score;
	}

	/**
	 * Increases the score of the Player by 1.
	 */
	public void putUp() {
		score++;
		updateText();
	}

	public void setObserver(@Nonnull StreamObserver<CommandResponse> observer) {
		this.observer = observer;
	}

	@Override
	public PlayerOuterClass.Player associated() {
		return PlayerOuterClass.Player.newBuilder().setId(getIdentifier()).addAllSeekerIds(seekers.keySet())
				.setCampId(camp != null ? camp.getIdentifier() : "").setScore(score).build();
	}

}
