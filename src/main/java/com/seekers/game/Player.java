package com.seekers.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.scvis.observable.InvalidationListener;
import io.scvis.observable.InvalidationListener.InvalidationEvent;
import io.scvis.observable.Observable;
import io.scvis.proto.Identifiable;
import io.scvis.proto.Mirror;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Player implements Identifiable, Observable<Player> {
	private final Map<String, Seeker> seekers = new HashMap<>();

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

	public Player(Game game) {
		this.game = game;
		this.color = Color.rgb(rand.nextInt(124) + 124, rand.nextInt(124) + 124, rand.nextInt(124) + 124);
		this.name = "Player " + hashCode();
		mirror.getReflection().setTextFill(color);
		mirror.getReflection().setPadding(new Insets(2.0));
		mirror.getReflection().setFont(Font.font("Ubuntu", 24.0));
		addInvalidationListener(e -> mirror.update(this));
		addInvalidationListener(e -> getGame().getHelpers().values().forEach(h -> h.getPlayers().add(this)));

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

	public Mirror<Player, Label> getMirror() {
		return mirror;
	}

	public Game getGame() {
		return game;
	}

	public Camp getCamp() {
		return camp;
	}

	public void setCamp(Camp camp) {
		this.camp = camp;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		invalidated();
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
		for (Seeker seeker : seekers.values()) {
			seeker.setColor(color);
		}
		mirror.getReflection().setTextFill(color);
		camp.getMirror().getReflection().setStroke(color);
	}

	public int getScore() {
		return score;
	}

	public void putUp() {
		score++;
		invalidated();
	}

	@Override
	public com.seekers.grpc.game.Player associated() {
		return com.seekers.grpc.game.Player.newBuilder().setId(getId()).addAllSeekerIds(seekers.keySet())
				.setCampId(camp.toString()).setName(name).setColor(color.toString()).setScore(score).build();
	}
}
