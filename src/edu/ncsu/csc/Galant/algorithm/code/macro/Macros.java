package edu.ncsu.csc.Galant.algorithm.code.macro;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
/** 
 * The macros used in preprocessing. 
 */
public class Macros
	{
		public static void macros()
			{
				Macro.MACROS.add(new Macro("\\bbool\\b"){
					@Override
					protected String modify(String code, MatchResult match) throws MalformedMacroException
						{
							return "boolean";
						}
				});

				
				Macro.MACROS.add(new Macro("algorithm"){
					@Override
					protected String modify(String code, MatchResult match) throws MalformedMacroException
						{
							return "public void run()" ;
						}
				});

				/*
				 * for_outgoing: iterates over the outgoing nodes and edges
				 * of a given node, based solely on source/target information
				 * and not on whether the graph is directed.
				 * 
				 * Usage: for_outgoing(<node>, <edge>, <adjacentNode>) {<code_block>}
				 * 
				 * Parameters:
				 * 
				 * node: the name of a variable of type Node. This is the starting node.
				 * 
				 * adjacentNode: a variable name. Within the code block, this can be used to refer
				 * to the current adjacent node as a Node object.
				 * 
				 * edge: a variable name. Within the code block, this can be used to refer to the
				 * current incident edge as an Edge object. <edge> connects <node> and <adjacentNode>.
				 * 
				 * code_block: a block of code that is executed for each adjacent node / incident
				 * edge of <node>. The curly braces are required.
				 */
				Macro.MACROS.add(new ParameterizedMacro("for_outgoing", 3, true){
					@Override
					protected String modifyMatch(String code, MatchResult nameMatch, String[] args, String whitespace,
						String block)
						{
							String node = args[0], edge = args[1], adjacentNode = args[2];
							return Matcher.quoteReplacement("for(Edge " + edge + " : " + node + ".getOutgoingEdges())" + whitespace +
								"{ Node " + adjacentNode + " = " + node + ".travel(" + edge + ");" + block + "}");
						}
				});

				/*
				 * for_incoming: iterates over the incoming nodes and edges
				 * of a given node (all edges in case of an undirected
				 * graph), based solely on source/target information and not
				 * on whether the graph is directed.
				 * 
				 * Usage: for_incoming(<node>, <edge>, <adjacentNode>) {<code_block>}
				 * 
				 * Parameters:
				 * 
				 * node: the name of a variable of type Node. This is the starting node.
				 * 
				 * adjacentNode: a variable name. Within the code block, this can be used to refer
				 * to the current adjacent node as a Node object.
				 * 
				 * edge: a variable name. Within the code block, this can be used to refer to the
				 * current incident edge as an Edge object. <edge> connects <node> and <adjacentNode>.
				 * 
				 * code_block: a block of code that is executed for each adjacent node / incident
				 * edge of <node>. The curly braces are required.
				 */
				Macro.MACROS.add(new ParameterizedMacro("for_incoming", 3, true){
					@Override
					protected String modifyMatch(String code, MatchResult nameMatch, String[] args, String whitespace,
						String block)
						{
							String node = args[0], edge = args[1], adjacentNode = args[2];
							return Matcher.quoteReplacement("for(Edge " + edge + " : " + node + ".getIncomingEdges())" + whitespace +
								"{ Node " + adjacentNode + " = " + node + ".travel(" + edge + ");" + block + "}");
						}
				});

				/*
				 * for_adjacent: iterates over all incident edges and
				 * adjacent nodes of a given node, but only the outgoing
				 * edges for a directed graph
				 * 
				 * Usage: for_adjacent(<node>, <edge>, <adjacentNode>) {<code_block>}
				 * 
				 * Parameters:
				 * 
				 * node: the name of a variable of type Node. This is the starting node.
				 * 
				 * adjacentNode: a variable name. Within the code block, this can be used to refer
				 * to the current adjacent node as a Node object.
				 * 
				 * edge: a variable name. Within the code block, this can be used to refer to the
				 * current incident edge as an Edge object. <edge> connects <node> and <adjacentNode>.
				 * 
				 * code_block: a block of code that is executed for each adjacent node / incident
				 * edge of <node>. The curly braces are required.
				 */
				Macro.MACROS.add(new ParameterizedMacro("for_adjacent", 3, true){
					@Override
					protected String modifyMatch(String code, MatchResult nameMatch, String[] args, String whitespace,
						String block)
						{
							String node = args[0], edge = args[1], adjacentNode = args[2];
							return Matcher.quoteReplacement("for(Edge " + edge + " : " + node + ".getIncidentEdges())" + whitespace +
								"{ Node " + adjacentNode + " = " + node + ".travel(" + edge + ");" + block + "}");
						}
				});

				/*
				 * for_nodes: iterates over all nodes in the graph.
				 * 
				 * Usage: for_nodes(<node>) <code_block>
				 * 
				 * Parameters:
				 * 
				 * node: a variable name. Within the code block, this can be used to refer
				 * to the current node as a Node object.
				 * 
				 * code_block: a block of code that is executed for each node in the graph.
				 */
				Macro.MACROS.add(new ParameterizedMacro("for_nodes", 1){
					@Override
					protected String modifyMatch(String code, MatchResult nameMatch, String[] args, String whitespace,
						String block)
						{
							return Matcher.quoteReplacement("for(Node " + args[0] + " : getNodes())");
						}
				});

				/*
				 * for_edges: iterates over all edges in the graph.
				 * 
				 * Usage: for_edges(<edge>) <code_block>
				 * 
				 * Parameters:
				 * 
				 * edge: a variable name. Within the code block, this can be used to refer
				 * to the current edge as an Edge object.
				 * 
				 * code_block: a block of code that is executed for each edge in the graph.
				 */
				Macro.MACROS.add(new ParameterizedMacro("for_edges", 1){
					@Override
					protected String modifyMatch(String code, MatchResult nameMatch, String[] args, String whitespace,
						String block)
						{
							return Matcher.quoteReplacement("for(Edge " + args[0] + " : getEdges())");
						}
				});

			Macro.MACROS.add(new ParameterizedMacro(MacroUtil.replaceWhitespace("new_function (\\S+)?  (\\S+)"), true){
					Map<String, String> wrapperMap = new HashMap<String, String>();
						{
							wrapperMap.put("int", "Integer");
							wrapperMap.put("long", "Long");
							wrapperMap.put("short", "Short");
							wrapperMap.put("byte", "Byte");
							wrapperMap.put("double", "Double");
							wrapperMap.put("float", "Float");
							wrapperMap.put("boolean", "Boolean");
							wrapperMap.put("char", "Character");
							// map "void" to " "(empty string)
							wrapperMap.put("void", " ");
						}
					private String getObjectType(String type)
						{
							int bracketIndex = type.indexOf('[');
							boolean isArrayType = bracketIndex != -1;
							String nonArrayType = isArrayType ? type.substring(0, bracketIndex).trim() : type;
							String wrapper = wrapperMap.get(nonArrayType);
							return wrapper == null ? type : isArrayType ? wrapper + type.substring(nonArrayType.length())
								: wrapper;
						}

					abstract class Param
						{
							public String name, declaredType, type;
							public String paramToString = "";
							protected int index;
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
									// this.index = index;

								}

							// it can convert more than two parameter into String.
							public String toString() 
								{	
									return paramToString;
								}	
						}

					// Notes: Since recursive mechanism is not used anymore, parent and child param is 
					// removed. -Yuang
					class PairParam extends Param
						{
							protected SingleParam first_param;
							protected SingleParam second_param;
							/** Creates a new <code>PairParam</code>. */
							public PairParam(int index, String[] paramStrings)
								{
									super(index, paramStrings);
									type = initType();
								}
							protected String initType()
								{
									// return "Pair<" + getObjectType(declaredType) + ", " + child.type + ">";
									return getObjectType(declaredType) + ", " + super.type ;
								}
						}
					class SingleParam extends Param
						{
							/** Creates a new <code>SingleParam</code>. */
							public SingleParam(String[] paramStrings)
								{
									super(0, paramStrings);
									type = getObjectType(declaredType);
								}
						}
					class NoParam extends Param
						{
							/** Creates a new <code>NoParam</code>. */
							public NoParam()
								{	
									// map "void" to " "(empty string)
									super(-1, null);
									type = " ";
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


							// if the function references itself in its body, it can't be initialized all at once
							// so declare the actual var first, so it can be referenced, then create a temp var that references
							// the actual var, then assign the temp var as the actual var
							// But since the actual var has to be final so it can be referenced in an anon class,
							// make it actually be a Pair with one of the elements being the "actual var"
							// So you initialize the Pair and don't change it, and assigning the temp var as the actual var
							// is just a matter of changing an element of the Pair.
							// (I would have used a single-element array, but Function is a parameterized type, and arrays
							// don't play well with those.)

							/* Note mainParam.type does not successfully return its pair param's name and type due to only the first
							   parameter is created  -Yuang */
							return Matcher.quoteReplacement("public" + returnType + " " + name + " ( " + mainParam.toString() + ")" + "{" 
								+  block 

								/*+ "}" 
                                      + " catch (Exception e)"
                                      + " { \n if ( e instanceof GalantException )"
                                      + " {GalantException ge = (GalantException) e;"
                                      + " ge.report(\"\"); ge.display(); }"
                                      + " else e.printStackTrace(System.out);} }" */
								+ "}" ); 
						}
				});				


				/*
				 * function: creates a function that can be called later.
				 * 				Functions are objects (of type Function), and so they can be assigned to variables
				 * 				and passed to other functions.
				 * (TODO: how to declare a function variable without the implementation-dependent type variables?)
				 * 
				 * Usage: function [<return_type>] <name>(<params>) {<code_block>}
				 * 		  <return_type> <- <name>(<args>)
				 * 
				 * Parameters:
				 * 
				 * return_type: a type. If the function returns a value, this should indicate the type of value returned.
				 * 				If no value is returned, this is not necessary.
				 * 
				 * name: a variable name. Used to identify and call the function.
				 * 
				 * params: a comma-separated list of variable names, including types (e.g., "int i, String str").
				 * 			Can be referenced from within the code block. May be empty, if there are no parameters.
				 * 
				 * args: values of the types determined by <params>, passed in when the function is called.
				 * 
				 * code_block: a block of code that is executed when the function is called. The curly braces are required.
				 */
				Macro.MACROS.add(new ParameterizedMacro(MacroUtil.replaceWhitespace("function (\\S+)?  (\\S+)"), true){
					Map<String, String> wrapperMap = new HashMap<String, String>();
						{
							wrapperMap.put("int", "Integer");
							wrapperMap.put("long", "Long");
							wrapperMap.put("short", "Short");
							wrapperMap.put("byte", "Byte");
							wrapperMap.put("double", "Double");
							wrapperMap.put("float", "Float");
							wrapperMap.put("boolean", "Boolean");
							wrapperMap.put("char", "Character");
							wrapperMap.put("void", "Void");
						}
					private String getObjectType(String type)
						{
							int bracketIndex = type.indexOf('[');
							boolean isArrayType = bracketIndex != -1;
							String nonArrayType = isArrayType ? type.substring(0, bracketIndex).trim() : type;
							String wrapper = wrapperMap.get(nonArrayType);
							return wrapper == null ? type : isArrayType ? wrapper + type.substring(nonArrayType.length())
								: wrapper;
						}

					abstract class Param
						{
							public String name, declaredType, type;
							protected int index;
							public Param(int index, String[] paramStrings)
								{
									if(paramStrings != null)
										{
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
									this.index = index;
								}
							public String declareArgs(String args)
								{
									return declaredType + " " + name + " = " + args + getToArg() + ";";
								}
							public abstract String getToArg();
							public abstract String makeArg(String[] args);
						}

					class PairParam extends Param
						{
							protected PairParam parent;
							protected PairParam child;
							/** Creates a new <code>PairParam</code>. */
							public PairParam(PairParam parent, int index, String[] paramStrings)
								{
									super(index, paramStrings);
									this.parent = parent;
									child = initChild(paramStrings);
									type = initType();
								}
							protected PairParam initChild(String[] paramStrings)
								{
									return index == paramStrings.length - 2 ? new LastParam(this, paramStrings)
										: new PairParam(this, index + 1, paramStrings);
								}
							protected String initType()
								{
									return "Pair<" + getObjectType(declaredType) + ", " + child.type + ">";
								}

							@Override
							public String makeArg(String[] args)
								{
									return "new " + type + "(" + args[index] + ", " + child.makeArg(args) + ")";
								}

							public String getToPair()
								{
									return parent.getToPair() + ".getElement2()";
								}

							@Override
							public String getToArg()
								{
									return getToPair() + ".getElement1()";
								}

							@Override
							public String declareArgs(String args)
								{
									return super.declareArgs(args) + (child == null ? "" : child.declareArgs(args));
								}
						}
					class FirstParam extends PairParam
						{
							public FirstParam(String[] paramStrings)
								{
									super(null, 0, paramStrings);
								}
							@Override
							public String getToPair()
								{
									return "";
								}
						}
					class LastParam extends PairParam
						{
							/** Creates a new <code>LastParam</code>. */
							public LastParam(PairParam parent, String[] paramStrings)
								{
									super(parent, paramStrings.length - 1, paramStrings);
									child = null;
								}
							@Override
							protected PairParam initChild(String[] paramStrings)
								{
									return null;
								}
							@Override
							protected String initType()
								{
									return getObjectType(declaredType);
								}
							@Override
							public String getToPair()
								{
									return parent.getToPair();
								}
							@Override
							public String getToArg()
								{
									return getToPair() + ".getElement2()";
								}
							@Override
							public String makeArg(String[] args)
								{
									return args[index];
								}
						}
					class SingleParam extends Param
						{
							/** Creates a new <code>SingleParam</code>. */
							public SingleParam(String[] paramStrings)
								{
									super(0, paramStrings);
									type = getObjectType(declaredType);
								}

							@Override
							public String makeArg(String[] args)
								{
									return args[index];
								}

							@Override
							public String getToArg()
								{
									return "";
								}
						}
					class NoParam extends Param
						{
							/** Creates a new <code>NoParam</code>. */
							public NoParam()
								{
									super(-1, null);
									type = "Void";
								}

							@Override
							public String makeArg(String[] args)
								{
									return "null";
								}

							@Override
							public String getToArg()
								{
									return null;
								}
							@Override
							public String declareArgs(String args)
								{
									return "";
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
								args.length == 0 ? new NoParam() : args.length == 1 ? new SingleParam(args) : new FirstParam(
									args);

							// get R from return type
							String declaredReturnType = nameMatch.group(1);
							String returnType = declaredReturnType == null ? "Void" : getObjectType(declaredReturnType);

							createCallMacro(name, args.length, mainParam);

							String funcType = "Function<" + mainParam.type + ", " + returnType + ">";
							// if the function references itself in its body, it can't be initialized all at once
							// so declare the actual var first, so it can be referenced, then create a temp var that references
							// the actual var, then assign the temp var as the actual var
							// But since the actual var has to be final so it can be referenced in an anon class,
							// make it actually be a Pair with one of the elements being the "actual var"
							// So you initialize the Pair and don't change it, and assigning the temp var as the actual var
							// is just a matter of changing an element of the Pair.
							// (I would have used a single-element array, but Function is a parameterized type, and arrays
							// don't play well with those.)
							return Matcher.quoteReplacement("final Pair<" + funcType + ", Void> " + name + " = new Pair<" +
								funcType + ", Void>(null, null); " + funcType + " $" + name + " = new " + funcType +
								"(){public " + returnType + " invoke(" + mainParam.type + " $args)" + whitespace + "{" +
								mainParam.declareArgs("$args") + block + (declaredReturnType == null ? "return null;" : "") +
								"}}; " + name + ".setElement1($" + name + ");");
						}

					private void createCallMacro(final String name, int numParams, final Param mainParam)
						{
							Macro.GENERATED_MACROS.add(new ParameterizedMacro(name, numParams){
								@Override
								protected String modifyMatch(String code, MatchResult nameMatch, String[] args,
									String whitespace, String block)
									{
										return Matcher.quoteReplacement(name + ".getElement1().invoke(" +
											mainParam.makeArg(args) + ")");
									}
							});
						}
				});
			}
	}

//  [Last modified: 2013 06 27 at 23:11:29 GMT]
