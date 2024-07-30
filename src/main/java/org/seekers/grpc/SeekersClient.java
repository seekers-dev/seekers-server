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

import java.io.File;
import java.io.IOException;

/**
 * A seekers client is a local network client that runs a single AI file. It communicates between the AI script file and
 * the seekers' server. It is created by a language loader, which creates different types of clients for different
 * file extensions. After a client is created, it hosts a file, runs the script until the match is finished and finally
 * closes all open resources.
 *
 * @author karlz
 */
public class SeekersClient implements AutoCloseable {

    private Process process;

    public SeekersClient(String file, String exec) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(exec.replace("{file}", file).split(" "));
        File log = new File(file + ".log");
        if (!log.exists()) {
            log.createNewFile();
        }
        builder.redirectError(log);
        builder.redirectOutput(log);
        process = builder.start();
    }

    /**
     * Closes the hosted file and any related gRPC network resources.
     *
     * @throws IOException if it could not close the script
     */
    public void close() throws IOException {
        process.destroy();
    }
}
