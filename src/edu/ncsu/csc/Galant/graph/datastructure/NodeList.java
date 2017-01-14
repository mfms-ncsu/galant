/**
 * A list of nodes; inherited directly from ArrayList<Node>.
 */

package edu.ncsu.csc.Galant.graph.datastructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import edu.ncsu.csc.Galant.graph.component.Node;

public class NodeList extends ArrayList<Node> {
  public NodeList() { super(); }
  public NodeList(Collection<Node> C) { super(C); }
}

//  [Last modified: 2017 01 14 at 22:38:16 GMT]
