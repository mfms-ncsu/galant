/** dfs.alg
 * Implements depth-first search of a directed graph only; directedness is
 * forced at the beginning of the algorithm.
 */

/** Required: node and edge labels visible */

int time;

int [] discovery;
int [] finish;

function visit( Node v ) {
    time = time + 1;
    discovery[id(v)] = time;
    beginStep();
    setLabel(v, "" + discovery[id(v)] );
    select(v);
    endStep();
    for_outgoing( v, e, w ) {
        beginStep();
        if ( ! selected(w) ) {
            select(e);
            visit(w);
        }
        else if ( finish[id(w)] == 0 ) { /* ancestor */
            setLabel(e, "B");
        }
        else if ( finish[id(w)] 
                  > discovery[id(v)] ) { /* descendant */
            setLabel(e, "F");
        }
        else {
            setLabel(e, "C");
        }
        endStep();
    }
    time = time + 1;
    finish[id(v)] = time;
    beginStep();
    mark(v);
    setLabel(v, "" + discovery[id(v)] + "/" + finish[id(v)]);
    endStep();
}

algorithm {
    time = 0;
    discovery = new int[nodeIds()];
    finish = new int[nodeIds()];

    setDirected(true);

    beginStep();
    hideEdgeWeights();
    hideNodeWeights();
    for_nodes( u ) {
        u.setLabel("");
    }
    for_edges( e ) {
        e.setLabel("");
    }
    endStep();

    for_nodes( u ) {
        if ( ! selected(u) ) {
            visit( u );
        }
    }
}

//  [Last modified: 2016 02 21 at 22:56:45 GMT]
