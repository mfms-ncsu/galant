package edu.ncsu.csc.Galant.gui.editor;

import java.awt.Color;
import java.util.Hashtable;

import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import edu.ncsu.csc.Galant.GalantPreferences;
import edu.ncsu.csc.Galant.gui.util.ExceptionDialog;
import edu.ncsu.csc.Galant.algorithm.code.macro.Macro;

/**
 * Adds syntax highlighting in the algorithm editor. Tooltips are displayed for highlighted API references in the editor.
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 *
 */
public class GAlgorithmSyntaxHighlighting implements Runnable {
	
	public static final String javaKeywordStyleName = "javaKeyword";
	
	public static final Hashtable<String, String> APIdictionary = new Hashtable<String, String>();
	
	public static Color javaKeywordColor = GalantPreferences.JAVA_KEYWORD_COLOR.get();
	public static Color apiKeywordColor = GalantPreferences.API_CALL_COLOR.get();
	public static Color macroKeywordColor = GalantPreferences.MACRO_KEYWORD_COLOR.get();
	
	private JTextPane textpane;
	
	public GAlgorithmSyntaxHighlighting(JTextPane _textpane) {
		textpane = _textpane;
	
		APIdictionary.put("beginStep", "Forces the animation to consider all graph changes one step until endStep() or another beginStep() is called.");
		APIdictionary.put("endStep", "Ends a step in the graph state. If no beginStep() has been called previously, does nothing.");
		APIdictionary.put("isDirected", "Returns True if the graph is directed, False otherwise.");
		APIdictionary.put("setDirected", "Sets the graph to directed for True and undirected for False");
		APIdictionary.put("getNodes", "Gets a list of all non-deleted nodes in the latest state of the graph.");
		APIdictionary.put("getEdges", "Gets a list of all non-deleted edges in the latest state of the graph.");
		APIdictionary.put("getNodeById", "Gets the node whose id matches the input. Returns null if the node doesn't exist or has been deleted.");
		APIdictionary.put("getEdgeById", "Gets the edge whose id matches the input. Returns null if the edge doesn't exist or has been deleted.");
		APIdictionary.put("select", "Sets the specified Node as the selected Node in the graph and deselects all other selected nodes.");
		APIdictionary.put("addNode", "Adds a Node with default settings to the graph and returns a pointer to the new Node.");
		APIdictionary.put("addEdge", "Adds an edge to the graph between the two specified nodes or node indices, and also stores the edge in each Node.");
		APIdictionary.put("isSelected", "Returns True if the Node or Edge is selected in the latest state and False otherwise.");
		APIdictionary.put("setSelected", "Sets the Node or Edge as selected. In contrast with the Graph's select function, this does not deselect other selected nodes in a graph."); 
		APIdictionary.put("isVisited", "Returns the true if the Node has been marked as visited and False otherwise.");
		APIdictionary.put("setVisited", "Sets the Node's visited/marked property to the input value.");
		APIdictionary.put("isMarked", "Returns the true if the Node has been marked and False otherwise."); 
		APIdictionary.put("mark", "Sets the Node's visited/marked property to True");
		APIdictionary.put("getWeight", "Returns the weight of the current Node or Edge. The default weight is 0.");
		APIdictionary.put("setWeight", "Sets the weight of the current Node or Edge.");
		APIdictionary.put("getUnvisitedPaths", "Returns a List<Edge> object of all the Node's incident edges connecting to unvisited Nodes in the latest state of the graph.");
		APIdictionary.put("getVisitedPaths", "Returns a List<Edge> object of all the Node's incident edges connecting to visited Nodes in the latest state of the graph.");
		APIdictionary.put("getUnvisitedAdjacentNodes", "Returns a List<Node> object of all the Node's adjacent Nodes whose visited properties are False in the latest state of the graph.");
		APIdictionary.put("travel", "Returns the other Node endpoint of the specified edge. Returns null if neither of the edge's endpoints is this Node. If e is a loop, returns this Node.");
		APIdictionary.put("getId", "Returns the numerical id of the node. This will not change during Algorithm execution unless explicitly set in the user code.");
		APIdictionary.put("getColor", "Gets the default color of the Node or Edge in the format '#RRGGBB'");
		APIdictionary.put("setColor", "Sets the default color of the Node or Edge. This color will corresponds to the colored ring around a node and will be the default color when the node is not selected. A valid color input should be of the form '#RRGGBB'.");
		APIdictionary.put("getLabel", "Returns the String label associated with the Node or Edge.");
		APIdictionary.put("setLabel", "Sets the label associated with the Node or Edge.");
		APIdictionary.put("getPosition", "Gets the position of the node on the coordinate plane.");
        APIdictionary.put("setPosition", "Sets the position of the Node on the coordinate plane.");
		APIdictionary.put("equals", "Returns true if the given Node points to the same Node as the current Node and false otherwise.");
		APIdictionary.put("setStringAttribute", "Stores a String in the Node or Edge under the specified key. This will overwrite any String, Integer, or Double attribute already set with the same key.");
		APIdictionary.put("getStringAttribute", "Gets the String value associated with the specified key. Returns null if the key doesn't exist or it exists but its value is not a String.");
		APIdictionary.put("setIntegerAttribute", "Stores an Integer in the Node or Edge under the specified key. This will overwrite any String, Integer, or Double attribute already set with the same key.");
		APIdictionary.put("getIntegerAttribute", "Gets the Integer value associated with the specified key. Returns null if the key doesn't exist or it exists but its value is not an Integer.");
		APIdictionary.put("setDoubleAttribute", "Stores a Double in the Node or Edge under the specified key, This will overwrite any String, Integer, or Double attribute already set with the same key.");
        APIdictionary.put("getDoubleAttribute", "Gets the Double value associated with the specified key. Returns null if the key doesn't exist or it exists but its value is not a Double.");
        APIdictionary.put("getSourceNode", "Returns the source Node of the current Edge.");
		APIdictionary.put("setSourceNode", "Sets the source Node of the current Edge.");
		APIdictionary.put("getTargetNode", "Returns the destination Node of the current Edge.");
		APIdictionary.put("setTargetNode", "Sets the destination Node of the current Edge.");
		APIdictionary.put("getOtherEndpoint", "Returns the other endpoint of an edge, or NULL if the provided node is not either endpoint in the specified edge.");
		APIdictionary.put("getId", "Returns the unique ID of the edge. This will not change during Algorithm execution unless explicitly set in the user code.");
        APIdictionary.put("equals", "Returns true if the given Edge points to the same Node as the current Edge and false otherwise.");
        APIdictionary.put("for_nodes", "Iterates over all Nodes in the graph.");
        APIdictionary.put("for_edges", "Iterates over all Edges in the graph.");
        APIdictionary.put("function", "Creates a function that can be called later.");

		StyledDocument doc = textpane.getStyledDocument();
		updateDocStyles(doc);

		// Initialize allMacrokeywords
		for(int i = 0; i < Macro.MACROS.size(); i++) {
			allMacrokeywords[i] = Macro.MACROS.get(i).getName();
		}
	}
	
