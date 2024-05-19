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

package org.seekers.plugin;

import org.seekers.game.*;

public interface GameMode {

    Game createGame(Game.Properties gameProperties, Camp.Properties campProperties,
                            Seeker.Properties seekerProperties, Goal.Properties goalProperties);

    GameMap createGameMap(Game game);

    Player createPlayer(Game game);

    Camp createCamp(Player player);

    Seeker createSeeker(Player player);

    Goal createGoal(Game game);
}
