package org.seekers.game;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.scvis.observable.WrappedObject;
import io.scvis.proto.Identifiable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * The Player class represents a player in the game.
 * 
 * @author karlz
 */
public class Player implements Identifiable, WrappedObject {

	private static final Random rand = new Random();

	@Nonnull
	private final Map<String, Seeker> seekers = new LinkedHashMap<>();
	@Nonnull
	private final Game game;
	@Nonnull
	private final Label render = new Label();

	private Camp camp;
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
		render.setPadding(new Insets(2.0));
		render.setFont(Font.font("Ubuntu", 24.0));
		render.setTextFill(color);

		game.getPlayers().add(this);
	}

	/**
	 * Increases the score of the Player by 1.
	 */
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

	@SuppressWarnings("null")
	@Nonnull
	@Override
	public String getId() {
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
	 * Gets the Mirror object associated with the Player.
	 *
	 * @return The Mirror object associated with the Player.
	 */
	@Nonnull
	@Override
	public Label get() {
		return render;
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
		render.setText(name + ": " + score);
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
		this.color = color;
		for (Seeker seeker : seekers.values()) {
			seeker.setColor(color);
		}
		render.setTextFill(color);

		final Camp checked = this.camp;
		if (checked != null)
			checked.get().setStroke(color);
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
		render.setText(name + ": " + score);
	}

	@Override
	public org.seekers.grpc.game.Player associated() {
		return org.seekers.grpc.game.Player.newBuilder().setId(getId()).addAllSeekerIds(seekers.keySet())
				.setCampId(camp != null ? camp.getId() : "").setName(name).setColor(color.toString()).setScore(score)
				.build();
	}

}
