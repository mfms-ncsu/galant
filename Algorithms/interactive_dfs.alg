/** 
 * interactive_dfs.alg - implements depth-first search of a directed graph
 * only; directedness is forced at the beginning of the algorithm; allows
 * user to specify start node and next node to visit if a visit call returns
 * without visiting all nodes
 */

Integer time;
NodeSet unvisited;

function visit( Node v ) {
    unvisited.remove(v);
    time = time + 1;
    beginStep();
    set(v, "discovery", time);
    Integer discovered_v
        = getInteger(v, "discovery");
    label(v, time);
    select(v);
    endStep();
    for_outgoing( v, e, w ) {
        Integer finished_w
            = getInteger(w, "finish");
        beginStep();
        color(e, BLUE);
        if ( ! selected(w) ) {
            highlight(e);
            visit(w);
        }
        else if ( finished_w == null ) {
            /* ancestor */
            label(e, "B");
        }
        else if ( finished_w
                  > discovered_v ) {
            /* descendant */
            label(e, "F");
        }
        else {
            label(e, "C");
        }
        endStep();
        beginStep();
        uncolor(e);
        endStep();
    } // end, traversing adj list
    time = time + 1;
    set(v, "finish", time);
    beginStep();
    mark(v);
    label(v, discovered_v
          + "/" + time);
    endStep();
}

algorithm {
    time = 0;
    setDirected(true);

    beginStep();
    hideEdgeWeights();
    hideNodeWeights();
    clearNodeLabels();
    clearEdgeLabels();
    endStep();

    unvisited = getNodeSet();
    while ( ! unvisited.isEmpty() ) {
        Node u
            = getNode("node to visit",
                      unvisited,
                      "node already visited"); 
        visit(u);
    }
}

//  [Last modified: 2016 10 03 at 18:08:13 GMT]
