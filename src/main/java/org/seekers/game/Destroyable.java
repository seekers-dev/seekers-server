package org.seekers.game;

/**
 * Interface for all objects that can be destroyed.
 *
 * @author karlz
 */
public interface Destroyable {
    /**
     * Destroys this object. Call only once. Depending on the implementation, this method should not be called during
     * an iteration, or it may raise a <code>ConcurrentModificationException</code>.
     *
     * @see java.util.ConcurrentModificationException
     */
    void destroy();
}
