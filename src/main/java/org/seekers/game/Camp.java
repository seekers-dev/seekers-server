package org.seekers.game;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.seekers.grpc.Corresponding;
import org.seekers.grpc.Identifiable;
import org.seekers.grpc.SeekersConfig;

import javax.annotation.Nonnull;

/**
 * The Camp class represents a camp in the game. It is associated with a
 * specific player and has a shape of a rectangle.
 *
 * @author karlz
 */
public class Camp extends Rectangle implements Corresponding<org.seekers.grpc.game.Camp>, Identifiable {

    private final @Nonnull Player player;
    private final @Nonnull Point2D position;

    private final double width;
    private final double height;

    /**
     * Constructs a new Camp object associated with the specified player and
     * positioned at the given position.
     *
     * @param player   the player that owns the camp
     * @param position the position of the camp
     */
    public Camp(@Nonnull Player player, @Nonnull Point2D position) {
        this.player = player;
        this.position = position;
        this.width = SeekersConfig.getConfig().getCampWidth();
        this.height = SeekersConfig.getConfig().getCampHeight();

        setLayoutX(position.getX() - width / 2);
        setLayoutY(position.getY() - height / 2);
        setWidth(width);
        setHeight(height);
        setFill(Color.TRANSPARENT);
        setStroke(player.getColor());
        setStrokeWidth(SeekersConfig.getConfig().getGoalRadius());

        player.setCamp(this);
        player.getGame().getCamps().add(this);
    }

    /**
     * Checks if a given position is inside the camp.
     *
     * @param p the position to check
     * @return true if the position is inside the camp, false otherwise
     */
    @Override
    public boolean contains(@Nonnull Point2D p) {
        Point2D deltaR = position.subtract(p);
        return 2 * Math.abs(deltaR.getX()) < width && 2 * Math.abs(deltaR.getY()) < height;
    }

    /**
     * Returns the player associated with this camp.
     *
     * @return the player
     */
    @Nonnull
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the position of the camp.
     *
     * @return the position
     */
    @Nonnull
    public Point2D getPosition() {
        return position;
    }

    @Override
    public org.seekers.grpc.game.Camp associated() {
        return org.seekers.grpc.game.Camp.newBuilder().setId(getIdentifier()).setPlayerId(player.getIdentifier())
                .setPosition(TorusMap.toMessage(position)).setWidth(width).setHeight(height).build();
    }

}
