package org.doogal.core.actor.object;

import static org.doogal.core.actor.object.Utility.toName;

/**
 * Used as a terminator for {@link ObjectBroker} chains.
 * 
 * @author Mark Aylett
 * 
 */

public final class NoSuchObjectBroker implements ObjectBroker {
	public final Object getObject(String name) {
		throw new NoSuchObjectException("no such object: " + name);
	}

	public final Object getObject(Class<?> clazz) {
		throw new NoSuchObjectException("no such object: " + toName(clazz));
	}
}
