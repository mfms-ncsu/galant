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
 * Adds syntax highlighting in the algorithm editor. Tooltips are displayed for
 *highlighted API references in the editor.
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 *
 */
public class GAlgorithmSyntaxHighlighting implements Runnable {
  /**
   * @todo colors for comments and string should probably be preferences
   */
  public static final Color COMMENT_COLOR = Color.GRAY;
  public static final Color STRING_COLOR = Color.decode("#990099");   // dark violet
  public static final String javaKeywordStyleName = "javaKeyword";
  public static final Hashtable<String,
                                String> APIdictionary = new Hashtable<String, String>();
  public static Color javaKeywordColor = GalantPreferences.JAVA_KEYWORD_COLOR.get();
  public static Color apiKeywordColor = GalantPreferences.API_CALL_COLOR.get();
  public static Color macroKeywordColor = GalantPreferences.MACRO_KEYWORD_COLOR.get();
  private JTextPane textpane;
  public GAlgorithmSyntaxHighlighting(JTextPane _textpane) {
    textpane = _textpane;

    /**
     * @todo The hints below do not appear to have any effect. And if
     * they did, they would have to be brought up to date, which is a
     * daunting task, given all the synonyms and overloading.
     */
    APIdictionary.put("beginStep",
                      "Forces the animation to consider all graph changes one step until endStep() or another beginStep() is called.");
    APIdictionary.put("endStep",
                      "Ends a step in the graph state. If no beginStep() has been called previously, does nothing.");
    APIdictionary.put("isDirected",
                      "Returns True if the graph is directed, False otherwise.");
    APIdictionary.put("setDirected",
                      "Sets the graph to directed for True and undirected for False");
    APIdictionary.put("getNodes",
                      "Gets a list of all non-deleted nodes in the latest state of the graph.");
    APIdictionary.put("getEdges",
                      "Gets a list of all non-deleted edges in the latest state of the graph.");
    APIdictionary.put("getNodeById",
                      "Gets the node whose id matches the input. Returns null if the node doesn't exist or has been deleted.");
    APIdictionary.put("getEdgeById",
                      "Gets the edge whose id matches the input. Returns null if the edge doesn't exist or has been deleted.");
    APIdictionary.put("select",
                      "Sets the specified Node as the selected Node in the graph and deselects all other selected nodes.");
    APIdictionary.put("addNode",
                      "Adds a Node with default settings to the graph and returns a pointer to the new Node.");
    APIdictionary.put("addEdge",
                      "Adds an edge to the graph between the two specified nodes or node indices, and also stores the edge in each Node.");
    APIdictionary.put("isSelected",
                      "Returns True if the Node or Edge is selected in the latest state and False otherwise.");
    APIdictionary.put("setSelected",
                      "Sets the Node or Edge as selected. In contrast with the Graph's select function, this does not deselect other selected nodes in a graph.");
    APIdictionary.put("isVisited",
                      "Returns the true if the Node has been marked as visited and False otherwise.");
    APIdictionary.put("setVisited",
                      "Sets the Node's visited/marked property to the input value.");
    APIdictionary.put("isMarked",
                      "Returns the true if the Node has been marked and False otherwise.");
    APIdictionary.put("mark",
                      "Sets the Node's visited/marked property to True");
    APIdictionary.put("getWeight",
                      "Returns the weight of the current Node or Edge. The default weight is 0.");
    APIdictionary.put("setWeight",
                      "Sets the weight of the current Node or Edge.");
    APIdictionary.put("getUnvisitedPaths",
                      "Returns a List<Edge> object of all the Node's incident edges connecting to unvisited Nodes in the latest state of the graph.");
    APIdictionary.put("getVisitedPaths",
                      "Returns a List<Edge> object of all the Node's incident edges connecting to visited Nodes in the latest state of the graph.");
    APIdictionary.put("getUnvisitedAdjacentNodes",
                      "Returns a List<Node> object of all the Node's adjacent Nodes whose visited properties are False in the latest state of the graph.");
    APIdictionary.put("travel",
                      "Returns the other Node endpoint of the specified edge. Returns null if neither of the edge's endpoints is this Node. If e is a loop, returns this Node.");
    APIdictionary.put("getId",
                      "Returns the numerical id of the node. This will not change during Algorithm execution unless explicitly set in the user code.");
    APIdictionary.put("getColor",
                      "Gets the default color of the Node or Edge in the format '#RRGGBB'");
    APIdictionary.put("setColor",
                      "Sets the default color of the Node or Edge. This color will corresponds to the colored ring around a node and will be the default color when the node is not selected. A valid color input should be of the form '#RRGGBB'.");
    APIdictionary.put("getLabel",
                      "Returns the String label associated with the Node or Edge.");
    APIdictionary.put("setLabel",
                      "Sets the label associated with the Node or Edge.");
    APIdictionary.put("getPosition",
                      "Gets the position of the node on the coordinate plane.");
    APIdictionary.put("setPosition",
                      "Sets the position of the Node on the coordinate plane.");
    APIdictionary.put("setStringAttribute",
                      "Stores a String in the Node or Edge under the specified key. This will overwrite any String, Integer, or Double attribute already set with the same key.");
    APIdictionary.put("getStringAttribute",
                      "Gets the String value associated with the specified key. Returns null if the key doesn't exist or it exists but its value is not a String.");
    APIdictionary.put("setIntegerAttribute",
                      "Stores an Integer in the Node or Edge under the specified key. This will overwrite any String, Integer, or Double attribute already set with the same key.");
    APIdictionary.put("getIntegerAttribute",
                      "Gets the Integer value associated with the specified key. Returns null if the key doesn't exist or it exists but its value is not an Integer.");
    APIdictionary.put("setDoubleAttribute",
                      "Stores a Double in the Node or Edge under the specified key, This will overwrite any String, Integer, or Double attribute already set with the same key.");
    APIdictionary.put("getDoubleAttribute",
                      "Gets the Double value associated with the specified key. Returns null if the key doesn't exist or it exists but its value is not a Double.");
    APIdictionary.put("getSourceNode",
                      "Returns the source Node of the current Edge.");
    APIdictionary.put("setSourceNode",
                      "Sets the source Node of the current Edge.");
    APIdictionary.put("getTargetNode",
                      "Returns the destination Node of the current Edge.");
    APIdictionary.put("setTargetNode",
                      "Sets the destination Node of the current Edge.");
    APIdictionary.put("getOtherEndpoint",
                      "Returns the other endpoint of an edge, or NULL if the provided node is not either endpoint in the specified edge.");
    APIdictionary.put("getId",
                      "Returns the unique ID of the edge. This will not change during Algorithm execution unless explicitly set in the user code.");
    APIdictionary.put("for_nodes",
                      "Iterates over all Nodes in the graph.");
    APIdictionary.put("for_edges",
                      "Iterates over all Edges in the graph.");
    APIdictionary.put("function",
                      "Creates a function that can be called later.");

    StyledDocument doc = textpane.getStyledDocument();
    updateDocStyles(doc);

    // Initialize allMacrokeywords
    for ( int i = 0; i < Macro.MACROS.size(); i++ ) {
      allMacrokeywords[i] = Macro.MACROS.get(i).getName();
    }
  }

