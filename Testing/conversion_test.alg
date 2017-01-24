/**
 * conversion_test.alg - tests the conversion functions offered by Galant;
 * also uses a query for a string at the beginning
 */

algorithm {
  double y = 1.49999;
  double z = 1.5;
  Integer j = 3;
  display("integer(1.49999) = " + integer(y));
  display("integer(1.5) = " + integer(z));
  display("integer(3) = " + integer(j));
  String s = getString("Need a string to convert to a number (possibly illegal)");
  Double x = real(s);
  display("conversion to float successful " + x);
  Integer i = integer(s);
  display("conversion to integer successful " + i);
}

//  [Last modified: 2017 01 24 at 17:21:44 GMT]
