**Feature 1:** Auto-scale Graphical Elements with Window Resizing

With Galant being opened, please load a Non-Layered Graph first. For example, &quot;unweighted\_8.graphml&quot; is loaded below. (_Figure 1.1_)

![](RackMultipart20210507-4-10unu9l_html_6388e01239619af7.png)

_Figure 1.1 Graph Loading_

You can then resize the window by dragging the border of the Graph Display Window. The graph will resize its graph elements based on the new window size automatically. (_Figure 1.2_)

![](RackMultipart20210507-4-10unu9l_html_d32d05c410901c94.png) ![](RackMultipart20210507-4-10unu9l_html_84bcc8a2a5c79c53.png)

_Figure 1.2 Window Resizing_

You can also drag a particular node towards the border of the Graph Display Window. As the dragged node gets closer to the border, the program should automatically expand the Graph Display Window towards that direction to adapt the movement you&#39;ve made. (_Figure 1.3_)

![](RackMultipart20210507-4-10unu9l_html_af563003e4844dae.png) ![](RackMultipart20210507-4-10unu9l_html_b464c74c699d483b.png)

_Figure 1.3 Boundary Check_

**Feature 2:** Enhancement of Editing Capability of Galant

Once you&#39;ve opened Galant, please load a Layered Graph. For example, the &quot;pyramid.graphml&quot; can be used as an option. (_Figure 2.1_)

![](RackMultipart20210507-4-10unu9l_html_a83bc1c5370aba74.png)

_Figure 2.1 Graph Loading_

Please load a Layered Graph Algorithm next in the editor window, for example, you can use &quot;mce.alg.&quot; Then click &quot;compile and run.&quot; (_Figure 2.2_)

![](RackMultipart20210507-4-10unu9l_html_e30983faa01028b8.png) ![](RackMultipart20210507-4-10unu9l_html_76e70fd3a3d99e16.png)

_Figure 2.2 Algorithm Execution_

When the algorithm is running, the user can drag nodes inside the graph window. (_Figure 2.3_)

![](RackMultipart20210507-4-10unu9l_html_22b37edaf7f82997.png) ![](RackMultipart20210507-4-10unu9l_html_a6c86e1c3a81cc28.png)

_Figure 2.3 Node Movement_

For LayeredGraph, the graph window will not expand when the node reaches the border, so the node may be dragged out of the window. But there are different ways to reposition all nodes back to their original positions.

Method 1: Resizing the graph window: (_Figure 2.4_)

![](RackMultipart20210507-4-10unu9l_html_a6c86e1c3a81cc28.png) → ![](RackMultipart20210507-4-10unu9l_html_f413b5549433499f.png)

_Figure 2.4 Resizing to Trigger Reposition_

Method 2: Steping forward/backward during the execution of an algorithm: (_Figure 2.5_)

![](RackMultipart20210507-4-10unu9l_html_c24405624db933f.png) → ![](RackMultipart20210507-4-10unu9l_html_fc0e8583bd521d9b.png)

![](RackMultipart20210507-4-10unu9l_html_cce471b299537a2b.png) → ![](RackMultipart20210507-4-10unu9l_html_47f17e7990c6d64.png)

_Figure 2.5 Stepping Forwards/Backwards to Trigger Reposition_

Method 3: Exiting the animation mode (terminating algorithm): (_Figure 2.6_)

![](RackMultipart20210507-4-10unu9l_html_ede22bb86559a354.png) → ![](RackMultipart20210507-4-10unu9l_html_46f501045906fce7.png)

_Figure 2.6 Terminating Algorithm Execution_

Notice that you can&#39;t resize the graph window/step forward/backward/exit animation mode without repositioning nodes.

Feature 3:

This feature is about simplifying the structure of code base and refactoring, it has no impact over the visual performance/user interface experience.