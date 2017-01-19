/**
 * euclidian.alg
 *  Implementation of Dijkstra's shortest path algorithm. Interprets
 *  undirected edges as going in both directions if run in undirected
 *  mode. Unlike dijkstra.alg, the edge weights are set initially by the
 *  algorithm, based on Euclidian distances between nodes.
 */

/** Required: node and edge weights visible */

import java.util.PriorityQueue;

double INFINITY = Double.POSITIVE_INFINITY;

function double euclidian(Node v, Node w) {
    return v.getPosition().distance(w.getPosition());
}

function init_edge_costs() {
    for_edges(edge) {
        double distance = euclidian(source(edge), target(edge));
        setWeight(edge, distance);
    }
}

algorithm {
    NodePriorityQueue pq = new NodePriorityQueue();
    Edge [] chosenEdge = new Edge[nodeIds()]; 

    init_edge_costs();

    beginStep();
    for_nodes(node) {
        node.setWeight(INFINITY);
        pq.add(node);
    }
    Node v = getStartNode();
    v.setSelected( true );
    v.setWeight(0);
    endStep();

    while ( ! pq.isEmpty() ) {
        v = pq.removeMin();
        v.setVisited( true );
        v.setSelected( false );


        for_outgoing ( v, e, w ) {
            if ( ! w.isVisited() )  {
                if ( ! w.isSelected() ) w.setSelected( true );
                double distance = v.getWeight() + e.getWeight();
                if ( distance < w.getWeight() ) {
                    beginStep();
                    e.setSelected( true );
                    Edge previous_chosen = chosenEdge[id(w)];
                    if (previous_chosen != null )
                        previous_chosen.setSelected( false );
                    pq.decreaseKey( w, distance );
                    chosenEdge[id(w)] = e;
                    endStep();
                }
            } // end, neighbor not visited (not in tree); do nothing if node is
            // already in tree
        } // end, adjacency list traversal
    } // stop when priority queue is empty
}

//  [Last modified: 2017 01 19 at 22:01:07 GMT]
