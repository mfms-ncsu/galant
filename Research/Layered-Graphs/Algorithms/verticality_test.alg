LayeredGraph layeredGraph;
algorithm {
    movesNodes();
    showEdgeWeights();

    layeredGraph
        = new LayeredGraph( getGraph() );
    for_edges(e) {
        layeredGraph.setNonverticality(e);
    }
    display("total nonverticality = ", layeredGraph.nonverticality());
}