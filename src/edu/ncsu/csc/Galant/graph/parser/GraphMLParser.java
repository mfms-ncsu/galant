/**
 * @file GraphMLParser.java
 * @brief code for creating a graph from a GraphML file/string
 *
 * @todo Would probably be best to make the x and y attributes of nodes act
 * as surrogates for positionInLayer and layer, respectively, when a graph
 * is layered. The only part of the code that needs to know about these
 * attributes is the display in GraphPanel. The change would require changes
 * in GraphPanel, Node, NodeState, and possibly Graph and GraphState.
 *
 * @todo A lot of attributes of nodes, edges, and graphs should be set to
 * null if they don't exist (and then not printed when the graph is saved or
 * exported). Default values can be used during display in GraphPanel instead
 * of forced at time of creation. Numerical attributes should be Integer or
 * Double instead of int or double. Changes would be required here, in
 * GraphPanel, in Node, NodeState, Edge, EdgeState, Graph, GraphState, and
 * various places where the numerical attributes are referred to.
 *
 * $Id: GraphMLParser.java 113 2015-05-05 15:31:47Z mfms $
 */

package edu.ncsu.csc.Galant.graph.parser;

import java.awt.Point;
import java.io.File;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.GraphState;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * Parses a text file and creates a <code>Graph</code> from it. Allows for graph-to-editor
 * and editor-to-graph manipulation.
 * @author Ty Devries
 *
 */
public class GraphMLParser {
	
	Graph graph;
	File graphMLFile;
	Document document;
	
	public GraphMLParser(File graphMLFile) {
		this.graph = generateGraph(graphMLFile);
	}
	
	public GraphMLParser(String xml) throws GalantException {
        if ( xml == null || xml.equals( "" ) ) {
            throw new GalantException( "empty graph when invoking GraphMLParser" );
        }
		this.graph = generateGraph(xml);
	}
	
	public DocumentBuilder getDocumentBuilder( DocumentBuilderFactory dbf )
        throws GalantException
    {
		DocumentBuilder db = null;
		
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
            throw new GalantException( e.getMessage()
                                       + "\n - in getDocumentBuilder",
                                       e );
		}
		
