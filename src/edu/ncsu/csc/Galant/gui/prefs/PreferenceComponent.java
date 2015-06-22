package edu.ncsu.csc.Galant.gui.prefs;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import edu.ncsu.csc.Galant.prefs.Preference;

/**
 * A component that is associated with a {@link Preference}.
 * @Author Alex McCabe
 */
public abstract class PreferenceComponent<V, C extends Component>
	{
		public static final Map<Preference<?>, PreferenceComponent<?, ? extends Component>> componentMap =
			new HashMap<Preference<?>, PreferenceComponent<?, ? extends Component>>();

		private Preference<V> preference;
		private C component;

		public PreferenceComponent(Preference<V> preference, C component)
			{
				this.preference = preference;
				this.component = component;
				componentMap.put(preference, this);
			}

		/** @return this <code>PreferenceComponent</code>'s preference. */
		public Preference<V> getPreference()
			{
				return preference;
			}

		/** @return this <code>PreferenceComponent</code>'s component. */
		public C getComponent()
			{
				return component;
			}

		/** Gets a value of type <code>V</code> from this component. */
		protected abstract V getValue();

		/** Sets the given value on this component. */
		protected abstract void setValue(V value);

		/**
		 * Gets the value from this component and applies it to the program. This involves setting
		 * the value stored by this <code>Preference</code>, as well as changing anything elsewhere
		 * that should change based on the new value. If the latter is necessary, this method should
		 * be overwritten, with a call to <code>super()</code> included.
		 */
		public void apply()
			{
				preference.put(getValue());
			}

		/** Sets this component to show its preference's value. */
		public void show()
			{
				setValue(preference.get());
			}

		/** Resets this component to the default value. */
		public void reset()
			{
				setValue(preference.getDefaultValue());
			}
	}
