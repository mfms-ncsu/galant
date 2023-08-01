/** attribute_test.alg - tests the setting and displaying of attributes using
 * both object-oriented and procedural syntax; requires that the graph have
 * at least five nodes and at least two edges */

algorithm {
  int NODES_TO_TEST = 5;
  int EDGES_TO_TEST = 2;

  Node [] node = new Node[NODES_TO_TEST];
  Edge [] edge = new Edge[EDGES_TO_TEST];
  
  // choosing nodes and edges to test
  int i = 0;
  for_nodes ( v ) {
    if ( i >= NODES_TO_TEST ) break;
    node[i] = v;
  }

  i = 0;
  for_edges ( e ) {
    if ( i >= EDGES_TO_TEST ) break;
    edge[i] = e;
  }

  // testing weights
  node[0].setWeight(1/3.0);
  node[1].setWeight(1.0/0);
  node[2].setWeight(1.0/1);
  node[3].setWeight(-1.0/0);
  node[4].setWeight(0.0/0);

  // testing message writing
  graph.writeMessage("Done setting weights");

  // testing colors
  beginStep();
  node[0].setColor("0000ff");
  edge[0].setColor("ff0000");

  graph.writeMessage("Done coloring of node " + id(node[0])
                   + " and edge " + id(edge[0]));
  endStep();

  // check clearing of weight
  graph.writeMessage("clearing weight of node " + id(node[0]));
  node[0].clearWeight();
  if ( node[4].hasWeight() )
    graph.writeMessage( "Node " + id(node[4]) + " has weight" );
  else
    graph.writeMessage( "Node " + id(node[4]) + " has no weight" );
  if ( node[0].hasWeight() )
    graph.writeMessage( "Node " + id(node[0]) + " has weight" );
  else
    graph.writeMessage( "Node " + id(node[0]) + " has no weight" );

  // testing labels
  node[0].setLabel("X");
  node[1].setLabel("Y");
  node[0].clearLabel();
  beginStep();
  if ( node[0].hasLabel() )
    graph.writeMessage( "Node " + id(node[0]) + " has label" );
  else
    graph.writeMessage( "Node " + id(node[0]) + " has no label" );
  if ( node[1].hasLabel() )
    graph.writeMessage( "Node " + id(node[1]) + " has label" );
  else
    graph.writeMessage( "Node " + id(node[1]) + " has no label" );

  // testing edge weights
  edge[0].setWeight(1/3.0);
  edge[1].setWeight(1.0/0);
  edge[0].clearWeight();
  if ( edge[0].hasWeight() )
    graph.writeMessage( "Edge " + id(edge[0]) + " has weight" );
  else
    graph.writeMessage( "Edge " + id(edge[0]) + " has no weight" );
  if ( edge[1].hasWeight() )
    graph.writeMessage( "Edge " + id(edge[1]) + " has weight" );
  else
    graph.writeMessage( "Edge " + id(edge[1]) + " has no weight" );

  // testing edge labels
  edge[0].setLabel("X");
  edge[1].setLabel("Y");
  edge[0].clearLabel();
  if ( edge[0].hasLabel() )
    graph.writeMessage( "Edge " + id(edge[0]) + " has label" );
  else
    graph.writeMessage( "Edge " + id(edge[0]) + " has no label" );
  if ( edge[1].hasLabel() )
    graph.writeMessage( "Edge " + id(edge[1]) + " has label" );
  else
    graph.writeMessage( "Edge " + id(edge[1]) + " has no label" );


  // testing traversal of incident edges
  for_outgoing( node[0], e, y ) {
    beginStep();
    e.setSelected( true );
    y.setSelected( true );
    endStep();
  }

  // testing message (using newer function)
  display("Done selecting outgoing edges for node " + node[0].getId() );

  // clearing the highlighting
  beginStep();
  for_nodes( v ) {
    v.setSelected( false );
  }

  for_edges( e ) {
    e.setSelected( false );
  }
  endStep();

  for_incoming ( node[0], e, y ) {
    beginStep();
    color(e, BLUE);
    color(y, BLUE);
    endStep();
  }

  for_adjacent ( node[0], e, y ) {
    beginStep();
    color(e, GREEN);
    color(y, GREEN);
    endStep();
  }
}

//  [Last modified: 2017 01 24 at 16:58:53 GMT]