  /**
   * An immutable list of all keywords employed in the Java language, plus
   * others that come from Java and are relevant to Galant. Also includes
   * all Galant-specific types.
   */
  public static final String[] allJavaKeywords = new String[] {
    "abstract", "assert", "boolean",
    "break", "byte", "case", "catch", "char", "class", "const",
    "continue", "default", "do", "double", "else", "enum", "extends",
    "final", "finally", "float", "for", "goto", "if",
    "implements", "import", "instanceof", "int", "interface", "long",
    "native", "new",
    "package", "private", "protected", "public", "return", "short",
    "static", "strictfp",
    "super", "switch", "synchronized", "this", "throw", "throws",
    "transient", "try",
    "void", "volatile", "while", "false", "null", "true", "equals",
    "Integer", "Double", "Boolean", "String", "Node", "Edge",
    "NodeList", "EdgeList", "NodeSet", "EdgeSet",
    "NodeQueue", "EdgeQueue", "NodePriorityQueue", "EdgePriorityQueue",
    "NodeStack", "EdgeStack"
  };

  /**
   * An immutable list of all API calls predefined for the user's
   * benefit.
   *
   * @todo there are almost certainly missing items
   */
  public static final String[] allAPIkeywords = new String[] {
    "beginStep", "endStep", "step",
    "display", "print", "error",
    "integer", "real",
    "movesNodes", "isDirected", "setDirected",
    "source", "target", "otherEnd",
    "neighbors", "inEdges", "outEdges",
    "visibleNeighbors", "visibleEdges",
    "visibleInEdges", "visibleOutEdges",
    "nodes", "edges", "startNode", "id", "select", "highlight",
    "selected", "marked", "mark", "unmark", "marked",
    "highlight", "unhighlight", "highlighted",
    "addNode", "addEdge",
    "setWeight", "weight",
    "label", "color", "uncolor", "hasColor", "set", "clear",
    "clearLabel", "clearWeight",
    "clearNodeLabels", "clearNodeWeights", "clearNodeColors",
    "clearNodeHighlighting", "clearMarks",
    "clearEdgeLabels", "clearEdgeWeights", "clearEdgeColors",
    "clearEdgeHighlighting",
    "clearAllNode", "clearAllEdge",
    "getPosition", "setPosition", "getX", "getY", "setX", "setY",
    "getInteger", "getDouble", "getBoolean", "getReal",
    "getNodeSet", "getEdgeSet",
    "neighborSet", "edgeSet", "incomingSet", "outgoingSet",
    "hide", "show", "visible", "hidden",
    "hideLabel", "showLabel",
    "hideWeight", "showWeight",
    "hideEdgeWeights", "showEdgeWeights",
    "hideEdgeLabels", "showEdgeLabels",
    "hideNodeWeights", "showNodeWeights",
    "hideNodeLabels", "showNodeLabels",
    "distance"
  };

