/*
 * Copyright (C) 2022  Seekers Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.seekers.game;

import javafx.geometry.Point2D;

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
