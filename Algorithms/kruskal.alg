/**
 * @file kruskal.alg
 * @brief implementation of Kruskal's MST algorithm, using Galant
 * $Id: kruskal.alg 74 2014-05-14 01:45:29Z mfms $
 */

/** Required: edge weights defined and visible */

// parent in the disjoint set forest
Node [] parent;
Graph graph;

// standard disjoint set untilities; not doing union by rank or path
// compression; efficiency is not an issue

function INIT_SET( Node x ) {
    parent[x.getId()] = x;
}

function LINK( Node x, Node y ) {
    parent[x.getId()] = y;
}

function Node FIND_SET( Node x ) {
    if (x != parent[x.getId()])
        parent[x.getId()] = FIND_SET(parent[x.getId()]);
    return parent[x.getId()];
}

function UNION( Node x, Node y ) {
	LINK( FIND_SET(x), FIND_SET(y) );
}

algorithm {
 graph = getGraph();
 parent= new Node[ graph.numberOfNodes() ];
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
     Node t = e.getTargetNode();
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
/*  [Last modified: 2015 08 07 at 19:42:54 GMT] */
