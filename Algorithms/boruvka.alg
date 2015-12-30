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
    display("labelComponent " + id(v) + " " + label);
    mark(v);
    label(v, label);
    showLabel(v);
    for_adjacent( v, e, w ) {
        if ( ! isMarked(w) && is(e, "inTree") ) {
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
        display("scanning edge " + id(v) + " " + id(w));
        if ( label(v).equals(label(w)) ) {
            display("in same component");
            if ( ! isMarked(w) && e.is("inTree") ) {
                color(e, BLUE);
                Edge bestBelow = lowestWeight(w);
                if ( bestBelow != null && weight(bestBelow) < bestWeight ) {
                    if ( best != null ) unHighlight(best);
                    highlight(bestBelow);
                    best = bestBelow;
                    bestWeight = weight(bestBelow);
                }
                else if ( bestBelow != null ) unHighlight(bestBelow);
            }
            else if ( ! e.is("inTree") ) {
                hide(e);
            }
        }
        else { //  label(w) != label(v)
            display("in another tree");
            // in another tree
            if ( weight(e) < bestWeight ) {
                display("better weight: " + weight(e) + " vs " + bestWeight);
                if ( best != null ) unHighlight(best);
                highlight(e);
                display(e.toString());
                best = e;
                bestWeight = weight(e);
            }
        }
    }
    return best;
}

function reset() {
    for_nodes( v ) {
        unMark(v);
        hideLabel(v);
    }
    for_edges( e ) {
        unHighlight(e);
    }
}

algorithm {
    hideNodeWeights();

    // number of edges in current spanning forest
    int forestEdges = 0;
    
    while ( forestEdges < numberOfNodes() - 1 ) {
        // need to label components first
        for_nodes( v ) {
            display("scanning node " + id(v));
            if ( ! isMarked(v) )
                labelComponent(v, "" + id(v));
        }
        reset();

        for_nodes( componentRepresentative ) {
            // search component in forest containing the component
            // representative to find the lowest cost outgoing edge
            if ( ! isMarked(componentRepresentative) ) {
                Edge bestEdge = lowestWeight(componentRepresentative);
                highlight(bestEdge);
                if ( ! bestEdge.is("inTree") ) {
                    set(bestEdge, "inTree");
                    forestEdges++;
                }
            }
        }
        reset();
    }
    for_edges(e) {
        if ( e.is("inTree") ) {
            showWeight(e);
            highlight(e);
        }
        else hide(e);
    }
}

/*  [Last modified: 2015 12 30 at 19:09:27 GMT] */
