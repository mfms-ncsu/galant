package edu.ncsu.csc.Galant.algorithm.code.compiled;
import java.util.*;
import edu.ncsu.csc.Galant.graph.component.GraphState;
import edu.ncsu.csc.Galant.algorithm.Algorithm;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.algorithm.code.macro.Function;
import edu.ncsu.csc.Galant.algorithm.code.macro.Pair;
import edu.ncsu.csc.Galant.GalantException;
/**
 * Illustrates what currently goes wrong when compiler interacts with threads
 * code:

 GraphState gs = this.getGraph().getGraphState();
        synchronized(gs){
           try{gs.wait();
           }
           catch (InterruptedException e){e.printStackTrace(System.out);
           }
        }

 * should occur at the beginning of the run method and

   if(gs.isLocked()) endStep();
   this.gw.getGraphPanel().setAlgorithmComplete();

 * should occur at the end.
 *
 * Eventually, the specification of this start and end code should happen in
 * a distinct module/class that handles all thread synchronization; for now
 * we'll put it wherever it's least disruptive to do so. 
 */

public class dfs_new_alg extends Algorithm{
    @Override public void run()
    {
        GraphState gs = this.getGraph().getGraphState();
        synchronized(gs) {
            try{gs.wait();
            }
            catch (InterruptedException e) {e.printStackTrace(System.out);
            }
        }
        // (1) try { should not be here

        public void run()
        {
            // (2) code before (1) belongs here
                
            graph = getGraph();
            discovery = new int[ graph.numberOfNodes() ];
            finish = new int[ graph.numberOfNodes() ];
            setDirected( true );
            beginStep();
            for(Node u : getNodes()) {
                u.setLabel("");
            }
            for(Edge e : getEdges()) {
                e.setLabel("");
            }
            endStep();
            for(Node u : getNodes()) {
                if ( ! u.isSelected() ) {
                    visit( u );
                }
            }
        }

        if(gs.isLocked()) endStep();
        this.gw.getGraphPanel().setAlgorithmComplete();

        /**
         * The part below will eventually be reinserted when we handle more
         * general Galant exceptions.
         */
 
//     catch (Exception e)
//         {
//             if ( e instanceof GalantException ) {GalantException ge = (GalantException) e;
//                 ge.report("");
//                 ge.display();
//             }
//             else e.printStackTrace(System.out);
//         }

//  [Last modified: 2015 07 07 at 13:44:21 GMT]
