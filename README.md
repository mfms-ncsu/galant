galant
======

This software is licensed by a Gnu Public License. See
https://www.gnu.org/licenses/gpl.html
for details.

If you have questions, comments or bugs to report, please contact me at
mfms@ncsu.edu

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

You may want to (re)compile everything, either because you edited and changed
some of the details in the source files, or, if you use GitHub to keep up with
the latest changes, you need Apache ant. Once that is installed, say
    ant jar
After that you can run Galant with
    ant run
    
Before you do a 'git pull' you should first either do 'ant clean' or remove
the file build/jar/Galant.jar (build\jar\Galant.jar)

***Simple Instructions***

* Use `File->Open` to open an algorithm or a graph
* *algorithms are in the `Algorithms` folder, graphs in `Example-Graphs` or, for sorting algorithms, the `0-SortingGraphs` folder below that*
* To run an algorithm on a graph, make sure the (drawing of the) graph is in the graph window and the (text of the) algorithm in the text window
* Click `Compile and Run` or `Run` (if the algorithm is already compiled)
* Click in the graph window
* The left/right arrow keys are used to move forward/backward in the algorithm; holding them down allows you to control speed
* The escape key stops the animation

Use `File->Preferences->Open/Save` to set the default starting directory for file browsing.

***Testing*** (see testingGalant.docx for more details)

First set the home directory for opening and saving files
      `File -> Preferences -> Open/Save`
to the top level directory of Galant so that all the relevant files
are easy to get to.

Then do the following test runs. Each time, open the algorithm and the graph
files and first hit compile when the text window shows the algorithm and Run
when the text window shows the algorithm and the graph window shows the
graph. The algorithms are in the `Algorithms` folder and the graphs in `Example-Graphs`.

| Algorithm |   Graph |
| :-- | :-- |
| `dfs_d.alg` |          `eight_node_graph.graphml`|
|`interactive_dfs`|   "|
|`dijkstra.alg`|         `weighted_example.graphml`|
|`insertion_sort.alg`|   `0-SortingGraphs/sorting_test.graphml`|
|`binary_tree.alg`|        an empty graph|

**Notes:**
* for `dijkstra.alg` try both the directed and undirected version of the graph (use `Ctrl-d` to toggle)
* to create an empty graph click on left arrow below `File` until `untitled graph` appears

**For more information and links to important resources, see** `0-index.html.`




