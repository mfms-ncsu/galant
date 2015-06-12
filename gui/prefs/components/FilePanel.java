package edu.ncsu.csc.Galant.gui.prefs.components;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import edu.ncsu.csc.Galant.gui.prefs.PreferenceComponent;
import edu.ncsu.csc.Galant.prefs.Preference;

/** 
 * A {@link PreferenceComponent} for choosing a file. 
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 */
public class FilePanel extends PreferenceComponent<File, JPanel>
	{
		private JTextField textField = new JTextField();

		/** Creates a <code>FilePanel</code> for the given preference. */
		public FilePanel(Preference<File> preference, final String approveButtonText,
			final int fileSelectionMode)
			{
				super(preference, new JPanel(new FlowLayout(FlowLayout.LEFT)));
				getComponent().add(textField);

				JButton selectFile = new JButton("<html>Browse&hellip;</html>");
				getComponent().add(selectFile);
				selectFile.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e)
						{
							JFileChooser chooser = new JFileChooser(getValue());
							chooser.setFileSelectionMode(fileSelectionMode);
							if(chooser.showDialog(getComponent(), approveButtonText) == JFileChooser.APPROVE_OPTION)
								setValue(chooser.getSelectedFile());
						}
				});
			}

		@Override
		public File getValue()
			{
				return new File(textField.getText());
			}

		@Override
		public void setValue(File file)
			{
				textField.setText(file.getAbsolutePath());
			}
	}