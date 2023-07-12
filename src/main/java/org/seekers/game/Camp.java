package org.seekers.game;

import javax.annotation.Nonnull;

import org.seekers.grpc.SeekersProperties;

import io.scvis.geometry.Vector2D;
import io.scvis.observable.WrappedObject;
import io.scvis.proto.Identifiable;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * The Camp class represents a camp in the game. It is associated with a
 * specific player and has a shape of a rectangle.
 * 
 * @author karlz
 */
public class Camp implements Identifiable, WrappedObject {
	@Nonnull
	private final Player player;
	@Nonnull
	private final Vector2D position;

	private double width;
	private double height;

	private final Rectangle render = new Rectangle();

	/**
	 * Constructs a new Camp object associated with the specified player and
	 * positioned at the given position.
	 *
	 * @param player   the player that owns the camp
	 * @param position the position of the camp
	 */
	public Camp(@Nonnull Player player, @Nonnull Vector2D position) {
		this.player = player;
		this.position = position;
		this.width = SeekersProperties.getDefault().getCampWidth();
		this.height = SeekersProperties.getDefault().getCampHeight();

		render.setLayoutX(position.getX() - width / 2);
		render.setLayoutY(position.getY() - height / 2);
		render.setWidth(width);
		render.setHeight(height);
		render.setFill(Color.TRANSPARENT);
		render.setStroke(player.getColor());
		render.setStrokeWidth(SeekersProperties.getDefault().getGoalRadius());

		player.setCamp(this);
		player.getGame().getCamps().add(this);
	}

	/**
	 * Checks if a given position is inside the camp.
	 *
	 * @param p the position to check
	 * @return true if the position is inside the camp, false otherwise
	 */
	public boolean contains(@Nonnull Vector2D p) {
		Vector2D deltaR = position.subtract(p);
		return 2 * Math.abs(deltaR.getX()) < width && 2 * Math.abs(deltaR.getY()) < height;
	}

	/**
	 * Returns the player associated with this camp.
	 *
	 * @return the player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Returns the mirror object associated with this camp.
	 *
	 * @return the mirror object
	 */
	@Override
	public Rectangle get() {
		return render;
	}

	/**
	 * Returns the position of the camp.
	 *
	 * @return the position
	 */
	@Nonnull
	public Vector2D getPosition() {
		return position;
	}

	@Override
	public org.seekers.grpc.game.Camp associated() {
		return org.seekers.grpc.game.Camp.newBuilder().setId(getId()).setPlayerId(player.getId())
				.setPosition(TorusMap.toMessage(position)).setWidth(width).setHeight(height).build();
	}

}
