package edu.ncsu.csc.Galant.test;

import static org.junit.Assert.assertEquals;
import edu.ncsu.csc.Galant.*;

import java.io.File;

import org.junit.Test;

import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.parser.GraphMLParser;

public class TestGraphMLParser {

	@Test
	public void testBuildFromFile() throws GalantException {
		File f = new File("test.gml");
		GraphMLParser gmp = new GraphMLParser(f);
		
		Graph g = gmp.getGraph();
		assertEquals(4, g.getNodes().size());
		assertEquals(5, g.getEdges().size());
	}
}
