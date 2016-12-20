/** Scaffoling for a variety of tests */

/* to test weights */

Node node0 = getNodeById(0);
Node node1 = getNodeById(1);
Node node2 = getNodeById(2);
Node node3 = getNodeById(3);
Node node4 = getNodeById(4);

Edge edge0 = getEdgeById(0);
Edge edge1 = getEdgeById(1);

node0.setWeight(1/3.0);
node1.setWeight(1.0/0);
node2.setWeight(1.0/1);
node3.setWeight(-1.0/0);
node4.setWeight(0.0/0);

/* to test message writing */
graph.writeMessage("Done setting weights");

beginStep();
node0.setColor("0000ff");
edge0.setColor("ff0000");

node4.setWeight(0.0/0);
graph.writeMessage("Done coloring of node 0 and edge 0");
endStep();

graph.writeMessage("checking node 0");
//beginStep();
node0.clearWeight();
if ( ! node4.hasWeight() )
    graph.writeMessage( "Node 4 has no weight" );
else
    graph.writeMessage( "Node 4 has weight" );
//endStep();
beginStep();
graph.writeMessage("checking node 1");
endStep();
beginStep();
if ( node1.hasWeight() )
    graph.writeMessage( "Node 1 has weight" );
else
    graph.writeMessage( "Node 1 has no weight" );
endStep();

node0.setLabel("X");
node1.setLabel("Y");
node0.clearLabel();
beginStep();
if ( node0.hasLabel() )
    graph.writeMessage( "Node 0 has label" );
else
    graph.writeMessage( "Node 0 has no label" );
endStep();
if ( node1.hasLabel() )
    graph.writeMessage( "Node 1 has label" );
else
    graph.writeMessage( "Node 1 has no label" );

edge0.setWeight(1/3.0);
edge1.setWeight(1.0/0);
edge0.clearWeight();
if ( edge0.hasWeight() )
    graph.writeMessage( "Edge 0 has weight" );
else
    graph.writeMessage( "Edge 0 has no weight" );
if ( edge1.hasWeight() )
    graph.writeMessage( "Edge 1 has weight" );
else
    graph.writeMessage( "Edge 1 has no weight" );

edge0.setLabel("X");
edge1.setLabel("Y");
edge0.clearLabel();
if ( edge0.hasLabel() )
    graph.writeMessage( "Edge 0 has label" );
else
    graph.writeMessage( "Edge 0 has no label" );
if ( edge1.hasLabel() )
    graph.writeMessage( "Edge 1 has label" );
else
    graph.writeMessage( "Edge 1 has no label" );


/* to test traversal of incident edges */

Node x = getNodeById(0);
for_outgoing( x, e, y ) {
    beginStep();
    e.setSelected( true );
    y.setSelected( true );
    endStep();
}

/* to test message writing (again) */
graph.writeMessage("Done selecting outgoing edges for node " + x.getId() );

beginStep();
for_nodes( v ) {
    v.setSelected( false );
}

for_edges( e ) {
    e.setSelected( false );
}
endStep();

for_incoming ( x, e, y ) {
    beginStep();
    e.setSelected( true );
    y.setSelected( true );
    endStep();
}

beginStep();
for_nodes( v ) {
    v.setSelected( false );
}

for_edges( e ) {
    e.setSelected( false );
}
endStep();

for_adjacent ( x, e, y ) {
    beginStep();
    e.setSelected( true );
    y.setSelected( true );
    endStep();
}

//  [Last modified: 2015 04 20 at 12:16:09 GMT]
