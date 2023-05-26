package com.seekers.game;

import com.seekers.grpc.SeekersDispatchHelper;

import io.scvis.geometry.Vector2D;
import io.scvis.proto.Corresponding;
import io.scvis.proto.Identifiable;
import io.scvis.proto.Mirror;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Camp implements Identifiable {
	private final Player player;

	public final Vector2D position;

	private double width;
	private double height;

	private final Mirror<Camp, Rectangle> mirror = new Mirror<Camp, Rectangle>(this, new Rectangle()) {
		@Override
		public void update(Camp reference) {
			getReflection().setWidth(reference.width);
			getReflection().setHeight(reference.height);
		}
	};

	public Camp(Player player, Vector2D position) {
		this.player = player;
		this.position = position;

		mirror.getReflection().setFill(Color.web(player.getColor()));

		width = Double.valueOf(player.getGame().getProperties().getProperty("camp.width"));
		height = Double.valueOf(player.getGame().getProperties().getProperty("camp.height"));

		player.getGame().getCamps().add(this);
		changed();
	}

	public boolean contains(Vector2D p) {
		Vector2D deltaR = position.subtract(p);
		return 2 * Math.abs(deltaR.getX()) < width && 2 * Math.abs(deltaR.getY()) < height;
	}

	public Player getPlayer() {
		return player;
	}

	public Vector2D getPosition() {
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

	public Mirror<Camp, Rectangle> getMirror() {
		return mirror;
	}
}
