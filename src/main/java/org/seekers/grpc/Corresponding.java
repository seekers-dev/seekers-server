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

package org.seekers.grpc;

import com.google.protobuf.Message;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Every corresponding instance is associated with another object.
 *
 * @param <T> the associated type
 * @author karlz
 */
public interface Corresponding<T> {
    /**
     * @return the associated object
     */
    T associated();

    /**
     * @return the id of the object, default hash code
     */
    default String getIdentifier() {
        return Integer.toHexString(this.hashCode());
    }

    /**
     * Transforms all corresponding objects into the associated objects.
     *
     * @param corresponding the objects that should be transformed
     * @return the associated objects
     * @param <T> the type of the associated objects
     */
    static <T> Collection<T> transform(Collection<? extends Corresponding<T>> corresponding) {
        return corresponding.stream().map(Corresponding::associated).collect(Collectors.toList());
    }

    /**
     * Generic interface that supports all proto buffer message types.
     */
    interface ExtendableCorresponding extends Corresponding<Message> {

    }
}
