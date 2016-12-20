algorithm {
    Node start = getStartNode();
    mark(start);
    beginStep();
    while ( true ) {
        if ( selected(start) ) break;
    }
    endStep();
}

//  [Last modified: 2016 11 26 at 14:10:31 GMT]
