#! /usr/bin/env python3

"""
translates from gph format, described below, to the
graphml format used by Galant. Nodes are given a position if that
information is provided in the original gph file. If it's missing, the
output can be used in any layout program that takes graphml input, e.g.,
Gephi, Galant, etc.  This program is a simple filter, translating from
standard input to standard output.
"""

"""
gph format is as follows:

   c comment line 1
   ...
   c comment line k

   g number_of_nodes number_of_edges
   n v_1 x_1 y_1
   ...
   n v_n x_n y_n
   e source_1 target_1 weight_1
   ...
   e source_m target_m weight_m

v_1 through v_n are node numbers, typically 1 through n
x_i, y_i are x and y coordinates of v_i
the lines beginning with n may be missing if no geometric information is given
sources and targets are node numbers from the set {v_1,...,v_n}

There is some convoluted logic to handle the (unlikely) case where some
but not all nodes have coordinates 
"""

import sys
import argparse
import random

# actual dimensions excluding padding
global _actual_width
global _actual_height
global args

# when maximum value is scaled we need to subtract a little to ensure
# boundary is not exceeded
EPSILON = 0.0001

def parse_arguments():
    parser = argparse.ArgumentParser(description="Translates a graph from gph format"
                                     + " to graphml suitable for Galant;"
                                     + " use stdin and stdout if not otherwise specified")
    parser.add_argument("-s", "--seed", type=int, help="seed for random generator;"
                        + " used to 'dither' nodes that may end up on top of each other"
                        + " or to generate random positions if none are given"
                        + " use internal random seed if missing")
    parser.add_argument("-o", "--out_file", help="output file name; stdout if not given")
    parser.add_argument("-i", "--in_file",  help="input file name; stdin if not given")
    parser.add_argument("-sz", "--size", help="height and width of window;"
                        + " made square so that edge lengths don't get distorted"
                        + " when based on geometry; default is 750", default=750)
    parser.add_argument("-wd", "--width", type=int, help="width of window for drawing the graph;"
                        + " in case different from size")
    parser.add_argument("-ht", "--height", type=int,
                        help="height of window for drawing the graph;"
                        + " in case different from size")
    parser.add_argument("-p", "--padding", type=int, help="minimum distance of nodes from"
                        + " window border; default is 50", default=50)
    parser.add_argument("-ew", "--max_edge_weight", type=int, help="maximum edge weight:"
                        + " edge weights will be scaled to be in range [1,max]")
    parser.add_argument("-uw", "--unit_weights", action='store_true',
                        help="give all edges weights of 1, equivalent to '-ew 1'")
    args = parser.parse_args()
    return args

def scale(original_value, scale_factor):
    scaled_value = original_value * scale_factor
    return int(scaled_value - EPSILON)

# @param in_stream the input stream from which the input is to be read
# @return a graph_tuple of the form:
#     ([node_1,...,node_n], [edge_1,..., edge_m])
# with node_i = (v_i,x_i,y_i)
# and each edge_i is a tuple of the form
#     (source_i,target_i,weight_i)
def read_gph( in_stream ):
    # note: node_set is a set of node numbers that appear as sources or
    # sinks among the edges; for situations when no information about
    # nodes is provided in the in_stream
    node_set = set([])
    edge_list = []
    node_tuple_list = []
    line = skip_comments(in_stream)
    # for now, assume next line begins with 'g'; do error checking later
    # since the number of nodes and edges is implicit, these can be ignored
    split_line = line.split()
    number_of_nodes = int(split_line[1])
    number_of_edges = int(split_line[2])
    line = read_nonblank(in_stream)
    while ( line ):
        split_line = line.split()
        if split_line[0] == 'n':
            node_number = int(split_line[1])
            if len(split_line) > 2:
                x_coordinate = float(split_line[2])
                y_coordinate = float(split_line[3])
                node_tuple = (node_number, x_coordinate, y_coordinate)
            else:
                node_tuple = (node_number)
            node_tuple_list.append(node_tuple)
        elif split_line[0] == 'e':
            source = int(split_line[1])
            target = int(split_line[2])
            node_set.add(source)
            node_set.add(target)
            weight = float(split_line[3])
            edge_tuple = (source, target, weight)
            edge_list.append(edge_tuple)
        else:
            sys.stderr.write('bad input line: %s\n' % line)
            sys.exit()
        line = read_nonblank(in_stream)
    # add any nodes that did not appear as 'n x y' lines in the input, but
    # did appear as endpoints of edges
    missing_nodes = node_set - set([x[0] for x in node_tuple_list])
    missing_node_tuples = [tuple([x]) for x in list(missing_nodes)]
    node_tuple_list += missing_node_tuples
    return ( node_tuple_list, edge_list )

