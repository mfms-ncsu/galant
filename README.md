galant
======

This software is licensed by a Gnu Public License. See
https://www.gnu.org/licenses/gpl.html
for details.

To compile everything and run the program for the first time, simply type
ant
while in the home directory.
After that you can run Galant, either by saying
ant run
or by executing
java -jar build/jar/Galant.jar (this may not work on Windows)

Make sure that
- you have Apache Ant installed (not necessary if running jar file?)
- you have a JDK installed (Java Development Kit)
- the JAVA_HOME environment variable is set properly; typical locations
   (Mac)      /Library/Java/JavaVirtualMachines/jdk1.x.x.jdk/Contents/Home/
   (Windows)  C:\Program Files\Java\jdk1.x.x
   (Linux)    ??

Otherwise you may get a null pointer exception from
     edu.ncsu.csc.Galant.algorithm.code.CompilerAndLoader.compile
Galant needs to be able to fire up a Java compiler while it is running.

*** Testing *** (see testingGalant.docx for more details)

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

Algorithms/dijkstra.alg                         weighted_example.graphml
     - try both undirected and directed

Algorithms/insertion_sort.alg                   sorting_test.graphml
     - show vertex weights

Algorithms/binary_tree.alg                      empty graph

For more information and links to important resources, see 0-index.html.




