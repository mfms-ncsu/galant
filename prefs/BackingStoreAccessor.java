package edu.ncsu.csc.Galant.prefs;

import java.util.prefs.Preferences;

/**
 * Something that allows you to store and retrieve values of a specific type from the backing store
 * used by {@link Preferences}.
 * @param <V> the type of value to store/retrieve.
 */
public interface BackingStoreAccessor<V>
	{
		/**
		 * Gets a value of type <code>V</code> from the given node with the given key. If the
		 * backing store is not available, should return <code>null</code>.
		 */
		public V get(String key);

		/** Puts a value of type <code>V</code> at the given node with the given key. */
		public void put(String key, V value);
	}
