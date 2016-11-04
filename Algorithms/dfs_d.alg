/**
 * Implements depth-first search of a directed graph only; directedness is
 * forced at the beginning of the algorithm.
 */

int time;

int [] discovery;
int [] finish;

function visit( Node v ) {
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
    clearNodeLabels();
    clearEdgeLabels();
    endStep();

    for_nodes( u ) {
        if ( ! selected(u) ) {
            visit( u );
        }
    }
}

//  [Last modified: 2016 11 04 at 16:58:48 GMT]
