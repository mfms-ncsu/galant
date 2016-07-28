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
    mark(v);
    label(v, label);
    showLabel(v);
    for_adjacent( v, e, w ) {
        if ( ! marked(w) && e.is("inTree") ) {
            color(e, GREEN);
            hideWeight(e);
            labelComponent(w, label);
        }
    }
}

/**
 * Recursively visits every node in the tree containing v using only the tree
 * edges
 * @return the lowest cost edge reachable from v that goes to another tree
 */
function Edge lowestWeight(Node v) {
    mark(v);
    Edge best = null;
    double bestWeight = INFINITY;
    for_adjacent( v, e, w ) {
        if ( label(v).equals(label(w)) ) {
            if ( ! marked(w) && e.is("inTree") ) {
                color(e, BLUE);
                Edge bestBelow = lowestWeight(w);
                if ( bestBelow != null && weight(bestBelow) < bestWeight ) {
                    if ( best != null && ! best.is("inTree") )
                        unHighlight(best);
                    highlight(bestBelow);
                    best = bestBelow;
                    bestWeight = weight(bestBelow);
                }
                else if ( bestBelow != null && ! best.is("inTree") )
                    unHighlight(bestBelow);
            }
            else if ( ! e.is("inTree") ) {
                hide(e);
            }
        }
        else { //  label(w) != label(v)
            // in another tree
            if ( weight(e) < bestWeight ) {
                if ( best != null && ! best.is("inTree") )
                    unHighlight(best);
                highlight(e);
                display(e.toString());
                best = e;
                bestWeight = weight(e);
            }
        }
    }
    return best;
}

/**
 * Turns the edges of the tree in v's component back to green; they were
 * turned blue to highlight them when looking for the lowest cost edge
 */
function recolorComponent(Node v) {
    for_adjacent(v, e, w) {
        if ( e.is("inTree") && color(e) != null && color(e).equals(BLUE) ) {
            color(e, GREEN);
            recolorComponent(w);
        }
    }
}

/**
 * Hides all edges that connect nodes already in the same tree
 */
function hideExtraneousEdges() {
    for_edges( e ) {
        Node v = source(e);
        Node w = target(e);
        if ( ! e.is("inTree") && label(v).equals(label(w)) ) hide(e);
    }
}

/**
 * Remove all marking and highlighting
 */
function reset() {
    display("");
    for_nodes( v ) {
        unMark(v);
    }
    for_edges( e ) {
        unHighlight(e);
    }
}

algorithm {
    hideNodeWeights();

    // number of edges in current spanning forest
    int forestEdges = 0;
    double totalWeight = 0;
    
    while ( forestEdges < numberOfNodes() - 1 ) {
        // need to label components first
        beginStep();
        for_nodes( v ) {
            if ( ! marked(v) ) {
                labelComponent(v, "" + id(v));
            }
        }
        hideExtraneousEdges();
        reset();
        display("showing components");
        endStep();

        Node previousRepresentative = null;
        for_nodes( componentRepresentative ) {
            // search component in forest containing the component
            // representative to find the lowest cost outgoing edge
            if ( ! marked(componentRepresentative) ) {
                beginStep();
                if ( previousRepresentative != null ) {
                    recolorComponent(previousRepresentative);
                }
                display("looking for lowest weight edge from component "
                        + label(componentRepresentative));
                beginStep();
                Edge bestEdge = lowestWeight(componentRepresentative);
                highlight(bestEdge);
                if ( ! bestEdge.is("inTree") ) {
                    set(bestEdge, "inTree");
                    forestEdges++;
                    totalWeight += weight(bestEdge);
                    display("forest has weight " + totalWeight);
                }
                else {
                    display("lowest weight outgoing edge already in tree");
                }
                endStep();
                previousRepresentative = componentRepresentative; 
            }
        }
        beginStep();
        reset();
        endStep();
    }

    beginStep();
    for_edges(e) {
        if ( e.is("inTree") ) {
            showWeight(e);
            highlight(e);
        }
        else hide(e);
    }
    display("weight of minimum spanning tree is " + totalWeight);
    endStep();
}

/*  [Last modified: 2016 07 28 at 01:01:45 GMT] */
