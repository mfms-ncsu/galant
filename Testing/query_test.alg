algorithm {
     String text = getString("give me text");
     display(text);
     print(text);
     Integer integer = getInteger("give me an integer");
     display(integer);
     print(integer);
     Double real = getDouble("give me a double");
     display(real);
     print(real);
     Node node = getNode("what node should I highlight?");
     highlight(node);
     Edge edge = getEdge("what edge should I highlight?");
     highlight(edge);
}

//  [Last modified: 2016 09 16 at 16:54:53 GMT]
