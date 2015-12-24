/**
 * Stores information peculiar to layered graphs.
 *
 * @todo LayeredGraph clearly needs to be a subclass of Graph and
 * LayeredGraphNode a subclass of Node.
 */

package edu.ncsu.csc.Galant.graph.component;

import java.util.ArrayList;
import edu.ncsu.csc.Galant.logging.LogHelper;

class LayerInformation {
    int numberOfLayers;
    /**
     * Stores the number of nodes on each layer
     */
    ArrayList<Integer> layerSize;
    
    protected LayerInformation() {
        layerSize = new ArrayList<Integer>();
    }

    /**
     * Uses layer and position in layer information about node v to update
     * information about number of layers and/or layer size
     */
    protected void addNode(Node v) {
        LogHelper.enterMethod(getClass(),
                              "addNode: node = " + v 
                              + ", numberOfLayers = " + numberOfLayers);
        int layer = v.getLayer();
        if ( layer >= numberOfLayers ) {
            numberOfLayers = layer + 1;
            int tempNumberOfLayers = layerSize.size();
            while ( tempNumberOfLayers < numberOfLayers ) {
                layerSize.add(0);
                tempNumberOfLayers++;
            }
            layerSize.set(layer, 1);
        }
        else {
            int sizeOfLayer = layerSize.get(layer);
            layerSize.set(layer, sizeOfLayer + 1);
        }
        LogHelper.exitMethod(getClass(),
                             "addNode: numberOfLayers = " + numberOfLayers
                             + ", sizeOfLayer = " + layerSize.get(layer));
    }

}

//  [Last modified: 2015 12 24 at 18:01:29 GMT]
