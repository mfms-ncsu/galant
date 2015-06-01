/**
 * @file kruskal.alg
 * @brief implementation of Kruskal's MST algorithm, using Galant
 * $Id: kruskal.alg 74 2014-05-14 01:45:29Z mfms $
 */

/** Required: edge weights defined and visible */

// parent in the disjoint set forest
final Node [] parent = new Node[ graph.numberOfNodes() ];

// standard disjoint set untilities; not doing union by rank or path
// compression; efficiency is not an issue

new_function INIT_SET( Node x ) {
    parent[x.getId()] = x;
}

new_function LINK( Node x, Node y ) {
    parent[x.getId()] = y;
}

new_function Node FIND_SET( Node x ) {
    if (x != parent[x.getId()])
        parent[x.getId()] = FIND_SET(parent[x.getId()]);
    return parent[x.getId()];
}

new_function UNION( Node x, Node y ) {
	LINK( FIND_SET(x), FIND_SET(y) );
}

algorithm {

    for_nodes(u) {
        INIT_SET(u);
    }

    List<Edge> edgeList = getEdges();
    Collections.sort( edgeList );

    // MST is only relevant for undirected graphs
    setDirected( false );

    int totalWeight = 0;
    for ( Edge e: edgeList ) {
        beginStep();
        Node h = e.getSourceNode();
        Node t = e.getDestNode();
        // show e's endpoints as it's being considered
        // marking is used for display purposes only
        h.mark();
        t.mark();
        endStep();

        // if the vertices aren't part of the same set
        if ( FIND_SET(h) != FIND_SET(t) ) {
            // add the edge to the MST and highlight it
            e.setSelected( true );
            UNION(h, t);
            totalWeight += e.getWeight();
            graph.writeMessage( "Weight so far is " + totalWeight );
        }
        else {
            graph.writeMessage( "Vertices are already in the same component." );
        }

        beginStep();
        h.unMark();
        t.unMark();
        endStep();
    }

    graph.writeMessage( "MST has total weight " + totalWeight );

}

/*  [Last modified: 2014 05 12 at 16:45:47 GMT] */
