/** dfs.alg
 * Implements depth-first search of a directed graph only; directedness is
 * forced at the beginning of the algorithm.
 *
 * 
 *
 * $Id: dfs_d.alg 86 2014-06-18 19:48:22Z mfms $
 */

/** Required: node and edge labels visible */

int time;

int [] discovery;
int [] finish;

function visit( Node v ) {
    time = time + 1;
    discovery[id(v)] = time;
    beginStep();
    v.setLabel( "" + discovery[id(v)] );
    v.setSelected( true );
    endStep();
    for_outgoing( v, e, w ) {
        beginStep();
        if ( ! w.isSelected() ) {
            e.setSelected( true ); // make edge thicker if easy to do
            visit(w);
        }
        else if ( finish[id(w)] == 0 ) { /* ancestor */
            e.setLabel( "B" );
        }
        else if ( finish[id(w)] 
                  > discovery[id(v)] ) { /* descendant */
            e.setLabel( "F" );
        }
        else {
            e.setLabel( "C" );
        }
        endStep();
    }
    time = time + 1;
    finish[id(v)] = time;
    beginStep();
    v.mark();
    v.setLabel( "" + discovery[ v.getId() ] + "/" + finish[ v.getId() ] );
    endStep();
}

algorithm {
    time = 0;
    discovery = new int[nodeIds()];
    finish = new int[nodeIds()];

    setDirected(true);

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

//  [Last modified: 2015 08 11 at 12:58:58 GMT]