	/**
	 * An immutable list of all keywords employed in the Java language, plus
	 * others that are relevant to Galant.
	 */
	public static final String[] allJavaKeywords = new String[] { "abstract", "assert", "boolean", 
		"break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do",
		"double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if",
		"implements", "import", "instanceof", "int", "interface", "long", "native", "new",
		"package", "private", "protected", "public", "return", "short", "static", "strictfp",
		"super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", 
                                                                  "void", "volatile", "while", "false", "null", "true", "boolean", "Integer", "Double" };
	
	/**
	 * An immutable list of all API calls predefined for the user's
	 * benefit.
     *
     * @todo Need to organize this
	 */
		public static final String[] allAPIkeywords = new String[] {
            "beginStep", "endStep", "movesNodes", "isDirected", "setDirected",
			"getNodes", "getEdges", "getNodeById", "getEdgeById", "select", "addNode", "addEdge",
            "isSelected", "setSelected", "isVisited", "setVisited", "isMarked", "mark", "unmark", "marked",
            "highlight", "unhighlight", "highlighted",
            "getWeight", "setWeight", "weight",
            "label", "color", "uncolor", "set", "clear",
            "getUnvisitedPaths", "getVisitedPaths",
                                                                   
			"getUnvisitedAdjacentNodes", "travel", "getId", "getColor", "setColor", "getLabel", "setLabel", "getPosition",
			"setPosition", "equals", "setStringAttribute", "getStringAttribute", "setIntegerAttribute", "getIntegerAttribute",
			"setDoubleAttribute", "getDoubleAttribute", "getSourceNode", "setSourceNode", "getTargetNode", "setTargetNode",
			"getOtherEndpoint", "Graph", "Node", "Edge", "for_adjacent", "for_nodes", "for_edges", "function", "graph",
			"setRootNode", "getRootNode", "getTargetNode", "setTargetNode", "getPaths", "NodeQueue", "EdgeQueue", "NodeStack",
                                                                   "EdgeStack", "NodePriorityQueue", "EdgePriorityQueue", "NodeSet", "EdgeSet", "nodeQ", "edgeQ", "nodeStack", "edgeStack", "nodePQ",
			"edgePQ", "getId", "id", "equals", "Graph", "Node", "Edge", "for_adjacent", "for_nodes", "for_edges", "function", "graph", "getInteger", "getNode", "getEdge", "getNodeSet", "getEdgeSet",
            "hide", "show", "hideLabel", "showLabel", "hideWeight", "showWeight",
            "hideEdgeWeights", "showEdgeWeights", "hideEdgeLabels", "showEdgeLabels",
            "hideNodeWeights", "showNodeWeights", "hideNodeLabels", "showNodeLabels",
            "clearEdgeLabels", "clearEdgeWeights", "clearNodeLabels", "clearNodeWeights"
        };