		return db;
	}
	
    /**
     * @todo Not clear the this is ever called
     */
	public void setDocument( DocumentBuilder db, File file )
        throws GalantException {
		try {
			this.document = db.parse(file);
		}
        catch ( Exception e ) {
            throw new GalantException( e.getMessage()
                                       + "\n - in setDocument(DocumentBuilder, File)",
                                       e );
        }
	}
	
	public void setDocument( DocumentBuilder db, String xml )
        throws GalantException
    {
		InputSource is = new InputSource(new StringReader(xml));
		try {
			this.document = db.parse(is);
		}
        catch (Exception e) {
            throw new GalantException( e.getMessage()
                                       + "\n - in setDocument(DocumentBuilder,String)",
                                       e );
		}
	}
	
	public Graph buildGraphFromInput( DocumentBuilder db ) 
        throws GalantException
    {
        LogHelper.enterMethod( getClass(), "buildGraphFromInput" );
		GraphDispatch dispatch = GraphDispatch.getInstance();
		
		//TODO populate Graph g
		Graph g = new Graph();
		System.out.println("Built an empty graph in GraphMLParser.buildGraphFromInput");
		NodeList nodes;
		NodeList edges;
		NodeList graph;
		GraphState gs = g.getGraphState();
		gs.setLocked(true);
		nodes = getNodes();
		edges = getEdges();
		graph = getGraphNode();

        boolean [] idHasBeenSeen = new boolean[ nodes.getLength() ];
		
		NamedNodeMap attributes = graph.item(0).getAttributes(); //only one graph => hardcode 0th index

        // the awkward ?: construction is needed because org.w3c.dom.Node
        // conflicts with Galant Node
		String directed = ((attributes.getNamedItem("edgedefault") != null)
                           ? attributes.getNamedItem("edgedefault").getNodeValue()
                           : "undirected");
		g.setDirected(directed.equalsIgnoreCase("directed"));

        String name = (attributes.getNamedItem( "name" ) != null )
            ? attributes.getNamedItem("name").getNodeValue()
            : null;
        g.setName( name );
        String comment = ( attributes.getNamedItem( "comment" ) != null )
                       ? attributes.getNamedItem("comment").getNodeValue()
                       : null;
        g.setComment( comment );
        String type = ( attributes.getNamedItem( "type" ) != null )
                       ? attributes.getNamedItem("type").getNodeValue()
                       : null;
        String typename = ( attributes.getNamedItem( "type" ) != null )
                       ? attributes.getNamedItem("type").getNodeValue()
                       : null;
        if ( typename != null && typename.equalsIgnoreCase( "layered" ) ) {
            g.setLayered( true );
        }
        else {
            g.setLayered( false );
        }
		
        LogHelper.logDebug( "Created new graph:\n" + g );
        LogHelper.logDebug( " number of nodes = " + nodes.getLength() );
        LogHelper.logDebug( " number of edges = " + edges.getLength() );

        /** @todo instead of retrieving specific attributes by name, go
         * through all the attributes and use special handling for the ones
         * below if and when they occur, setting up default values initially */

        // to ensure that there are no duplicate node id's

        LogHelper.beginIndent();
		for (int nodeIndex = 0; nodeIndex < nodes.getLength(); nodeIndex++) {
            LogHelper.logDebug( " processing node " + nodeIndex );
			attributes = nodes.item(nodeIndex).getAttributes();
            String idString = ((attributes.getNamedItem("id") != null)
                               ? attributes.getNamedItem("id").getNodeValue()
                               : "_");
            int id = -1;
            try {
                id = Integer.parseInt( idString );
            }
            catch (NumberFormatException e) {
                throw new GalantException( e.getMessage()
                                           + "\n bad node id number "
                                           + idString,
                                           e );
            }
            if ( g.nodeIdExists( id ) ) {
                throw new GalantException( "Duplicate id: " + id 
                                           + "\n when processing nodes"
                                           + "\n in buildGraphFrom Input");
            }
			String color = ((attributes.getNamedItem("color") != null) ? attributes.getNamedItem("color").getNodeValue() : Graph.NOT_A_COLOR );
			String label = ((attributes.getNamedItem("label") != null) ? attributes.getNamedItem("label").getNodeValue() : Graph.NOT_A_LABEL);
            String weightString
                = (attributes.getNamedItem("weight") != null )
                ? (attributes.getNamedItem("weight").getNodeValue())
                : null;
            double weight = Graph.NOT_A_WEIGHT;
            if ( weightString != null ) { 
                try {
                    weight = Double.parseDouble( weightString );
                }
                catch (NumberFormatException e) {
                    weight = Graph.NOT_A_WEIGHT;
                }
            }
			Boolean highlighted = (attributes.getNamedItem("highlighted") != null) ? Boolean.parseBoolean(attributes.getNamedItem("highlighted").getNodeValue()) : false;
			
			Point position = null;
			if (attributes.getNamedItem("x") != null && attributes.getNamedItem("y") != null) {
				String sX = attributes.getNamedItem("x").getNodeValue();
				String sY = attributes.getNamedItem("y").getNodeValue();
				
				try {
					int x = Integer.parseInt(sX);
					int y = Integer.parseInt(sY);
					
					position = new Point(x, y);
				} catch (Exception e) {
                    System.out.println( "Warning: "
                                        + sX + "," + sY
                                        + "is not a legitimate point.");
                    System.out.println( "Choosing a random point instead" );
                    position = Node.genRandomPosition();
                }
			}
            else {
                position = Node.genRandomPosition();
            }

            LogHelper.logDebug( "standard attributes set" );
            LogHelper.logDebug( " id = " + id );
            LogHelper.logDebug( " color = " + color );
            LogHelper.logDebug( " weight = " + weight );
            LogHelper.logDebug( " label = " + label );
            LogHelper.logDebug( " position = " + position );

            Node n = null;

            /**
             * Added this for layered graphs.
             * @todo The right way to do it is to read all named attributes
             * and do the following with each:
             * - if it parses as an int, make it an integer attribute
             * - if it parses as a double, make it a double
             * - otherwise make it a string
             */
            if ( g.isLayered() ) {
                int layer = 0;
                int positionInLayer = 0;
                LogHelper.logDebug( "adding node to layered graph" );
                try {
                    if ( attributes.getNamedItem( "layer" ) != null ) {
                        String layerString
                            = attributes.getNamedItem( "layer" ).getNodeValue();
                        layer = Integer.parseInt( layerString );
                    }
                    if ( attributes.getNamedItem( "positionInLayer" ) != null ) {
                        String positionString
                            = attributes.getNamedItem( "positionInLayer" ).getNodeValue();
                        positionInLayer = Integer.parseInt( positionString );
                    }

				
                    n = new Node(gs, highlighted, false, id, weight, color, label,
                                 layer, positionInLayer );

				} catch (Exception e) {
                    System.out.println( "Warning: something went wrong with layering:" );
                    System.out.println( "" + e );
                    System.out.println( "Treating as not layered." );
                    g.setLayered( false );
                    n = new Node(gs, id, weight, color, label, highlighted, false);
                }

                LogHelper.logDebug( "done adding node to layered graph: " + n );
            }
            else {
                // not layered
                n = new Node(gs, id, weight, color, label, highlighted, false);
            }
            if (position != null) {
                n.setFixedPosition(position);
            }
            LogHelper.logDebug( "adding node " + n );
            g.addNode(n);
		}
        LogHelper.endIndent();

		for(int i = 0; i < edges.getLength(); i++) {
            String sourceString = null; // for exception handling (no longer needed?)
            String targetString = null;
            Node source = null;
            Node target = null;
            String color = Graph.NOT_A_COLOR;
            Boolean highlighted = false;
            double weight = Graph.NOT_A_WEIGHT;
            String label = Graph.NOT_A_LABEL;
                
            attributes = edges.item(i).getAttributes();
            try {
                sourceString
                    = attributes.getNamedItem("source").getNodeValue();
                source = g.getNodeById( Integer.parseInt( sourceString ) );
                targetString
                    = attributes.getNamedItem("target").getNodeValue();
                target = g.getNodeById( Integer.parseInt( targetString ) );
            }
            catch ( Exception e ) {
                throw new GalantException( e.getMessage() + " \n - bad source or target for edge " + i );
            }
                
            if ( attributes.getNamedItem("color") != null ) {
                color = attributes.getNamedItem("color").getNodeValue();
            }
            if ( attributes.getNamedItem("label") != null ) {
                label = attributes.getNamedItem("label").getNodeValue();
            }
            String highlightedString = null;
            if ( attributes.getNamedItem("highlighted") != null ) {
                highlightedString
                    = attributes.getNamedItem("highlighted").getNodeValue();
                    
            }
            if ( highlightedString != null ) {
                highlighted = Boolean.parseBoolean( highlightedString );
            }
            String weightString = null; 
            if ( attributes.getNamedItem("weight") != null ) {
                weightString
                    = attributes.getNamedItem("weight").getNodeValue();
            }
            try {
                if ( weightString != null ) {
                    weight = Double.parseDouble( weightString );
                }
            }
            catch ( Exception e ) {
                throw new GalantException( e.getMessage() + " \n - bad weight for edge " + i );
            }

            Edge e = new Edge(gs, Integer.valueOf(i), source, target, highlighted, weight, color, label);
            g.addEdge(e);
            source.addEdge(e);
            target.addEdge(e);
            LogHelper.logDebug( "adding edge " + e );
		} // adding edge
        g.getGraphState().setLocked(false);
        LogHelper.exitMethod( getClass(), "buildGraphFromInput:\n" + g );
        return g;
    } // buildGraphFromInput
	
    /**
     * @todo the exception handling here needs to go at least one level up
     */
	public Graph generateGraph(String xml) {
        LogHelper.enterMethod( getClass(), "generateGraph( String )" );
		DocumentBuilderFactory dbf = null;
		DocumentBuilder db = null;
        Graph newGraph = null;

        try {
            dbf = DocumentBuilderFactory.newInstance();
            db = getDocumentBuilder(dbf);
            setDocument(db, xml);
            newGraph = buildGraphFromInput(db);
        }
//         catch ( GalantException exception ) {
//             exception.report( exception.getMessage()
//                               + "\n called from generateGraph(String xml)" );
//             exception.display();
//         }
        catch ( Exception exception ) {
            if ( exception instanceof GalantException ) {
                GalantException ge = (GalantException) exception;
                ge.report( "" );
                ge.display();
            }
            else exception.printStackTrace( System.out );
        }

        LogHelper.exitMethod( getClass(), "generateGraph( String )" );
		return newGraph;
	}
	
	public Graph generateGraph(File file) {
        LogHelper.enterMethod( getClass(), "generateGraph( File )" );
		DocumentBuilderFactory dbf = null;
		DocumentBuilder db = null;
        Graph newGraph = null;

        try {
            dbf = DocumentBuilderFactory.newInstance();
            db = getDocumentBuilder(dbf);
            setDocument(db, file);
            newGraph = buildGraphFromInput(db);
        }
//         catch ( GalantException exception ) {
//             exception.report( exception.getMessage()
//                               + "\n called from generateGraph(File)" );
//             exception.display();
//         }
        catch ( Exception exception ) {
            GalantException topLevelException
                = new GalantException( exception.getMessage()
                                       + "\n - in generateGraph(File)" );
            topLevelException.report( "" );
            topLevelException.display();
        }

        LogHelper.exitMethod( getClass(), "generateGraph( File )" );
        return newGraph;
	}
	
// 	public void addNode(Node n) {
// 		//TODO add node to front of Nodes, return complete graph string
// 		graph.addNode(n, 0);
// 	}
	
// 	public void addEdge(Edge e) {
// 		//TODO add edge to front of Edges, return complete graph string
// 		graph.addEdge(e, 0);
// 	}
	
	public NodeList getGraphNode() {
		return this.document.getElementsByTagName("graph");
	}
	
	public NodeList getNodes() {
		return this.document.getElementsByTagName("node");
	}
	
	public NodeList getEdges() {
		return this.document.getElementsByTagName("edge");
	}
	
	public Graph getGraph() {
		return this.graph;
	}
	
}

//  [Last modified: 2015 05 21 at 19:26:47 GMT]
