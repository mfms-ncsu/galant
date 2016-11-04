/**
 * grid.alg - creates a grid graph with dimensions supplied by user
 */

algorithm {
    Integer width = getInteger("width =");
    Integer height = getInteger("height =");
    Integer space = getInteger("space between vertices");
    Node [][] grid = new Node[width][height];
    for ( int i = 0; i < width; i++ ) {
        for ( int j = 0; j < height; j++ ) {
            grid[i][j] = addNode((i+1) * space, (j+1) * space);
            if ( i > 0 ) addEdge(grid[i][j], grid[i-1][j]);
            if ( j > 0 ) addEdge(grid[i][j], grid[i][j-1]);
        }
    }
}

//  [Last modified: 2016 11 04 at 12:20:53 GMT]