  /**
   * An immutable list of all Macros predefined for the user's benefit.
   * It gets initialized when this GAlgorithmSyntaxHighlighting was created. See
   *constructor.
   */
  public static String[] allMacrokeywords = new String[Macro.MACROS.size()];

  @Override
  public void run() {
    try {
      String content = textpane.getText().replace("\r\n", "\n");
      StyledDocument doc = textpane.getStyledDocument();
      doc.setCharacterAttributes(0, doc.getLength(),
                                 doc.getStyle("regular"), true);
      applyStyleToKeywords(doc, content,
                           allJavaKeywords, javaKeywordStyleName);
      applyStyleToKeywords(doc, content,
                           allAPIkeywords, "apiKeyword");
      applyStyleToKeywords(doc, content,
                           allMacrokeywords, "macroKeyword");
      applyStyleToCommentsAndStrings(doc, content, "comment", "string");
      textpane.setDocument(doc);
    } catch ( Exception e ) {
      ExceptionDialog.displayExceptionInDialog(e);
    }
  }

  private static void updateDocStyles(StyledDocument doc) {
    Style def =
      StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
    StyleConstants.setBold(def, true);
    Style regular = doc.addStyle("regular", def);
    StyleConstants.setFontFamily(def, "SansSerif");

    Style s = doc.addStyle(javaKeywordStyleName, regular);
    StyleConstants.setForeground(s, javaKeywordColor);

    Style q = doc.addStyle("apiKeyword", regular);
    StyleConstants.setForeground(q, apiKeywordColor);

    Style m = doc.addStyle("macroKeyword", regular);
    StyleConstants.setForeground(m, macroKeywordColor);

    Style comment = doc.addStyle("comment", regular);
    StyleConstants.setItalic(comment, true);
    StyleConstants.setForeground(comment, COMMENT_COLOR);

    Style string = doc.addStyle("string", regular);
    StyleConstants.setFontFamily(string, "Monospaced");
    StyleConstants.setForeground(string, STRING_COLOR);
  }

