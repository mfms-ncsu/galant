#! /usr/bin/env python

## sgf2layered_graphml.py - translates from sgf format, described below, to the
# graphml format used by Galant. This program is a simple filter, translating
# from standard input to standard output.
#
# $Id: sgf2layered_graphml.py 110 2015-06-12 12:26:27Z mfms $
#
# sfg format is as follows:
#
#    c comment line 1
#    ...
#    c comment line k
#
#    t graph_name
#
#    n id_1 layer_1 position_1
#    n id_2 layer_2 position_2
#    ...
#    n id_n layer_n position_n
#
#    e source_1 target_1
#    ...
#    e source_m target_m

import sys

def usage( program_name ):
    print("Usage:", program_name, " < INPUT_FILE > OUTPUT_FILE")
    print("Takes an sgf file from standard input and converts to graphml.")
    print("The graphml file encodes node positions (layer/position in layer) and edges.")

# @return a tuple of the form (name, graph_list)
# and graph_list has the form:
#     [type (attr value) ... (attr value)]
# where type is either 'node' or 'edge'
# in this case, the only attributes are:
#    id, layer, and position_in_layer for nodes,
#    source and target for edges
def read_sgf( input ):
    graph_list = []
    line = skip_comments( input )
    # for now, assume next line begins with 't'; do error checking later
    # since the number of nodes and edges is implicit, these can be ignored
    name = line.split()[1]
    line = read_nonblank( input )
    while ( line ):
        type = line.split()[0]
        if type == 'n':
            graph_list.append( process_node( line ) )
        elif type == 'e':
            graph_list.append( process_edge( line ) )
            # otherwise error (ignore for now)
        line = read_nonblank( input )
    return (name, graph_list)

def process_node( line ):
    line_fields = line.split()
    id = line_fields[1]
    layer = line_fields[2]
    position_in_layer = line_fields[3]
    return ['node',
            ('id', id),
            ('layer', layer),
            ('positionInLayer', position_in_layer)]

def process_edge( line ):
    line_fields = line.split()
    source = line_fields[1]
    target = line_fields[2]
    return ['edge', ('source', source), ('target', target)]

# @return the first non-blank line in the input
def read_nonblank( input ):
    line = input.readline()
    while ( line and line.strip() == "" ):
        line = input.readline()
    return line

# reads and skips lines that begin with 'c' and collects them into the global
# list of strings _comments, one element per comment line
# @return the first line that is not a comment line
def skip_comments( input ):
    global _comments
    _comments = []
    line = read_nonblank( input )
    while ( line.split()[0] == 'c' ):
        _comments.append( line.strip().lstrip( "c" ) )
        line = read_nonblank( input )
    return line

def print_opening():
    print('<?xml version="1.0" encoding="UTF-8"?>')
    print('<graphml xmlns="http://graphml.graphdrawing.org/xmlns"')
    print('xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"')
    print('xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns')
    print('http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">')

def print_graph_body( internal_graph ):
    name = internal_graph[0]
    print('<graph edgedefault="directed" name="'+ name + '"' + ' type="layered">')
    
    for item in internal_graph[1]:
        print_item( item )
    print('</graph>')

def print_item( item ):
    # need to put everything on the same line (not clear why), so can't use
    # print
    sys.stdout.write( '<%s ' % ( item[0] ) )
    sys.stdout.write( graphml_version( item[1:] ) )
    sys.stdout.write( '/>\n' )

# @param attribute_list has the form [(attr_1,value_1),..., (attr_k,value_k)]
# @return a string of the form "attr_1=value_1 ... attr_k=value_k"
def graphml_version( attribute_list ):
    return ' '.join( map( graphml_attribute, attribute_list ) )
    
# @param attribute_pair has the form (attr,value)
# @return "attr=value"
def graphml_attribute( attribute_pair ):
    return '%s="%s"' % attribute_pair

def print_comments():
    print("<comments>")
    for comment in _comments:
        print(comment)
    print("</comments>")

def print_closing():
    print('</graphml>')

def print_graphml( internal_graph ):
    print_opening()
    print_comments()
    print_graph_body( internal_graph )
    print_closing()
    
def main():
    if len( sys.argv ) != 1:
        usage( sys.argv[0] )
        sys.exit()
    internal_graph = read_sgf( sys.stdin )
    print_graphml( internal_graph )

main()

#  [Last modified: 2015 05 31 at 18:40:40 GMT]
