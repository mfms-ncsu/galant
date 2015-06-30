/** dfs.alg
 * Implements depth-first search of a directed graph only; directedness is
 * forced at the beginning of the algorithm.
 *
 * 
 *
 * $Id: dfs_d.alg 86 2014-06-18 19:48:22Z mfms $
 */

/** Required: node and edge labels visible */

/**
 * An unfortunate consequence of the way algorithms are encapsulated in Java
 * is the inability of functions to modify global variables. The exceptions
 * are arrays and instances of classes -- in those cases the functions can
 * change *contents* at will, as long as the references to the arrays/objects
 * remain unchanged.
 */

int time;
Graph graph;
int [] discovery;
// Append initialization in algorithm{} or in Fectching Macro way: nodesList discovery;
int [] finish;
// Append initialization in algorithm{} or in Fectching Macro way: nodesList finish;

function visit( Node v ) {
    time++;
    discovery[ v.getId() ] = time;
    beginStep();
    v.setLabel( "" + discovery[ v.getId() ] );
    v.setSelected( true );
    endStep();
    for_outgoing( v, e, w ) {
        beginStep();
        if ( ! w.isSelected() ) {
            e.setSelected( true ); // make edge thicker if easy to do
            visit( w );
        }
        else if ( finish[ w.getId() ] == 0 ) { /* ancestor */
            e.setLabel( "B" );
        }
        else if ( finish[ w.getId() ] 
                  > discovery[ v.getId() ] ) { /* descendant */
            e.setLabel( "F" );
        }
        else {
            e.setLabel( "C" );
        }
        endStep();
    }
    time++;
    finish[ v.getId() ] = time;
    beginStep();
    v.mark();
    v.setLabel( "" + discovery[ v.getId() ] + "/" + finish[ v.getId() ] );
    endStep();
}

algorithm { 
    graph = getGraph();
    discovery = new int[ graph.numberOfNodes() ];
    finish = new int[ graph.numberOfNodes() ];
    setDirected( true );

    beginStep();
    for_nodes( u ) {
        u.setLabel("");
    }
    for_edges( e ) {
        e.setLabel("");
    }
    endStep();

    for_nodes( u ) {
        if ( ! u.isSelected() ) {
            visit( u );
        }
    }
}

//  [Last modified: 2015 06 23 at 15:17:03 GMT]
