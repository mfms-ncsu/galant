/**
 * @file kruskal.alg
 * @brief implementation of Kruskal's MST algorithm, using Galant
 * $Id: kruskal.alg 74 2014-05-14 01:45:29Z mfms $
 */

/** Required: edge weights defined and visible */

// parent in the disjoint set forest
Node [] parent;

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

algorithm {
    hideNodeWeights();

    parent= new Node[nodeIds()];
    for_nodes(u) {
        INIT_SET(u);
    }

    EdgeList edgeList = getEdges();
    sort(edgeList);

    // MST is only relevant for undirected graphs
    setDirected(false);

    int totalWeight = 0;
    for ( Edge e: edgeList ) {
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
            totalWeight += e.getWeight();
            display( "Weight so far is " + totalWeight );
        }
        else {
            display( "Vertices are already in the same component." );
        }
        endStep();

        beginStep();
        unMark(h);
        unMark(t);
        endStep();
    }

    display( "MST has total weight " + totalWeight );
}

/*  [Last modified: 2016 02 18 at 22:55:06 GMT] */
