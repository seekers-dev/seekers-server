package org.seekers.game;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.seekers.grpc.SeekerProperties;

import com.google.protobuf.Message;

import io.scvis.entity.Entity;
import io.scvis.geometry.Kinetic;
import io.scvis.geometry.Vector2D;
import io.scvis.observable.InvalidationListener;
import io.scvis.observable.InvalidationListener.InvalidationEvent;
import io.scvis.observable.Observable;
import io.scvis.observable.WrappedObject;
import io.scvis.proto.Corresponding.ExtendableCorresponding;
import io.scvis.proto.Identifiable;
import io.scvis.proto.Mirror;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public abstract class Physical
		implements Entity, Kinetic, WrappedObject, Observable<Physical>, Identifiable, ExtendableCorresponding {
	@Nonnull
	private final Game game;
	@Nonnull
	private Vector2D acceleration = Vector2D.ZERO;
	@Nonnull
	private Vector2D velocity = Vector2D.ZERO;
	@Nonnull
	private Vector2D position;

	private double maxSpeed = SeekerProperties.getDefault().getPhysicalMaxSpeed();
	private double mass = 1.0;
	private double range = 1.0;
	private double friction = SeekerProperties.getDefault().getPhysicalFriction();
	private double baseThrust = maxSpeed * friction;
	@Nonnull
	private final Circle object = new Circle(10, Color.CRIMSON);

	private final Mirror<Physical, Pane> mirror = new Mirror<>(this, new Pane(object)) {
		@Override
		public void update(Physical reference) {
			getReflection().setLayoutX(reference.getPosition().getX());
			getReflection().setLayoutY(reference.getPosition().getY());
		}
	};

	/**
	 * Constructs a new instance of the Physical class.
	 *
	 * @param game     The Game object associated with the Physical object.
	 * @param position The initial position of the Physical object.
	 */
	protected Physical(@Nonnull Game game, @Nullable Vector2D position) {
		this.game = game;
		this.position = position == null ? Vector2D.ZERO : position;

		addInvalidationListener(e -> mirror.update(this));
		getGame().getPhysicals().add(this);
	}

	@Override
	public void update(double deltaT) {
		Kinetic.super.update(deltaT);
		checks();
		invalidated();
	}

	@Override
	public void velocitate(double deltaT) {
		setVelocity(getVelocity().multiply(1 - friction * deltaT));
		setVelocity(getVelocity().add(getAcceleration().multiply(getThrust() * deltaT)));
	}

	@Override
	public void displacement(double deltaT) {
		setPosition(getPosition().add(getVelocity().multiply(deltaT)));
		getGame().putNormalizedPosition(this);
	}

	/**
	 * Performs collision checks with other Physical objects.
	 */
	private void checks() {
		for (Physical physical : getGame().getPhysicals()) {
			if (physical == this)
				continue;
			double min = range + physical.range;
			double dist = getGame().getTorusDistance(position, physical.position);
			if (min > dist) {
				collision(physical, min);
			}
		}
	}

	/**
	 * Handles a collision with another Physical object.
	 *
	 * @param another     The Physical object with which a collision occurred.
	 * @param minDistance The minimum distance required for a collision to occur.
	 */
	public void collision(Physical another, double minDistance) {
		Vector2D distance = game.getTorusDifference(getPosition(), another.getPosition());

		Vector2D deltaR = distance.normalize();
		Vector2D deltaV = another.getVelocity().subtract(getVelocity());

		double dualV = deltaV.getX() * deltaR.getX() + deltaV.getY() * deltaR.getY();
		double dualM = 2 / (mass + another.mass);

		if (dualV < 0) {
			setVelocity(getVelocity().add(deltaR.multiply(another.mass * dualM * dualV)));
			another.setVelocity(another.getVelocity().subtract(deltaR.multiply(mass * dualM * dualV)));
		}
		double ddn = distance.getX() * deltaR.getX() + distance.getY() * deltaR.getY();
		if (ddn < minDistance) {
			setPosition(getPosition().add(deltaR.multiply(ddn - minDistance)));
			another.setPosition(another.getPosition().subtract(deltaR.multiply(ddn - minDistance)));
		}
	}

	private List<InvalidationListener<Physical>> listeners = new ArrayList<>();

	/**
	 * Fires an invalidation event to all registered listeners.
	 *
	 * @param event The invalidation event to be fired.
	 */
	public void fireInvalidationEvent(InvalidationEvent<Physical> event) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).invalidated(event);
		}
	}

	/**
	 * Notifies listeners that the Physical object has been invalidated.
	 */
	protected void invalidated() {
		fireInvalidationEvent(new InvalidationEvent<>(this));
	}

	@Override
	public void addInvalidationListener(InvalidationListener<Physical> listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeInvalidationListener(InvalidationListener<Physical> listener) {
		this.listeners.remove(listener);
	}

	/**
	 * Retrieves the Mirror object associated with the Physical object.
	 *
	 * @return The Mirror object.
	 */
	public Mirror<Physical, Pane> getMirror() {
		return mirror;
	}

	/**
	 * Retrieves the Circle object representing the Physical object in the UI.
	 *
	 * @return The Circle object.
	 */
	public Circle getObject() {
		return object;
	}

	/**
	 * Retrieves the thrust applied to the Physical object.
	 *
	 * @return The thrust value.
	 */
	public double getThrust() {
		return baseThrust;
	}

	/**
	 * Retrieves the Game object associated with the Physical object.
	 *
	 * @return The Game object.
	 */
	public Game getGame() {
		return game;
	}

	/**
	 * Retrieves the position of the Physical object.
	 *
	 * @return The position vector.
	 */
	public Vector2D getPosition() {
		return position;
	}

	/**
	 * Sets the position of the Physical object.
	 *
	 * @param position The new position vector.
	 */
	public void setPosition(Vector2D position) {
		this.position = position;
		invalidated();
	}

	/**
	 * Retrieves the velocity of the Physical object.
	 *
	 * @return The velocity vector.
	 */
	public Vector2D getVelocity() {
		return velocity;
	}

	/**
	 * Sets the velocity of the Physical object.
	 *
	 * @param velocity The new velocity vector.
	 */
	public void setVelocity(Vector2D velocity) {
		this.velocity = velocity;
	}

	/**
	 * Retrieves the acceleration of the Physical object.
	 *
	 * @return The acceleration vector.
	 */
	public Vector2D getAcceleration() {
		return acceleration;
	}

	/**
	 * Sets the acceleration of the Physical object.
	 *
	 * @param acceleration The new acceleration vector.
	 */
	public void setAcceleration(Vector2D acceleration) {
		this.acceleration = acceleration;
	}

	/**
	 * Sets the mass of the Physical object.
	 *
	 * @param mass The new mass value.
	 */
	public void setMass(double mass) {
		this.mass = mass;
	}

	/**
	 * Retrieves the mass of the Physical object.
	 *
	 * @return The mass value.
	 */
	public double getMass() {
		return mass;
	}

	/**
	 * Sets the range of the Physical object.
	 *
	 * @param range The new range value.
	 */
	public void setRange(double range) {
		this.range = range;
		object.setRadius(range);
	}

	/**
	 * Retrieves the range of the Physical object.
	 *
	 * @return The range value.
	 */
	public double getRange() {
		return range;
	}

	@Override
	public Message associated() {
		return org.seekers.grpc.game.Physical.newBuilder().setId(getId())
				.setAcceleration(TorusMap.toMessage(acceleration)).setPosition(TorusMap.toMessage(position))
				.setVelocity(TorusMap.toMessage(velocity)).build();
	}

	@Override
	public Mirror<Physical, Pane> get() {
		return mirror;
	}
}
