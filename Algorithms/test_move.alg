/** Tests how algorithm affects position and how, if at all, this is
 * influenced by actions of the user */

Node x = getNodeById( 0 );
System.out.println( "State = " + getState() + " position = " + x.getPosition() );
x.setPosition( 200, 200 );
System.out.println( "State = " + getState() + " position = " + x.getPosition()  );
x.setColor("FF0000");
x.setPosition( 200, 400 );
System.out.println( "State = " + getState() + " position = " + x.getPosition()  );
x.setColor("00FF00");
beginStep();
x.setPosition( 400, 400 );
System.out.println( "State = " + getState() + " position = " + x.getPosition()  );
x.setColor("#0000FF");
endStep();
beginStep();
x.setPosition( 600, 400 );
System.out.println( "State = " + getState() + " position = " + x.getPosition()  );
x.setColor("00FF00");
endStep();

//  [Last modified: 2014 05 14 at 19:29:21 GMT]
