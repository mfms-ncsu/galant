package edu.ncsu.csc.Galant.algorithm.code.macro;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The macros used in preprocessing.
 */
public class Macros {
    /**
     * Defines macros and add it to an array list: MACROS<br>
     * Current implementation includes:<br>
     * SimpleReplacementMacro "algorithm" (for syntax highlighting only)<br>
     * ParameterizedMacro "sort" <br>
     * ParameterizedMacro "for_outgoing"<br>
     * ParameterizedMacro "for_incoming"<br>
     * ParameterizedMacro "for_adjacent"<br>
     * ParameterizedMacro "for_nodes"<br>
     * ParameterizedMacro "for_edges"<br>
     * ParameterizedMacro "function (.., ..) {...}"</p>
     */

    public static void macros() {

        /** This is only for the purpose of syntax highlighting */
        Macro.MACROS.add(new SimpleReplacementMacro("algorithm", "algorithm") {
                public String getName() {
                    return "algorithm";
                }
            });

        /**
         * @todo For some odd reason, a simple replacement does not work
         * here; maybe it's the fact that when I tried it in an algorithm, I
         * said 'sort(' - no space between 'sort' and the paren; in any case
         * it made the macro preprocessor hang.
         */
        // Macro.MACROS.add(new SimpleReplacementMacro("sort", "Collections.sort") {
        //         public String getName() {
        //             return "sort";
        //         }
        //     });

        /**
         * Either sort(Collection L) or sort(Collection L, Comparator C)
         */
        Macro.MACROS.add(new ParameterizedMacro("sort", 1, 2, false) {
            @Override
            protected String modifyMatch(String code,
                                         MatchResult nameMatch,
                                         String[] args,
                                         String whitespace,
                                         String block) {
              String toBeSorted = args[0];
              if ( args.length == 1 )
                return Matcher.quoteReplacement("Collections.sort("
                                                + toBeSorted + ")");
              else {
                String comparator = args[1];
                return Matcher.quoteReplacement("Collections.sort("
                                                + toBeSorted
                                                + ", " + comparator + ")");
              }
            }
          }
          );

        /**
         * for_outgoing: iterates over the outgoing nodes and edges
         * of a given node, based solely on source/target information
         * and not on whether the graph is directed.
         *
         * Usage: for_outgoing(<em>node</em>, <em>edge</em>, <em>adjNode</em>) {<code_block>}
         *
         * Parameters:
         * <em>node</em>: the name of a variable of type Node; this is the
         * starting node.
         * <em>adjacentNode</em>: a variable name; within the code block,
         * this can be used to refer to the current adjacent node as a Node
         * object.
         * <em>edge</em>: a variable name; within the code block, this can be
         * used to refer to the current incident edge as an Edge object, so,
         * <em>edge</em> connects <em>node</em> and <em>adjNode</em>.
         * <em>code_block</em>: a block of code that is executed for each
         * adjacent node / incident edge of <em>node</em>. The curly braces
         * are required.
         */
        Macro.MACROS.add(new ParameterizedMacro("for_outgoing", 3, true){
                @Override
                protected String modifyMatch(String code,
                                             MatchResult nameMatch,
                                             String[] args,
                                             String whitespace,
                                             String block) {
                    String node = args[0],
                        edge = args[1],
                        adjacentNode = args[2];
                    return Matcher.quoteReplacement("for ( Edge "
                                                    + edge + " : "
                                                    + "outEdges(" + node + ") )"
                                                    + whitespace
                                                    + "{ Node "
                                                    + adjacentNode
                                                    + " = "
                                                    + "otherEnd("
                                                    + edge + ", "
                                                    + node + ");"
                                                    + block + "}");
                }
            });

        /** * for_incoming: iterates over the incoming nodes and edges
         * of a given node (all edges in case of an undirected
         * graph), based solely on source/target information and not
         * on whether the graph is directed.
         *
         * Usage:
         * for_incoming(<em>node</em>, <em>edge</em>,
         *              <em>adjNode</em>) {<em>code_block</em>}
         *
         * Parameters:
         * <em>node</em>: the name of a variable of type Node; this is the
         * starting node.
         * <em>adjNode</em>: a variable name; within the code block, this can
         * be used to refer to the current adjacent node as a Node object.
         * <em>edge</em>: a variable name; within the code block, this can be
         * used to refer to the current incident edge as an Edge object.
         * <em>edge</em> connects <em>node</em> and <em>adjNode</em>.
         * <em>code_block</em>: a block of code that is executed for each
         * adjacent node / incident edge of <em>node</em>. The curly braces
         * are required.
         */
        Macro.MACROS.add(new ParameterizedMacro("for_incoming", 3, true){
                @Override
                protected String modifyMatch(String code,
                                             MatchResult nameMatch,
                                             String[] args,
                                             String whitespace,
                                             String block) {
                    String node = args[0],
                        edge = args[1],
                        adjacentNode = args[2];
                    return Matcher.quoteReplacement("for ( Edge "
                                                    + edge + " : "
                                                    + "inEdges(" + node + ") )"
                                                    + whitespace
                                                    + "{ Node "
                                                    + adjacentNode
                                                    + " = "
                                                    + "otherEnd("
                                                    + edge + ", "
                                                    + node + ");"
                                                    + block + "}");
                }
            });

        /**
         * for_adjacent: iterates over all incident edges and
         * adjacent nodes of a given node, but only the outgoing
         * edges for a directed graph
         *
         * Usage:
         * for_adjacent(<em>node</em>, <em>edge</em>, <em>adjNode</em>) {
         *    <em>code_block</em>
         * }
         *
         * Parameters:
         * <em>node</em>: the name of a variable of type Node. This is the
         * starting node.<br>
         * <em>adjNode</em>: a variable name. Within the code block, this can
         * be used to refer to the current adjacent node as a Node object.<br>
         * <em>edge</em>: a variable name. Within the code block, this can be
         * used to refer to the current incident edge as an Edge
         * object. <em>edge</em> connects <em>node</em> and <em>adjNode</em><br>
         * <em>code_block</em>: a block of code that is executed for each
         * adjacent node / incident edge of <em>node</em>. The curly braces
         * are required.
         */
        Macro.MACROS.add(new ParameterizedMacro("for_adjacent", 3, true){
                @Override
                protected String modifyMatch(String code,
                                             MatchResult nameMatch,
                                             String[] args,
                                             String whitespace,
                                             String block) {
                    String node = args[0],
                        edge = args[1],
                        adjacentNode = args[2];
                    return Matcher.quoteReplacement("for ( Edge "
                                                    + edge + " : "
                                                    + "edges(" + node + ") )"
                                                    + whitespace
                                                    + "{ Node "
                                                    + adjacentNode
                                                    + " = "
                                                    + "otherEnd("
                                                    + edge + ", "
                                                    + node + ");"
                                                    + block + "}");
                }
            });

        /**
         * for_nodes: iterates over all nodes in the graph.  Usage:
         * for_nodes(<em>node</em>) <em>code_block</em> Parameters:
         * <em>node</em>: a variable name. Within the code block, this can be
         * used to refer to the current node as a Node object.
         * <em>code_block</em>: a block of code that is executed for each
         * node in the graph.
         */
        Macro.MACROS.add(new ParameterizedMacro("for_nodes", 1){
                @Override
                protected String modifyMatch(String code,
                                             MatchResult nameMatch,
                                             String[] args,
                                             String whitespace,
                                             String block) {
                    return Matcher.quoteReplacement("for(Node "
                                                    + args[0]
                                                    + " : getNodes())");
                }
            });

        /**
         * <code>for_edges</code>: iterates over all edges in the graph.
         * Usage: for_edges(<em>edge</em>) <em>code_block</em>
         * Parameters:
         * <em>edge</em>: a variable name. Within the code block, this can be
         * used to refer to the current edge as an Edge object.
         * <em>code_block</em>: a block of code that is executed for each
         * edge in the graph.
         */
        Macro.MACROS.add(new ParameterizedMacro("for_edges", 1){
                @Override
                protected String modifyMatch(String code, MatchResult nameMatch, String[] args, String whitespace,
                                             String block)
                {
                    return Matcher.quoteReplacement("for(Edge " + args[0] + " : getEdges())");
                }
            });

        Macro.MACROS.add(new ParameterizedMacro(MacroUtil.replaceWhitespace("function (\\S+)?  (\\S+)"), true){

                @Override
                public String getName() {
                    return "function";
                }

                private String getObjectType(String type)
                {
                    return " " + type;
                }

                abstract class Param
                {
                    public String name, declaredType, type;
                    public String paramToString = "";
                    public Param(int index, String[] paramStrings)
                    {
                        if(paramStrings != null)
                            {

                                StringBuilder stringBuilder = new StringBuilder();
                                for(int i = 0; i < paramStrings.length; i++) {
                                    stringBuilder.append(paramStrings[i]);
                                    if( i < paramStrings.length - 1 ){
                                        stringBuilder.append(",");
                                    }
                                }

                                paramToString = stringBuilder.toString();

                                String paramString = paramStrings[index];
                                Matcher matcher = Pattern.compile("\\s+").matcher(paramString);
                                StringBuilder bracketBuilder = new StringBuilder();
                                int start = 0;
                                while(matcher.find())
                                    {
                                        bracketBuilder.append(paramString.substring(start, matcher.end()));
                                        start = matcher.end();
                                    }
                                name = paramString.substring(start);
                                // trim just trailing whitespace
                                declaredType = ("a" + bracketBuilder).trim().substring(1);
                            }	
                    }

                    public String toString() 
                    {	
                        return paramToString;
                    }	
                }

                class PairParam extends Param
                {
                    /** Creates a new <code>PairParam</code>. */
                    public PairParam(int index, String[] paramStrings)
                    {
                        super(index, paramStrings);
                    }

                }
                class SingleParam extends Param
                {
                    /** Creates a new <code>SingleParam</code>. */
                    public SingleParam(String[] paramStrings)
                    {
                        super(0, paramStrings);
                    }
                }
                class NoParam extends Param
                {
                    /** Creates a new <code>NoParam</code>. */
                    public NoParam()
                    {	
                        super(-1, null);
                    }
                }

                @Override
                protected String modifyMatch(String code, MatchResult nameMatch, String[] args, String whitespace,
                                             String block)
                {
                    // name
                    String name = nameMatch.group(2);

                    // get P from params
                    Param mainParam =
                        args.length == 0 ? new NoParam() : args.length == 1 ? new SingleParam(args) : new PairParam(
                                                                                                                    0, args);

                    // get R from return type
                    String declaredReturnType = nameMatch.group(1);
                    String returnType = declaredReturnType == null ? " void" : getObjectType(declaredReturnType);

                    return Matcher.quoteReplacement("public" + returnType + " " + name + " ( " + mainParam.toString() + ")" + " throws Terminate, GalantException {" 
                                                    +  block + "}" ); 
                }
            });

    }
}

//  [Last modified: 2017 01 19 at 17:11:15 GMT]
