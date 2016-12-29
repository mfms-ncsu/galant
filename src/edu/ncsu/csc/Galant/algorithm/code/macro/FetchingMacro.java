package edu.ncsu.csc.Galant.algorithm.code.macro;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.ncsu.csc.Galant.algorithm.code.macro.MacroUtil.IdentifierPartLocation;
import edu.ncsu.csc.Galant.algorithm.code.macro.MacroUtil.NestedRegexResult;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * <p>
 * A macro of the form: <br />
 * <i>name</i> <i>variable</i><code>;</code>
 * </p>
 * <p>
 * Fetch and return Node/Edges on the graph. Declare the variable as global
 * and initilizae them inside of algorithm{}.
 * </p>
 * <p>
 * The initial use (now deprecated) was to, for example, allow the animator
 * to use the variable NodeList to refer to the list of nodes in the
 * graph. This does not require any macro replacement in the traditional
 * sense, but does require the insertion of a declaration and initialization
 * at the beginning of the algorithm block.
 * </p>
 */
public abstract class FetchingMacro extends Macro {
    String initializationPrefix = "";
    String name = "";

    /**
     * Creates a new <code>FetchingMacro</code>. <code>name</code>
     * speclify the name.  <code>intilizationPrefix</code> defines the
     * expected variable type. e.g: for macro that gets the number of
     * nodes on the graph, we use "int" as our
     * <code>intilizationPrefix</code>.
     */
    public FetchingMacro(String name, String initializationPrefix) {
        super(name + "(.*)(\\;)");
        this.initializationPrefix = initializationPrefix;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * This method returns the variable name as a string. e.g: for macro
     * that gets the number of nodes on the graph, this method would
     * return <code>t</code> if input were <code>numOfNodes t;</code>.
     * @param code the code of this macro apply to
     * @throws MalformedMacroException if input is not in the correct
     * pattern or there is no <code>;</code> at the end of the syntax.
     */
    public String getVariableName(String code)
        throws MalformedMacroException
    {
        Matcher matcher = getPattern().matcher(code);
        if ( matcher.find() ) {
            String originalExpression = matcher.group(0);
            LogHelper.logDebug("getVariableName ->" + originalExpression + "<--");
            matcher = Pattern.compile("(\\s)(.*)(\\;)").matcher(originalExpression);
            try{
                matcher.find();
                return matcher.group(0).replace(";", "");
            }
            catch(Exception e) {
                throw new MalformedMacroException("Wrong macro sytax. Separate variable with white space");
            }
        }
        return null;
    }

    @Override
    protected final String modify(String code, MatchResult match)
        throws MalformedMacroException
    {
        return initializationPrefix + getVariableName(code);
    }
}

//  [Last modified: 2016 12 29 at 19:40:00 GMT]
