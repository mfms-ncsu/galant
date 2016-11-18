/**
 * Used to jump to the end of the run method -- not a real exception
 */
package edu.ncsu.csc.Galant.algorithm;

public class Terminate extends Exception {
    public Terminate() { System.out.println("Terminate thrown"); }
}

//  [Last modified: 2016 11 18 at 12:50:19 GMT]
