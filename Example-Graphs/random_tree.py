#! /usr/bin/env python3

# random_tree.py - generates a random tree; various options are available:
# see parse_arguments below

import argparse
import math
import random
import sys

def parse_arguments():
    parser = argparse.ArgumentParser()
    parser.add_argument("-n", "--nodes", type=int, required=True,
                        help="number of nodes")
    parser.add_argument("-ew", "--edge_weights", type=int, dest="edge_weights",
                        help="edge weights in the range [1,EDGE_WEIGHTS];"
                        + " if node_positions are given, these are determined"
                        + " by distances, appropriately scaled")
    parser.add_argument("-nw", "--node_weights", type=int, dest="node_weights",
                        help="node weights in the range [1,NODE_WEIGHTS]")
    parser.add_argument("-np", "--node_positions", dest="node_positions",
                        help="random node positions in a width,height window;"
                        + " where width and height are integers;"
                        + " each node is attached to the closest neighbor")
    parser.add_argument("-pad", "--padding", type=int, dest="padding", default=50,
                        help="padding around edges of window;"
                        + " applies only if node positions are requested")
    parser.add_argument("-pL", "--power_law", type=float, dest="power_law",
                        help="each node attaches to the i-th previous node with"
                        + " probability (POWER_LAW)^i")
    parser.add_argument("-s", "--seed", type=int, dest="seed", default=None,
                        help="random seed for weights and connections;"
                        + " current time is used if none is given")
    args = parser.parse_args()
    return args

# returns a list of the form [(1,x_1,y_1), ..., (n,x-n,y_n)],
# where the first element of each tuple id is the id of a node and
# (x_i,y_i) is the position of the node with id i
# the arguments come straight from the command line
def nodes_with_positions(n, width, height, padding):
    node_list = []
    min_x = padding
    max_x = width - padding
    min_y = padding
    max_y = height - padding
    for node_id in range(1, n + 1):
        x_coord = random.randint(min_x, max_x)
        y_coord = random.randint(min_y, max_y)
        node_list.append((node_id, x_coord, y_coord))
    return node_list

# returns a list of edges where each node except the first links to a random
# preceding node; if edge_weights is specfied each edge is given a random
# weight in the range [1,edge_weights];
# an edge has format (source,target,weight),
# where weight is 0 if edge_weights is None, and ignored when graph is printed
def random_edges(node_list, edge_weights):
    edge_list = []
    for list_position in range(1, len(node_list)):
        node = node_list[list_position]
        other_node = random.choice(node_list[:list_position])
        if edge_weights != None:
            weight = random.randint(1, edge_weights)
            edge_list.append((node[0], other_node[0], weight))
        else:
            edge_list.append((node[0], other_node[0]))
    return edge_list

# returns a list of edges where the k-th node attempts to attach to the i-th
# node, for i = 1, ..., k-1, with probability (power_law)^i; if no attachment
# occurs, the choice is then made uniformly; edge_weights are as with random_edges
def power_law_edges(node_list, power_law, edge_weights):
    edge_list = []
    for list_position in range(1, len(node_list)):
        node_id = node_list[list_position][0]
        edge = None
        base = power_law
        for other_node in node_list[:list_position]:
            if random.random() <= base:
                edge = (node_id, other_node[0])
                break
            base *= power_law
        if edge == None:
            other_node = random.choice(node_list[:list_position])
            edge = (node_id, other_node[0])
        if edge_weights != None:
            weight = random.randint(1, edge_weights)
            edge = (edge[0], edge[1], weight)
        edge_list.append(edge)
    return edge_list

def euclidean_distance(node_one, node_two):
    x_1 = node_one[1]
    y_1 = node_one[2]
    x_2 = node_two[1]
    y_2 = node_two[2]
    return math.sqrt((x_1 - x_2) ** 2 + (y_1 - y_2) ** 2)

