/** 
 * Implementation of breadth-first search. If the graph is undirected, the
 * edges are regarded as being directed both ways.
 */

algorithm {
    showNodeWeights();
    showNodeLabels();

    NodeQueue queue;
    int sequence_number = 0;
    queue = new NodeQueue();
    beginStep();
    for_nodes( v ) {
        setWeight(v, INFINITY);
    }

    Node start = getStartNode();
    setWeight(start, 0);
    highlight(start);
    put(start, queue);
    label(start, "#" + sequence_number);
    sequence_number++;
    endStep();

    while ( ! empty(queue) ) {
        Node next = get(queue);
        beginStep();
        mark(next);

        for_outgoing( next, e, other ) {
            if ( ! highlighted(other) ) {
                highlight(e);
                highlight(other);
                Double distance = weight(next) + 1;
                setWeight(other, distance);
                put(other, queue);
                label(other, "#" + sequence_number);
                sequence_number++;
            }
            else color(e, BLUE);
        }
        endStep();
    }

    beginStep();
    for_edges( e ) {
      if ( ! highlighted(e) ) hide(e);
    }
    endStep();
  }
