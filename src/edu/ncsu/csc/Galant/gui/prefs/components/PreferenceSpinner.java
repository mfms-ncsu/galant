package edu.ncsu.csc.Galant.gui.prefs.components;

import java.awt.Dimension;
import java.text.NumberFormat;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JTextField;
import javax.swing.JFormattedTextField;

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
        private final static int SPINNER_FIELD_WIDTH = 3;
        
		public PreferenceSpinner(Preference<Integer> preference, Integer min, Integer max,
			Integer step)
			{
				super(preference, new JSpinner(new SpinnerNumberModel(preference.getDefaultValue(),
					min, max, step)));
                JComponent editor = getComponent().getEditor();
                JFormattedTextField textField
                    = ((JSpinner.DefaultEditor) editor).getTextField();
                textField.setColumns( SPINNER_FIELD_WIDTH );
                textField.setHorizontalAlignment( JTextField.CENTER );
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

//  [Last modified: 2017 04 26 at 21:00:21 GMT]
