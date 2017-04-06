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
    
  /**
   * Stores the maximum position of a node on each layer
   */
  ArrayList<Integer> maxPosition;

    protected LayerInformation() {
        layerSize = new ArrayList<Integer>();
        maxPosition = new ArrayList<Integer>();
    }

    /**
     * Uses layer and position in layer information about node v to update
     * information about number of layers, layer size and max position
     * INVARIANT: layerSize.size() == maxPosition.size()
     */
    protected void addNode(Node v) {
      LogHelper.disable();
        LogHelper.enterMethod(getClass(),
                              "addNode: node = " + v 
                              + ", numberOfLayers = " + numberOfLayers);
        int layer = v.getLayer();
        if ( layer >= numberOfLayers ) {
            numberOfLayers = layer + 1;
            int tempNumberOfLayers = layerSize.size();
            while ( tempNumberOfLayers < numberOfLayers ) {
                layerSize.add(0);
                maxPosition.add(0);
                tempNumberOfLayers++;
            }
            layerSize.set(layer, 1);
            maxPosition.set(layer, v.getPositionInLayer());
        }
        else {
            int sizeOfLayer = layerSize.get(layer);
            layerSize.set(layer, sizeOfLayer + 1);
            int currentMaxPosition = maxPosition.get(layer);
            if ( v.getPositionInLayer() > currentMaxPosition ) {
              maxPosition.set(layer, v.getPositionInLayer());
            }
        }
        LogHelper.exitMethod(getClass(),
                             "addNode: numberOfLayers = " + numberOfLayers
                             + ", sizeOfLayer = " + layerSize.get(layer)
                             + ", maxPosition = " + maxPosition.get(layer));
        LogHelper.restoreState();
    }

}

//  [Last modified: 2017 04 06 at 17:32:37 GMT]
