int HORIZONTAL_GAP = 100;
final int VERTICAL_GAP = 100;
final int TOP_GAP = 2*VERTICAL_GAP;
int arraySize = 0;
for_nodes(n){
	arraySize++;
}
Node[] nodeArray = new Node[arraySize];
int j = 0;
int xPosition = HORIZONTAL_GAP;
int yPosition = TOP_GAP;
for_nodes(n){
	n.setPosition(xPosition,yPosition);
	xPosition += HORIZONTAL_GAP;
	nodeArray[j] = n;
	j++;
}
