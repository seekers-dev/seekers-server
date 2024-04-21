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

import org.seekers.grpc.SeekersClient;

import java.util.Collection;

/**
 * Loader for language clients.
 *
 * @author karlz
 * @see SeekersClient
 */
public interface LanguageLoader {

    /**
     * Defines all patterns of file extensions that this loader supports.
     *
     * @return the patterns
     */
    Collection<String> recognizedPatterns();

    /**
     * Checks if this loader can host the file.
     *
     * @param file the file to check
     * @return true if it can host, false otherwise
     */
    default boolean canHost(String file) {
        for (String pattern : recognizedPatterns()) {
            if (file.endsWith(pattern)) return true;
        }
        return false;
    }

    /**
     * Creates a new seekers client that loads the language file.
     *
     * @return a new instance
     */
    SeekersClient create();
}
