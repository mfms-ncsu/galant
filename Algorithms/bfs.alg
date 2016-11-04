/** 
 * Implementation of breadth-first search. Undirected edges are regarded as
 * being directed both ways.
 */

NodeQueue queue;

algorithm {
    queue = new NodeQueue();
    beginStep();
    for_nodes( v ) {
        setWeight(v, INFINITY);
    }

    Node start = getStartNode();
    setWeight(start, 0);
    highlight(start);
    queue.enqueue(start);
    endStep();

    while ( ! queue.isEmpty() ) {
        Node n = queue.dequeue();
        print("current node = " + n);
        highlight(n);

        for_outgoing( n, e, head ) {
            if ( ! highlighted(head) ) {
                beginStep();
                highlight(e);
                highlight(head);
                Double distance = weight(n) + 1;
                setWeight(head, distance);
                queue.enqueue(head);
                endStep();
            }
        }
    }
}

//  [Last modified: 2016 11 04 at 13:28:27 GMT]
