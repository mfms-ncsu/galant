package edu.ncsu.csc.Galant.gui.prefs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import edu.ncsu.csc.Galant.prefs.Preference;
import edu.ncsu.csc.Galant.prefs.PreferenceGroup;
import edu.ncsu.csc.Galant.prefs.PreferenceVisitor;

/**
 * A panel for displaying a list of preferences to the user. It also includes a button that resets
 * the preferences to their default values.
 * @author Alex McCabe
 */
public class PreferenceGroupPanel extends JScrollPane
	{
		private PreferenceGroup group;
		private JPanel panel = new JPanel(new GridBagLayout());
		private int row = 0;

		/** Creates a new <code>PreferenceGroupPanel</code>. */
		public PreferenceGroupPanel(PreferenceGroup group)
			{
				this.group = group;
				JPanel outerPanel = new JPanel(new BorderLayout());
				setViewportView(outerPanel);

				// add main panel in main area
				outerPanel.add(panel);
				// add reset-to-default button below
				outerPanel.add(initResetButton(), BorderLayout.SOUTH);

				// add preferences in the group
				group.doVisits(new PreferenceVisitor(){
					@Override
					public void visit(Preference<?> preference)
						{
							addPreference(preference);
						}
				});
			}

		private Component initResetButton()
			{
				Box buttonBox = new Box(BoxLayout.X_AXIS);
				buttonBox.add(Box.createHorizontalGlue());
				JButton reset = new JButton("Reset to Default Values");
				buttonBox.add(reset);
				reset.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e)
						{
							group.doVisits(new PreferenceVisitor(){
								@Override
								public void visit(Preference<?> preference)
									{
										PreferenceComponent.componentMap.get(preference).reset();
									}
							});
						}
				});
				return buttonBox;
			}

		/**
		 * Adds the given {@link Preference} to the end of this <code>PreferenceGroupPanel</code>.
		 * The <code>Preference</code>'s associated component and label will be shown in the panel.
		 */
		public void addPreference(Preference<?> preference)
			{
				PreferenceComponent<?, ? extends Component> preferenceComponent =
					PreferenceComponent.componentMap.get(preference);

				// add label

				GridBagConstraints labelConstraints = new GridBagConstraints();
				// labels are in first column
				labelConstraints.gridx = 0;
				// put labels at the top of the display area, and next to their components
				labelConstraints.anchor = GridBagConstraints.NORTHEAST;

				JLabel label = new JLabel(preference.getLabel() + ":");
				label.setLabelFor(preferenceComponent.getComponent());
				panel.add(label, labelConstraints);

				// add associated component

				GridBagConstraints componentConstraints = new GridBagConstraints();
				// each component is in the row after the previous component
				componentConstraints.gridy = row++;
				// components should get any extra horizontal space
				componentConstraints.weightx = 1;

				panel.add(preferenceComponent.getComponent(), componentConstraints);
			}
	}
