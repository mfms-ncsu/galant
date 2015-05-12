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
class GlobalVariables {
    public int time;
}

final GlobalVariables globals = new GlobalVariables();

final int [] discovery = new int[ graph.numberOfNodes() ];
final int [] finish = new int[ graph.numberOfNodes() ];

function visit( Node v ) {
    globals.time++;
    discovery[ v.getId() ] = globals.time;
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
    globals.time++;
    finish[ v.getId() ] = globals.time;
    beginStep();
    v.mark();
    v.setLabel( "" + discovery[ v.getId() ] + "/" + finish[ v.getId() ] );
    endStep();
}

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

//  [Last modified: 2014 05 12 at 16:48:42 GMT]
