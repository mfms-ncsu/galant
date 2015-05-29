final int HORIZONTAL_GAP = 100;
final int VERTICAL_GAP = 100;
final int TOP_GAP=2*VERTICAL_GAP;
int arraySize=0;
for_nodes(n){
	arraySize++; // count how many nodes we have
}

Node[] nodeArray= new Node[arraySize]; // create an array of them
int j=0;

int xPosition=HORIZONTAL_GAP;
int yPosition=TOP_GAP;
for_nodes(n){ // line up all of the nodes nicely
    n.setPosition( xPosition, yPosition );
    xPosition += HORIZONTAL_GAP;
	nodeArray[j]=n;
	j++;
}
