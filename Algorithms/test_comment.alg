/***/

/** */

/**  */

/** 
  */

/** 
  *
  */

//

/** Comment line Tester
  * This is a algorithm that tests comment style */  

int /** Comment line Tester
  * This is a algorithm that tests comment style */    variable_one;
int variable_two;/** Comment line Tester
  * This is a algorithm that tests comment style */  
/** Comment line Tester
  * This is a algorithm that tests comment style */  int variable_three;

float variable_four;/** single line double-asterisk */
float /** single line double-asterisk */ variable_five;
 /** single line double-asterisk */ float variable_six;


double variable_seven; /* keep everything before and after single line c-style comment */ 
double /* keep everything before and after single line c-style comment */ variable_eight;
/* keep everything before and after single line c-style comment */  double variable_nine;


// more multiple line test

/** Expected output:	
  *  int variable_one; int variable_two; 
  * int variable three; float variable_four;
  * float variable_five; float variable_six; 
  * double variable_seven; double variable_eight; 
  * double variable_nine
  */

algorithm{ // keep every before double-slash and ignore the rest of comment
}