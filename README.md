galant
======
To compile everything and run the program for the first time, simply type
ant
while in the home directory.
After that you can run Galant, either by saying
ant run
or by executing
build/jar/Galant.jar

*** Testing ***

First set the home directory for opening and saving files
      File -> Preferences -> Open/Save
to the top level directory of Galant so that all the relevant files
are easy to get to.

Then do the following test runs. Each time, open the algorithm and the graph
files and first hit compile when the text window shows the algorithm and Run
when the text window shows the algorithm and the graph window shows the
graph.

Algorithm                                     Graph

Algorithms/dfs_d.alg                            eight_node_graph.graphml
     - set as directed, show vertex and edge labels

Algorithms/dijkstra.alg                         weighted_example.graphml
     - try both undirected and directed, show vertex and edge weights

Algorithms/insertion_sort.alg                   sorting_test.graphml
     - show vertex weights

Crossing-Algorithms/barycenter.alg              Crossing_Graphs/1_test.graphml
     - show vertex weights

Crossing-Algorithms/mce.alg                     Crossing_Graphs/1_test.graphml
     - no weights or labels

Algorithms/binary_tree.alg                      empty graph
     - no weights or labels, has a problem with going back to the empty graph
       when done, just hit 'Continue'


