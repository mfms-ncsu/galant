#! /usr/bin/env python3

# random_tree.py - generates a random tree; various options are available:
# see parse_arguments below

import argparse
import random

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
    parser.add_argument("-s", "--seed", type=int, dest="seed", default=None,
                        help="random seed for weights and connections;"
                        + " current time is used if none is given")
    args = parser.parse_args()
    return args

# returns a list of the form [(id,x,y) ...], where id is the id of a node and
# (x,y) is its position
# the arguments come straight from the command line
def nodes_with_positions(n, width, height, padding):
    node_list = []
    min_x = padding
    max_x = width - padding
    min_y = padding
    max_y = height - padding
    for node_id in range(1, n + 1):
        print(node_id)
    return node_list

def main():
    args = parse_arguments()
    random.seed(args.seed)
    if args.node_positions != None:
        dimensions = args.node_positions.split(",")
        width = int(dimensions[0])
        height = int(dimensions[1])
        print(width, height)
#        node_list = nodes_with_positions(args.nodes, args.)

main()    

#  [Last modified: 2017 05 12 at 21:17:35 GMT]
