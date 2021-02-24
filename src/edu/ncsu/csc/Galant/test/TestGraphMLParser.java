package edu.ncsu.csc.Galant.test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.parser.GraphMLParser;

public class TestGraphMLParser {

	@Test
	public void testBuildFromFile() {
		File f = new File("test.graphml");
		GraphMLParser gmp = new GraphMLParser(f);
		
		Graph g = gmp.getGraph();
		assertEquals(4, g.getNodes().size());
		assertEquals(5, g.getEdges().size());
	}
}

//  [Last modified: 2021 02 12 at 22:06:04 GMT]
