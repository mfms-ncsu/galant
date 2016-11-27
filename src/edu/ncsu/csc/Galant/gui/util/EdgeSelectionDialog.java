/**
 * Selects an edge after user specifies its endpoints during algorithm execution
 */
package edu.ncsu.csc.Galant.gui.util;

import java.awt.Frame;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.gui.util.EdgeSpecificationDialog;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.graph.container.EdgeSet;

public class EdgeSelectionDialog extends EdgeSpecificationDialog {

    /** set from which the edge is to be selected, null if there is no
     * restriction */
    private EdgeSet restrictedSet = null;
    /**
     * error message if selected edge does not belong to restricted set
     */
    private String errorMessage = "";

    public EdgeSelectionDialog(String prompt) {
        super(GraphWindow.getGraphFrame(), prompt);
        GraphDispatch.getInstance().getWorkingGraph().setSelectedEdge(null);
    }

    public EdgeSelectionDialog(String prompt, EdgeSet restrictedSet, String errorMessage) {
        super(GraphWindow.getGraphFrame(), prompt);
        this.restrictedSet = restrictedSet;
        this.errorMessage = errorMessage;
        GraphDispatch.getInstance().getWorkingGraph().setSelectedEdge(null);
    }

    protected void performAction(Node source, Node target)
        throws Terminate, GalantException {
        GraphDispatch dispatch = GraphDispatch.getInstance();
        Graph graph = dispatch.getWorkingGraph();
        Edge selectedEdge = graph.getEdge(source, target);
        if ( restrictedSet != null
             && ! restrictedSet.contains(selectedEdge) ) {
            throw new GalantException(errorMessage);
        }
        graph.setSelectedEdge(selectedEdge);
    }
}

//  [Last modified: 2016 11 27 at 19:00:50 GMT]
