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
import edu.ncsu.csc.Galant.graph.datastructure.EdgeSet;

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
        initDialog();
    }

    public EdgeSelectionDialog(String prompt, EdgeSet restrictedSet, String errorMessage) {
        super(GraphWindow.getGraphFrame(), prompt);
        this.restrictedSet = restrictedSet;
        this.errorMessage = errorMessage;
        initDialog();
    }

    private void initDialog() {
        GraphDispatch dispatch = GraphDispatch.getInstance();
        dispatch.getWorkingGraph().setSelectedEdge(null);
        dispatch.setActiveQuery(this);
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
        dispatch.setActiveQuery(null);
    }
}

//  [Last modified: 2017 01 14 at 22:37:37 GMT]
