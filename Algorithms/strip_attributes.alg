/**
 * strip_attributes.alg - strips out unneccessary attributes from graphs
 * created before the cleaner handling of attributes was put in place; or
 * from ones where weights, labels and colors were created inadvertently
 * during editing.
 */

algorithm {
    display("start cleaning nodes");
    beginStep();
    boolean no_weights = true;
    for_nodes( node ) {
        if ( label(node) != null
             && label(node).equals("") ) clearLabel(node);
        if ( weight(node) != null && weight(node) != 0 ) no_weights = false;
        uncolor(node);
        if ( ! graph.isLayered() ) {
            clear(node, "layer");
            clear(node, "positionInLayer");
        }
    }
    if ( no_weights ) {
        for_nodes( node ) clearWeight(node);
    }
    endStep();

    display("done cleaning nodes, start with edges");
    beginStep();
    no_weights = true;
    for_edges( edge ) {
        clear(edge, "id");
        if ( label(edge) != null
             && label(edge).equals("") ) clearLabel(edge);
        if ( weight(edge) != null && weight(edge) != 0 ) no_weights = false;
        uncolor(edge);
    }
    if ( no_weights ) {
        for_edges( edge ) clearWeight(edge);
    }
    endStep();
    display("done cleaning edges");
}

//  [Last modified: 2016 12 14 at 17:46:41 GMT]
