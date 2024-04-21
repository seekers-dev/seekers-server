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
import javafx.scene.shape.Rectangle;
import org.ini4j.Ini;
import org.seekers.grpc.Corresponding;

import javax.annotation.Nonnull;

/**
 * The Camp class represents a camp in the game. It is associated with a
 * specific player and has a shape of a rectangle.
 *
 * @author karlz
 */
public class Camp extends Rectangle implements Corresponding<org.seekers.grpc.game.Camp> {

    private final @Nonnull Player player;
    private final @Nonnull Properties properties;

    private @Nonnull Point2D position = Point2D.ZERO;

    /**
     * Constructs a new Camp object associated with the specified player and
     * positioned at the given position.
     *
     * @param player     the player that owns the camp
     * @param properties the properties of the camp
     */
    public Camp(@Nonnull Player player, Properties properties) {
        this.player = player;
        this.properties = properties;

        setWidth(properties.width);
        setHeight(properties.height);
        setFill(Color.TRANSPARENT);
        setStroke(player.getColor());
        setStrokeWidth(6);

        player.getGame().getBack().getChildren().add(this);
        player.setCamp(this);
        player.getGame().getCamps().add(this);
    }

    public static class Properties {
        private static final String SECTION = "camp";

        public Properties(Ini ini) {
            width = ini.fetch(SECTION, "width", double.class);
            height = ini.fetch(SECTION, "height", double.class);
        }

        private final double width;
        private final double height;
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
        return 2 * Math.abs(deltaR.getX()) < properties.width && 2 * Math.abs(deltaR.getY()) < properties.height;
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

    public void setPosition(@Nonnull Point2D position) {
        this.position = position;
        setLayoutX(position.getX() - properties.width * 0.5);
        setLayoutY(position.getY() - properties.height * 0.5);
    }

    @Override
    public org.seekers.grpc.game.Camp associated() {
        return org.seekers.grpc.game.Camp.newBuilder().setId(getIdentifier()).setPlayerId(player.getIdentifier())
                .setPosition(TorusMap.toMessage(position)).setWidth(properties.width).setHeight(properties.height).build();
    }

}
