/**
 * A list of edges; inherited directly from ArrayList<Edge>.
 */

package edu.ncsu.csc.Galant.graph.datastructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import edu.ncsu.csc.Galant.graph.component.Edge;

public class EdgeList extends ArrayList<Edge> {
  public EdgeList() { super(); }
  public EdgeList(Collection<Edge> C) { super(C); }
}

//  [Last modified: 2017 01 14 at 22:42:22 GMT]
