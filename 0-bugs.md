### Bugs in Priya's implementation, one that allows undo/redo during editing.

1. if edges (and possibly nodes) are deleted, they reappear at the beginning of algorithm execution; problem does not arise if I save before running; need to check whether this behavior is also standard for the stable version

2. if a node or edge has the `deleted` attribute set, the element shows up when the graph is saved. Problem arises when the graph is read and its xml string is supposed to be shown in the editor panel. The file `test_graph.graphml` is an example. A deleted attribute should never be written to a file in the first place; this issue may be tangled with (1) above: we may not want a deleted item to disappear completely when we start an animation **[should not happen - file is never saved in that state]**

### Improvement ideas
1. Need a way to update the working graph from the text window without saving it; when a graph is saved you might lose information about its current state.
2. Rethink the logging process so that it takes advantage of formatted printing; this has the advantage that the only string that is passed to the logger in the format string, a constant, and the full string is not created unless debugging is turned on. **[not possible]**
