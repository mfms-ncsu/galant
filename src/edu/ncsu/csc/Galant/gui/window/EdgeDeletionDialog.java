/**
 * Deletes an edge after its endpoints are selected
 */
package edu.ncsu.csc.Galant.gui.window;

import java.awt.Frame;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.gui.util.EdgeSelectionDialog;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.Node;

public class EdgeDeletionDialog extends EdgeSelectionDialog {

    public EdgeDeletionDialog(Frame frame) {
        super(frame);
    }

    protected void performAction(Node source, Node target) 
        throws Terminate, GalantException {
        GraphDispatch dispatch = GraphDispatch.getInstance();
        Graph graph = dispatch.getWorkingGraph();
        if ( dispatch.isAnimationMode() ) {
            graph.deleteEdge(source, target);
        }
        else {
            graph.removeEdge(source, target);
        }
    }
}

//  [Last modified: 2016 06 27 at 16:48:08 GMT]
