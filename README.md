galant
======

This software is licensed by a Gnu Public License. See
https://www.gnu.org/licenses/gpl.html
for details.

You can run Galant immediately by opening a terminal window, going to the
galant home directory and executing:

    java -jar build/jar/Galant.jar
or (in Windows)    
    java -jar build\jar\Galant.jar

Make sure that
- you have a JDK installed (Java Development Kit)
- the JAVA_HOME environment variable is set properly; typical locations
   (Mac)      /Library/Java/JavaVirtualMachines/jdk1.x.x.jdk/Contents/Home/
   (Windows)  C:\Program Files\Java\jdk1.x.x
   (Linux)    don't need to do anything special on most Linux/Unix systems

Otherwise you may get an exception (null pointer or unexpected) from
     edu.ncsu.csc.Galant.algorithm.code.CompilerAndLoader.compile
Galant needs to be able to fire up a Java compiler while it is running.

If may want to (re)compile everything, either because you edited and changed
some of the details in the source files, or if you use GitHub to keep up with
the latest changes, you need Apache ant. Once that is installed, say
    ant jar
After that you can run Galant with
    ant run

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

Algorithms/interactive_dfs                      "

Algorithms/dijkstra.alg                         weighted_example.graphml
     - try both undirected and directed

Algorithms/insertion_sort.alg                   sorting_test.graphml
     - show vertex weights

Algorithms/binary_tree.alg                      empty graph

For more information and links to important resources, see 0-index.html.




