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
  ArrayList<Integer> maxPositionInLayer;

  /**
   * maximum position among all layers
   */
  int maxPosition = 0;

  /**
   * true if at least one layer has a node whose position is greater than the
   * size of the layer minus one, i.e., there's at least one layer that has
   * gaps in its positions; this is used to determine how to draw the graph:
   * if vertical is true, the drawing assumes each layer has the same number
   * of positions
   */
  boolean vertical = false;

  protected LayerInformation() {
    layerSize = new ArrayList<Integer>();
    maxPositionInLayer = new ArrayList<Integer>();
  }

  /**
   * Uses layer and position in layer information about node v to update
   * information about number of layers, layer size and max position
   * INVARIANT: layerSize.size() == maxPositionInLayer.size()
   */
  protected void addNode(Node v) {
    LogHelper.disable();
    LogHelper.enterMethod(getClass(),
                          "addNode: node = " + v
                          + ", numberOfLayers = " + numberOfLayers);
    int layer = v.getLayer();
    int position = v.getPositionInLayer();
    maxPosition = (position > maxPosition) ? position : maxPosition;
    if ( layer >= numberOfLayers ) {
      numberOfLayers = layer + 1;
      int tempNumberOfLayers = layerSize.size();
      while ( tempNumberOfLayers < numberOfLayers ) {
        layerSize.add(0);
        maxPositionInLayer.add(0);
        tempNumberOfLayers++;
      }
      layerSize.set(layer, 1);
      maxPositionInLayer.set(layer, position);
    }
    else {
      int sizeOfLayer = layerSize.get(layer);
      layerSize.set(layer, sizeOfLayer + 1);
      int currentMaxPosition = maxPositionInLayer.get(layer);
      if ( position > currentMaxPosition ) {
        maxPositionInLayer.set(layer, position);
      }
    }
    LogHelper.exitMethod(getClass(),
                         "addNode: numberOfLayers = " + numberOfLayers
                         + ", sizeOfLayer = " + layerSize.get(layer)
                         + ", maxPositionInLayer = " + maxPositionInLayer.get(layer));
    LogHelper.restoreState();
  }

  /**
   * sets the vertical flag properly now that all layers are filled in
   */
  void initializeAfterParsing() {
    vertical = false;
    for ( int layer = 0; layer < numberOfLayers; layer++ ) {
      if ( maxPositionInLayer.get(layer)
           > layerSize.get(layer) - 1 ) vertical = true;
    }
  }
}

//  [Last modified: 2017 04 18 at 20:07:28 GMT]
