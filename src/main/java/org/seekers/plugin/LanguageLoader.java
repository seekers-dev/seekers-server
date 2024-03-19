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
     * Creates a new seekers client that loads the language file.
     *
     * @return a new instance
     */
    SeekersClient create();
}
