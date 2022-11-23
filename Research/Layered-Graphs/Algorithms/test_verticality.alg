LayeredGraph layeredGraph;
algorithm {
    movesNodes();
    setDirected( true );

    layeredGraph
        = new LayeredGraph( getGraph() );
    for_edges(e) {
        layeredGraph.setNonverticality(e);
    }
    display("total nonverticality = ", layeredGraph.nonverticality());
}