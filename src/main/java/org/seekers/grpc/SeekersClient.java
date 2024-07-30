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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(SeekersClient.class);

    private final Process process;

    public SeekersClient(String file, String exec) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(exec.replace("{file}", file).split(" "));
        File log = new File(file + ".log");
        if (!log.exists()) {
             if (log.createNewFile()) {
                 logger.debug("Logfile was created");
             } else if (!log.exists()) {
                 logger.error("Could not create log file for file {}!", file);
             }
        }
        builder.redirectError(log);
        builder.redirectOutput(log);
        logger.info("Start driver process");
        process = builder.start();
    }

    /**
     * Closes the hosted file and any related gRPC network resources.
     *
     * @throws IOException if it could not close the script
     */
    public void close() throws IOException {
        logger.info("Close process");
        process.destroy();
    }
}
