package com.seekers.game;

import com.google.protobuf.Message;
import com.karlz.bounds.Vector;
import com.karlz.entity.Entity;
import com.karlz.exchange.Corresponding;
import com.karlz.exchange.Corresponding.ExtendableCorresponding;
import com.karlz.exchange.Identifier;

public abstract class Physical implements Entity, Identifier, ExtendableCorresponding {
	private final Game game;

	private Vector acceleration = Vector.ZERO;
	private Vector velocity = Vector.ZERO;
	private Vector position;

	private double maxSpeed;
	private double mass;
	private double range;
	private double friction;
	private double baseThrust;

	public Physical(Game game, Vector position) {
		this.game = game;
		this.position = position;

		maxSpeed = Double.valueOf(game.getProperties().getProperty("physical.max-speed"));
		friction = Double.valueOf(game.getProperties().getProperty("physical.friction"));
		baseThrust = maxSpeed * friction;

		getGame().getPhysicals().add(this);
	}

	@Override
	public void update(double deltaT) {
		move(deltaT);
		checks();
	}

	public void move(double deltaT) {
		setVelocity(getVelocity().multiply(1 - friction * deltaT));
		accelerate(deltaT);
		setVelocity(getVelocity().add(getAcceleration().multiply(getThrust() * deltaT)));
		setPosition(getPosition().add(getVelocity().multiply(deltaT)));
		getGame().putNormalizedPosition(this);
	}

	protected abstract void accelerate(double deltaT);

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
		Vector distance = game.getTorusDifference(getPosition(), another.getPosition());

		Vector deltaR = distance.normalize();
		Vector deltaV = getVelocity().subtract(another.getVelocity());

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

	public double getThrust() {
		return baseThrust;
	}

	public Game getGame() {
		return game;
	}

	public Vector getPosition() {
		return position;
	}

	public void setPosition(Vector position) {
		this.position = position;
		changed();
	}

	public Vector getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector velocity) {
		this.velocity = velocity;
	}

	public Vector getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(Vector acceleration) {
		this.acceleration = acceleration;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public void setRange(double range) {
		this.range = range;
	}

	abstract void changed();

	@Override
	public Message associated() {
		return com.seekers.grpc.game.Physical.newBuilder().setId(getId())
				.setAcceleration(Corresponding.transform(acceleration)).setPosition(Corresponding.transform(position))
				.setVelocity(Corresponding.transform(velocity)).build();
	}
}
