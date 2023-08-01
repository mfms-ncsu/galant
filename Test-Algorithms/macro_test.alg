/**
 * macro_test.alg - test of various (parameterized) macros
 */

function Node processNodes(Node v, int n) {
    Node [] nodes= new Node[n];
    int i = 0;
    for_adjacent(v, e, w) {
        if ( i >= n ) break;
        highlight(e);
        nodes[i] = w;
        i++;
    }
    return nodes[0];
}

algorithm {
    for_nodes( v ) {
        print(v);
    }
    for_nodes( v ) print(v);
    Node w = processNodes(startNode(), 3);
    highlight(w);
}

//  [Last modified: 2016 12 30 at 22:17:05 GMT]
