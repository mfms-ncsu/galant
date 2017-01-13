/**
 * Makes it possible to define comparators with embedded information about
 * attributes and direction of comparison. Convenient for priority queues
 * that use attributes other than weight
 */

package edu.ncsu.csc.Galant.graph.container;

import java.util.Comparator;

import edu.ncsu.csc.Galant.graph.component.GraphElement;

public class GraphElementComparator implements Comparator<GraphElement> {
  public String attribute = "weight";
  // true if sorting is descending or heap is max-heap
  public boolean reverse = false;
  public GraphElementComparator(String attribute, boolean reverse) {
    this.attribute = attribute;
    this.reverse = reverse;
  }
  public int compare(GraphElement ge1, GraphElement ge2) {
    Double value_1 = ge1.getWeight();
    Double value_2 = ge2.getWeight();
    if ( value_1 > value_2 ) return 1;
    else if ( value_2 > value_1 ) return -1;
    else return 0;
  }
}

//  [Last modified: 2017 01 13 at 20:00:31 GMT]
