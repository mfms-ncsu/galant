/**
 * infinite_loop_test.alg - a deliberate infinite loop to see if timeout works
 */

algorithm {
    Node start = getStartNode();
    mark(start);
    beginStep();
    while ( true ) {
        if ( selected(start) ) break;
    }
    endStep();
}

//  [Last modified: 2017 01 24 at 17:45:04 GMT]
