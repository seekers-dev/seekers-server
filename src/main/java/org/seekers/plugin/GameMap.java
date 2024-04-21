package org.seekers.plugin;

import javafx.geometry.Point2D;
import org.seekers.game.Physical;

import javax.annotation.Nonnull;

/**
 * Standard definition of a generic game map.
 */
public interface GameMap {

    /**
     * Norms the position of the physical
     *
     * @param physical the physical to norm
     */
    void normPosition(@Nonnull Physical<?> physical);

    /**
     * @param p0 the first point P<sub>0</sub>
     * @param p1 the second point P<sub>1</sub>
     * @return the difference between the first and second point.
     */
    @Nonnull Point2D getDifference(@Nonnull Point2D p0, @Nonnull Point2D p1);

    /**
     * @param p0 the first point P<sub>0</sub>
     * @param p1 the second point P<sub>1</sub>
     * @return the distance between the fist and second point.
     */
    default double getDistance(@Nonnull Point2D p0, @Nonnull Point2D p1) {
        return getDifference(p0, p1).magnitude();
    }

    /**
     * @param p0 the first point P<sub>0</sub>
     * @param p1 the second point P<sub>1</sub>
     * @return the normalized vector between the fist and second point.
     */
    default @Nonnull Point2D getDirection(@Nonnull Point2D p0, @Nonnull Point2D p1) {
        return getDifference(p0, p1).normalize();
    }

    /**
     *
     * @return a random position that is inside this game map.
     */
    @Nonnull Point2D getRandomPosition();

    /**
     * The diameter is the largest possible distance between two points that are both inside this map.
     *
     * @return the diameter of this map.
     */
    double getDiameter();
}
