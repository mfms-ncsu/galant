import edu.ncsu.csc.Galant.local.LayeredGraph;
LayeredGraph layeredGraph;
algorithm {
    movesNodes();
    showEdgeWeights();

    layeredGraph
        = new LayeredGraph(getGraph());
    int max_nonverticality = 0;
    for_edges(e) {
        layeredGraph.setNonverticality(e);
        if ( layeredGraph.nonverticality(e) > max_nonverticality) max_nonverticality = layeredGraph.nonverticality(e);
    }
    display("total nonverticality = ", layeredGraph.nonverticality(),
            ", max nonverticality = ", max_nonverticality);
}
