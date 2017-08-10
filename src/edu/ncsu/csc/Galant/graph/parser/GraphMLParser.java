/**
 * @file GraphMLParser.java
 * @brief code for creating a graph from a GraphML file/string
 *
 * @todo Would probably be best to make the x and y attributes of nodes act
 * as surrogates for positionInLayer and layer, respectively, when a graph
 * is layered. The only part of the code that needs to know about these
 * attributes is the display in GraphPanel. The change would require changes
 * in GraphPanel, Node, NodeState, and possibly Graph.
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
import edu.ncsu.csc.Galant.graph.component.*;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.logging.LogHelper;
import edu.ncsu.csc.Galant.Timer;

/**
 * Parses a text file and creates a <code>Graph</code> from it. Allows for
 * graph-to-editor and editor-to-graph manipulation.
 * @author Ty Devries
 */
public class GraphMLParser {

  Graph graph;
  File graphMLFile;
  Document document;

  public GraphMLParser(File graphMLFile) throws GalantException {
    Timer.parsingTime.start();
    this.graph = generateGraph(graphMLFile);
    Timer.parsingTime.stop();
  }

  public GraphMLParser(String xml) throws GalantException {
    if ( xml == null || xml.equals( "" ) ) {
      throw new GalantException( "No text when invoking GraphMLParser" );
    }
    Timer.parsingTime.start();
    this.graph = generateGraph(xml);
    Timer.parsingTime.stop();
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

  /**
   * Sets the value stored in the xml node. Parsing is left to the
   * graphElement via initializeAfterParsing().
   * @see edu.ncsu.csc.Galant.graph.component.GraphElement
   * @see edu.ncsu.csc.Galant.graph.component.Edge
   * @see edu.ncsu.csc.Galant.graph.component.Node
   */
  private void processAttribute(GraphElement graphElement, org.w3c.dom.Node xmlNode) {
    LogHelper.logDebug("-> processAttribute for " + graphElement);
    String attributeName = xmlNode.getNodeName();
    String attributeValueString = xmlNode.getTextContent();
    try {
      graphElement.set(attributeName, attributeValueString);
    }
    catch ( Terminate t ) { // should not happen
      t.printStackTrace();
    }
    LogHelper.logDebug("<- processAttribute for " + graphElement);
  }

  public Graph buildGraphFromInput( DocumentBuilder db )
    throws GalantException
  {
    LogHelper.disable();
    LogHelper.enterMethod( getClass(), "buildGraphFromInput" );
    GraphDispatch dispatch = GraphDispatch.getInstance();

    Graph graphUnderConstruction = new Graph();
    NodeList nodes;
    NodeList edges;
    NodeList graph;
    nodes = getNodes();
    edges = getEdges();
    graph = getGraphNode();

    //only one graph => hardcode 0th index
    NamedNodeMap attributes = graph.item(0).getAttributes();

    // the awkward ?: construction is needed because org.w3c.dom.Node
    // conflicts with Galant Node
    String directed = ((attributes.getNamedItem("edgedefault") != null)
                       ? attributes.getNamedItem("edgedefault").getNodeValue()
                       : "undirected");
    graphUnderConstruction.setDirected(directed.equalsIgnoreCase("directed"));

    String name = (attributes.getNamedItem( "name" ) != null )
      ? attributes.getNamedItem("name").getNodeValue()
      : null;
    graphUnderConstruction.setName( name );
    String comment = ( attributes.getNamedItem( "comment" ) != null )
      ? attributes.getNamedItem("comment").getNodeValue()
      : null;
    graphUnderConstruction.setComment( comment );
    String type = ( attributes.getNamedItem( "type" ) != null )
      ? attributes.getNamedItem("type").getNodeValue()
      : null;
    String typename = ( attributes.getNamedItem( "type" ) != null )
      ? attributes.getNamedItem("type").getNodeValue()
      : null;
    if ( typename != null && typename.equalsIgnoreCase( "layered" ) ) {
      graphUnderConstruction.setLayered( true );
    }
    else {
      graphUnderConstruction.setLayered( false );
    }

    LogHelper.logDebug( "Created new graph:\n" + graphUnderConstruction );
    LogHelper.logDebug( " number of nodes = " + nodes.getLength() );
    LogHelper.logDebug( " number of edges = " + edges.getLength() );

    LogHelper.disable();
    LogHelper.beginIndent();
    for ( int nodeIndex = 0; nodeIndex < nodes.getLength(); nodeIndex++ ) {
      LogHelper.logDebug( " processing " + nodeIndex + "th node." );
      org.w3c.dom.Node xmlNode = nodes.item(nodeIndex);
      Node graphNode = new Node(graphUnderConstruction);
      NamedNodeMap nodeAttributes = xmlNode.getAttributes();
      if ( attributes != null ) {
        for ( int i = 0; i < nodeAttributes.getLength(); i++ ) {
          org.w3c.dom.Node attribute = nodeAttributes.item(i);
          processAttribute(graphNode, attribute);
          LogHelper.logDebug("Node attribute " + attribute.getNodeName()
                             + ", value = " + attribute.getTextContent());
        }
      }
      graphNode.initializeAfterParsing();
      LogHelper.logDebug( "adding node " + graphNode );
      graphUnderConstruction.addNode(graphNode);
    }
    LogHelper.endIndent();

    LogHelper.disable();
    LogHelper.beginIndent();
    for ( int nodeIndex = 0; nodeIndex < edges.getLength(); nodeIndex++ ) {
      LogHelper.logDebug( " processing " + nodeIndex + "th edge." );
      org.w3c.dom.Node xmlNode = edges.item(nodeIndex);
      Edge graphEdge = new Edge(graphUnderConstruction);
      NamedNodeMap edgeAttributes = xmlNode.getAttributes();
      if ( attributes != null ) {
        for ( int i = 0; i < edgeAttributes.getLength(); i++ ) {
          org.w3c.dom.Node attribute = edgeAttributes.item(i);
          processAttribute(graphEdge, attribute);
          LogHelper.logDebug("Edge attribute " + attribute.getNodeName()
                             + ", value = " + attribute.getTextContent());
        }
      }
      graphEdge.initializeAfterParsing();
      LogHelper.logDebug( "adding edge " + graphEdge );
      graphUnderConstruction.addEdge(graphEdge);
    }
    LogHelper.endIndent();
    LogHelper.restoreState();
    graphUnderConstruction.initializeAfterParsing();
    LogHelper.exitMethod( getClass(), "buildGraphFromInput:\n" + graphUnderConstruction );
    LogHelper.restoreState();
    return graphUnderConstruction;
  } // buildGraphFromInput

    /**
     * @todo the exception handling here needs to go at least one level up
     */
  public Graph generateGraph(String xml) throws GalantException {
    LogHelper.disable();
    LogHelper.enterMethod( getClass(), "generateGraph( String )" );
    DocumentBuilderFactory dbf = null;
    DocumentBuilder db = null;
    Graph newGraph = null;

    dbf = DocumentBuilderFactory.newInstance();
    db = getDocumentBuilder(dbf);
    setDocument(db, xml);
    newGraph = buildGraphFromInput(db);

    // /**
    //  * @todo these exceptions should really be caught by the editor
    //  */
    // catch ( Exception exception ) {
    //     if ( exception instanceof GalantException ) {
    //         GalantException ge = (GalantException) exception;
    //         ge.report( "" );
    //         try {
    //             ge.display();
    //         }
    //         catch ( Terminate t ) { // should not happen
    //             t.printStackTrace();
    //         }
    //     }
    //     else exception.printStackTrace( System.out );
    // }

    LogHelper.exitMethod( getClass(), "generateGraph( String )" );
    LogHelper.restoreState();
    return newGraph;
  }

  public Graph generateGraph(File file) throws GalantException {
    LogHelper.enterMethod( getClass(), "generateGraph( File )" );
    DocumentBuilderFactory dbf = null;
    DocumentBuilder db = null;
    Graph newGraph = null;

    dbf = DocumentBuilderFactory.newInstance();
    db = getDocumentBuilder(dbf);
    setDocument(db, file);
    newGraph = buildGraphFromInput(db);

    // catch ( Exception exception ) {
    //     GalantException topLevelException
    //         = new GalantException( exception.getMessage()
    //                                + "\n - in generateGraph(File)" );
    //     topLevelException.report( "" );
    //     try {
    //         topLevelException.display();
    //     }
    //     catch ( Terminate t ) {
    //         // ignore - probably not relevant
    //     }
    // }

    LogHelper.exitMethod( getClass(), "generateGraph( File )" );
    return newGraph;
  }

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

//  [Last modified: 2017 07 25 at 19:42:32 GMT]
