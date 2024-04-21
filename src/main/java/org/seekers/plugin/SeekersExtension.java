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