  private static void applyStyleToKeywords(StyledDocument doc,
                                           String         content,
                                           String[]       keywords,
                                           String         styleName)
  {
    for ( String keyword : keywords ) {
      int index = 0;
      while ( ( index = content.indexOf(keyword, index) ) != -1 ) {
        char prev = (index > 0) ? content.charAt(index - 1) : ' ';
        char next = ( index + keyword.length() < content.length() )
          ? content.charAt( index + keyword.length() ) : ' ';
        if ( ! Character.isJavaIdentifierPart(prev)
             && ! Character.isJavaIdentifierPart(next) ) {
          doc.setCharacterAttributes(index,
                                     keyword.length(),
                                     doc.getStyle(styleName),
                                     true);
        }
        index += keyword.length();
      }
    }
  }

  /**
   * @todo eventually, comments in this context and in CodeIntegrator might
   * be handled by a single mechanism encapsulated in a class
   */
  private enum State {
    DEFAULT,                    // not in comment, last char not '/'
    SLASH,                      // not in comment, last char is '/'
    SLASH_STAR,                 // in C-style comment, last char not '*'
    STAR,                       // in C-style comment, last char is '*'
    SLASH_SLASH,                // in C++-style comment
    IN_STRING,                  // inside a double-quoted string
    BACKSLASH                   // inside double-quoted string, escaped (\)
  }

  private static void applyStyleToCommentsAndStrings(StyledDocument doc,
                                                     String         content,
                                                     String         commentStyle,
                                                     String         stringStyle) {
    State state = State.DEFAULT;
    int startComment = -1;      // position of beginning of most recent comment
    int startString = -1;       // position of beginning of most recent string

    for ( int i = 0; i < content.length(); i++ ) {
      char current = content.charAt(i);

      if ( state == State.DEFAULT ) {
        if ( current == '/' ) state = State.SLASH;
        else if ( current == '"' ) {
          state = State.IN_STRING;
          startString = i;
        }
      } else if ( state == State.SLASH ) {
        if ( current == '*' ) {
          state = State.SLASH_STAR;
          startComment = i - 1;
        } else if ( current == '/' ) {
          state = State.SLASH_SLASH;
          startComment = i - 1;
        } else {
          state = State.DEFAULT;
        }
      } else if ( state == State.SLASH_STAR ) {
        if ( current == '*' ) state = State.STAR;
      } else if ( state == State.STAR ) {
        if ( current == '/' ) {
          state = State.DEFAULT;
          doc.setCharacterAttributes(startComment,
                                     i - startComment,
                                     doc.getStyle(commentStyle),
                                     true);
        } else state = State.SLASH_STAR;
      } else if ( state == State.SLASH_SLASH ) {
        if ( current == '\n' ) {
          state = State.DEFAULT;
          doc.setCharacterAttributes(startComment,
                                     i - startComment,
                                     doc.getStyle(commentStyle),
                                     true);
        }
      } else if ( state == State.IN_STRING ) {
        if ( current == '"' ) {
          state = State.DEFAULT;
          // we want the " to be included in the highlighting;
          // hence the + 1
          doc.setCharacterAttributes(startString,
                                     i - startString + 1,
                                     doc.getStyle(stringStyle),
                                     true);
        } else if ( current == '\\' ) {
          state = State.BACKSLASH;
        }
      } else if ( state == State.BACKSLASH ) {
        state = State.IN_STRING;
      }       // states
    }     // for all chars in content
  }   // applyStyleToCommentsAndStrings
}

// [Last modified: 2020 05 10 at 20:16:29 GMT]
