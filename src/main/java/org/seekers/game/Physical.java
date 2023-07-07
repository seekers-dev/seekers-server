package org.seekers.game;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.seekers.grpc.SeekersProperties;

import com.google.protobuf.Message;

import io.scvis.entity.Entity;
import io.scvis.geometry.Kinetic;
import io.scvis.geometry.Vector2D;
import io.scvis.observable.WrappedObject;
import io.scvis.proto.Corresponding.ExtendableCorresponding;
import io.scvis.proto.Identifiable;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public abstract class Physical implements Entity, Kinetic, WrappedObject, Identifiable, ExtendableCorresponding {
	@Nonnull
	private final Game game;

	private @Nonnull Vector2D acceleration = Vector2D.ZERO;
	private @Nonnull Vector2D velocity = Vector2D.ZERO;
	private @Nonnull Vector2D position;

	private double mass = 1.0;
	private double range = 1.0;
	private double friction = SeekersProperties.getDefault().getPhysicalFriction();
	private double thrust = SeekersProperties.getDefault().getPhysicalThrust();

	@Nonnull
	private final Circle object = new Circle(10, Color.CRIMSON);
	@Nonnull
	private final Pane render = new Pane(object);

	/**
	 * Constructs a new instance of the Physical class.
	 *
	 * @param game     The Game object associated with the Physical object.
	 * @param position The initial position of the Physical object.
	 */
	protected Physical(@Nonnull Game game, @Nullable Vector2D position) {
		this.game = game;
		this.position = position == null ? Vector2D.ZERO : position;
		getGame().getPhysicals().add(this);
	}

	@Override
	public void update(double deltaT) {
		Kinetic.super.update(deltaT);
		checks();
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
	@OverridingMethodsMustInvokeSuper
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
		return thrust;
	}

	/**
	 * Retrieves the Game object associated with the Physical object.
	 *
	 * @return The Game object.
	 */
	@Nonnull
	public Game getGame() {
		return game;
	}

	/**
	 * Retrieves the Mirror object associated with the Physical object.
	 *
	 * @return The Mirror object.
	 */
	@Override
	public Pane get() {
		return render;
	}

	/**
	 * Retrieves the position of the Physical object.
	 *
	 * @return The position vector.
	 */
	@Nonnull
	public Vector2D getPosition() {
		return position;
	}

	/**
	 * Sets the position of the Physical object.
	 *
	 * @param position The new position vector.
	 */
	public void setPosition(@Nonnull Vector2D position) {
		this.position = position;
		render.setLayoutX(position.getX());
		render.setLayoutY(position.getY());
	}

	/**
	 * Retrieves the velocity of the Physical object.
	 *
	 * @return The velocity vector.
	 */
	@Nonnull
	public Vector2D getVelocity() {
		return velocity;
	}

	/**
	 * Sets the velocity of the Physical object.
	 *
	 * @param velocity The new velocity vector.
	 */
	public void setVelocity(@Nonnull Vector2D velocity) {
		this.velocity = velocity;
	}

	/**
	 * Retrieves the acceleration of the Physical object.
	 *
	 * @return The acceleration vector.
	 */
	@Nonnull
	public Vector2D getAcceleration() {
		return acceleration;
	}

	/**
	 * Sets the acceleration of the Physical object.
	 *
	 * @param acceleration The new acceleration vector.
	 */
	public void setAcceleration(@Nonnull Vector2D acceleration) {
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

}
