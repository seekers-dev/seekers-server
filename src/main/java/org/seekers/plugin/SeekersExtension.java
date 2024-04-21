package org.seekers.plugin;

import org.pf4j.ExtensionPoint;

import java.util.List;

/**
 * @author karlz
 */
public interface SeekersExtension extends ExtensionPoint {

    /**
     * Extension point to add your own custom language loaders.
     *
     * @param loaders list of language loaders
     */
    void addLanguageLoaders(final List<LanguageLoader> loaders);
}
