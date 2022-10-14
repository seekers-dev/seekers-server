package org.seekers.world;

import org.seekers.grpc.Buildable;
import org.seekers.grpc.CampStatus;

import javafx.geometry.Point2D;

public class Camp implements Buildable {
	private final Player player;

	public final Point2D position;

	private double width;
	private double height;

	public Camp(Player player, Point2D position) {
		this.player = player;
		this.position = position;

		width = Double.valueOf(player.getWorld().getProperties().getProperty("camp.width"));
		height = Double.valueOf(player.getWorld().getProperties().getProperty("camp.height"));

		player.getWorld().getCamps().put(toString(), this);
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
	public Object asBuilder() {
		return CampStatus.newBuilder().setId(toString()).setPlayerId(player.toString()).setPosition(asVector(position))
				.setWidth(width).setHeight(height).build();
	}
}
