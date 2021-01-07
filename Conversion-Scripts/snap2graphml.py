#! /usr/bin/env python3

"""
 translates from snap format, described below, to the graphml format
 used by Galant or Gephi;
 this program is a simple filter, translating from standard input to standard output.

 snap format is as follows:

    # comment line 1
    ...
    # comment line k

    source_1 target_1
    ...
    source_m target_m

 sources and targets are vertex numbers starting at 1
"""

import sys
import random                   # to set random coordinates when not given

def usage( program_name ):
    print("Usage:", program_name, "MAX_WINDOW_SIZE OFFSET < INPUT_FILE > OUTPUT_FILE")
    print("Takes a snap file from standard input and converts to graphml.")
    print("The graphml file has nodes at randomly assigned coordinates")
    print("MAX_WINDOW_SIZE is maximum dimension (both width and height) of a window fitting the coordinates.")
    print("OFFSET is the minimum distance between a node and a window boundary")

global MAX_WINDOW_SIZE
global OFFSET
# size of the area between offsets
global canvas_size

# @param input the input stream from which the input is to be read
# @return a graph_tuple of the form:
#     (node_dictionary, [edge_1, ... , edge_m])
# where node_i is the integer number of a node
# and each edge_i is a tuple of the form (source, target)
def read_gph( input ):
    node_dictionary = {}
    edge_list = []
    line = skip_comments( input )
    while ( line ):
        split_line = line.split()
        source = int( split_line[0] )
        target = int( split_line[1] )
        edge_tuple = ( source, target )
        edge_list.append( edge_tuple )
        # make sure the endpoints of the edge have dictionary entries
        if source not in node_dictionary:
            source_x = random.randint(0, canvas_size) + OFFSET
            source_y = random.randint(0, canvas_size) + OFFSET
            node_dictionary[source] = (source_x,source_y)
        if target not in node_dictionary:
            target_x = random.randint(0, canvas_size) + OFFSET
            target_y = random.randint(0, canvas_size) + OFFSET
            node_dictionary[target] = (target_x,target_y)
        line = read_nonblank( input )

    return ( node_dictionary, edge_list )

# @return the first non-blank line in the input
def read_nonblank( input ):
    line = input.readline()
    while ( line and line.strip() == "" ):
        line = input.readline()
    return line

# reads and skips lines that begin with '#' and collects them into the global
# list of strings _comments, one element per comment line
# @return the first line that is not a comment line
def skip_comments( input ):
    global _comments
    _comments = []
    line = read_nonblank( input )
    while ( line.split()[0][0] == '#' ):
        _comments.append( line.strip().lstrip( "#" ) )
        line = read_nonblank( input )
    return line

def print_opening():
    print('<?xml version="1.0" encoding="UTF-8"?>')
    print('<graphml xmlns="http://graphml.graphdrawing.org/xmlns"')
    print('xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"')
    print('xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns')
    print('http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">')

def print_graph_body( graph_tuple ):
    print('<graph edgedefault="undirected">')
    print_nodes( graph_tuple[0] )
    print_edges( graph_tuple[1] )
    print('</graph>')

def print_nodes( node_dictionary ):
    for id in node_dictionary:
        point = node_dictionary[ id ]
        x = point[0]; y = point[1]
        print(('<node id="%d" x="%d" y="%d" />' % ( id, x, y )))

def print_edges( edge_list ):
    for edge in edge_list:
        source = edge[0]
        target = edge[1]
        print('<edge source="%d" target="%d" />' % (edge[0], edge[1]))

        
def print_comments():
    print("<comments>")
    for comment in _comments:
        print(comment)
    print("</comments>")

def print_closing():
    print('</graphml>')

def print_graphml( graph_tuple ):
    print_opening()
    print_comments()
    print_graph_body( graph_tuple )
    print_closing()
    
def main():
    global MAX_WINDOW_SIZE
    global OFFSET
    global canvas_size
    if len( sys.argv ) < 3:
        usage( sys.argv[0] )
        sys.exit()
    MAX_WINDOW_SIZE = int(sys.argv[1])
    OFFSET = int(sys.argv[2])
    canvas_size = MAX_WINDOW_SIZE - 2 * OFFSET
    graph_tuple = read_gph( sys.stdin )
    print_graphml( graph_tuple )

main()

#  [Last modified: 2019 09 17 at 21:52:31 GMT]
