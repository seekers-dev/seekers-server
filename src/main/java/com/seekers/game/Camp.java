package com.seekers.game;

import com.karlz.bounds.Vector;
import com.karlz.exchange.Corresponding;
import com.karlz.exchange.Observable;
import com.seekers.grpc.SeekersDispatchHelper;

public class Camp implements Observable {
	private final Player player;

	public final Vector position;

	private double width;
	private double height;

	public Camp(Player player, Vector position) {
		this.player = player;
		this.position = position;

		width = Double.valueOf(player.getGame().getProperties().getProperty("camp.width"));
		height = Double.valueOf(player.getGame().getProperties().getProperty("camp.height"));

		player.getGame().getCamps().add(this);
		changed();
	}

	public boolean contains(Vector p) {
		Vector deltaR = position.subtract(p);
		return 2 * Math.abs(deltaR.getX()) < width && 2 * Math.abs(deltaR.getY()) < height;
	}

	public Player getPlayer() {
		return player;
	}

	public Vector getPosition() {
		return position;
	}

	@Override
	public com.seekers.grpc.game.Camp associated() {
		return com.seekers.grpc.game.Camp.newBuilder().setId(getId()).setPlayerId(player.getId())
				.setPosition(Corresponding.transform(position)).setWidth(width).setHeight(height).build();
	}

	public void changed() {
		for (SeekersDispatchHelper helper : getPlayer().getGame().getHelpers().values()) {
			helper.getCamps().add(this);
		}
	}
}
