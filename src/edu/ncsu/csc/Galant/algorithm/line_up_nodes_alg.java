package edu.ncsu.csc.Galant.algorithm;
import java.util.*;
import edu.ncsu.csc.Galant.algorithm.Algorithm;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.algorithm.code.macro.Function;
import edu.ncsu.csc.Galant.algorithm.code.macro.Pair;
import edu.ncsu.csc.Galant.GalantException;
<<<<<<< HEAD
=======
import edu.ncsu.csc.Galant.graph.component.GraphState;
>>>>>>> threads
public class line_up_nodes_alg extends Algorithm {
	public line_up_nodes_alg() {
		super();
		System.out.println("Created new line_up_nodes_alg using hardcoded implementation.");
	}
	
<<<<<<< HEAD
=======
	
	
>>>>>>> threads
	@Override public void run() {
		System.out.println("Starting hardcoded class's run(); method");
		try {
			final int HORIZONTAL_GAP = 100;
			final int VERTICAL_GAP = 100;
			final int TOP_GAP = 2*VERTICAL_GAP;
			int arraySize = 0;
			for(Node n : getNodes()){
				arraySize++;
			}
			Node[] nodeArray = new Node[arraySize];
			int j = 0;
			int xPosition = HORIZONTAL_GAP;
			int yPosition = TOP_GAP;
<<<<<<< HEAD
=======
			GraphState.setInitializationComplete();
			
>>>>>>> threads
			for(Node n : getNodes()){
				n.setPosition(xPosition,yPosition);
				xPosition += HORIZONTAL_GAP;
				nodeArray[j] = n;
				j++;
			}
		}
		catch (Exception e) {
			if ( e instanceof GalantException ) {
				GalantException ge = (GalantException) e;
				ge.report("");
				ge.display();
			}
			else e.printStackTrace(System.out);
		} 
		System.out.println("Done with hardcoded class's run(); method");

	}
}