/**
 * strip_attributes.alg - strips out unneccessary attributes from graphs
 * created before the cleaner handling of attributes was put in place
 */

algorithm {
    boolean no_weights = true;
    for_nodes( node ) {
        if ( label(node).equals("") ) clearLabel(node);
        if ( weight(node) != 0 ) no_weights = false;
        uncolor(node);
        clear(node, "layer");
        clear(node, "positionInLayer");
    }
    if ( no_weights ) {
        for_nodes( node ) clearWeight(node);
    }

    no_weights = true;
    for_edges( edge ) {
        clear(edge, "id");
        if ( label(edge).equals("") ) clearLabel(edge);
        if ( weight(edge) != 0 ) no_weights = false;
        uncolor(edge);
    }
    if ( no_weights ) {
        for_edges( edge ) clearWeight(edge);
    }

}

//  [Last modified: 2016 11 04 at 12:57:06 GMT]
