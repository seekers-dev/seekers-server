package org.seekers.game;

import com.google.protobuf.Message;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.seekers.grpc.Corresponding;
import org.seekers.grpc.Identifiable;
import org.seekers.grpc.SeekersConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.List;

public abstract class Physical extends Pane implements Entity, Identifiable, Corresponding.ExtendableCorresponding {

    private static final double PHYSICAL_FRICTION = SeekersConfig.getConfig().getPhysicalFriction();

    private final @Nonnull Game game;
    private final @Nonnull Circle object = new Circle(10, Color.CRIMSON);

    private @Nonnull Point2D acceleration = Point2D.ZERO;
    private @Nonnull Point2D velocity = Point2D.ZERO;
    private @Nonnull Point2D position;

    private double mass = 1.0;
    private double range = 1.0;
    private double thrust;

    /**
     * Constructs a new instance of the Physical class.
     *
     * @param game     The Game object associated with the Physical object.
     * @param position The initial position of the Physical object.
     */
    protected Physical(@Nonnull Game game, @Nullable Point2D position) {
        this.game = game;
        this.position = position == null ? Point2D.ZERO : position;
        getChildren().add(object);
        getGame().getEntities().add(this);
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
        setVelocity(getVelocity().multiply(1 - PHYSICAL_FRICTION));
        setVelocity(getVelocity().add(getAcceleration().multiply(getThrust())));
    }

    public void displacement() {
        setPosition(getPosition().add(getVelocity()));
        getGame().putNormalizedPosition(this);
    }

    /**
     * Performs collision checks with other Physical objects.
     */
    private void checks() {
        final List<Entity> entities = getGame().getEntities();
        for (Entity entity : entities) {
            if (!(entity instanceof Physical))
                return;
            Physical physical = (Physical) entity;
            if (physical == this)
                continue;
            double min = range + physical.range;
            double dist = getGame().getTorusDistance(position, physical.position);
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
    public void collision(@Nonnull Physical another, double minDistance) {
        Point2D distance = game.getTorusDifference(getPosition(), another.getPosition());

        Point2D deltaR = distance.normalize();
        Point2D deltaV = another.getVelocity().subtract(getVelocity());

        double dualV = deltaV.getX() * deltaR.getX() + deltaV.getY() * deltaR.getY();
        double dualM = 2 / (mass + another.mass);

        if (dualV < 0) {
            setVelocity(getVelocity().add(deltaR.multiply(another.mass * dualM * dualV)));
            another.setVelocity(another.getVelocity().subtract(deltaR.multiply(mass * dualM * dualV)));
        }
        double ddn = distance.getX() * deltaR.getX() + distance.getY() * deltaR.getY();
        if (ddn < minDistance) {
            setPosition(getPosition().add(deltaR.multiply(ddn - minDistance)));
            another.setPosition(another.getPosition().subtract(deltaR.multiply(ddn - minDistance)));
        }
    }

    /**
     * Retrieves the Circle object representing the Physical object in the UI.
     *
     * @return The Circle object.
     */
    @Nonnull
    public Circle getObject() {
        return object;
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
        setLayoutX(position.getX());
        setLayoutY(position.getY());
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

    /**
     * Retrieves the mass of the Physical object.
     *
     * @return The mass value.
     */
    public double getMass() {
        return mass;
    }

    /**
     * Sets the mass of the Physical object.
     *
     * @param mass The new mass value.
     */
    public void setMass(double mass) {
        this.mass = mass;
    }

    /**
     * Retrieves the thrust applied to the Physical object.
     *
     * @return The thrust value.
     */
    public double getThrust() {
        return thrust;
    }

    public void setThrust(double thrust) {
        this.thrust = thrust;
    }

    /**
     * Retrieves the range of the Physical object.
     *
     * @return The range value.
     */
    public double getRange() {
        return range;
    }

    /**
     * Sets the range of the Physical object.
     *
     * @param range The new range value.
     */
    public void setRange(double range) {
        this.range = range;
        object.setRadius(range);
    }

    @Override
    public Message associated() {
        return org.seekers.grpc.game.Physical.newBuilder().setId(getIdentifier())
                .setAcceleration(TorusMap.toMessage(acceleration)).setPosition(TorusMap.toMessage(position))
                .setVelocity(TorusMap.toMessage(velocity)).build();
    }

}
