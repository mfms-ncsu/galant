/**
 * Quadratic implementation of Hamalainen's two-median algorithm
 */

Integer total_weight;     // total weight of all vertices in (sub)-tree --
                          // used when calculating a 1-median
Node parent[];            // parent of a node, direction with respect to 1-median
Integer deg1[];           // degree during 1-median algorithm
Integer deg2[];           // degree during 2-median algorithm
Integer wt[];             // current weight during 1-median algorithm
NodeQueue leaves;         // current leaves during 1-median algorithm
NodePriorityQueue leaf_pq;      // priority queue in post-order edge-deletion
                                // computation in stage two

// the following are global for convenience, so they don't have to be
// returned from the function that computes them
Node one_median;
Node first_median;
Node second_median;
// the following are global variables that keep track of information about
// the minimum cut edge
Integer min_total_cost;
Edge min_cut_edge;
Node min_first_median;
Node min_second_median;
// the maximum "reduction" as defined by the conjecture in the paper
Integer max_reduction;

/**
 * POST: deg1[id(v)] = degree(v) for all vertices v; leaves contains all
 * degree one vertices
 */
function initialize_degrees_and_weights() {
  // in case no edge weight have been set - default weight is 1
  for_edges ( e ) {
    if ( hasNoWeight(e) || weight(e) == 0 ) {
      setWeight(e, 1);
    }
  }
  total_weight = 0;
  deg1 = new Integer[nodeIds()];
  leaves = new NodeQueue();
  wt = new Integer[nodeIds()];
  for_nodes(v) {
    deg1[id(v)] = degree(v);
    if ( deg1[id(v)] == 1 ) put(v, leaves); 
    wt[id(v)] = integer(weight(v));
    total_weight = total_weight + wt[id(v)];
  }
}

function Node compute_one_median() {
  Node median = null;
  Boolean done = false;
  // exit the following loop if either
  //  - only one node remains (it will also be the sole leaf), or
  //  - a leaf has weight more than half of the total
  while ( true ) {
    if ( empty(leaves) ) {
      error("Graph is not a tree.");
    }
    // merge a leaf x with its neighbor y and let the new weight of y be
    // the sum of the two weights
    Node x = get(leaves);
    if ( empty(leaves) || wt[id(x)] >= total_weight / 2 ) {
      median = x; break;
    }
    Node y = first(unmarkedNeighbors(x));
    deg1[id(y)] = deg1[id(y)] - 1;
    wt[id(y)] = wt[id(x)] + wt[id(y)];
    if ( deg1[id(y)] == 1 ) put(y, leaves);
    mark(x);
  }
  return median;
}

/**
 * Side effects:
 *  - the label of node is set to total weight of the subtree
 *  - sets the parent of each adjacent node v (except parent of root) to be the
 *    root for the recursive call, by setting parent[id(v)]
 * @return the weight of the subtree rooted at root
 */
function Integer recursive_weight(Node node) {
  Integer weight = integer(weight(node));
  for_adjacent(node, e, child) {
    if ( child != parent[id(node)] ) {
      parent[id(child)] = node; 
      Integer subtree_weight = recursive_weight(child);
      weight = weight + subtree_weight;
    }
  }
  label(node, weight);
  return weight;
}

/**
 * sets the label (for display) and "subtree_weight" of each node incident on
 * the median to the total weight of the corresponding subtree; setup for
 * 2-median computation
 */
function compute_subtree_weights(Node root) {
  for_adjacent(root, e, subtree_root) {
    parent[id(subtree_root)] = root;
    Integer subtree_weight = recursive_weight(subtree_root);
    set(subtree_root, "subtree_weight", subtree_weight);
    label(subtree_root, getInteger(subtree_root, "subtree_weight"));
  }
}

/**
 * visits nodes reachable from the root (one edge has been blocked at this
 * point) and sets the appropriate array values, queueing up leaves in the
 * process, as with the initialization for the one_median algorithm
 */
function dfs(Node v) {
  mark(v);
  deg1[id(v)] = degree(v);
  if ( deg1[id(v)] <= 1 ) put(v, leaves);
  wt[id(v)] = integer(weight(v));
  total_weight = total_weight + wt[id(v)];
  for_adjacent(v, e, w) {
    if ( ! marked(w) ) {
      dfs(w);
    }
  }
}

/**
 * Performs initialization for the one_median algorithm by visiting only the
 * nodes reachable from the root without traversing the edge.
 */
function init_with_dfs(Node root, Node other) {
  leaves = new NodeQueue();
  total_weight = 0;
  deg1 = new Integer[nodeIds()];
  wt = new Integer[nodeIds()];
  clearMarks();
  mark(other);
  dfs(root);
  deg1[id(root)] = deg1[id(root)] - 1;
  if ( deg1[id(root)] == 1 ) put(root, leaves);
}

/**
 * @return the median of the subtree rooted at root; pretend that the edge
 * between root and other is missing
 */
