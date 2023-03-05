/**
 * Implements depth-first search of a graph, directed or undirected; in the
 * undirected case, it behaves as if edges were directed in both directions.
 */

int time;
int [] discovery;
int [] finish;

/**
 * set of nodes not yet visited
 */
NodeSet unvisited;

function visit(Node v, Node parent) {
    unvisited.remove(v);
    time = time + 1;
    discovery[id(v)] = time;
    beginStep();
    label(v, discovery[id(v)]);
    mark(v);
    endStep();
    for_outgoing( v, e, w ) {
        // the following 'exemption' ensures that the classification of an
        // edge is based on its first encounter only
        if ( ! isDirected() &&
             ( w == parent
               || hasLabel(e) ) )
            continue;
        beginStep();
        if ( ! marked(w) ) {
            highlight(e);
            highlight(w);
            visit(w, v);
        }
        else if ( finish[id(w)] == 0 ) { /* ancestor */
            label(e, "B");
        }
        else if ( finish[id(w)]
                  > discovery[id(v)] ) { /* descendant */
            label(e, "F");
        }
        else {
            label(e, "C");
        }
        endStep();
    }
    time = time + 1;
    finish[id(v)] = time;
    setLabel(v, discovery[id(v)] + "/" + finish[id(v)]);
}

algorithm {
  showNodeLabels();
  showEdgeLabels();

  discovery = new int[nodeIds()];
  finish = new int[nodeIds()];
  time = 0;

  beginStep();
  clearNodeLabels();
  clearEdgeLabels();
  endStep();

  unvisited = getNodeSet();
  while ( ! unvisited.isEmpty() ) {
      Node u = getNode("node to visit",
                       unvisited,
                       "node already visited");
      step();
      visit(u, null);
  }

  beginStep();
  for_edges( e ) {
    if ( ! highlighted(e) ) hide(e);
  }
  endStep();
}
