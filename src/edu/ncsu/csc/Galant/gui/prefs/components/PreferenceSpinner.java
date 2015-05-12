package edu.ncsu.csc.Galant.gui.prefs.components;

import java.awt.Dimension;
import java.text.NumberFormat;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import edu.ncsu.csc.Galant.gui.prefs.PreferenceComponent;
import edu.ncsu.csc.Galant.prefs.Preference;

/**
 * Enables users to choose preference options via an aesthetically pleasing JSpinner
 * @author Alex McCabe
 */
public class PreferenceSpinner extends PreferenceComponent<Integer, JSpinner>
	{
		/**
		 * Creates a new <code>PreferenceSpinner</code> for the given preference.
		 * @param preference the {@link Preference} that this component is associated with.
		 * @param min the minimum possible value for the spinner.
		 * @param max the maximum possible value for the spinner.
		 * @param step the step size of the spinner.
		 * @see SpinnerNumberModel#SpinnerNumberModel(Number, Comparable, Comparable, Number)
		 */
		public PreferenceSpinner(Preference<Integer> preference, Integer min, Integer max,
			Integer step)
			{
				super(preference, new JSpinner(new SpinnerNumberModel(preference.getDefaultValue(),
					min, max, step)));
				getComponent().addChangeListener(new ChangeListener(){
					@Override
					public void stateChanged(ChangeEvent e)
						{
							JComponent editor = getComponent().getEditor();
							Dimension preferred = editor.getPreferredSize();
							int prevWidth = preferred.width;
							preferred.width =
								SwingUtilities.computeStringWidth(editor.getFontMetrics(editor.getFont()), NumberFormat
									.getInstance().format(getValue()));
							editor.setPreferredSize(preferred);
							if(prevWidth != preferred.width)
								getComponent().getParent().validate();
						}
				});
			}
		@Override
		protected Integer getValue()
			{
				Object value = getComponent().getValue();
				if(!(value instanceof Integer))
					throw new ClassCastException("Expected Integer; Found " + value.getClass());
				return (Integer)value;
			}

		@Override
		protected void setValue(Integer value)
			{
				getComponent().setValue(value);
			}
	}
