**Galant Installation Guide**

There are multiple ways to runGalant, the user can either run a jar file or run on an IDE, or using ant to compile and run Galant.

You can run Galant immediately by opening a terminal window, going to the

galant home directory and executing:

java -jar build/jar/Galant.jar

or (in Windows)

java -jar build\jar\Galant.jar

Make sure that

- you have a JDK installed (Java Development Kit)

- the JAVA\_HOME environment variable is set properly; typical locations

(Mac) /Library/Java/JavaVirtualMachines/jdk1.x.x.jdk/Contents/Home/

(Windows) C:\Program Files\Java\jdk1.x.x

(Linux) don&#39;t need to do anything special on most Linux/Unix systems

Otherwise you may get an exception (null pointer or unexpected) from

edu.ncsu.csc.Galant.algorithm.code.CompilerAndLoader.compile

Galant needs to be able to fire up a Java compiler while it is running.

You may want to (re)compile everything, either because you edited and changed

some of the details in the source files, or, if you use GitHub to keep up with

the latest changes, you need Apache ant. Once that is installed, say

ant jar

After that you can run Galant with

ant run

Before you do a &#39;git pull&#39; you should first either do &#39;ant clean&#39; or remove

the file build/jar/Galant.jar (build\jar\Galant.jar)

Note: If you are using an IDE, when you run the project as a java application, your IDE may warn you for errors, just ignore the warnings and go ahead. Those errors are from old version tests which are no longer appropriate for Galant.

**Simple Instructions**

- Use `File-\&gt;Open` to open an algorithm or a graph
- algorithms are in the `Algorithms` folder, graphs in `Example-Graphs` or, for sorting algorithms, the `0-SortingGraphs` folder below that\*
- To run an algorithm on a graph, make sure the (drawing of the) graph is in the graph window and the (text of the) algorithm in the text window
- Click `Compile and Run` or `Run` (if the algorithm is already compiled)
- Click in the graph window
- The left/right arrow keys are used to move forward/backward in the algorithm; holding them down allows you to control speed
- The escape key stops the animation

Use `File-\&gt;Preferences-\&gt;Open/Save` to set the default starting directory for file browsing.