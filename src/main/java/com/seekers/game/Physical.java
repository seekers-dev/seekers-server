package com.seekers.game;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.Message;
import com.seekers.grpc.SeekerProperties;

import io.scvis.entity.Entity;
import io.scvis.geometry.Kinetic;
import io.scvis.geometry.Vector2D;
import io.scvis.observable.InvalidationListener;
import io.scvis.observable.InvalidationListener.InvalidationEvent;
import io.scvis.observable.Observable;
import io.scvis.proto.Corresponding;
import io.scvis.proto.Corresponding.ExtendableCorresponding;
import io.scvis.proto.Identifiable;
import io.scvis.proto.Mirror;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public abstract class Physical implements Entity, Kinetic, Observable<Physical>, Identifiable, ExtendableCorresponding {
	private final Game game;

	private Vector2D acceleration = Vector2D.ZERO;
	private Vector2D velocity = Vector2D.ZERO;
	private Vector2D position;

	private double maxSpeed = SeekerProperties.getDefault().getPhysicalMaxSpeed();
	private double mass = 1.0;
	private double range = 1.0;
	private double friction = SeekerProperties.getDefault().getPhysicalFriction();
	private double baseThrust = maxSpeed * friction;

	private final Mirror<Physical, Circle> mirror = new Mirror<>(this, new Circle(10, Color.CRIMSON)) {
		@Override
		public void update(Physical reference) {
			getReflection().setCenterX(reference.getPosition().getX());
			getReflection().setCenterY(reference.getPosition().getY());
		}
	};

	protected Physical(Game game, Vector2D position) {
		this.game = game;
		this.position = position;

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

	public void collision(Physical another, double minDistance) {
		Vector2D distance = game.getTorusDifference(getPosition(), another.getPosition());

		Vector2D deltaR = distance.normalize();
		Vector2D deltaV = getVelocity().subtract(another.getVelocity());

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

	public void fireInvalidationEvent(InvalidationEvent<Physical> event) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).invalidated(event);
		}
	}

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

	public Mirror<Physical, Circle> getMirror() {
		return mirror;
	}

	public double getThrust() {
		return baseThrust;
	}

	public Game getGame() {
		return game;
	}

	public Vector2D getPosition() {
		return position;
	}

	public void setPosition(Vector2D position) {
		this.position = position;
		invalidated();
	}

	public Vector2D getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector2D velocity) {
		this.velocity = velocity;
	}

	public Vector2D getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(Vector2D acceleration) {
		this.acceleration = acceleration;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public void setRange(double range) {
		this.range = range;
		mirror.getReflection().setRadius(range);
	}

	@Override
	public Message associated() {
		return com.seekers.grpc.game.Physical.newBuilder().setId(getId())
				.setAcceleration(Corresponding.transform(acceleration)).setPosition(Corresponding.transform(position))
				.setVelocity(Corresponding.transform(velocity)).build();
	}
}
