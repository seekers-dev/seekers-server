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
import javafx.scene.layout.BorderPane;
import org.seekers.plugin.GameMap;
import org.seekers.plugin.GameMode;

import javax.annotation.Nonnull;

public class StandardMode implements GameMode {

    @Override
    public Game createGame(Game.Properties gameProperties, Camp.Properties campProperties,
                           Seeker.Properties seekerProperties, Goal.Properties goalProperties) {
        Game game = new Game(new BorderPane(), gameProperties, campProperties, seekerProperties, goalProperties);
        game.setGameMap(createGameMap(game));
        for (int i = 0; i < game.getGameProperties().goals; i++)
            createGoal(game).setPosition(game.getGameMap().getRandomPosition());
        return game;
    }

    @Override
    public GameMap createGameMap(@Nonnull Game game) {
        return new TorusMap(game.getGameProperties().width, game.getGameProperties().height);
    }

    @Override
    public Player createPlayer(Game game) {
        Player player = new Player(game);
        createCamp(player).setPosition(new Point2D(game.getGameProperties().width * (game.getPlayers().size() - 0.5)
                / game.getGameProperties().players, game.getGameProperties().height * 0.5));
        for (int i = 0; i < game.getGameProperties().seekers; i++)
            createSeeker(player).setPosition(game.getGameMap().getRandomPosition());
        return player;
    }

    @Override
    public Camp createCamp(Player player) {
        return new Camp(player, player.getGame().getCampProperties());
    }

    @Override
    public Seeker createSeeker(Player player) {
        return new Seeker(player, player.getGame().getSeekerProperties());
    }

    @Override
    public Goal createGoal(Game game) {
        return new Goal(game, game.getGoalProperties());
    }
}
