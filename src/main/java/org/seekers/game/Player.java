package org.seekers.game;

import java.util.HashMap;
import java.util.Map;

import org.seekers.grpc.Corresponding;
import org.seekers.grpc.Observable;
import org.seekers.grpc.PushHelper;
import org.seekers.grpc.StatusReply;

import javafx.scene.paint.Color;

public class Player implements Observable, Corresponding<StatusReply.Player> {
	private final Map<String, Seeker> seekers = new HashMap<>();

	private final Game game;

	private Camp camp;
	private String color;
	private String name;
	private int score;

	public Player(Game game) {
		this.game = game;
		this.color = Color.rgb((int) (Math.random() * 124 + 124), (int) (Math.random() * 124 + 124),
				(int) (Math.random() * 124 + 124)).toString();
		this.name = "Player " + (int) (Math.random() * 1e6);
		game.getPlayers().add(this);
		changed();
	}

	public Map<String, Seeker> getSeekers() {
		return seekers;
	}

	private transient String id;

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
	public StatusReply.Player associated() {
		return StatusReply.Player.newBuilder().setId(getId()).addAllSeekerIds(seekers.keySet())
				.setCampId(camp.toString()).setName(name).setColor(color.toString()).setScore(score).build();
	}

	@Override
	public void changed() {
		for (PushHelper helper : getGame().getHelpers().values())
			helper.getPlayers().add(this);
	}
}
