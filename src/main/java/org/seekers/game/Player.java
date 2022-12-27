package org.seekers.game;

import java.util.HashMap;
import java.util.Map;

import org.seekers.grpc.Corresponding;
import org.seekers.grpc.PlayerStatus;

import javafx.scene.paint.Color;

public class Player implements Corresponding<PlayerStatus> {
	private final Map<String, Seeker> seekers = new HashMap<>();

	private final Game game;
	private final Color color;

	private final String token;

	private Camp camp;
	private int score;

	public Player(Game game, String token) {
		this.game = game;
		this.token = token;
		this.color = Color.rgb((int) (Math.random() * 124 + 124), (int) (Math.random() * 124 + 124),
				(int) (Math.random() * 124 + 124));

		game.getPlayers().put(toString(), this);
	}

	public Map<String, Seeker> getSeekers() {
		return seekers;
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
	public PlayerStatus associated() {
		return PlayerStatus.newBuilder().setId(toString()).addAllSeekerIds(seekers.keySet()).setCampId(camp.toString())
				.setColor(color.toString()).setScore(score).build();
	}
}