function Node side_median(Node root, Node other) {
  init_with_dfs(root, other);
  clearMarks();
  mark(other);
  return compute_one_median();
}

/**
 * Computes the weight of each node times its distance from the median. The
 * breadth first search does not traverse the forbidden edge.
 *
 * @return total cost of the tree with respect to the median
 */
function Integer compute_cost(Node median, Edge forbidden_edge) {
  clearMarks();
  Integer total_cost = 0;
  NodeQueue pathFound = new NodeQueue();
  Integer distance[] = new Integer[nodeIds()];
  mark(median);
  distance[id(median)] = 0;
  put(median, pathFound);
  while ( ! empty(pathFound) ) {
    Node v = get(pathFound);
    mark(v);
    Integer cost = distance[id(v)] * integer(weight(v));
    total_cost = total_cost + cost;
    for_adjacent(v, e, w) {
      if ( e != forbidden_edge && ! marked(w) ) {
        distance[id(w)] = distance[id(v)] + integer(weight(e));
        put(w, pathFound);
      }
    }
  }
  return total_cost;
}

/**
 * cleans up the display after compute_two_medians
 */
function cleanup(Edge edge) {
  uncolor(first_median);
  uncolor(second_median);
  color(one_median, BLUE);
  clearMarks();
  unhighlight(edge);
  display("");
}

/**
 * does the dfs required to correctly perform initialize_priority_queue; this
 * is in context of the 2-median algorithm - hence the use of deg2, which
 * ends up being the degree of each vertex
 * Side effect:
 *    sets the "serve" attribute of each edge e = (v,w) to a child w
 *    one-median to the cost assuming v serves as 'median' for T_u 
 * @return the sum of the "serve" attributes for all the edges to children of v
 */
function Integer pq_dfs(Node v) {
  print("-> pq_dfs: ", id(v));
  Integer sum_of_serves = 0;
  deg2[id(v)] = degree(v);
  set(v, "leaf_weight", integer(weight(v)));
  if ( deg2[id(v)] == 1 ) {
    label(v, getInteger(v, "leaf_weight"));
    add(v, leaf_pq);
    color(v, BLACK);
  }
  for_adjacent(v, e, w) {
    if ( w != parent[id(v)] ) {
      // start with the service costs of the subtrees
      Integer serve = pq_dfs(w);
      // add the cost of v serving w and its subtrees
      serve += integer(weight(e) * wt[id(w)]);
      set(e, "serve", serve);
      print("  serve for ", e, " is ", serve);
      sum_of_serves += serve;
    }
  }
  print("<- pq_dfs: ", sum_of_serves);
  return sum_of_serves;
}

/**
 * POST: deg[id(v)] = degree(v) for all vertices v in the subtree rooted at
 * root; leaf_pq contains all degree one vertices with priority =
 * getInteger(v, "leaf_weight")
 * ASSUME: parent[v] = parent of v, using orientation with 1-median as root
 * Side effect:
 *    sets the "serve" attribute of the edge from the root to the one-median
 */
function initialize_priority_queue(Node root) {
  Integer serve = pq_dfs(root);
  Edge root_edge = edge(root, one_median);
  serve += integer(weight(root_edge) * wt[id(root)]);
  set(root_edge, "serve", serve);
}

/**
 * Does brute-force computation of one medians on either side of the edge
 * between node and parent_node; updates the optimal choice of two medians
 * if appropriate.
 */
function compute_two_medians(Node node, Node parent_node) {
  Edge edge = edge(node, parent_node);
  highlight(edge);
  first_median = side_median(node, parent_node);
  if ( first_median == one_median ) color(first_median, TEAL);
  else color(first_median, GREEN);
  Integer first_cost = compute_cost(first_median, edge);
  display("first median is " + id(first_median)
          + ", cost = " + first_cost);
  Integer reduction = getInteger(edge, "serve") - first_cost;
  String reduction_indicator = "";
  if ( reduction > max_reduction && parent_node != one_median ) {
    max_reduction = reduction;
    reduction_indicator = " +++";
  }

  second_median = side_median(parent_node, node);
  if ( second_median == one_median ) color(second_median, VIOLET);
  else color(second_median, RED);
  Integer second_cost = compute_cost(second_median, edge);
  Integer total_cost = first_cost + second_cost;
  String update_indicator = "";
  if ( total_cost < min_total_cost ) {
    min_total_cost = total_cost;
    min_cut_edge = edge;
    min_first_median = first_median;
    min_second_median = second_median;
    update_indicator = " *** new";
  }
  display("first median is ", id(first_median),
          ", cost = ", first_cost,
          ", second median is ", id(second_median),
          ", cost = ", second_cost,
          ", total = ", total_cost,
          " *** min so far = ", min_total_cost, update_indicator,
          " reduction = ", reduction, reduction_indicator);
  step();
  cleanup(edge);
}

