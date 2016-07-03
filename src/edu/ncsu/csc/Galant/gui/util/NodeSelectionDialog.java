/**
 * Selects a node in response to a user's input during algorithm execution.
 */
package edu.ncsu.csc.Galant.gui.util;

import java.awt.Frame;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.gui.util.NodeSpecificationDialog;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.graph.component.Graph;

public class NodeSelectionDialog extends NodeSpecificationDialog {

    public NodeSelectionDialog(String prompt) {
        super(GraphWindow.getGraphFrame(), prompt);
    }

    protected void performAction(Node node) 
        throws Terminate, GalantException {
        GraphDispatch dispatch = GraphDispatch.getInstance();
        Graph graph = dispatch.getWorkingGraph();
        graph.setSelectedNode(node);
    }
}

//  [Last modified: 2016 07 03 at 15:58:51 GMT]
