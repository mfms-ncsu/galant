/**
 * Used to display messages on the graph window
 */
package edu.ncsu.csc.Galant.graph.component;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.algorithm.Terminate;

public class MessageBanner extends GraphElement {
    public MessageBanner(Graph graph) {
        super(graph);
    }

    public boolean set(String message) throws Terminate {
        return super.set("message", message);
    }

    public void clear() throws Terminate {
        super.remove("message");
    }

    /**
     * get the current message (only makes sense in context of an algorithm)
     */
    public String get(int state) {
        String message = super.getString(state, "message");
        if ( message == null ) return "";
        return message;
    }
}

//  [Last modified: 2015 12 08 at 14:17:12 GMT]
