package edu.ncsu.csc.Galant.gui.window.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.GraphElement;
import edu.ncsu.csc.Galant.gui.prefs.PreferenceComponent;
import edu.ncsu.csc.Galant.logging.LogHelper;

/** 
 * A {@link PreferenceComponent} for choosing a color. 
 * @author Alex McCabe
 */
public class ColorPanel extends JPanel
{
    private Color color;
    private JLabel label = new JLabel();
		
    private final GraphElement workingElement;

    /** Creates a <code>ColorPanel</code> for the given preference. */
    public ColorPanel(GraphElement _workingElement)
    {
        super();
				
        this.workingElement = _workingElement;
				
        this.add(label);
				
        Color c;
        try  {
            String color = workingElement.getColor();
            c = Color.decode(color);
        } catch (Exception e) {
            c = Color.BLACK;
        }
        this.setValue(c);

        // when clicked, brings up a color chooser
        this.addMouseListener(new MouseAdapter(){
                @Override
					public void mouseClicked(MouseEvent event) {
                    Color newColor =
                        JColorChooser.showDialog(event.getComponent(), "Choose a Color",
                                                 getValue());
                    if(newColor != null) {
                        setValue(newColor);
								
                        GraphDispatch dispatch = GraphDispatch.getInstance();
                        Graph g = dispatch.getWorkingGraph();
								
                        LogHelper.logDebug(label.getText());
                        try {
                            workingElement.setColor(label.getText());
                        }
                        catch ( Exception e ) {
                            e.printStackTrace();
                        }
								
                        dispatch.pushToTextEditor();
                        dispatch.pushToGraphEditor();
                    }
                }
            });
    }

    public Color getValue()
    {
        return color;
    }
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
        this.validate();
    }
		
}

//  [Last modified: 2015 12 04 at 21:41:49 GMT]
