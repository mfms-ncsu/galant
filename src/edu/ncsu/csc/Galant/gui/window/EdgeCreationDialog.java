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
        if ( dispatch.isAnimationMode() ) {
            edge = graph.addEdge(source, target);
        }
        else {
            edge = graph.addInitialEdge(source, target);
        }
        GraphPanel panel = window.getGraphPanel();
        panel.setSelectedNode(null);
        panel.setSelectedEdge(edge);
        window.getComponentEditPanel().setWorkingComponent(edge);
        panel.setEdgeTracker(null);
        LogHelper.exitMethod(getClass(), "performAction");
    }
}

//  [Last modified: 2016 06 27 at 18:02:27 GMT]
