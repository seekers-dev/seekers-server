package org.seekers.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;

import io.scvis.observable.InvalidationListener;
import io.scvis.observable.InvalidationListener.InvalidationEvent;
import io.scvis.observable.Observable;
import io.scvis.observable.WrappedObject;
import io.scvis.proto.Identifiable;
import io.scvis.proto.Mirror;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * The Player class represents a player in the game.
 * 
 * @author karlz
 */
public class Player implements Identifiable, WrappedObject, Observable<Player> {
	private final Map<String, Seeker> seekers = new HashMap<>();
	@Nonnull
	private final Game game;

	private Camp camp;
	private Color color;
	private String name;
	private int score;

	private static final Random rand = new Random();

	private final Mirror<Player, Label> mirror = new Mirror<>(this, new Label()) {
		@Override
		public void update(Player reference) {
			getReflection().setText(name + ": " + score);
		}
	};

	/**
	 * Constructs a new instance of the Player class.
	 *
	 * @param game The Game object associated with the Player object.
	 */
	public Player(@Nonnull Game game) {
		this.game = game;
		this.color = Color.rgb(rand.nextInt(124) + 124, rand.nextInt(124) + 124, rand.nextInt(124) + 124);
		this.name = "Player " + hashCode();
		mirror.getReflection().setTextFill(color);
		mirror.getReflection().setPadding(new Insets(2.0));
		mirror.getReflection().setFont(Font.font("Ubuntu", 24.0));
		addInvalidationListener(e -> mirror.update(this));

		game.getPlayers().put(getId(), this);
		invalidated();
	}

	private List<InvalidationListener<Player>> listeners = new ArrayList<>();

	public void fireInvalidationEvent(InvalidationEvent<Player> event) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).invalidated(event);
		}
	}

	protected void invalidated() {
		fireInvalidationEvent(new InvalidationEvent<>(this));
	}

	@Override
	public void addInvalidationListener(InvalidationListener<Player> listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeInvalidationListener(InvalidationListener<Player> listener) {
		this.listeners.remove(listener);
	}

	/**
	 * Increases the score of the Player by 1.
	 */
	/**
	 * Gets the map of Seekers associated with the Player.
	 *
	 * @return The map of Seekers associated with the Player.
	 */
	public Map<String, Seeker> getSeekers() {
		return seekers;
	}

	private String id;

	@Override
	public String getId() {
		if (id == null)
			id = Integer.toHexString(hashCode());
		return id;
	}

	/**
	 * Gets the Mirror object associated with the Player.
	 *
	 * @return The Mirror object associated with the Player.
	 */
	public Mirror<Player, Label> getMirror() {
		return mirror;
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
	@Nonnull
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
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the Player.
	 *
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
		invalidated();
	}

	/**
	 * Gets the color of the Player.
	 *
	 * @return The color of the Player.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the color of the Player.
	 *
	 * @param color The color to set.
	 */
	public void setColor(Color color) {
		this.color = color;
		for (Seeker seeker : seekers.values()) {
			seeker.setColor(color);
		}
		mirror.getReflection().setTextFill(color);
		camp.getMirror().getReflection().setStroke(color);
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
		invalidated();
	}

	@Override
	public org.seekers.grpc.game.Player associated() {
		return org.seekers.grpc.game.Player.newBuilder().setId(getId()).addAllSeekerIds(seekers.keySet())
				.setCampId(camp.toString()).setName(name).setColor(color.toString()).setScore(score).build();
	}

	@Override
	public Mirror<Player, Label> get() {
		return mirror;
	}
}
