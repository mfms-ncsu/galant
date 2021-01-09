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
    set(v, "discovery", time);
    Integer discovered_v
        = getInteger(v, "discovery");
    label(v, time);
    select(v);
    for_outgoing( v, e, w ) {
        step();
        if ( ! selected(w) ) {
            color(e, RED);
            visit(w);
        }
        else if ( ! hasInteger(w, "finish") ) {
            /* not finished => ancestor */
            label(e, "B"); color(e, GREEN);
        }
        else {
            Integer finished_w = getInteger(w, "finish");
            if ( finished_w > discovered_v ) {
                /* finished after v was discovered => descendant */
                label(e, "F"); color(e, BROWN);
            }
            else {
                label(e, "C"); color(e, BLUE);
            }
        }
        step();
    } // end, traversing adj list
    time = time + 1;
    set(v, "finish", time);
    step();
    mark(v);
    label(v, discovered_v + "/" + time);
}

algorithm {
    time = 0;
    setDirected(true);

    beginStep();
    showNodeLabels();
    showEdgeLabels();
    clearNodeLabels();
    clearEdgeLabels();
    endStep();

    unvisited = getNodeSet();
    while ( ! unvisited.isEmpty() ) {
        // ask user to specify a node from set set unvisited
        // tell user "node already visited" if not in the set
        Node u = getNode("node to visit", unvisited, "node already visited");
        step();
        visit(u);
    }
}

//  [Last modified: 2021 01 09 at 18:01:15 GMT]
