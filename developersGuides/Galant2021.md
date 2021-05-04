# Galant 2021 Developer's Guide

## About this Document

Since Galant is such a large project, we figured it would be useful to create documentation specifically about the parts of Galant we learned about during our semester developing upon it. Generally, this document contains information we learned throughout that would have been useful at the beginning. Additionally, we will try to include some insights about future plans for Galant and some very general advice about the codebase.

## Table of Contents

- [Introduction](Galant2021.md#Introduction)
- [Getting Started](Galant2021.md#Getting-Started)
- [Our Work](Galant2021.md#Our-Work)
  - Auto-Scale Node Positions
  - Allow Moving Layered Node during Animation/Edit Mode
  - Combining Layered Graph into existing Graph
- Java Resources
- I/O
- Rendering
- [Known Bugs](Galant2021.md#Known Bugs)
- Branches
- [Futures](Galant2021.md#Futures)

## Introduction

Galant is an academic and research graph visualization tool. It features a GUI editor and code editors. The GUI editor is used to display and edit graphs as well as display animations (which can be paged through like a presentation). The code editors can be used to write animations in Java or directly modify graphs in GraphML format. The tool also supports LayeredGraphs. If you are familiar with Graphs (collection of nodes and edges) then the only thing you need to know about LayeredGraphs is that each node is a member of a layer. In Galant layers are represented by vertical position, which means two nodes will have the same y-position iff they are on the same layer. In general, it seems like the highest priority for new features in Galant are ones that simplify viewing/exploring graphs as well as refactoring and simplifying the code.

## Getting Started

Galant can be made into a .jar file, which is a Java executable. 

If you are willing to use Eclipse IDE it contains everything needed to build and run tests. Otherwise see the [Custom IDE](Custom-IDE) section.

## Custom IDE

This section is completely optional. If you really do not want to use Eclipse, this section will inform you how to develop Galant without it. The program [Apache Ant](https://ant.apache.org/manual/install.html) is the only program actually required to build Galant. It can be installed on Mac easily using HomeBrew. For text editing, your choice of editor will work. However, Visual Studio Code is recommended due to its Java language support (plug-in) and exceptional project searching feature. The searching feature in particular was very useful for me when learning this codebase.

I was the only person on my team who used a custom IDE (except for Dr. Stallmann) but neither of us were involved in JUnit Testing, so no one is sure how to do this without Eclipse. If you find out please include it your team's Developer's Guide.

## Known Bugs

When the user creates a new graph and creates new node on the edge of the graph window, the node will not display on the window, and if the user try multiple times or scale the window, Galant will crash. This issue may comes from the virtual window we used in feature 1, and only happens when creating new nodes in a new graph. This issue is not happening on any pre-created graph.

## Our Work

- Auto-Scale Node Positions

This feature makes sure that the graph still fills the window whenever it is resized. We introduced the ViewTransform to the project. Right before rendering each node, it computes where the node should appear on screen. This computation is done using (1) the node's position, (2) the VirtualWindow bounds, and (3) the window size.

We also introduced the VirtualWindow to the project during this iteration. This structure is represented in the nodes' coordinate space (known as logical position). The VirtualWindow is roughly equivalent to the bounding box of (nodes_min_x, nodes_min_y) and (nodes_max_x, nodes_max_y). It is used to determine the right transformation to use in order to show all nodes within the window.

- Allow Moving Layered Node during Animation/Edit Mode
- Combining Layered Graph into existing Graph

## Futures

It is likely that Dr. Stallmann will be thinking about some of these features in the future.

### Panning

Panning is when the user is able to move the entire view of graph. This is helpful when viewing and editing.

Luckily this feature will be much easier to implement using the VirtualWindow. Simply adjusting the x and y coordinates of VirtualWindow will causing panning. The next consideration is user input. For that we recommend using JPanel mouse events...

### Zoom
