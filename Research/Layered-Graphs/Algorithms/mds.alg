/**
 * mds.alg
 * An animation of the mds preprocessor for crossing minimization.
 *
 * $Id: mds.alg 104 2015-03-27 16:23:25Z mfms $
 */

import edu.ncsu.csc.Galant.local.LayeredGraph;

beginStep();
final LayeredGraph layeredGraph
    = new LayeredGraph( getGraph() );
endStep();

/**
 * writes a message about current number of crossings, along with context information
 */
function displayMessage( String sortingDirection ) {
    int crossings = layeredGraph.numberOfCrossings();
    graph.writeMessage( "sorting with " + sortingDirection + " in middle finished, crossings = " + crossings );
}

setDirected( true );

/**
 * Now the mds algorithm. Illustrate for both cases: largest degree in middle
 * and smallest degree in middle
 */

beginStep();
layeredGraph.middleDegreeSort( true );
layeredGraph.displayPositions();
displayMessage( "largest" );
endStep();

beginStep();
layeredGraph.middleDegreeSort( false );
layeredGraph.displayPositions();
displayMessage( "smallest" );
endStep();

//  [Last modified: 2014 08 14 at 16:11:15 GMT]
