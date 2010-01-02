package org.doogal.core.util;

/**
 * Explicit destroy method for non-memory resources that are not garbage
 * collected.
 * 
 * @author Mark Aylett
 * 
 */
public interface Destroyable {
    void destroy();
}
