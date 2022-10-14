package org.seekers.world;

import java.util.HashMap;
import java.util.Map;

import org.seekers.grpc.Buildable;
import org.seekers.grpc.PlayerStatus;

import javafx.scene.paint.Color;

public class Player implements Buildable {
	private final Map<String, Seeker> seekers = new HashMap<>();

	private final World world;
	private final Color color;

	private final String token;

	private Camp camp;
	private int score;

	public Player(World world, String token) {
		this.world = world;
		this.token = token;
		this.color = Color.rgb((int) (Math.random() * 124 + 124), (int) (Math.random() * 124 + 124),
				(int) (Math.random() * 124 + 124));

		world.getPlayers().put(toString(), this);
	}

	public Map<String, Seeker> getSeekers() {
		return seekers;
	}

	public World getWorld() {
		return world;
	}

	public Camp getCamp() {
		return camp;
	}

	public void setCamp(Camp camp) {
		this.camp = camp;
	}

	public String getToken() {
		return token;
	}

	public Color getColor() {
		return color;
	}

	public int getScore() {
		return score;
	}

	public void putUp() {
		score++;
	}

	@Override
	public Object asBuilder() {
		return PlayerStatus.newBuilder().setId(toString()).addAllSeekerIds(seekers.keySet()).setCampId(camp.toString())
				.setColor(color.toString()).setScore(score).build();
	}
}
