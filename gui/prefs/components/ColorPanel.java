package edu.ncsu.csc.Galant.gui.prefs.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import edu.ncsu.csc.Galant.gui.prefs.PreferenceComponent;
import edu.ncsu.csc.Galant.prefs.Preference;

/** 
 * A {@link PreferenceComponent} for choosing a color. 
 * @author Alex McCabe
 */
public class ColorPanel extends PreferenceComponent<Color, JPanel>
	{
		private Color color;
		private JLabel label = new JLabel();

		/** Creates a <code>ColorPanel</code> for the given preference. */
		public ColorPanel(Preference<Color> preference)
			{
				super(preference, new JPanel(new FlowLayout(FlowLayout.LEFT)));
				getComponent().add(label);

				// when clicked, brings up a color chooser
				getComponent().addMouseListener(new MouseAdapter(){
					@Override
					public void mouseClicked(MouseEvent e)
						{
							Color newColor =
								JColorChooser.showDialog(getComponent(), "Choose a Color",
									getValue());
							if(newColor != null)
								setValue(newColor);
						}
				});
			}

		@Override
		public Color getValue()
			{
				return color;
			}
		@Override
		public void setValue(final Color color)
			{
				this.color = color;
				label.setText("#" + Integer.toHexString(color.getRGB()).toUpperCase().substring(2));
				label.setIcon(new Icon(){
					@Override
					public void paintIcon(Component c, Graphics g, int x, int y)
						{
							g.setColor(color);
							g.fillRect(x, y, getIconWidth(), getIconHeight());
						}

					@Override
					public int getIconWidth()
						{
							return getIconHeight();
						}

					@Override
					public int getIconHeight()
						{
							return label.getFontMetrics(label.getFont()).getAscent();
						}
				});
				getComponent().validate();
			}
	}