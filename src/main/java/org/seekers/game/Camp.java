package org.seekers.game;

import java.util.ArrayList;
import java.util.List;

import org.seekers.grpc.SeekerProperties;

import io.scvis.geometry.Vector2D;
import io.scvis.observable.InvalidationListener;
import io.scvis.observable.InvalidationListener.InvalidationEvent;
import io.scvis.observable.Observable;
import io.scvis.observable.WrappedObject;
import io.scvis.proto.Identifiable;
import io.scvis.proto.Mirror;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * The Camp class represents a camp in the game. It is associated with a
 * specific player and has a shape of a rectangle.
 * 
 * @author karlz
 */
public class Camp implements Identifiable, WrappedObject, Observable<Camp> {
	private final Player player;

	public final Vector2D position;

	private double width;
	private double height;

	private final Mirror<Camp, Rectangle> mirror = new Mirror<Camp, Rectangle>(this, new Rectangle()) {
		@Override
		public void update(Camp reference) {
			mirror.getReflection().setLayoutX(position.getX() - width / 2);
			mirror.getReflection().setLayoutY(position.getY() - height / 2);
		}
	};

	/**
	 * Constructs a new Camp object associated with the specified player and
	 * positioned at the given position.
	 *
	 * @param player   the player that owns the camp
	 * @param position the position of the camp
	 */
	public Camp(Player player, Vector2D position) {
		this.player = player;
		this.position = position;
		player.getGame().getCamps().put(getId(), this);

		width = SeekerProperties.getDefault().getCampWidth();
		height = SeekerProperties.getDefault().getCampHeight();

		mirror.getReflection().setWidth(width);
		mirror.getReflection().setHeight(height);
		mirror.getReflection().setFill(Color.TRANSPARENT);
		mirror.getReflection().setStroke(player.getColor());
		mirror.getReflection().setStrokeWidth(SeekerProperties.getDefault().getGoalRadius());
		addInvalidationListener(e -> mirror.update(this));
		addInvalidationListener(e -> getPlayer().getGame().getHelpers().values().forEach(h -> h.getCamps().add(this)));

		invalidated();
	}

	/**
	 * Checks if a given position is inside the camp.
	 *
	 * @param p the position to check
	 * @return true if the position is inside the camp, false otherwise
	 */
	public boolean contains(Vector2D p) {
		Vector2D deltaR = position.subtract(p);
		return 2 * Math.abs(deltaR.getX()) < width && 2 * Math.abs(deltaR.getY()) < height;
	}

	private List<InvalidationListener<Camp>> listeners = new ArrayList<>();

	public void fireInvalidationEvent(InvalidationEvent<Camp> event) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).invalidated(event);
		}
	}

	/**
	 * Notifies all registered listeners that the camp has been invalidated.
	 */
	protected void invalidated() {
		fireInvalidationEvent(new InvalidationEvent<>(this));
	}

	@Override
	public void addInvalidationListener(InvalidationListener<Camp> listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeInvalidationListener(InvalidationListener<Camp> listener) {
		this.listeners.remove(listener);
	}

	/**
	 * Returns the mirror object associated with this camp.
	 *
	 * @return the mirror object
	 */
	public Mirror<Camp, Rectangle> getMirror() {
		return mirror;
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
	 * Returns the position of the camp.
	 *
	 * @return the position
	 */
	public Vector2D getPosition() {
		return position;
	}

	@Override
	public org.seekers.grpc.game.Camp associated() {
		return org.seekers.grpc.game.Camp.newBuilder().setId(getId()).setPlayerId(player.getId())
				.setPosition(TorusMap.toMessage(position)).setWidth(width).setHeight(height).build();
	}

	@Override
	public Mirror<Camp, Rectangle> get() {
		return mirror;
	}
}
