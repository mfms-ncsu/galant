/** dfs.alg
 * Implements depth-first search of a graph, directed or undirected; in the
 * undirected case, it behaves as if edges were directed in both directions.
 * $Id: dfs.alg 47 2014-03-10 16:11:00Z mfms $
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

GlobalVariables globals = new GlobalVariables();

int [] discovery; 
// Append initialization in algorithm{} or in Fectching Macro way: nodesList discovery;
int [] finish; 
// Append initialization in algorithm{} or in Fectching Macro way: nodesList finish;
Graph graph;

function visit( Node v, Node parent ) {
    globals.time++;
    discovery[ v.getId() ] = globals.time;
    beginStep();
    v.setLabel( "" + discovery[ v.getId() ] );
    v.mark();
    endStep();
    for_outgoing( v, e, w ) {
        // the following 'exemption' ensures that the classification of an
        // edge is based on its first encounter only
        if ( ! isDirected() &&
             ( w == parent 
               || ! e.getLabel().equals("") ) )
            continue;
        beginStep();
        if ( ! w.isMarked() ) {
            e.setSelected(true); // make edge thicker if easy to do
            w.setSelected(true);
            visit( w, v );
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
    v.setLabel( "" + discovery[ v.getId() ] + "/" + finish[ v.getId() ] );
}

algorithm {
 graph = getGraph();
 discovery	= new int[ graph.getNodes().size() ];
 finish = new int[ graph.getNodes().size() ];

 beginStep();
 for_nodes( u ) {
     u.setLabel("");
 }

 for_edges( e ) {
     e.setLabel("");
 }
 endStep();

 for_nodes( u ) {
     if ( ! u.isMarked() ) {
         visit( u, null );
     }
 }
}

//  [Last modified: 2013 07 15 at 14:07:48 GMT]
