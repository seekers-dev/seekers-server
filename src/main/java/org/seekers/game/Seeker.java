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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.ini4j.Ini;
import org.seekers.grpc.game.PhysicalOuterClass;
import org.seekers.grpc.game.SeekerOuterClass;
import org.seekers.plugin.GameMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * The Seeker class represents a seeker in the game.
 *
 * @author karlz
 */
public class Seeker extends Physical<Seeker.Properties> {

    public static Iterable<? extends SeekerOuterClass.Seeker> transform(Collection<? extends Seeker> seekers) {
        return seekers.stream().map(Seeker::associated).collect(Collectors.toList());
    }

    private final @Nonnull Player player;
    private final @Nonnull SeekerAnimation animation;

    private @Nonnull Point2D target = getPosition();
    private @Nonnull Color activated = Color.WHITE;
    private @Nonnull Color disabled = Color.WHITE;

    private double magnet = 0.0;
    private double disabledCounter = 0.0;

    /**
     * Constructs a new instance of the Seeker class.
     *
     * @param player   The Player object associated with the Seeker.
     */
    public Seeker(@Nonnull Player player, Properties properties) {
        super(player.getGame(), properties);
        this.player = player;
        this.animation = new SeekerAnimation(getGame());
        setColor(player.getColor());
        player.getSeekers().put(getIdentifier(), this);
        getGame().getSeekers().add(this);
    }

    public static class Properties extends Physical.Properties {
        private static final String SECTION = "seeker";

        public Properties(Ini ini) {
            super(ini, SECTION);
            magnetSlowdown = ini.fetch(SECTION, "magnet-slowdown", double.class);
            disabledTime = ini.fetch(SECTION, "disabled-time", double.class);
        }

        private final double magnetSlowdown;
        private final double disabledTime;
    }

    @Override
    public void update() {
        super.update();
        if (isSeekerDisabled()) {
            disabledCounter = Math.max(disabledCounter - 1, 0);
            if (!isSeekerDisabled()) {
                setFill(activated);
            }
        }
    }

    @Override
    public void accelerate() {
        if (!isSeekerDisabled()) {
            setAcceleration(getGame().getGameMap().getDirection(getPosition(), getTarget()));
        } else {
            setAcceleration(Point2D.ZERO);
        }
    }

    @Override
    public void collision(@Nonnull Physical<?> another, double minDistance) {
        if (another instanceof Seeker) {
            Seeker collision = (Seeker) another;
            if (collision.isSeekerDisabled()) {
                disable();
            } else if (magnet != 0) {
                disable();
                if (collision.magnet != 0)
                    collision.disable();
            } else if (collision.magnet != 0) {
                collision.disable();
            } else {
                disable();
                collision.disable();
            }
        }

        super.collision(another, minDistance);
    }

    /**
     * Finds the nearest Physical object to a given position.
     *
     * @param p         The position to find the nearest Physical object from.
     * @param physicals The collection of Physical objects to search from.
     * @return The nearest Physical object.
     */
    private static @Nullable Physical<?> getNearestPhysicalOf(GameMap map, @Nonnull Point2D p, @Nonnull Iterable<? extends Physical<?>> physicals) {
        double distance = map.getDiameter();
        Physical<?> nearest = null;

        for (Physical<?> physical : physicals) {
            double dif = map.getDistance(p, physical.getPosition());
            if (dif < distance) {
                distance = dif;
                nearest = physical;
            }
        }
        return nearest;
    }

    public void setAutoCommands() {
        @SuppressWarnings("null")
        @Nullable final Goal goal = (Goal) getNearestPhysicalOf(getGame().getGameMap(), getPosition(), getGame().getGoals());
        if (goal != null) {
            if (getGame().getGameMap().getDistance(getPosition(), goal.getPosition()) > 30) {
                setTarget(goal.getPosition());
                setMagnet(0);
            } else {
                final Camp checked = getPlayer().getCamp();
                if (checked != null) {
                    setTarget(checked.getPosition());
                    setMagnet(1);
                }
            }
        }
    }

