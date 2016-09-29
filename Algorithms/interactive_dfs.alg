/** 
 * interactive_dfs.alg - implements depth-first search of a directed graph
 * only; directedness is forced at the beginning of the algorithm; allows
 * user to specify start node and next node to visit if a visit call returns
 * without visiting all nodes
 */

int time;

int [] discovery;
int [] finish;
NodeSet unvisited;

function visit( Node v ) {
    print("visiting " + id(v));
    unvisited.remove(v);
    time = time + 1;
    discovery[id(v)] = time;
    beginStep();
    label(v, discovery[id(v)] );
    select(v);
    endStep();
    for_outgoing( v, e, w ) {
        print("neighbor = " + id(w));
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

    unvisited = getNodeSet();
    while ( ! unvisited.isEmpty() ) {
        print("top of loop");
        Node u
            = getNode("choose next node to visit",
                      unvisited,
                      "node already vsited"); 
        visit(u);
    }
}

//  [Last modified: 2016 09 29 at 20:15:36 GMT]
