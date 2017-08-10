package edu.ncsu.csc.Galant;

/**
 * for global timers to be used for profiling efficiency; the global timers
 * are initialized in Galant.java and printing is invoked right before each
 * System.exit() call; initialization is unconditional, but start(), stop(),
 * and print() depend on TIMING
 */

public class Timer {
  public static final boolean TIMING = true;

  public static Timer parsingTime;
  public static Timer drawingTime;

  /**
   * name of this timer, used for printing
   */
  String name;
  
  /**
   * cumulative time in milliseconds
   */
  long cumulativeTime = 0;

  /**
   * time of checkpoint, i.e., time of last start() call
   */
  long startTime;

  public Timer(String name) {
    this.name = name;
    cumulativeTime = 0;
  }
  
  /**
   * @return cumulative time in seconds
   */
  public double getTotalTime() {
    return cumulativeTime / 1000.0;
  }

  /**
   * prints the total time
   */
  public void print() {
    if ( TIMING )
      System.out.printf("%s_time\t%5.2f\n", name, getTotalTime());
  }

  /**
   * starts counting time at this point
   */
  public void start() {
    if ( TIMING )
      startTime = System.currentTimeMillis();
  }

  /**
   * adds the time since the last start to the cumulative time
   */
  public void stop() {
    if ( TIMING )
      cumulativeTime += System.currentTimeMillis() - startTime;
  }
}

//  [Last modified: 2017 07 25 at 19:32:18 GMT]
