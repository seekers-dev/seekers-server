package com.seekers.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.seekers.grpc.SeekersDispatchHelper;

import io.scvis.proto.Identifiable;
import javafx.scene.paint.Color;

public class Player implements Identifiable {
	private final Map<String, Seeker> seekers = new HashMap<>();

	private final Game game;

	private Camp camp;
	private String color;
	private String name;
	private int score;

	private static final Random rand = new Random();

	public Player(Game game) {
		this.game = game;
		this.color = Color.rgb(rand.nextInt(124) + 124, rand.nextInt(124) + 124, rand.nextInt(124) + 124).toString();
		this.name = "Player " + hashCode();
		game.getPlayers().add(this);
		changed();
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
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public int getScore() {
		return score;
	}

	public void putUp() {
		score++;
		changed();
	}

	@Override
	public com.seekers.grpc.game.Player associated() {
		return com.seekers.grpc.game.Player.newBuilder().setId(getId()).addAllSeekerIds(seekers.keySet())
				.setCampId(camp.toString()).setName(name).setColor(color).setScore(score).build();
	}

	public void changed() {
		for (SeekersDispatchHelper helper : getGame().getHelpers().values())
			helper.getPlayers().add(this);
	}
}
