/**
 * Implements depth-first search of a graph, directed or undirected; in the
 * undirected case, it behaves as if edges were directed in both directions.
 * @deprecated See interactive_dfs.alg. which ...
 * - allows user to select a start node (for each component in an undirected graph)
 * - uses integer attributes instead of arrays to store discovery and finish times
 * - uses different colors for different kinds of non-tree edges
 */

int time;
int [] discovery;
int [] finish;

function visit(Node v, Node parent) {
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

  for_nodes( u ) {
    if ( ! marked(u) ) {
      visit( u, null );
    }
  }
}

//  [Last modified: 2017 01 23 at 22:38:28 GMT]
