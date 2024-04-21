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
