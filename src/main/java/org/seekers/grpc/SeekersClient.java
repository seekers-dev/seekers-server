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
public interface SeekersClient extends AutoCloseable {

    /**
     * Hosts a single AI file. Cannot be called more than once.
     *
     * @param file the file to host
     */
    void host(File file);

    /**
     * Closes the hosted file and any related gRPC network resources.
     *
     * @throws IOException if it could not close the script
     */
    void close() throws IOException;
}