	/**
	 * An immutable list of all Macroes predefined for the user's benefit.
	 * It get initialized when this GAlgorithmSyntaxHighlighting was created. See constructor. 
	 */
		public static String[] allMacrokeywords = new String[Macro.MACROS.size()];

	@Override
	public void run() {
		try {
			String content = textpane.getText().replace("\r\n", "\n");
			StyledDocument doc = textpane.getStyledDocument();
			doc.setCharacterAttributes (0, doc.getLength (), doc.getStyle("regular"), true);
	        
	        applyStyleToKeywords(doc, content, allJavaKeywords, javaKeywordStyleName);
	        applyStyleToKeywords(doc, content, allAPIkeywords, "apiKeyword");
	        applyStyleToKeywords(doc, content, allMacrokeywords, "macroKeyword");
	        
	        textpane.setDocument(doc);
		} catch (Exception e) {
			ExceptionDialog.displayExceptionInDialog(e);
		}
	}
	
	private static void updateDocStyles(StyledDocument doc) {
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setBold(def, true);
		Style regular = doc.addStyle("regular",  def);
		StyleConstants.setFontFamily(def, "SansSerif");
				
		Style s = doc.addStyle(javaKeywordStyleName, regular);
		StyleConstants.setForeground(s, javaKeywordColor);
		
		Style q = doc.addStyle("apiKeyword", regular);
		StyleConstants.setForeground(q, apiKeywordColor);

		Style m = doc.addStyle("macroKeyword", regular);
		StyleConstants.setForeground(m, macroKeywordColor);
	}
	
	private static void applyStyleToKeywords(StyledDocument doc, String content, String[] keywords, String styleName)
		{
			for(String keyword : keywords) {
	        	int index = 0;
	        	while((index = content.indexOf(keyword, index)) != -1) {
	        		char prev = (index > 0) ? content.charAt(index-1) : ' ';
	        		char next = (index+keyword.length() < content.length()) ? content.charAt(index+keyword.length()) : ' ';
	        		if(!Character.isJavaIdentifierPart(prev) && !Character.isJavaIdentifierPart(next)) {
	        			doc.setCharacterAttributes(index, keyword.length(), doc.getStyle(styleName), true);	        		}	
	        		index += keyword.length();
	        	}
	        }
		}
	
}

//  [Last modified: 2016 10 26 at 17:40:18 GMT]
