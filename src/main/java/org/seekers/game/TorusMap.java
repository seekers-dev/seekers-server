package org.seekers.game;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.scvis.geometry.Vector2D;

/**
 * The TorusMap class provides utility methods for handling positions and
 * distances on a torus-shaped map.
 * 
 * @author karlz
 */
public interface TorusMap {

	static org.seekers.grpc.game.Vector2D toMessage(Vector2D vec) {
		return org.seekers.grpc.game.Vector2D.newBuilder().setX(vec.getX()).setY(vec.getY()).build();
	}

	/**
	 * Adjusts the position of a Physical object to the normalized position on the
	 * torus map.
	 * 
	 * @param physical The Physical object to adjust the position for.
	 */
	default void putNormalizedPosition(@Nonnull Physical physical) {
		Vector2D p = physical.getPosition();

		physical.setPosition(physical.getPosition().subtract(Math.floor(p.getX() / getWidth()) * getWidth(),
				Math.floor(p.getY() / getHeight()) * getHeight()));
	}

	/**
	 * Finds the nearest Physical object to a given position.
	 * 
	 * @param p         The position to find the nearest Physical object from.
	 * @param physicals The collection of Physical objects to search from.
	 * @return The nearest Physical object.
	 */
	@Nullable
	default Physical getNearestPhysicalOf(@Nonnull Vector2D p, @Nonnull Iterable<? extends Physical> physicals) {
		double distance = getWidth() * getHeight();
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

	private static double distance(double p0, double p1, double d) {
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
	default double getTorusDistance(@Nonnull Vector2D p0, @Nonnull Vector2D p1) {
		return new Vector2D(distance(p0.getX(), p1.getX(), getWidth()), distance(p0.getY(), p1.getY(), getHeight()))
				.magnitude();
	}

	private static double difference(double p0, double p1, double d) {
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
	default Vector2D getTorusDifference(@Nonnull Vector2D p0, @Nonnull Vector2D p1) {
		return new Vector2D(difference(p0.getX(), p1.getX(), getWidth()),
				difference(p0.getY(), p1.getY(), getHeight()));
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
	default Vector2D getTorusDirection(@Nonnull Vector2D p0, @Nonnull Vector2D p1) {
		return getTorusDifference(p0, p1).normalize();
	}

	/**
	 * Generates a random position on the torus map.
	 * 
	 * @return A random position on the torus map.
	 */
	@Nonnull
	default Vector2D getRandomPosition() {
		return new Vector2D(Math.random() * getWidth(), Math.random() * getHeight());
	}

	/**
	 * Returns the diameter of the torus map.
	 * 
	 * @return The diameter of the torus map.
	 */
	default double getDiameter() {
		return Math.hypot(getWidth(), getHeight());
	}

	/**
	 * Returns the diameter of the torus map.
	 * 
	 * @return The diameter of the torus map.
	 */
	@Nonnull
	default Vector2D getCenter() {
		return new Vector2D(getWidth() / 2, getHeight() / 2);
	}

	/**
	 * Returns the width of the torus map.
	 * 
	 * @return The width of the torus map.
	 */
	double getWidth();

	/**
	 * Returns the height of the torus map.
	 * 
	 * @return The height of the torus map.
	 */
	double getHeight();

}
