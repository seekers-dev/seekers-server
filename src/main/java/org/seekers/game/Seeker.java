package org.seekers.game;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.ini4j.Ini;
import org.seekers.plugin.GameMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The Seeker class represents a seeker in the game.
 *
 * @author karlz
 */
public class Seeker extends Physical<Seeker.Properties> {

    public static Iterable<org.seekers.grpc.game.Seeker> transform(Collection<? extends Seeker> seekers) {
        return seekers.stream().map(Seeker::associated).collect(Collectors.toList());
    }

    private final @Nonnull Player player;
    private final @Nonnull List<Circle> indicators = new ArrayList<>();

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
        setColor(player.getColor());

        for (int i = 1; i < 3; i++) {
            Circle indicator = new Circle(properties.radius + i * 0.25 * getAnimationRange());
            indicator.setFill(Color.TRANSPARENT);
            indicator.setStrokeWidth(3);
            indicator.setStroke(player.getColor());
            indicator.setVisible(false);
            getIndicators().add(indicator);
        }
        getChildren().addAll(getIndicators());

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
        animate();
        if (isSeekerDisabled()) {
            disabledCounter = Math.max(disabledCounter - 1, 0);
            if (!isSeekerDisabled()) {
                getObject().setFill(activated);
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

    /**
     * Animates the Seeker.
     */
    protected void animate() {
        for (Circle indicator : getIndicators()) {
            double expansion = (indicator.getRadius() + Math.signum(magnet)) % getAnimationRange();
            indicator.setRadius(expansion + properties.radius);
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
     * Returns the animation range of the Seeker.
     *
     * @return The animation range of the Seeker.
     */
    public double getAnimationRange() {
        return 26;
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
            if (magnet == 0) {
                getIndicators().forEach(c -> c.setVisible(false));
            } else {
                getIndicators().forEach(c -> c.setVisible(true));
            }
        }
    }

    /**
     * Disables the Seeker.
     */
    public void disable() {
        if (!isSeekerDisabled()) {
            disabledCounter = properties.disabledTime;
            setMagnet(0.0);
            for (Circle indicator : getIndicators()) {
                indicator.setVisible(false);
            }
            getObject().setFill(disabled);
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
        getObject().setFill(color);
        for (Circle circle : getIndicators()) {
            circle.setStroke(color);
        }
    }

    @Override
    public org.seekers.grpc.game.Seeker associated() {
        return org.seekers.grpc.game.Seeker.newBuilder().setSuper((org.seekers.grpc.game.Physical) super.associated())
                .setPlayerId(player.getIdentifier()).setMagnet(magnet).setTarget(TorusMap.toMessage(target))
                .setDisableCounter(disabledCounter).build();
    }

    @Nonnull
    public List<Circle> getIndicators() {
        return indicators;
    }
}
