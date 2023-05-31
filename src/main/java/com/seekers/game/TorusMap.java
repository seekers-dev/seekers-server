package com.seekers.game;

import com.seekers.grpc.SeekerProperties;

import io.scvis.geometry.Vector2D;

public class TorusMap {

	private double height = SeekerProperties.getDefault().getMapHeight();
	private double width = SeekerProperties.getDefault().getMapWidth();

	public void putNormalizedPosition(Physical physical) {
		Vector2D p = physical.getPosition();

		physical.setPosition(physical.getPosition().subtract(Math.floor(p.getX() / width) * width,
				Math.floor(p.getY() / height) * height));
	}

	public Physical getNearestPhysicalOf(Vector2D p, Iterable<? extends Physical> physicals) {
		double distance = width * height;
		Physical nearest = null;

		for (Physical physical : physicals) {
			double dif = getTorusDistance(p, physical.getPosition());
			if (dif < distance) {
				distance = dif;
				nearest = physical;
			}
		}
		return nearest;
	}

	private double distance(double p0, double p1, double d) {
		double temp = Math.abs(p0 - p1);
		return Math.min(temp, d - temp);
	}

	public double getTorusDistance(Vector2D p0, Vector2D p1) {
		return new Vector2D(distance(p0.getX(), p1.getX(), width), distance(p0.getY(), p1.getY(), height)).magnitude();
	}

	private double difference(double p0, double p1, double d) {
		double temp = Math.abs(p0 - p1);
		return (temp < d - temp) ? p1 - p0 : p0 - p1;
	}

	public Vector2D getTorusDifference(Vector2D p0, Vector2D p1) {
		return new Vector2D(difference(p0.getX(), p1.getX(), width), difference(p0.getY(), p1.getY(), height));
	}

	public Vector2D getTorusDirection(Vector2D p0, Vector2D p1) {
		return getTorusDifference(p0, p1).normalize();
	}

	public Vector2D getRandomPosition() {
		return new Vector2D(Math.random() * width, Math.random() * height);
	}

	public double getDiameter() {
		return Math.hypot(width, height);
	}

	public Vector2D getCenter() {
		return new Vector2D(width / 2, height / 2);
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}
}