    /**
     * Calculates the magnetic force between the Seeker and a given position.
     *
     * @param p The position to calculate the magnetic force with.
     * @return The magnetic force vector.
     */
    @Nonnull
    public Point2D getMagneticForce(@Nonnull Point2D p) {
        double r = getGame().getGameMap().getDistance(getPosition(), p) / getGame().getGameMap().getDiameter() * 10;
        Point2D d = getGame().getGameMap().getDirection(getPosition(), p);
        double s = (r < 1) ? Math.exp(1 / (Math.pow(r, 2) - 1)) : 0;
        return (isSeekerDisabled()) ? Point2D.ZERO : d.multiply(-getMagnet() * s);
    }

    /**
     * Returns the thrust of the Seeker, taking into account the magnet's effect.
     *
     * @return The thrust of the Seeker.
     */
    @Override
    public double getThrust() {
        return properties.thrust * (magnet != 0 ? properties.magnetSlowdown : 1);
    }

    /**
     * Returns the Player object associated with the Seeker.
     *
     * @return The Player object associated with the Seeker.
     */
    @Nonnull
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the magnet value of the Seeker.
     *
     * @return The magnet value of the Seeker.
     */
    public double getMagnet() {
        return magnet;
    }

    /**
     * Sets the magnet value of the Seeker.
     *
     * @param magnet The magnet value to set.
     */
    public void setMagnet(double magnet) {
        if (!isSeekerDisabled()) {
            this.magnet = Math.max(Math.min(magnet, 1), -8);
            animation.setVisible(magnet != 0);
        }
    }

    /**
     * Disables the Seeker.
     */
    public void disable() {
        if (!isSeekerDisabled()) {
            disabledCounter = properties.disabledTime;
            setMagnet(0.0);
            animation.setVisible(false);
            setFill(disabled);
        }
    }

    /**
     * Checks if the Seeker is disabled.
     *
     * @return True if the Seeker is disabled, false otherwise.
     */
    public boolean isSeekerDisabled() {
        return disabledCounter > 0;
    }

    /**
     * Returns the target position of the Seeker.
     *
     * @return The target position of the Seeker.
     */
    @Nonnull
    public Point2D getTarget() {
        return target;
    }

    /**
     * Sets the target position of the Seeker.
     *
     * @param target The target position to set.
     */
    public void setTarget(@Nonnull Point2D target) {
        this.target = target;
    }

    /**
     * Sets the color of the Seeker.
     *
     * @param color The color to set.
     */
    @SuppressWarnings("null")
    public void setColor(final @Nonnull Color color) {
        this.activated = color;
        this.disabled = color.darker().darker();
        setFill(color);
        animation.indicator.setStroke(color);
    }

    @Override
    public void setPosition(@Nonnull Point2D position) {
        super.setPosition(position);
        animation.indicator.setCenterX(getPosition().getX());
        animation.indicator.setCenterY(getPosition().getY());
    }

    @Override
    public SeekerOuterClass.Seeker associated() {
        return SeekerOuterClass.Seeker.newBuilder().setSuper((PhysicalOuterClass.Physical) super.associated())
                .setPlayerId(player.getIdentifier()).setMagnet(magnet).setTarget(TorusMap.toMessage(target))
                .setDisableCounter(disabledCounter).build();
    }

    public class SeekerAnimation extends Animation {

        private final @Nonnull Circle indicator = new Circle(properties.radius + 0.25 * getAnimationRange());

        protected SeekerAnimation(@Nonnull Game game) {
            super(game);
            indicator.setFill(Color.TRANSPARENT);
            indicator.setStrokeWidth(5);
            indicator.setStroke(player.getColor());
            getChildren().add(indicator);
            setVisible(false);
        }

        @Override
        public void update() {
            double expansion = (indicator.getRadius() + Math.signum(magnet)) % getAnimationRange();
            indicator.setRadius(expansion + properties.radius);
        }

        @Override
        public void destroy() {
            throw new UnsupportedOperationException();
        }

        /**
         * Returns the animation range of the Seeker.
         *
         * @return The animation range of the Seeker.
         */
        public double getAnimationRange() {
            return 26;
        }
    }
}