# returns a list of edges for the tree; each edge connects the next node with
# the closest of the nodes that came before it; an edge has the format
#   (source,target,weight)
# if edge_weights are called for then weight is the scaled distance between
# source and target, scaled so that min distance maps to 1 and max distance
# maps to the edge_weights parameter; otherwise, weight is simply the
# distance and is ignored when the tree is printed
def closest_point_edges(node_list, edge_weights):
    edge_list = []
    # add an edge from each node to its closest neighbor, keeping track of
    # the maximum and minimum closest distances encountered so far
    min_distance = float("infinity")
    max_distance = 0
    for list_position in range(1, len(node_list)):
        node = node_list[list_position]
        closest_node = None
        closest_distance = float("infinity")
        for other_node in node_list[:list_position]:
            distance = euclidean_distance(node, other_node)
            if distance < closest_distance:
                closest_distance = distance
                closest_node = other_node
        edge_list.append((node[0], closest_node[0], closest_distance))
        if closest_distance > max_distance:
            max_distance = closest_distance
        if closest_distance < min_distance:
            min_distance = closest_distance
    # scale distances if called for, mapping
    # [min_distance, max_distance] to [1, edge_weights]
    if edge_weights != None:
        if max_distance == min_distance:
            edge_list = [(edge[0],edge[1],1) for edge in edge_list]
        else:
            scale_factor = (edge_weights - 1) / (max_distance - min_distance)
            edge_list = [(edge[0],edge[1],
                          1 + int((edge[2] - min_distance) * scale_factor))
                         for edge in edge_list]
    else:
        edge_list = [(edge[0],edge[1]) for edge in edge_list]
    return edge_list

def print_graphml_header():
    print('<?xml version="1.0" encoding="UTF-8"?>')
    print('<graphml xmlns="http://graphml.graphdrawing.org/xmlns"')
    print('xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"')
    print('xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns')
    print('http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">')
    print('<graph edgedefault="undirected">')

def print_graphml_nodes(node_list):
    for node in node_list:
        if len(node) == 3:      # node has a position
            sys.stdout.write('<node id="%d" x="%d" y="%d" />\n' % node)
        elif len(node) == 4:    #  node has position and weight
            sys.stdout.write('<node id="%d" x="%d" y="%d" weight="%d" />\n' % node)
        elif node[1] != 0:      # node has weight (but no position)
            sys.stdout.write('<node id="%d" weight="%d" />\n' % node)
        else:                   # node has neither position nor weight
            sys.stdout.write('<node id="%d" />\n' % (node[0]))
            
def print_graphml_edges(edge_list):
    for edge in edge_list:
        if len(edge) == 3:      # edge has a weight
            sys.stdout.write('<edge source="%d" target="%d" weight="%d" />\n' % edge)
        else:
            sys.stdout.write('<edge source="%d" target="%d" />\n' % (edge[0], edge[1]))

def print_graphml_ending():
    print('</graph>')
    print('</graphml>')
    
def print_graphml(node_list, edge_list):
    print_graphml_header()
    print_graphml_nodes(node_list)
    print_graphml_edges(edge_list)
    print_graphml_ending()
            
def main():
    args = parse_arguments()
    random.seed(args.seed)
    if args.node_positions != None:
        dimensions = args.node_positions.split(",")
        width = int(dimensions[0])
        height = int(dimensions[1])
        node_list = nodes_with_positions(args.nodes, width, height, args.padding)
        if args.node_weights != None:
            node_list = [(node[0], node[1], node[2],
                          random.randint(1, args.node_weights))
                         for node in node_list]
        edge_list = closest_point_edges(node_list, args.edge_weights)
    else:
        node_list = [node_id for node_id in range(1, args.nodes + 1)]
        if args.node_weights != None:
            node_list = [(node_id, random.randint(1, args.node_weights))
                         for node_id in node_list]
        else:
            node_list = [(node_id, 0) for node_id in node_list]
        if args.power_law != None:
            edge_list = power_law_edges(node_list, args.power_law, args.edge_weights)
        else:
            edge_list = random_edges(node_list, args.edge_weights)
    # at this point the list items are as follows
    #  node_list has items of the form
    #     (id,0)     if nodes have neither position nor weight
    #     (id,x,y)   if nodes have positions but no weights
    #     (id,w)     if nodes have weights
    #     (id,x,y,w) if nodes have both positions and weights
    #  edge_list has items of the form
    #     (source,target)    if edges have no weights
    #     (source,target,w)  if edges have weights
    print_graphml(node_list, edge_list)

main()    

#  [Last modified: 2017 05 15 at 12:40:33 GMT]
