package edu.ncsu.csc.Galant.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.Node;

public class TestNode {

	@Test
	public void testGetEdgesUndirected() {
		Graph g = new Graph();
		g.setDirected(false);
		
		Node n1 = g.addInitialNode();
		Node n2 = g.addInitialNode();
		Node n3 = g.addInitialNode();
		Node n4 = g.addNode();
		Node n5 = g.addNode();
		Edge e1 = g.addEdge(n1, n2);
		Edge e2 = g.addEdge(n3, n2);
		Edge e3 = g.addEdge(n4, n2);
		Edge e4 = g.addEdge(n2, n5);
		
		assertEquals(0, n2.getIncidentEdges(1).size());
		assertEquals(0, n2.getIncidentEdges(2).size());
		assertEquals(0, n2.getIncidentEdges(3).size());
		assertEquals(1, n2.getIncidentEdges(4).size());
		assertEquals(2, n2.getIncidentEdges(5).size());
		assertEquals(3, n2.getIncidentEdges(6).size());
		assertEquals(4, n2.getIncidentEdges(7).size());
		assertEquals(4, n2.getIncidentEdges().size());

	}
	
	@Test
	public void testGetEdgesDirected() {
		Graph g = new Graph();
		g.setDirected(true);
		
		Node n1 = g.addInitialNode();
		Node n2 = g.addInitialNode();
		Node n3 = g.addInitialNode();
		Node n4 = g.addNode();
		Node n5 = g.addNode();
		Edge e1 = g.addEdge(n2, n1);
		Edge e2 = g.addEdge(n3, n2);
		Edge e3 = g.addEdge(n4, n2);
		Edge e4 = g.addEdge(n2, n5);
		
		assertEquals(0, n2.getIncidentEdges(1).size());
		assertEquals(0, n2.getIncidentEdges(2).size());
		assertEquals(0, n2.getIncidentEdges(3).size());
		assertEquals(1, n2.getIncidentEdges(4).size());
		assertEquals(1, n2.getIncidentEdges(5).size());
		assertEquals(1, n2.getIncidentEdges(6).size());
		assertEquals(2, n2.getIncidentEdges(7).size());
		assertEquals(2, n2.getIncidentEdges().size());
	}
	
	@Test
	public void testIsCreated() {
		Graph g = new Graph();
		
		Node n1 = g.addNode();
		Node n2 = g.addNode();		
		
		assertFalse(n1.isCreated(1));
		assertTrue(n1.isCreated(2));
		assertFalse(n2.isCreated(2));
		assertTrue(n2.isCreated(3));
	}
	
	@Test
	public void testIsDeleted() {
		Graph g = new Graph();
		
		Node n1 = g.addNode();
		Node n2 = g.addNode();		
		
		g.deleteNode(n1);
		g.addNode();
		
		assertFalse(n1.isDeleted(1));
		assertFalse(n1.isDeleted(2));
		assertFalse(n1.isDeleted(3));
		assertTrue(n1.isDeleted(4));
		assertTrue(n1.isDeleted(5));
		assertTrue(n1.isDeleted());
		
	}
	
	@Test
	public void testNodeComparison() {
		Graph g = new Graph();
		
		Node n1 = g.addInitialNode();
		Node n2 = g.addInitialNode();		
		Node n3 = g.addInitialNode();
		Node n4 = g.addInitialNode();
		Node n5 = g.addInitialNode();
		
		n1.setWeight(3);
		n2.setWeight(0);
		n3.setWeight(12);
		n4.setWeight(2);
		n5.setWeight(363);
		
		List<Node> sortList = new ArrayList<Node>();
		sortList.add(n1);
		sortList.add(n2);
		sortList.add(n3);
		sortList.add(n4);
		sortList.add(n5);
		
		Collections.sort(sortList);
		
		assertEquals(n2, sortList.get(0));
		assertEquals(n4, sortList.get(1));
		assertEquals(n1, sortList.get(2));
		assertEquals(n3, sortList.get(3));
		assertEquals(n5, sortList.get(4));
	}
	
	@Test
	public void testNodeSetColor() {
		Graph g = new Graph();
		Node n1 = g.addInitialNode();
		
		String color = n1.getColor();
		Color c = Color.decode(n1.getColor());
		
		assertEquals(Color.BLACK, c);
		
		String newColor = "#442167";
		n1.setColor(newColor);
		assertEquals(Color.BLACK, Color.decode(n1.getColor(1)));
		assertEquals(newColor, n1.getColor(2));
		assertEquals(newColor, n1.getColor());
	}
	
	@Test
	public void testNodeGetLabel() {
		Graph g = new Graph();
		Node n1 = g.addInitialNode();
		
		String label = n1.getLabel();
		
		assertEquals(null, label);
		
		String newLabel = "TESTLABEL";
		n1.setLabel(newLabel);
		assertEquals(null, n1.getLabel(1));
		assertEquals(newLabel, n1.getLabel(2));
		assertEquals(newLabel, n1.getLabel());
	}
	
	@Test
	public void testSetAttributes() {
		Graph g = new Graph();
		Node n1 = g.addInitialNode();
		Node n2 = g.addInitialNode();
		
		String keyI = "I";
		String keyD = "D";
		String keyS = "S";
		String valueS = "value";
		
		n1.setIntegerAttribute(keyI, 123);
		n2.setIntegerAttribute(keyI, 44);
		n1.setDoubleAttribute(keyD, 2.56);
		n1.setStringAttribute(keyS, valueS);
		
		assertEquals((Integer) 123, n1.getIntegerAttribute(keyI));
		assertEquals((Integer) 44, n2.getIntegerAttribute(keyI));
		assertEquals((Double) 2.56, n1.getDoubleAttribute(keyD));
		assertEquals((String) valueS, n1.getStringAttribute(keyS));
		
		assertEquals(null, n1.getStringAttribute(keyI));
		assertEquals(null, n1.getDoubleAttribute(keyI));
		assertEquals(null, n1.getIntegerAttribute(keyD));
	}
		
}
