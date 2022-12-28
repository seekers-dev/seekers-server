package org.seekers.game;

import org.seekers.grpc.Corresponding;
import org.seekers.grpc.StatusReply;

import javafx.geometry.Point2D;

public class Camp implements Corresponding<StatusReply.Camp> {
	private final Player player;

	public final Point2D position;

	private double width;
	private double height;

	public Camp(Player player, Point2D position) {
		this.player = player;
		this.position = position;

		width = Double.valueOf(player.getGame().getProperties().getProperty("camp.width"));
		height = Double.valueOf(player.getGame().getProperties().getProperty("camp.height"));

		player.getGame().getCamps().add(this);
	}

	public boolean contains(Point2D p) {
		Point2D deltaR = position.subtract(p);
		return 2 * Math.abs(deltaR.getX()) < width && 2 * Math.abs(deltaR.getY()) < height;
	}

	public Player getPlayer() {
		return player;
	}

	public Point2D getPosition() {
		return position;
	}

	@Override
	public StatusReply.Camp associated() {
		return StatusReply.Camp.newBuilder().setId(getId()).setPlayerId(player.getId())
				.setPosition(Corresponding.transform(position)).setWidth(width).setHeight(height).build();
	}
}
