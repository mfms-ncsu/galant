/**
 * @file kruskal.alg
 * animation of Kruskal's MST algorithm; this version uses a priority queue
 * instead of simply sorting the edges
 */

// parent in the disjoint set forest
Node [] parent;

/**
 * @return the scaled Euclidian distance between v and w
 */
function float euclidian(Node v, Node w) {
    float MAX_DISTANCE = (float) 10.0;
    float SCALE_FACTOR = (float) (MAX_DISTANCE / diagonalLength());
    return (float) (distance(v, w) * SCALE_FACTOR);
}

// standard disjoint set untilities; not doing union by rank or path
// compression; efficiency is not an issue

function INIT_SET(Node x) {
    parent[id(x)] = x;
}

function LINK(Node x, Node y) {
    parent[id(x)] = y;
}

function Node FIND_SET(Node x) {
    if (x != parent[id(x)])
        parent[id(x)] = FIND_SET(parent[id(x)]);
    return parent[id(x)];
}

function UNION(Node x, Node y) {
	LINK(FIND_SET(x), FIND_SET(y));
}

function init_edge_costs() {
  for_edges(edge) {
    if ( ! hasWeight(edge) ) {
      double distance = euclidian(source(edge), target(edge));
      setWeight(edge, distance);
    }
  }
}

algorithm {
    showEdgeWeights();

    beginStep();
    init_edge_costs();
    endStep();

    parent = new Node[nodeIds()];
    for_nodes(u) {
        INIT_SET(u);
    }

    EdgePriorityQueue pq = new EdgePriorityQueue(getEdges());

    // MST is only relevant for undirected graphs
    setDirected(false);

    int numNodes = numberOfNodes();
    int numTreeEdges = 0;
    float totalWeight = 0;
    while ( ! empty(pq) && numTreeEdges < numNodes - 1 ) {
      Edge e = removeBest(pq);
        beginStep();
        Node h = source(e);
        Node t = target(e);
        // show e's endpoints as it's being considered
        // marking is used for display purposes only
        mark(h);
        mark(t);
        endStep();

        beginStep();
        // if the vertices aren't part of the same set
        if ( FIND_SET(h) != FIND_SET(t) ) {
            // add the edge to the MST and highlight it
            highlight(e);
            UNION(h, t);
            totalWeight += weight(e);
            numTreeEdges++;
            display( "Weight so far is ", totalWeight);
        }
        else {
            display( "Vertices are already in the same component." );
            color(e, YELLOW);
        }
        endStep();

        beginStep();
        unmark(h);
        unmark(t);
        endStep();
    }

    display( "MST has total weight " + totalWeight );
}

/*  [Last modified: 2020 05 10 at 20:30:56 GMT] */
