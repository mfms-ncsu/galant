package edu.ncsu.csc.Galant.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.Node;

public class TestEdge {

	@Test
	public void testGetOtherEndpoint() {
		Graph g = new Graph();
		
		Node n1 = g.addInitialNode();
		Node n2 = g.addInitialNode();
		Node n3 = g.addInitialNode();
		
		Edge e = g.addEdge(n1, n2);
		
		assertEquals(n2, e.getOtherEndpoint(n1));
		assertEquals(n1, e.getOtherEndpoint(n2));
		assertEquals(null, e.getOtherEndpoint(n3));	
	}
	
	@Test
	public void testIsCreated() {
		Graph g = new Graph();
		
		Node n1 = g.addInitialNode();
		Node n2 = g.addInitialNode();		
		Edge e = g.addEdge(n1, n2);
		Edge e2 = g.addEdge(n2, n1);
		
		assertFalse(e.isCreated(1));
		assertTrue(e.isCreated(2));
		assertFalse(e2.isCreated(2));
		assertTrue(e2.isCreated(3));
	}
	
	@Test
	public void testIsDeleted() {
		Graph g = new Graph();
		
		Node n1 = g.addInitialNode();
		Node n2 = g.addInitialNode();		
		Edge e = g.addEdge(n1, n2);
		g.deleteEdge(e);
		g.addNode();
		
		assertFalse(e.isDeleted(1));
		assertFalse(e.isDeleted(2));
		assertTrue(e.isDeleted(3));
		assertTrue(e.isDeleted(4));
		assertTrue(e.isDeleted());
		
	}
	
	@Test
	public void testInScope() {
		Graph g = new Graph();
		
		Node n1 = g.addInitialNode();
		Node n2 = g.addInitialNode();		
		Edge e = g.addEdge(n1, n2);
		g.deleteEdge(e);
		g.addNode();
		
		assertFalse(e.inScope(1));
		assertTrue(e.inScope(2));
		assertFalse(e.inScope(3));
		assertFalse(e.inScope(4));
		assertFalse(e.inScope());
	}
	
	@Test
	public void testEdgeComparison() {
		Graph g = new Graph();
		
		Node n1 = g.addInitialNode();
		Node n2 = g.addInitialNode();		
		Edge e1 = g.addEdge(n1, n2);
		Edge e2 = g.addEdge(n1, n2);
		Edge e3 = g.addEdge(n1, n2);
		Edge e4 = g.addEdge(n1, n2);
		Edge e5 = g.addEdge(n1, n2);
		
		e1.setWeight(3);
		e2.setWeight(0);
		e3.setWeight(12);
		e4.setWeight(2);
		e5.setWeight(363);
		
		List<Edge> sortList = new ArrayList<Edge>();
		sortList.add(e1);
		sortList.add(e2);
		sortList.add(e3);
		sortList.add(e4);
		sortList.add(e5);
		
		Collections.sort(sortList);
		
		assertEquals(e2, sortList.get(0));
		assertEquals(e4, sortList.get(1));
		assertEquals(e1, sortList.get(2));
		assertEquals(e3, sortList.get(3));
		assertEquals(e5, sortList.get(4));
	}
	
}
