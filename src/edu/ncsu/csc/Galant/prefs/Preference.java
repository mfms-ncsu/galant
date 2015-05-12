package edu.ncsu.csc.Galant.prefs;

import java.util.prefs.Preferences;
import edu.ncsu.csc.Galant.Galant;

/**
 * <p>
 * A representation of a user preference. It's essentially just a container for a value, which can
 * be pushed to a persistent backing store via a {@link Preferences} node. Calling {@link #store()}
 * pushes the main value to the backing store. When the <code>Preference</code> object is created,
 * it gets its inital value from the backing store if available.
 * </p>
 * @param <V> The type of the value.
 */
public class Preference<V>
	{
		/** The {@link Preferences} node at which preferences for Galant should be stored. */
		public static final Preferences PREFERENCES_NODE = Preferences
			.userNodeForPackage(Galant.class);

		private String key, label;
		private V defaultValue, value;
		private BackingStoreAccessor<V> accessor;

		/**
		 * Creates a preference with the given key, label, value type, and default value. Its value
		 * is initally loaded from the backing store, or set to the default value if there is no
		 * such preexisting value.
		 */
		public Preference(String key, String label, V defaultValue, BackingStoreAccessor<V> accessor)
			{
				this.key = key;
				this.label = label;
				this.value = this.defaultValue = defaultValue;
				this.accessor = accessor;

				V storedValue = getAccessor().get(getKey());
				if(storedValue != null)
					put(storedValue);
			}
		/** Creates a preference whose key and label are the same. */
		public Preference(String keyLabel, V defaultValue, BackingStoreAccessor<V> accessor)
			{
				this(keyLabel, keyLabel, defaultValue, accessor);
			}

		// Getters and setters

		/**
		 * @return the key used to uniquely identify this preference to the preferences node.
		 * @see #PREFERENCES_NODE
		 */
		public String getKey()
			{
				return key;
			}

		/**
		 * Returns this preference's label, a human-readable string used to identify it to the user.
		 * @return the label for this preference.
		 */
		public String getLabel()
			{
				return label;
			}

		/**
		 * Sets this preference's label, a human-readable string used to identify it to the user.
		 * @param label the new label.
		 */
		public void setLabel(String label)
			{
				this.label = label;
			}

		/** @return the default value for this preference. */
		public V getDefaultValue()
			{
				return defaultValue;
			}

		/** @return the value for this preference. */
		public V get()
			{
				return value;
			}

		/**
		 * Sets this preference's value.
		 * @param value the new value.
		 */
		public void put(V value)
			{
				this.value = value;
			}

		/** @return this <code>Preference</code>'s accessor. */
		public BackingStoreAccessor<V> getAccessor()
			{
				return accessor;
			}

		/**
		 * Sets this <code>Preference</code>'s accessor to the given
		 * <code>BackingStoreAccessor<V></code>.
		 * @param accessor the new accessor.
		 */
		public void setAccessor(BackingStoreAccessor<V> accessor)
			{
				this.accessor = accessor;
			}

		// End getters and setters

		/** Stores this preference's value to the persistent location. */
		public void store()
			{
				getAccessor().put(getKey(), get());
			}

		/** Allows the given {@link PreferenceVisitor} to visit this <code>Preference</code>. */
		public void accept(PreferenceVisitor visitor)
			{
				visitor.visit(this);
			}
	}
