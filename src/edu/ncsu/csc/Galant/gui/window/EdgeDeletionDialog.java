/**
 * Deletes an edge after its endpoints are selected
 */
package edu.ncsu.csc.Galant.gui.window;

import java.awt.Frame;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.gui.util.EdgeSpecificationDialog;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.gui.window.panels.GraphPanel;
import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.Node;

public class EdgeDeletionDialog extends EdgeSpecificationDialog {

    public EdgeDeletionDialog(Frame frame) {
        super(frame, "Give node id's for edge to delete");
    }

    protected void performAction(Node source, Node target) 
        throws Terminate, GalantException {
        GraphDispatch dispatch = GraphDispatch.getInstance();
        Graph graph = dispatch.getWorkingGraph();
        GraphPanel panel = GraphWindow.getGraphPanel();
        graph.removeEdge(source, target);
        GraphWindow.componentEditPanel.setWorkingComponent(null);
        panel.repaint();
        dispatch.pushToTextEditor();
    }
}

//  [Last modified: 2016 06 28 at 00:33:33 GMT]
