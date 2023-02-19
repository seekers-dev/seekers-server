package org.seekers.game;

import static org.seekers.grpc.Corresponding.transform;

import org.seekers.grpc.Corresponding.ExtendableCorresponding;
import org.seekers.grpc.Observable;
import org.seekers.grpc.StatusReply;

import com.karlz.entity.Entity;

import javafx.geometry.Point2D;

public abstract class Physical implements Entity, Observable, ExtendableCorresponding {
	private final Game game;

	private Point2D acceleration = Point2D.ZERO;
	private Point2D velocity = Point2D.ZERO;
	private Point2D position;

	private double maxSpeed;
	private double mass;
	private double range;
	private double friction;
	private double baseThrust;

	public Physical(Game game, Point2D position) {
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
		Point2D distance = game.getTorusDifference(getPosition(), another.getPosition());

		Point2D deltaR = distance.normalize();
		Point2D deltaV = getVelocity().subtract(another.getVelocity());

		double dualV = deltaV.dotProduct(deltaR);
		double dualM = 2 / (mass + another.mass);

		if (dualV < 0) {
			setVelocity(getVelocity().add(deltaR.multiply(another.mass * dualM * dualV)));
			another.setVelocity(another.getVelocity().subtract(deltaR.multiply(mass * dualM * dualV)));
		}
		double ddn = distance.dotProduct(deltaR);
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

	public Point2D getPosition() {
		return position;
	}

	public void setPosition(Point2D position) {
		this.position = position;
		changed();
	}

	public Point2D getVelocity() {
		return velocity;
	}

	public void setVelocity(Point2D velocity) {
		this.velocity = velocity;
	}

	public Point2D getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(Point2D acceleration) {
		this.acceleration = acceleration;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public void setRange(double range) {
		this.range = range;
	}

	@Override
	public Object associated() {
		return StatusReply.Physical.newBuilder().setId(getId()).setAcceleration(transform(acceleration))
				.setPosition(transform(position)).setVelocity(transform(velocity)).build();
	}
}
