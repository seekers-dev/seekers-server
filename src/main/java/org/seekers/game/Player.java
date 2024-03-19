package org.seekers.game;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.seekers.grpc.Corresponding;
import org.seekers.grpc.Identifiable;

/**
 * The Player class represents a player in the game.
 * 
 * @author karlz
 */
public class Player extends Label implements Corresponding<org.seekers.grpc.game.Player>, Identifiable, Destroyable {

	private static final @Nonnull Random rand = new Random();

	private final @Nonnull Game game;
	private final @Nonnull Map<String, Seeker> seekers = new LinkedHashMap<>();

	private @Nullable Camp camp;
	private @Nonnull Color color;
	private @Nonnull String name;
	private int score;

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
		setPadding(new Insets(2.0));
		setFont(Font.font("Ubuntu", 24.0));
		setTextFill(color);
		getGame().getInfo().getChildren().add(this);
		game.getPlayers().add(this);
	}

	@Override
	public void destroy() {
		getGame().getPlayers().remove(this);
		getGame().getInfo().getChildren().remove(this);
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
		setText(name + ": " + score);
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
		setText(name + ": " + score);
	}

	@Override
	public org.seekers.grpc.game.Player associated() {
		return org.seekers.grpc.game.Player.newBuilder().setId(getIdentifier()).addAllSeekerIds(seekers.keySet())
				.setCampId(camp != null ? camp.getIdentifier() : "").setName(name).setColor(color.toString())
				.setScore(score).build();
	}

}