/**
 * The meat of the algorithm. here the cut edges are explored in post-order
 * using the leaf_pq to decide which edge to consider next; priority of each
 * node on leaf_pq is its "leaf_weight" and nodes are considered smallest
 * weight first; when a node is considered it is currently a leaf; the edge
 * to its parent is considered as a cut edge and the cost of the medians is
 * computed; finally, the cut edge is contracted and, if the parent becomes a
 * leaf, it is added to the queue with a weight corresponding to its own
 * weight plus that of the leaf.
 * The computation of the two medians is brute force rather than using
 * incremental updates.
 */
function explore_cut_edges() {
  while ( ! empty(leaf_pq) ) {
    Node current = removeBest(leaf_pq);
    if ( current != one_median ) {
      Node parent_node = parent[id(current)];
      compute_two_medians(current, parent_node);
      if ( parent_node != one_median ) {
        Integer new_weight = getInteger(parent_node, "leaf_weight")
          + getInteger(current, "leaf_weight");
        set(parent_node, "leaf_weight", new_weight);
        label(parent_node, new_weight);
        label(current, "X");
        deg2[id(parent_node)] = deg2[id(parent_node)] - 1;
        if ( deg2[id(parent_node)] == 1 ) {
          add(parent_node, leaf_pq);
          color(parent_node, BLACK);
        }
      } // parent_node is not one_median
      uncolor(current);
    } // current is not one_median
  } // while leaf_pq not empty
}

algorithm {
  parent = new Node[nodeIds()];
  min_total_cost = MAX_INTEGER;
  min_cut_edge = null;
  min_first_median = null;
  min_second_median = null;
  max_reduction = 0;
  beginStep();
  initialize_degrees_and_weights();
  one_median = compute_one_median();
  color(one_median, BLUE);
  Integer one_median_cost = compute_cost(one_median, null);
  display("cost of one-median = ", one_median_cost);
  clearMarks();
  step();
  compute_subtree_weights(one_median);
  step();
  // let v_1 be the root of the maximum weight subtree
  NodeList subtree_roots = neighbors(one_median);
  Node v_1 = null;
  Integer largest_weight = 0;
  for ( Node root : subtree_roots ) {
    Integer this_weight = getInteger(root, "subtree_weight"); 
    if ( this_weight > largest_weight ) {
      v_1 = root;
      largest_weight = this_weight;
    }
  }
  clearNodeLabels();
  // first explore cut edges in the tree rooted at v_1
  step();
  leaf_pq = new NodePriorityQueue(getIntegerComparator("leaf_weight"));
  deg2 = new Integer[nodeIds()];
  initialize_priority_queue(v_1);
  clearMarks();
  step();
  explore_cut_edges();
  // then do it in the union of the other subtrees
  leaf_pq = new NodePriorityQueue(getIntegerComparator("leaf_weight"));
  deg2 = new Integer[nodeIds()];
  step();
  for ( Node root : subtree_roots ) {
    if ( root != v_1 ) {
      initialize_priority_queue(root);
    }
  }
  clearMarks();
  explore_cut_edges();
  step();

  display("Min cost solution: first median is " + id(min_first_median)
          + ", second median is " + id(min_second_median)
          + ", total cost = " + min_total_cost);
  highlight(min_cut_edge);
  if ( min_first_median == one_median )
    color(min_first_median, TEAL);
  else
    color(min_first_median, GREEN);
  if ( min_second_median == one_median )
    color(min_second_median, VIOLET);
  else
    color(min_second_median, RED);

  // let y have child x and parent z on P and let C_1, C_2 be the
  // costs of the two subtrees if edge xy is cut and m_1 is the median on
  // the side with x, m_2 on the side with y
  // let c[y] = sum of cost[v] for all children v of y except x

  // to update the cost if we move the cut to yz and keep the medians the
  // same (assuming y != m_2);
  //   C_1 = C_1 + c[y] + weight(y) + (d(m_1,y) + 1) * (wt[y] - wt[x])
  //   C_2 = C_2 - c[y] - weight(y) - (d(m_2,y) + 1) * (wt[y] - wt[x])
  // If distances are nonuniform, there is a d(m_i,y) factor in front of
  // weight(y) and the "+ 1's" are replaced with d(y,t), where t is the
  // root of a subtree with respect to y.
    
  // to update cost, moving from m_1 to m', the next node on the path,
  // while keeping cut edge the same, i.e., yz
  //   C_1 = C_1 + wt[m_1] + weight(m_1) - weight(m') - (wt[y] - wt[m_1])
  // and to move m_2 to m" (there may be several choices)
  //   C_2 = C_2 - wt[m"] - weight(m") + weight(m_2) + wt[m_2] - wt[m"]

  // The wt[x] and c[x] values are computed bottom up as follows. Suppose x
  // has children y_1,...,y_k, whose values have been computed. Then
  //    wt[x] = weight(x) + sum_i wt[y_i]
  //    c[x] = sum_i c[y_i] + sum_i w[y_i]
  // If edges have nonuniform distances, the terms in the last sum are
  // multiplied by d(x,y_i)
}

//  [Last modified: 2017 05 19 at 19:27:32 GMT]
