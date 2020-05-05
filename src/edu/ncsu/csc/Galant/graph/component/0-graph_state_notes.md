Editing versus algorithm execution - some notes for *future*
refactoring.

1.  For transparency it would be good to have variables algorithmGraph
    and editGraph. During editing, the former is null and workingGraph =
    editGraph. During algorithm execution, the workingGraph =
    algorithmGraph, initialized to the state of the editGraph as of
    beginning of execution, and editGraph does not change state.

2.  Conceptually, it makes more sense to initialize the algorithmGraph
    as a new graph that has the same structure and whose components,
    including the graph itself, have one initial state - a copy of the
    current state of the edit graph.

3.  Item 2 suggests that most of the work be done by constructors,
    either ones that already exist or new ones designed for this
    purpose. The current implementation strategy is to have the copy
    methods call on default constructors - not good programming practice
    since it allows the creation of objects that are completely
    un-ininitialized.

4.  Existing or new copy constructors have the following signatures:

Graph(Graph otherGraph) - there appears to be a placeholder for this but
the argument is called newGraph, which is backward.

Node(Node otherNode) - does not exist but code can easily be borrowed
from other constructors; sometimes constructors have helper methods that
do work that is common to more than one constructor.

Edge(Edge otherEdge, Node source, Node target) - here it's important to
pass the source and target - these will be new nodes; does not exist but
will have similarities to existing constructors

GraphElement(GraphElement otherElement) - currently there's only one
constructor for a GraphElement, some of its code can be borrowed

Layer(Layer otherLayer) - will be needed eventually when we incorporate
layered graphs

5.  The copy constructor for a Graph needs to do the following:

a.  create a new copy of each node (using the copy constructor) and
    enter the node to the nodeById structure (which is initially empty)

b.  create a new copy of each edge; first the source and target for the
    new edge, which must be new nodes, are retrieved from the nodeById
    structure; the copy constructor must also use the constructor for
    the super class (GraphElement) to get the other attributes; finally,
    the copy constructor must add the new edge to the list of incident
    edges for both source and target; one can debate whether this last
    step should be the responsibility of the Graph copy constructor
    instead

c.  create deep copies of any of the graph attributes and of the current
    edit state, which becomes the sole state on the list of states.

6.  Most other details can be handled as they are in the current copy
    methods.