# @return the first non-blank line in the in_stream
def read_nonblank(in_stream):
    line = in_stream.readline()
    while ( line and line.strip() == "" ):
        line = in_stream.readline()
    return line

# reads and skips lines that begin with 'c' and collects them into the global
# list of strings _comments, one element per comment line
# @return the first line that is not a comment line
def skip_comments( in_stream ):
    global _comments
    _comments = []
    line = read_nonblank( in_stream )
    while ( line.split()[0] == 'c' ):
        _comments.append( line.strip().lstrip( "c" ) )
        line = read_nonblank( in_stream )
    return line

def modified_graph(graph_tuple):
    """
    @param graph_tuple a tuple of the form:
               ([node_1,...,node_n], [edge_1,..., edge_m])
            with node_i = (v_i,x_i,y_i) or, in some cases, just (v_i)
            and each edge_i is a tuple of the form
               (source_i,target_i,weight_i)
    @return a tuple in the same format, with the x,y coordinates and weights scaled
            or otherwise modified based on command-line arguments
    """
    node_tuple_list = graph_tuple[0]
    max_x = get_max_x_coordinate(node_tuple_list)
    max_y = get_max_y_coordinate(node_tuple_list)
    # assign random coordinates to any nodes that don't have them
    node_tuple_list = assign_random_coordinates(node_tuple_list, max_x, max_y)
    edge_tuple_list = graph_tuple[1]
    # avoid division by 0 when selecting scale factor
    # also, alert remainder of logic that edges should not be scaled using node coordinates
    scale_edges_using_nodes = True
    if max_x == 0:
        max_x = 1
        scale_edges_using_nodes = False
    if max_y == 0:
        max_y = 1
        scale_edges_using_nodes = False
    max_coordinate = max(max_x, max_y)
    if _actual_width == _actual_height:
        # square destination window, scale uniformly based on max coordinates
        x_scale_factor = _actual_width / max_coordinate
        y_scale_factor = _actual_height / max_coordinate
        edge_scale_factor = _actual_width / max_coordinate
    else:
        # scale each dimension separately
        x_scale_factor = _actual_width / max_x
        y_scale_factor = _actual_height / max_y
        edge_scale_factor = max(x_scale_factor, y_scale_factor)
    node_tuple_list = assign_positions(node_tuple_list, x_scale_factor, y_scale_factor,
                                       args.padding)
    if args.unit_weights:
        # unit weights take precedence
        edge_tuple_list = assign_unit_weights(edge_tuple_list)
    else:
        if args.max_edge_weight:
            min_weight, max_weight = get_min_max_edge_weights(edge_tuple_list)
            offset = min_weight
            if min_weight == max_weight:
                weight_range = 1
            else:
                weight_range = max_weight - min_weight 
            edge_scale_factor = args.max_edge_weight / weight_range
        else:
            offset = 0
        edge_tuple_list = scale_edges(edge_tuple_list, edge_scale_factor, offset)
    return (node_tuple_list, edge_tuple_list)
        
def assign_random_coordinates(node_tuple_list, max_x_coordinate, max_y_coordinate):
    """
    @param node_tuple_list a list with entries of the form (v, x, y) or (v),
           where v is a node number and x,y are coordinates, if present
    @return a modified node_tuple_list in which tuples with no coordinates
            are replaced by random ones in the ranges
            [1,max_x_coordinate] or [1,max_y_coordinate], respectively;
            if a max coordinate is 0, the range is based on the other coordinate;
            if both are 0, the range is [0.0, 1.0]
    """
    if max_x_coordinate == 0:
        max_x_coordinate = max_y_coordinate
    if max_y_coordinate == 0:
        max_y_coordinate = max_x_coordinate
    # if both were 0 originally, they will continue to be 0
    new_node_tuple_list = []
    for node_tuple in node_tuple_list:
        v = node_tuple[0]
        if len(node_tuple) == 3:
            x = node_tuple[1]
            y = node_tuple[2]
        else:
            if max_x_coordinate == 0:
                x = random.uniform(0, 1)
            else:
                x = random.randint(1, max_x_coordinate)
            if max_y_coordinate == 0:
                y = random.uniform(0, 1)
            else:
                y = random.randint(1, max_y_coordinate)
        new_node_tuple_list.append((v, x, y))
    return new_node_tuple_list
                
def get_max_x_coordinate(node_tuple_list):
    """
    @param node_tuple_list a list with entries of the form (v, x, y) or (v),
           where v is a node number and x,y are coordinates, if present
    @return the maximum x coordinate or 0 if none are present
    """
    max_coordinate = 1
    for node_tuple in node_tuple_list:
        if len(node_tuple) == 3 and node_tuple[1] > max_coordinate:
            max_coordinate = node_tuple[1]
    return max_coordinate
        
