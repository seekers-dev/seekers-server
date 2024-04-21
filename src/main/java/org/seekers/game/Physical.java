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

import com.google.protobuf.Message;
import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import org.ini4j.Ini;
import org.seekers.grpc.Corresponding;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.List;

public abstract class Physical<P extends Physical.Properties> extends Circle implements Entity,
        Corresponding.ExtendableCorresponding {

    private final @Nonnull Game game;
    private @Nonnull Point2D acceleration = Point2D.ZERO;
    private @Nonnull Point2D velocity = Point2D.ZERO;
    private @Nonnull Point2D position = Point2D.ZERO;

    protected final @Nonnull P properties;

    /**
     * Constructs a new instance of the Physical class.
     *
     * @param game The Game object associated with the Physical object.
     */
    protected Physical(@Nonnull Game game, @Nonnull P properties) {
        this.game = game;
        this.properties = properties;

        setRadius(properties.radius);
        getGame().getFront().getChildren().add(this);
        getGame().getEntities().add(this);
    }

    public static class Properties {
        final double mass;
        final double radius;
        final double thrust;
        final double friction;
        public Properties(Ini ini, String section) {
            mass = ini.fetch(section, "mass", double.class);
            radius = ini.fetch(section, "radius", double.class);
            thrust = ini.fetch(section, "thrust", double.class);
            friction = ini.fetch(section, "friction", double.class);
        }
    }

    @OverridingMethodsMustInvokeSuper
    @Override
    public void update() {
        accelerate();
        velocity();
        displacement();
        checks();
    }

    public abstract void accelerate();

    public void velocity() {
        setVelocity(getVelocity().multiply(1 - properties.friction));
        setVelocity(getVelocity().add(getAcceleration().multiply(getThrust())));
    }

    public void displacement() {
        setPosition(getPosition().add(getVelocity()));
        getGame().getGameMap().normPosition(this);
    }

    /**
     * Performs collision checks with other Physical objects.
     */
    private void checks() {
        final List<Entity> entities = getGame().getEntities();
        for (Entity entity : entities) {
            if (entity == this || !(entity instanceof Physical))
                continue;
            Physical<?> physical = (Physical<?>) entity;
            double min = properties.radius + physical.properties.radius;
            double dist = getGame().getGameMap().getDistance(position, physical.position);
            if (min > dist) {
                collision(physical, min);
            }
        }
    }

    /**
     * Handles a collision with another Physical object.
     *
     * @param another     The Physical object with which a collision occurred.
     * @param minDistance The minimum distance required for a collision to occur.
     */
    @OverridingMethodsMustInvokeSuper
    public void collision(@Nonnull Physical<?> another, double minDistance) {
        Point2D distance = getGame().getGameMap().getDifference(getPosition(), another.getPosition());

        Point2D deltaR = distance.normalize();
        Point2D deltaV = another.getVelocity().subtract(getVelocity());

        double dualV = deltaV.getX() * deltaR.getX() + deltaV.getY() * deltaR.getY();
        double dualM = 2 / (properties.mass + another.properties.mass);

        if (dualV < 0) {
            setVelocity(getVelocity().add(deltaR.multiply(another.properties.mass * dualM * dualV)));
            another.setVelocity(another.getVelocity().subtract(deltaR.multiply(properties.mass * dualM * dualV)));
        }
        double ddn = distance.getX() * deltaR.getX() + distance.getY() * deltaR.getY();
        if (ddn < minDistance) {
            setPosition(getPosition().add(deltaR.multiply(ddn - minDistance)));
            another.setPosition(another.getPosition().subtract(deltaR.multiply(ddn - minDistance)));
        }
    }

    /**
     * Retrieves the Game object associated with the Physical object.
     *
     * @return The Game object.
     */
    @Nonnull
    public Game getGame() {
        return game;
    }

    /**
     * Retrieves the position of the Physical object.
     *
     * @return The position vector.
     */
    @Nonnull
    public Point2D getPosition() {
        return position;
    }

    /**
     * Sets the position of the Physical object.
     *
     * @param position The new position vector.
     */
    public void setPosition(@Nonnull Point2D position) {
        this.position = position;
        setCenterX(position.getX());
        setCenterY(position.getY());
    }

    /**
     * Retrieves the velocity of the Physical object.
     *
     * @return The velocity vector.
     */
    @Nonnull
    public Point2D getVelocity() {
        return velocity;
    }

    /**
     * Sets the velocity of the Physical object.
     *
     * @param velocity The new velocity vector.
     */
    public void setVelocity(@Nonnull Point2D velocity) {
        this.velocity = velocity;
    }

    /**
     * Retrieves the acceleration of the Physical object.
     *
     * @return The acceleration vector.
     */
    @Nonnull
    public Point2D getAcceleration() {
        return acceleration;
    }

    /**
     * Sets the acceleration of the Physical object.
     *
     * @param acceleration The new acceleration vector.
     */
    public void setAcceleration(@Nonnull Point2D acceleration) {
        this.acceleration = acceleration;
    }

    public double getThrust() {
        return properties.thrust;
    }

    @Override
    public Message associated() {
        return org.seekers.grpc.game.Physical.newBuilder().setId(getIdentifier())
                .setAcceleration(TorusMap.toMessage(acceleration)).setPosition(TorusMap.toMessage(position))
                .setVelocity(TorusMap.toMessage(velocity)).build();
    }

}
