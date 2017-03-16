/**
 * Implements (interactive) depth-first search of a directed graph and
 * computes strongly connected components. Also does topological sort if the
 * graph is a dag and topologically sorts components otherwise.
 */

int time;

int [] discovery;
int [] finish;

/**
 * Each component is given a unique color.
 */
String [] component_color =
{
  BLACK, RED, BLUE, BROWN, CYAN, ORANGE,
  TEAL, YELLOW, MOSS, VIOLET, GREEN, MAGENTA, GRAY
};

/**
 * set of nodes not yet visited during first dfs
 */
NodeSet unvisited;

/**
 * A stack (implemented as a list) is used to visit nodes by decreasing
 * finish times in second dfs
 */
NodeList finished;

function visit( Node v ) {
    unvisited.remove(v);
    time = time + 1;
    discovery[id(v)] = time;
    beginStep();
    label(v, discovery[id(v)] );
    select(v);
    endStep();
    for_outgoing( v, e, w ) {
        beginStep();
        if ( ! selected(w) ) {
            select(e);
            visit(w);
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
    beginStep();
    mark(v);
    label(v, discovery[id(v)] + "/" + finish[id(v)]);
    push(v, finished);
    endStep();
}

function reverse_visit( Node v, int component ) {
  beginStep();
  color(v, component_color[component % component_color.length]);
  setWeight(v, component);
  showWeight(v);
  //  label(v, component_label[component % component_label.length]);
  mark(v);
  for_incoming( v, e, w ) {
    if ( ! marked(w) ) {
      color(e, component_color[component % component_color.length]);
      reverse_visit(w, component);
    }
    else if ( color(w).equals(color(v)) ) {
      color(e, component_color[component % component_color.length]);
    }
  }
}

algorithm {
  showNodeLabels();
  showEdgeLabels();
  showNodeWeights();

  time = 0;
  discovery = new int[nodeIds()];
  finish = new int[nodeIds()];

  setDirected(true);

  beginStep();
  clearNodeLabels();
  clearEdgeLabels();
  hideAllNodeWeights();
  endStep();

  finished = new NodeList();
  
  unvisited = getNodeSet();
  while ( ! unvisited.isEmpty() ) {
    Node u = getNode("node to visit",
                      unvisited,
                      "node already visited");
    step();
    visit(u);
  }

  beginStep();
  clearEdgeLabels();
  clearEdgeHighlighting();
  clearNodeHighlighting();
  clearNodeMarks();
  endStep();

  int component = 1;
  while ( ! empty(finished) ) {
    Node u = pop(finished);
    if ( ! marked(u) ) {
      reverse_visit(u, component);
      component++;
    }
  }
}

//  [Last modified: 2017 03 16 at 17:50:21 GMT]