def get_max_y_coordinate(node_tuple_list):
    """
    @param node_tuple_list a list with entries of the form (v, x, y) or (v),
           where v is a node number and x,y are coordinates, if present
    @return the maximum y coordinate or 0 if none are present
    """
    max_coordinate = 0
    for node_tuple in node_tuple_list:
        if len(node_tuple) == 3 and node_tuple[2] > max_coordinate:
            max_coordinate = node_tuple[2]
    return max_coordinate

def get_min_max_edge_weights(edge_list):
    """
    @param edge_list a list with entries of the form (s, t, w),
           where s and t are the two endpoints of an edge and w is the weight
    @return the minimum and maximum weight in the list
    """
    return min([edge_tuple[2] for edge_tuple in edge_list]), \
        max([edge_tuple[2] for edge_tuple in edge_list])
    
def assign_positions(node_tuple_list, x_scale_factor, y_scale_factor, padding):
    """
    @param node_tuple_list a list with entries of the form (i, x, y),
           where i is a node number and x,y are coordinates if present
           Note: at this point, random coordinates have been assigned
                 where there were none
    @return a modified list in which node coordinates are
            multiplied by the appropriate scale factors and padding is added
    """
    return [(v[0], padding + scale(v[1], x_scale_factor),
             padding + scale(v[2], y_scale_factor))
            for v in node_tuple_list]

def scale_edges(edge_list, scale_factor, offset):
    """
    @param edge_list a list with entries of the form (s, t, w),
           where s and t are the two endpoints of an edge and w is the weight
    @return a modified list with weights scaled by the given scale factor;
            add 1 to make sure no edges have weight 0;
            offset is used if the min weight is to be mapped to 1
    """
    return [(e[0], e[1], 1 + scale(e[2] - offset, scale_factor)) for e in edge_list]

def assign_unit_weights(edge_list):
    """
    @param edge_list a list with entries of the form (s, t, w),
           where s and t are the two endpoints of an edge and w is the weight
    @return a modified list with all weights equal to 1
    """
    return [(e[0], e[1], 1) for e in edge_list]
    
def print_opening(out_stream):
    out_stream.write('<?xml version="1.0" encoding="UTF-8"?>\n')
    out_stream.write('<graphml xmlns="http://graphml.graphdrawing.org/xmlns"\n')
    out_stream.write('xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"\n')
    out_stream.write('xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns\n')
    out_stream.write('http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">\n')

def print_graph_body(out_stream, graph_tuple ):
    out_stream.write('<graph edgedefault="undirected">\n')
    print_node_tuples(out_stream, graph_tuple[0])
    print_edges(out_stream, graph_tuple[1])
    out_stream.write('</graph>\n')

def print_node_tuples(out_stream, node_tuple_list):
    for node_tuple in node_tuple_list:
        node_id = node_tuple[0]
        if len(node_tuple) > 1:
            x = node_tuple[1]
            y = node_tuple[2]
            out_stream.write(('<node id="%d" x="%d" y="%d" />\n' % (node_id, x, y)))
        else:
            out_stream.write(('<node id="%d">\n' % (node_id)))

def print_edges(out_stream, edge_list):
    for edge in edge_list:
        out_stream.write('<edge source="%d" target="%d" weight="%d" />\n' \
            % ( edge[0], edge[1], edge[2] ))
        
def print_comments(out_stream):
    """
    @todo add more information to the comments
    """
    out_stream.write("<comments>\n")
    out_stream.write("generated by gph2graphml with seed %s\n" % str(args.seed))
    for comment in _comments:
        out_stream.write("%s\n" % comment)
    out_stream.write("</comments>\n")

def print_closing(out_stream):
    out_stream.write('</graphml>\n')

def print_graphml(out_stream, graph_tuple):
    print_opening(out_stream)
    print_comments(out_stream)
    print_graph_body(out_stream, graph_tuple)
    print_closing(out_stream)
    
if __name__ == '__main__':
    global _actual_width
    global _actual_height
    global args
    args = parse_arguments()
    random.seed(args.seed)
    width = args.size
    height = args.size
    if args.width:
        width = args.width
    if args.height:
        height = args.height
    _actual_width = width - 2 * args.padding
    _actual_height = height - 2 * args.padding
    if args.in_file:
        input_stream = open(args.in_file, 'r')
    else:
        input_stream = sys.stdin
    graph_tuple = read_gph(input_stream)
    graph_tuple = modified_graph(graph_tuple)
    if args.out_file:
        output_stream = open(args.out_file, 'w')
    else:
        output_stream = sys.stdout
    print_graphml(output_stream, graph_tuple)
