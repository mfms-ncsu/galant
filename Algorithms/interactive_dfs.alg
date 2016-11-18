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
    step();
    set(v, "discovery", time);
    Integer discovered_v
        = getInteger(v, "discovery");
    label(v, time);
    select(v);
    for_outgoing( v, e, w ) {
        Integer finished_w
            = getInteger(w, "finish");
        step();
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
        step();
        uncolor(e);
    } // end, traversing adj list
    time = time + 1;
    set(v, "finish", time);
    step();
    mark(v);
    label(v, discovered_v
          + "/" + time);
}

algorithm {
    time = 0;
    setDirected(true);

    beginStep();
    showNodeLabels();
    showEdgeLabels();
    clearNodeLabels();
    clearEdgeLabels();

    unvisited = getNodeSet();
    while ( ! unvisited.isEmpty() ) {
        Node u
            = getNode("node to visit",
                      unvisited,
                      "node already visited");
        step();
        visit(u);
    }
}

//  [Last modified: 2016 11 18 at 17:18:20 GMT]
