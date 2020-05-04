// Author: Jimmy Long (jwlong)
// Last Modified: 3/22/18
//
// This algorithm implements the location/allocation heuristic as described
// in the "Big Picture" document distributed by Dr. Stallmann.

/**
 * This only works for unweighted edges - it uses BFS, not Dijkstra's algorithm
 */

import java.util.ArrayList;

Integer numMedians;
boolean running;

String[] colors;

// Get the euclidian distance between two nodes.
function double euclidian(Node v, Node w) {
    return getPosition(v).distance(getPosition(w));
}

// Get the shortest distance between two nodes.
function int distanceBtwn(Node v, Node w) {
	// Create the data structures we need.
	NodeQueue q = new NodeQueue();
	NodeSet visited = new NodeSet();

	Map<Node, Integer> distances = new HashMap<Node, Integer>();

	// Initialize Djikstra's Algorithm
	distances.put(v, 0);
	put(v, q);
	add(v, visited);

	// Start the algorithm.
	while (!empty(q)) {
		// Dequeue the first element.
		Node x = get(q);

		for (Node y : neighbors(x)) {
			if (!y.in(visited)) {
				distances.put(y, distances.get(x) + 1);
				put(y, q);
				add(y, visited);

				if (y.equals(w)) {
					return distances.get(y);
				}
			}
		}
	}
	
	return distances.get(w);
}

algorithm {
	// Set up the colors array, since we couldn't do that globally.
	colors = new String[] { RED, BLUE, GREEN, YELLOW, MAGENTA, CYAN, TEAL, VIOLET, 
				ORANGE, GRAY, BLACK, WHITE};
	//colors = new String[] {"#FF0000", "#00FF00", "#0000FF"};

	// Figure out the value of p, and then set up an array to keep
	// track of all the medians we have found.
	numMedians = getInteger("How many medians?");
	Node[] medians = new Node[numMedians];
	
	display("Choosing starting nodes.");
	// Let the user select the starting medians.
	for (int i = 0; i < medians.length; i++) {
		medians[i] = getNode("Select a node to be one of the starting medians.");

		// Color that median
		medians[i].setColor(colors[i]);
	}
	
	// Allocate each node to its nearest median.
	beginStep();
	
	// Create the sets of nodes, one for each median.
	// Each ArrayList will contain the nodes for which the
	// median located in medians[i] is the nearest median.
	ArrayList<Node>[] sets = new ArrayList[numMedians];
	
	// This will let us start the algorithm.
	running = true;

	endStep();

	while (running) {

		display("Allocating each node to it's nearest median.");
		beginStep();

		// Reset the sets.
		for (int i = 0; i < sets.length; i++) {
			sets[i] = new ArrayList<Node>();
		}

		// Process each node.
		for_nodes(v) {
			// Keep track of the index of the closest median found so far.
			Integer closestMedianIndex = 0;
			boolean isMedian = false;

			// Compare against each median to find its nearest one.
			for (int i = 0; i < medians.length; i++) {
				// If it's marked, unmark it to prevent confusion.
				v.unmark();
				// If this node is a median, disregard it.
				if (v.equals(medians[i])) {
					// Mark the median so it stands out.
					v.mark();

					// Make sure we mark ourselves, as the closest median,
					// otherwise we'll get colored incorrectly later.
					closestMedianIndex = i;
					break;
				}
				
				// If v is closer to this median than the previously found closest,
				// update the index of the closest index.
				if (distanceBtwn(v, medians[i]) < distanceBtwn(v, medians[closestMedianIndex])) {
					closestMedianIndex = i;
				}
			}
			
			// Whether or not it is a median, make sure it gets colored.
			v.setColor(colors[closestMedianIndex]);	
			
			// Add the node to the proper set and change it's color.
			sets[closestMedianIndex].add(v);
			
			
		}
		
		endStep();

		display("Finding the one-median of each set.");
		beginStep();

		// Have the user pick the 1-median for each set
		Node[] oneMedians = new Node[numMedians];
		// for (int i = 0; i < oneMedians.length; i++) {
		// 	oneMedians[i] = getNode("Select the one-median for the set with color: " + colors[i]);
		// }

		// Brute-force the one medians
		for (int i = 0; i < sets.length; i++) {
			// Since we are just brute-forcing the one medians,
			// we will need to store the total service distance
			// for each node in the set.
			int minDistance = Integer.MAX_VALUE;
			int oneMedianIndex = 0;

			for (int j = 0; j < sets[i].size(); j++) {
				int thisDistance = 0;
				for (int k = 0; k < sets[i].size(); k++) {
					thisDistance += distanceBtwn(sets[i].get(j), sets[i].get(k));
				}

				if (thisDistance < minDistance) {
					oneMedianIndex = j;
					minDistance = thisDistance;
				}
			}

			medians[i].unmark();
			oneMedians[i] = sets[i].get(oneMedianIndex);
			oneMedians[i].mark();
		}

		endStep();
		
		// Let's see if we're finished. (All the one medians are the same as the arbitrarily
		// chosen nodes.

		display("Checking to see if the solution has been found.");		
		beginStep();

		running = false;
		for (int i = 0; i < medians.length; i++) {
			if (!medians[i].equals(oneMedians[i])) {
				running = true;
				break;
			}
		}

		// If we are about to do another iteration, we need to update the medians.
		if (running) {
			for (int i = 0; i < medians.length; i++) {
				medians[i] = oneMedians[i];
			}
		}	

		endStep();
	}
	
	display("Solution found.");
		
}

//  [Last modified: 2018 05 23 at 17:13:16 GMT]
