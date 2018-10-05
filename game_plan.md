Current game plan.

- Merge the changes into the regular `ptanir1` branch and the `development` branch. 
- Do more thorough testing of the current code (in the `development` branch), using the list in the `testingGalant` document. Also run some of the research algorithms to make sure nothing is amiss. Because the testing is going on in the `development` branch, new code can be added simultaneously in one of the other new branches.
- Implement (and test) undo/redo in edit mode only:
    create and test buttons (push button and print)
    add an edit state in GraphDispatch
    getDisplayState() should return display state from executor or dispatch's edit state
    change startStepIfRunning so that it increments editState if in edit mode (different name)
    newState() in Graph and GraphElement needs to use the latestValid state if in edit mode, based on current edit state
    addState() in Graph and GraphElement needs to insert the new state after the latestValidState instead of at the end
    this means that changes in edit state can occur in two ways: (a) if there is a change to the graph (from Graph or GraphElement - call "startStepIfRunning()"); (b) user hits undo or redo button (call incrementEditState() or decrementEditState() in GraphDispatch)
    at every stage, print the list of states on the console
   
The algorithm will start in the wrong place (the very beginning) for now. The code needs to be made oblivious to whether the current working graph is the edit graph or the algorithm graph and GraphDispatch needs to keep track of the edit state. Complications will no doubt arise - they always do, no matter how simple the design seems.
- Modify the copy methods so that they pick up the current edit state for each element rather than the initial state.
- Figure out the best way to handle movement of nodes during algorithm execution.
- Merge changes into the `development` branch and do thorough testing.
- Meanwhile, refactor the code to use copy constructors instead of the copy methods. Plus look for other ways to make the new (and existing) code more transparent.
- Publish a new release with the undo/redo feature in the `master` branch.
