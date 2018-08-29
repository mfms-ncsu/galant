/**
 * @file boruvka.alg
 * implementation of Boruvka's MST algorithm, using Galant; designed
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

function double euclidian(Node v, Node w) {
    return getPosition(v).distance(getPosition(w));
}

function init_edge_costs() {
  for_edges(edge) {
    if ( ! hasWeight(edge) ) {
      double distance = euclidian(source(edge), target(edge));
      setWeight(edge, distance);
    }
  }
}

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
 * edges. Marks the nodes of the tree and colors the edges blue.
 * @return the lowest cost edge reachable from v that goes to another tree
 */
function Edge lowestWeight(Node v) {
  mark(v);
  Edge best = null;
  double bestWeight = INFINITY;
  for_adjacent( v, e, w ) {
    if ( label(v).equals(label(w)) ) {
      if ( ! marked(w) && e.is("inTree") ) {
        // a tree edge within the same component is colored blue
        // and we search for the lowest weight outgoing edge in the subtree
        // below it
        color(e, BLUE);
        Edge bestBelow = lowestWeight(w);
        if ( bestBelow != null && weight(bestBelow) < bestWeight ) {
          // update the current lowest weight outgoing edge if a better one
          // is found in the subtree below and change highlighting
          best = bestBelow;
          bestWeight = weight(bestBelow);
        }
      }
      else if ( ! e.is("inTree") ) {
        // edges within the component but not in tree should be hidden
        hide(e);
      }
    }
    else { //  label(w) != label(v)
      // in another tree
      if ( weight(e) < bestWeight ) {
        best = e;
        bestWeight = weight(e);
      }
    }
  }
  if ( best != null ) {
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
  showEdgeWeights();
  showNodeLabels();             // for component labeling

  init_edge_costs();
  
  // number of edges in current spanning forest
  int forestEdges = 0;
  int totalWeight = 0;

  while ( forestEdges < numberOfNodes() - 1 ) {
    // need to label components first
    beginStep();
    for_nodes( v ) {
      if ( ! marked(v) ) {
        labelComponent(v, "c" + id(v));
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
        step();
        if ( previousRepresentative != null ) {
          recolorComponent(previousRepresentative);
        }
        display("looking for lowest weight edge from component "
                + label(componentRepresentative));
        step();
        Edge bestEdge = lowestWeight(componentRepresentative);
        display("best edge = " + string(bestEdge));
        highlight(bestEdge);
        highlight(source(bestEdge));
        highlight(target(bestEdge));
        step();
        unhighlight(source(bestEdge));
        unhighlight(target(bestEdge));
        if ( ! bestEdge.is("inTree") ) {
          set(bestEdge, "inTree");
          forestEdges++;
          totalWeight += weight(bestEdge);
          display("forest has weight ", totalWeight);
        }
        else {
          display("lowest weight outgoing edge already in tree");
        }
        step();
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

/*  [Last modified: 2018 08 29 at 01:24:45 GMT] */
