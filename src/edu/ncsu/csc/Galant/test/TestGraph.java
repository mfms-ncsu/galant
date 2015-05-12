package edu.ncsu.csc.Galant.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.Node;

public class TestGraph {

	@Test
	public void testGetNodes() {
		Graph g = generateTestGraph();
		
		assertEquals(3, g.getNodes(1).size());
		assertEquals(4, g.getNodes(2).size());
		assertEquals(5, g.getNodes().size());
	}
	
	@Test
	public void testGetEdges() {
		Graph g = generateTestGraph();
		
		assertEquals(0, g.getEdges(1).size());
		assertEquals(2, g.getEdges(5).size());
		assertEquals(4, g.getEdges().size());
	}
	
	@Test
	public void testDeleteEdge() {		
		Graph g = new Graph();
		Node n1 = g.addInitialNode();
		Node n2 = g.addInitialNode();
		Edge e1 = g.addInitialEdge(n1, n2);
		
		assertEquals(1, g.getEdges().size());
		
		g.deleteEdge(e1);
		
		assertEquals(0, g.getEdges().size());
	}
	
	@Test
	public void testDeleteNode() {
		Graph g = new Graph();
		Node n1 = g.addInitialNode();
		Node n2 = g.addInitialNode();
		Edge e1 = g.addInitialEdge(n1, n2);
		
		assertEquals(2, g.getNodes().size());
		assertEquals(1, g.getEdges().size());
		
		g.deleteNode(n1);
		
		assertEquals(1, g.getNodes().size());
		assertEquals(0, g.getEdges().size());
		
		assertFalse(e1.isDeleted(1));
		assertTrue(e1.isDeleted(2));
	}
	
	@Test
	public void testGetNumberOfNodes() {
		Graph g = new Graph();
		
		assertEquals(0, g.numberOfNodes());
		
		Node n1 = g.addNode();
		Node n2 = g.addNode();
		Edge e1 = g.addEdge(n1, n2);
		
		assertEquals(2, g.numberOfNodes());
		
		g.deleteNode(n2);
		
		assertEquals(1, g.numberOfNodes());
	}
	
	@Test
	public void testGetNumberOfEdges() {
		Graph g = new Graph();
		
		assertEquals(0, g.numberOfEdges());
		
		Node n1 = g.addNode();
		Node n2 = g.addNode();
		
		Edge e1 = g.addEdge(n1, n2);
		Edge e2 = g.addEdge(n1, n1);
		
		assertEquals(2, g.numberOfEdges());
		
		g.deleteEdge(e2);
		
		assertEquals(1, g.numberOfEdges());
	}
	
	@Test
	public void testTravel() {
		Graph g = new Graph();
		Node n1 = g.addInitialNode();
		Node n2 = g.addInitialNode();
		Edge e1 = g.addEdge(n1, n2);
		Edge e2 = g.addEdge(n1, n1);
		
		assertEquals(n2, n1.travel(e1));
		assertEquals(n1, n1.travel(e2));
		assertEquals(null, n2.travel(e2));
	}
	
	@Test
	public void testWriteMessage() {
		String msg1 = "msg1";
		String msg2 = "msg1";
		String msg3 = "msg1";
		
		Graph g = new Graph();
		g.addNode();
		g.writeMessage(msg1);
		g.addNode();
		g.writeMessage(msg2);
		g.writeMessage(msg3);
		
		assertEquals(null, g.getMessage(1));
		assertEquals(msg1, g.getMessage(2));
		assertEquals(msg3, g.getMessage(3));
		assertEquals(null, g.getMessage(4));
	}
	
	@Test
	public void testSelect() {
		Graph g = new Graph();
		Node n1 = g.addInitialNode();
		Node n2 = g.addInitialNode();
		
		assertFalse(n1.isSelected());
		g.select(n1);
		assertTrue(n1.isSelected());
		
		assertFalse(n2.isSelected());
		g.select(n2.getId());
		assertTrue(n2.isSelected());
		assertFalse(n1.isSelected());
	}
	
	
	
	
	private Graph generateTestGraph() {
		Graph g = new Graph();
		
		//state 1
		Node n1 = g.addInitialNode();
		Node n2 = g.addInitialNode();
		Node n3 = g.addInitialNode();
		
		//state 2
		Node n4 = g.addNode();
		
		//state 2
		Node n5 = g.addNode();
		
		//state 4
		Edge e1 = g.addEdge(n1, n2);
		
		//state 5
		Edge e2 = g.addEdge(n3, n2);
		
		//state 6
		Edge e3 = g.addEdge(n4, n2);
		
		//state 7
		Edge e4 = g.addEdge(n2, n5);
		
		return g;
	}
}
