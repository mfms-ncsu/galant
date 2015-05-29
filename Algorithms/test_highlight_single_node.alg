/**
 * test_highlight_single_node.alg - simple test to see if Galant highlighting
 * and coloring works.
 *
 * $Id: test_highlight_single_node.alg 106 2015-04-15 13:01:30Z mfms $
 */

getNodeById(0).highlight();
getNodeById(0).unHighlight();
getNodeById(0).setColor("#007777");
System.out.println( "at the end, node = " + getNodeById(0) );

//  [Last modified: 2015 05 21 at 18:53:37 GMT]
