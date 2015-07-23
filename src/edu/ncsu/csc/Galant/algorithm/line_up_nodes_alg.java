package edu.ncsu.csc.Galant.algorithm;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.graph.component.GraphState;
import edu.ncsu.csc.Galant.graph.component.Node;
public class line_up_nodes_alg extends Algorithm {
	public line_up_nodes_alg() {
		super();
		
		
	}
	// this is the old line_up_nodes
	
	
	@Override public void run() {
		
		GraphState gs = this.getGraph().getGraphState();
		synchronized(gs){
			try{
				gs.wait();
			}
			catch (InterruptedException e){
				e.printStackTrace(System.out);
			}
		}
		
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
			
			///gs.setInitializationComplete();
			
			
			/* This "initialization complete" can be replaced with a beginStep() and endStep() for the user after they've done everything that they consider initialization.
			 * A macro that does a beginstep followed by code followed by end step would probably be better
			 * GDR has a macro called wait() with a message argument; another idea of what to do here.
			 */
			int i = 0;
			for(Node n : getNodes()){
				n.setPosition(xPosition,yPosition);
				//if(i==1) System.out.printf("%d\n", 1/0); //Divide-by-zero done here to demonstrate that algorithm explodes while moving forward, not at beginning */
				if (i==1) Thread.sleep(15000); // wait for six seconds to demonstrate that the buttons are grayed out appropriately.
				xPosition += HORIZONTAL_GAP;
				i++;
				nodeArray[j] = n;
				j++;
			}
			if(gs.isLocked()) endStep();
		}
		catch (Exception e) {
			if (e instanceof GalantException) {
				GalantException ge = (GalantException) e;
				ge.report("");
				ge.display();
			}
			else e.printStackTrace(System.out);
			
		} 
		//this.gw.getGraphPanel().setAlgorithmComplete();

	}
}