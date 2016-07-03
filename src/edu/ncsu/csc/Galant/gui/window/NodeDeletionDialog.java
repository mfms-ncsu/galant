/**
 * Deletes an edge after its endpoints are selected
 */
package edu.ncsu.csc.Galant.gui.window;

import java.awt.Frame;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.gui.util.NodeSpecificationDialog;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.gui.window.panels.GraphPanel;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.Node;

public class NodeDeletionDialog extends NodeSpecificationDialog {

    public NodeDeletionDialog(Frame frame) {
        super(frame, "Give id of node to delete");
    }

    protected void performAction(Node node) 
        throws Terminate, GalantException {
        GraphDispatch dispatch = GraphDispatch.getInstance();
        Graph graph = dispatch.getWorkingGraph();
        GraphPanel panel = GraphWindow.getGraphPanel();
        graph.removeNode(node);
        GraphWindow.componentEditPanel.setWorkingComponent(null);
        panel.repaint();
        dispatch.pushToTextEditor();
    }
}

//  [Last modified: 2016 07 03 at 15:42:23 GMT]
