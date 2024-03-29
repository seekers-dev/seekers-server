package org.seekers.plugin;

import javafx.geometry.Point2D;
import org.seekers.game.Physical;

import javax.annotation.Nonnull;

/**
 * Standard definition of a generic game map.
 */
public interface GameMap {

    void normPosition(@Nonnull Physical<?> physical);

    @Nonnull Point2D getDifference(@Nonnull Point2D p0, @Nonnull Point2D p1);

    default double getDistance(@Nonnull Point2D p0, @Nonnull Point2D p1) {
        return getDifference(p0, p1).magnitude();
    }

    default @Nonnull Point2D getDirection(@Nonnull Point2D p0, @Nonnull Point2D p1) {
        return getDifference(p0, p1).normalize();
    }

    @Nonnull Point2D getRandomPosition();

    double getDiameter();
}
