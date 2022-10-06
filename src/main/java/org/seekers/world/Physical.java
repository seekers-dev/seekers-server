package org.seekers.world;

import org.seekers.grpc.Buildable;
import org.seekers.grpc.PhysicalStatus;

import javafx.geometry.Point2D;

public abstract class Physical implements Entity, Buildable {
	private final World world;

	private Point2D acceleration;
	private Point2D position;
	private Point2D velocity = Point2D.ZERO;

	private double maxSpeed = 5;
	private double mass = 1;
	private double range = 10;
	private double friction = 0.02;
	private double baseThrust = maxSpeed * friction;

	public Physical(World world, Point2D position, Point2D acceleration) {
		this.world = world;
		this.position = position;
		this.acceleration = acceleration;

		getWorld().getPhysicals().put(toString(), this);
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
		getWorld().putNormalizedPosition(this);
	}

	protected abstract void accelerate(double deltaT);

	private void checks() {
		for (Physical physical : getWorld().getPhysicals().values()) {
			if (physical == this)
				continue;
			double min = range + physical.range;
			double dist = getWorld().getTorusDistance(position, physical.position);
			if (min > dist) {
				collision(physical, min);
			}
		}
	}

	public void collision(Physical another, double minDistance) {
		Point2D distance = world.getTorusDifference(getPosition(), another.getPosition());

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

	public World getWorld() {
		return world;
	}

	public Point2D getPosition() {
		return position;
	}

	public void setPosition(Point2D position) {
		this.position = position;
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
	public Object asBuilder() {
		return PhysicalStatus.newBuilder().setId(toString()).setAcceleration(asVector(acceleration))
				.setPosition(asVector(position)).setVelocity(asVector(velocity)).build();
	}
}
