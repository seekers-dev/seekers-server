package org.seekers.game;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.seekers.grpc.SeekerProperties;

import io.scvis.geometry.Vector2D;

/**
 * The TorusMap class provides utility methods for handling positions and
 * distances on a torus-shaped map.
 * 
 * @author karlz
 */
public class TorusMap {

	public static org.seekers.grpc.game.Vector2D toMessage(Vector2D vec) {
		return org.seekers.grpc.game.Vector2D.newBuilder().setX(vec.getX()).setY(vec.getY()).build();
	}

	private double height = SeekerProperties.getDefault().getMapHeight();
	private double width = SeekerProperties.getDefault().getMapWidth();

	/**
	 * Adjusts the position of a Physical object to the normalized position on the
	 * torus map.
	 * 
	 * @param physical The Physical object to adjust the position for.
	 */
	public void putNormalizedPosition(@Nonnull Physical physical) {
		Vector2D p = physical.getPosition();

		physical.setPosition(physical.getPosition().subtract(Math.floor(p.getX() / width) * width,
				Math.floor(p.getY() / height) * height));
	}

	/**
	 * Finds the nearest Physical object to a given position.
	 * 
	 * @param p         The position to find the nearest Physical object from.
	 * @param physicals The collection of Physical objects to search from.
	 * @return The nearest Physical object.
	 */
	@Nullable
	public Physical getNearestPhysicalOf(@Nonnull Vector2D p, @Nonnull Iterable<? extends Physical> physicals) {
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

	/**
	 * Calculates the torus distance between two positions.
	 * 
	 * @param p0 The first position.
	 * @param p1 The second position.
	 * @return The torus distance between the two positions.
	 */
	public double getTorusDistance(@Nonnull Vector2D p0, @Nonnull Vector2D p1) {
		return new Vector2D(distance(p0.getX(), p1.getX(), width), distance(p0.getY(), p1.getY(), height)).magnitude();
	}

	private double difference(double p0, double p1, double d) {
		double temp = Math.abs(p0 - p1);
		return (temp < d - temp) ? p1 - p0 : p0 - p1;
	}

	/**
	 * Calculates the difference between two positions on the torus map.
	 * 
	 * @param p0 The first position.
	 * @param p1 The second position.
	 * @return The torus difference between the two positions.
	 */
	@Nonnull
	public Vector2D getTorusDifference(@Nonnull Vector2D p0, @Nonnull Vector2D p1) {
		return new Vector2D(difference(p0.getX(), p1.getX(), width), difference(p0.getY(), p1.getY(), height));
	}

	/**
	 * 
	 * Calculates the torus direction from one position to another.
	 * 
	 * @param p0 The starting position.
	 * @param p1 The target position.
	 * @return The torus direction from the starting position to the target
	 *         position.
	 */
	@Nonnull
	public Vector2D getTorusDirection(@Nonnull Vector2D p0, @Nonnull Vector2D p1) {
		return getTorusDifference(p0, p1).normalize();
	}

	/**
	 * Generates a random position on the torus map.
	 * 
	 * @return A random position on the torus map.
	 */
	@Nonnull
	public Vector2D getRandomPosition() {
		return new Vector2D(Math.random() * width, Math.random() * height);
	}

	/**
	 * Returns the diameter of the torus map.
	 * 
	 * @return The diameter of the torus map.
	 */
	public double getDiameter() {
		return Math.hypot(width, height);
	}

	/**
	 * Returns the diameter of the torus map.
	 * 
	 * @return The diameter of the torus map.
	 */
	@Nonnull
	public Vector2D getCenter() {
		return new Vector2D(width / 2, height / 2);
	}

	/**
	 * Returns the width of the torus map.
	 * 
	 * @return The width of the torus map.
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * Sets the width of the torus map.
	 * 
	 * @param width The width to set.
	 */
	public void setWidth(double width) {
		this.width = width;
	}

	/**
	 * Returns the height of the torus map.
	 * 
	 * @return The height of the torus map.
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * Sets the height of the torus map.
	 * 
	 * @param height The height to set.
	 */
	public void setHeight(double height) {
		this.height = height;
	}
}
