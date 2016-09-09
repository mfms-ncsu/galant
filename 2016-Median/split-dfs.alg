/**
 * Systematically goes through all the edges (of a tree) and does two
 * depth-first searches, one from each endpoint. Each search assumes that the
 * relevant edge is missing. 
 */

function dfs(Node v) {
    mark(v);
    for_adjacent(v, e, w) {
        if ( ! marked(w) ) {
            dfs(w);
        }
    }
}

function split_dfs(Node root, Node otherSide) {
    beginStep();
    clearNodeMarks();
    endStep();
    mark(otherSide);
    beginStep();
    dfs(root);
    endStep();
}

algorithm {
    for_edges(e) {
        highlight(e);
        split_dfs(source(e), target(e));
        split_dfs(target(e), source(e));
        unhighlight(e);
    }
}

//  [Last modified: 2016 06 21 at 18:16:21 GMT]
