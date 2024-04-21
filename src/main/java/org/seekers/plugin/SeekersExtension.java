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

import org.ini4j.Profile;
import org.pf4j.ExtensionPoint;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author karlz
 */
public interface SeekersExtension extends ExtensionPoint {

    /**
     * Setups the plugin with the config from the server.
     *
     * @param section the section of the config
     */
    void setup(@CheckForNull Profile.Section section);

    /**
     * Extension point to add your own custom language loaders.
     *
     * @param loaders list of language loaders
     */
    void addLanguageLoaders(@Nonnull final List<LanguageLoader> loaders);
}
