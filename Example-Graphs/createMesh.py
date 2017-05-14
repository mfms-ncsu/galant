#! /usr/bin/env python3

## createMesh.py
# - creates a mesh with specified width, height, and inter-node distance in
#   graphml format
#
# $Id: createMesh.py 106 2015-04-15 13:01:30Z mfms $

import sys

def usage( program_name ):
    print("Usage:", program_name, " width height separation > OUTPUT_FILE")
    print(" creates a width x height mesh with nodes separated by the specified")
    print(" separation in pixels")

def print_opening():
    print('<?xml version="1.0" encoding="UTF-8"?>')
    print('<graphml xmlns="http://graphml.graphdrawing.org/xmlns"')
    print('xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"')
    print('xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns')
    print('http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">')

def print_graph_body( width, height, separation ):
    print('<graph edgedefault="undirected">')
    print_nodes( width, height, separation )
    print_edges( width, height, separation )
    print('</graph>')
    
def print_node( id, x, y ):
    # need to put everything on the same line (not clear why), so can't use
    # print
    sys.stdout.write( '<node id="%d" x="%d" y="%d" />\n' % ( id, x, y ) )

def print_edge( id, source, target, weight ):
    sys.stdout.write( '<edge id="%d" source="%d" target="%d" weight="%d" />\n' % ( id, source, target, weight ) )

def print_nodes( width, height, separation ):
    for id in range( width * height ):
        x = (1 + id % width) * separation
        y = (1 + id / width) * separation
        print_node( id, x, y )

def print_edges( width, height, separation ):
    edge_id = 0
    for id in range( width * height ):
        if (id % width) + 1 < width:
            print_edge( edge_id, id, id + 1, separation )
            edge_id = edge_id + 1
        if (id + width) < width * height:
            print_edge( edge_id, id, id + width, separation )
            edge_id = edge_id + 1

def print_closing():
    print('</graphml>')

def print_graphml( width, height, separation ):
    print_opening()
    print_graph_body( width, height, separation )
    print_closing()
    
def main():
    if len( sys.argv ) != 4:
        usage( sys.argv[0] )
        sys.exit()
    width = int(sys.argv[1])
    height = int(sys.argv[2])
    separation = int(sys.argv[3])
    print_graphml( width, height, separation )

main()

#  [Last modified: 2017 05 14 at 00:01:24 GMT]
