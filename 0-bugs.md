### Bugs in Priya's implementation, one that allows undo/redo during editing.

1. if edges (and possibly nodes) are deleted, they reappear at the beginning of algorithm execution; problem does not arise if I save before running; need to check whether this behavior is also standard for the stable version

2. if a node or edge has the `deleted` attribute set, the element shows up when the graph is saved. Problem arises when the graph is read and its xml string is supposed to be shown in the editor panel. The file `test_graph.graphml` is an example. A deleted attribute should never be written to a file in the first place; this issue may be tangled with (1) above: we may not want a deleted item to disappear completely when we start an animation
