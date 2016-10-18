package edu.ncsu.csc.Galant;

import edu.ncsu.csc.Galant.gui.util.ExceptionDialog;
import edu.ncsu.csc.Galant.logging.LogHelper;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.algorithm.AlgorithmSynchronizer;

/**
 * A catch all for all exceptions occurring in Galant; this acts as the
 * end of any chain of exceptions and allows the program to report the
 * exception on the console and/or display it in a GUI window.
 *
 * $Id: GalantException.java 114 2015-05-08 15:21:55Z mfms $
 */
public class GalantException extends Exception {
    public static final int STACK_PRINT_LIMIT = 10;
    public static final int LINE_NUMBER_POSITION = 32;

    public GalantException( String message ) {
        super( message );
    }

    public GalantException( String message, Exception exception ) {
        super( message, exception );
    }

    public void report( String message ) {
        System.out.printf( "%s\n", this.getMessage() );

        if ( LogHelper.isEnabled() ) {
            this.printStackTrace( System.out );
            return;
        }

        StackTraceElement [] traceElements = this.getStackTrace();
        int stackPrintLimit = traceElements.length;
        if ( STACK_PRINT_LIMIT < stackPrintLimit )
            stackPrintLimit = STACK_PRINT_LIMIT;
        for ( int i = 0; i < stackPrintLimit; i++
                  // StackTraceElement element : traceElements
              ) {
            StackTraceElement element = traceElements[ i ];
            //            System.out.printf( " %s\t", element.getClassName() );
            String fileName = element.getFileName();
            int fileNameLength = fileName.length();
            System.out.printf( "%s", element.getFileName() );
            for ( int j = fileNameLength; j < LINE_NUMBER_POSITION; j++ )
                System.out.printf( " " );
            System.out.printf( "%s\t", element.getLineNumber() );
            System.out.printf( "%s\n", element.getMethodName() );
        }
        if ( stackPrintLimit < traceElements.length ) {
            System.out.printf( " ... %d more\n",
                               traceElements.length - stackPrintLimit );
        }
    }

    public synchronized void display() throws Terminate {
        GraphDispatch dispatch = GraphDispatch.getInstance();
        System.out.println("animation mode: " + dispatch.isAnimationMode());
        if ( ! dispatch.isAnimationMode() ) {
            AlgorithmSynchronizer synchronizer = dispatch.getAlgorithmSynchronizer();
            System.out.println("synchronizer: " + synchronizer);
            System.out.println("algorithm finished: " + synchronizer.algorithmFinished() );
            if ( ! (synchronizer == null) ||
                 ! synchronizer.algorithmFinished() ) {
                throw new Terminate();
            }
        }
        ExceptionDialog.displayExceptionInDialog( this );
    }
}

//  [Last modified: 2016 10 18 at 15:02:21 GMT]
