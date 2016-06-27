/**
 * Creates an edge after its endpoints are selected
 */
package edu.ncsu.csc.Galant.gui.window;

import java.awt.Frame;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.gui.util.EdgeSelectionDialog;
import edu.ncsu.csc.Galant.gui.window.panels.GraphPanel;
import edu.ncsu.csc.Galant.gui.window.panels.ComponentEditPanel;
import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.logging.LogHelper;

public class EdgeCreationDialog extends EdgeSelectionDialog {

    public EdgeCreationDialog(Frame frame) {
        super(frame);
    }

    protected void performAction(Node source, Node target)
        throws Terminate, GalantException
    {
        LogHelper.enterMethod(getClass(), "performAction" + source + " " + target);
        GraphDispatch dispatch = GraphDispatch.getInstance();
        Graph graph = dispatch.getWorkingGraph();
        GraphWindow window = dispatch.getGraphWindow();
        Edge edge = null;
        edge = graph.addInitialEdge(source, target);
        GraphPanel panel = GraphWindow.getGraphPanel();
        panel.setSelectedNode(null);
        panel.setSelectedEdge(edge);
        window.getComponentEditPanel().setWorkingComponent(edge);
        panel.setEdgeTracker(null);
        panel.repaint();
        dispatch.pushToTextEditor();
        LogHelper.exitMethod(getClass(), "performAction");
    }
}

//  [Last modified: 2016 06 27 at 22:11:36 GMT]
