package org.seekers.game;

import javafx.geometry.Point2D;
import org.seekers.plugin.GameMap;

import javax.annotation.Nonnull;

/**
 * The TorusMap class provides utility methods for handling positions and
 * distances on a torus-shaped map.
 * 
 * @author karlz
 */
public class TorusMap implements GameMap {

	private final double width;
	private final double height;

	public TorusMap(double width, double height) {
        this.width = width;
        this.height = height;
    }

	static org.seekers.grpc.game.Vector2D toMessage(Point2D vec) {
		return org.seekers.grpc.game.Vector2D.newBuilder().setX(vec.getX()).setY(vec.getY()).build();
	}

	/**
	 * Adjusts the position of a Physical object to the normalized position on the
	 * torus map.
	 * 
	 * @param physical The Physical object to adjust the position for.
	 */
	@Override
	public void normPosition(@Nonnull Physical<?> physical) {
		Point2D p = physical.getPosition();

		physical.setPosition(physical.getPosition().subtract(Math.floor(p.getX() / getWidth()) * getWidth(),
				Math.floor(p.getY() / getHeight()) * getHeight()));
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
	@Override
	public double getDistance(@Nonnull Point2D p0, @Nonnull Point2D p1) {
		return new Point2D(distance(p0.getX(), p1.getX(), getWidth()), distance(p0.getY(), p1.getY(), getHeight()))
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
	@Override
	@Nonnull
	public Point2D getDifference(@Nonnull Point2D p0, @Nonnull Point2D p1) {
		return new Point2D(difference(p0.getX(), p1.getX(), getWidth()),
				difference(p0.getY(), p1.getY(), getHeight()));
	}

	/**
	 * Calculates the torus direction from one position to another.
	 * 
	 * @param p0 The starting position.
	 * @param p1 The target position.
	 * @return The torus direction from the starting position to the target
	 *         position.
	 */
	@Override
	@Nonnull
	public Point2D getDirection(@Nonnull Point2D p0, @Nonnull Point2D p1) {
		return getDifference(p0, p1).normalize();
	}

	/**
	 * Generates a random position on the torus map.
	 * 
	 * @return A random position on the torus map.
	 */
	@Nonnull
	public Point2D getRandomPosition() {
		return new Point2D(Math.random() * getWidth(), Math.random() * getHeight());
	}

	/**
	 * Returns the diameter of the torus map.
	 * 
	 * @return The diameter of the torus map.
	 */
	public double getDiameter() {
		return Math.hypot(getWidth(), getHeight());
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
	 * Returns the height of the torus map.
	 * 
	 * @return The height of the torus map.
	 */
	public double getHeight() {
		return height;
	}

}
