package org.doogal.core.actor.util;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.jcip.annotations.ThreadSafe;

/**
 * One to many listener multicast.
 * 
 * @author Mark Aylett
 * 
 * @param <T>
 *            update argument type.
 */

@ThreadSafe
public final class UpdateListenerSet<T> implements UpdateListener<T> {
	private final Set<UpdateListener<T>> listeners;

	public UpdateListenerSet() {
		listeners = new CopyOnWriteArraySet<UpdateListener<T>>();
	}

	public final void update(T arg) {
		for (final UpdateListener<T> listener : listeners)
			listener.update(arg);
	}

	public final void clear() {
		listeners.clear();
	}

	public final void addListener(UpdateListener<T> listener) {
		listeners.add(listener);
	}

	public final void removeListener(UpdateListener<T> listener) {
		listeners.remove(listener);
	}
}