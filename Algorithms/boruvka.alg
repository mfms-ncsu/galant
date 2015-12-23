/**
 * @file boruvka.alg
 * @brief implementation of Boruvka's MST algorithm, using Galant; designed
 * to showcase some of the newer Galant facilities.
 *
 * Among these are:
 *  - hideLabel() and showLabel() for graph elements -- a new "visible"
 *    attribute that's used during algorithm execution
 *  - hide() and show() for graph elements -- a new "hidden" attribute
 *  - color() for graph elements with two arguments: the element and a color
 *    string (which may have declared constants in Algorithm.java); also
 *    uncolor() 
 */

/**
 * Labels each node in the same component as v (using spanning tree edges
 * only) with the given label. Also colors the tree edges green.
 */
function labelComponent(Node v, String label) {
    for_adjacent( v, e, w ) {
        if ( is(e, "inTree") ) {
            color(e, GREEN);
            labelComponent(w, label);
        }
    }
}

/**
 * Recursively visits every node in the tree containing v using only the tree
 * edges
 * @return the lowest cost edge reachable from v that goes to another tree
 */
function lowestWeight(Node v) {
    mark(v);
    for_adjacent( v, e, w ) {
        Edge best = null;
        double bestWeight = INFINITY;
        if ( is(e, "inTree") ) {
            color(e, BLUE);
            Edge bestBelow = lowestWeight(w);
            if ( weight(bestBelow) < bestWeight ) {
                unHighlight(best);
                highlight(bestBelow);
                best = bestBelow;
                bestWeight = weight(bestBelow);
            }
        }
        else if ( ! getLabel(v).equals(getLabel(w)) ) {
            // in another tree
            if ( weight(e) < bestWeight ) {
                unHighlight(best);
                highlight(e);
                best = e;
                bestWeight = weight(e);
            }
        }
        else {
            // edge goes to node in same component, make it disappear
            hide(e);
        }
    }
}

algorithm {
    // declare that edge weights and node labels should be made visible,
    // except as specified for individual elements later
    showEdgeWeights();
    showNodeLabels();

    // number of edges in current spanning forest
    int forestEdges = 0;
    
    while ( forestEdges < numberOfNodes() - 1 ) {
        // need to label components first
        for_nodes( v ) {
            labelComponent(v, "" + id(v));
        }

        for_nodes( componentRepresentative ) {
            // search component in forest containing the component
            // representative to find the lowest cost outgoing edge
            Edge bestEdge = lowestCost(componentRepresentative);
            highlight(bestEdge);
            set(bestEdge, "inTree");
            // remove any coloring, marking, etc., that was done during search
            reset(componentRepresentative);
        }
        colorTreeEdges();
    }
}

/*  [Last modified: 2015 12 22 at 14:22:22 GMT] */
