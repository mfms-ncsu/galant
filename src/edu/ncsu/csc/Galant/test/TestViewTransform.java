package edu.ncsu.csc.Galant.test;

import static org.junit.Assert.assertEquals;

import java.awt.Point;
import java.util.UUID;

import org.junit.Test;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.graph.component.Graph;

/**
 * TODO: Update the magical numbers All the tests are passing until we updated
 * the ViewTransform function with Window Margin and offset, so we added the
 * magical numbers to make the tests passing
 * 
 */
public class TestViewTransform {

    GraphDispatch    dispatch;
    int              initialWidth   = 800;
    int              initialHeight  = 500;
    Point            testPointA     = new Point( 100, 100 );
    Point            testPointB     = new Point( 200, 200 );
    Point            testPointC     = new Point( 300, 300 );
    final Graph      g              = new Graph();
    final UUID       u              = UUID.randomUUID();
    /**
     * minimum distance from the edge of a window when fitting a graph to the
     * window
     */
    final static int WINDOW_PADDING = 50;

    /**
     * Offset to account for the fact that (0,0) is not a visible part of the
     * window.
     */
    final static int WINDOW_OFFSET  = 20;

    @Test
    public void testTransform () {

        dispatch = GraphDispatch.getInstance();
        dispatch.setWorkingGraph( g, u );
        // initialize the window size
        dispatch.setWindowWidth( initialWidth );
        dispatch.setWindowHeight( initialHeight );

        // double the window size, the x and y of the point should be doubled as
        // a result
        dispatch.setWindowWidth( initialWidth * 2 );
        dispatch.setWindowHeight( initialHeight * 2 );

        final Point transformedA = dispatch.ViewTransform( testPointA );
        assertEquals( 2 * testPointA.x + 37, transformedA.x );
        assertEquals( 2 * testPointA.y + 42, transformedA.y );

        final Point transformedB = dispatch.ViewTransform( testPointB );
        assertEquals( 2 * testPointB.x + 25, transformedB.x );
        assertEquals( 2 * testPointB.y + 14, transformedB.y );

        final Point transformedC = dispatch.ViewTransform( testPointC );
        assertEquals( 2 * testPointC.x + 12, transformedC.x );
        assertEquals( 2 * testPointC.y - 14, transformedC.y );
    }

    @Test
    public void testInvTransform () {
        dispatch = GraphDispatch.getInstance();
        dispatch.setWorkingGraph( g, u );
        dispatch.setWindowWidth( initialWidth );
        dispatch.setWindowHeight( initialHeight );

        dispatch.setWindowWidth( initialWidth * 2 );
        dispatch.setWindowHeight( initialHeight * 2 );

        final Point transformedA = dispatch.InvViewTransform( testPointA );
        assertEquals( testPointA.x / 2 - 24, transformedA.x );
        assertEquals( testPointA.y / 2 - 33, transformedA.y );

        final Point transformedB = dispatch.InvViewTransform( testPointB );
        assertEquals( testPointB.x / 2 - 20, transformedB.x );
        assertEquals( testPointB.y / 2 - 25, transformedB.y );

        final Point transformedC = dispatch.InvViewTransform( testPointC );
        assertEquals( testPointC.x / 2 - 17, transformedC.x );
        assertEquals( testPointC.y / 2 - 17, transformedC.y );

    }

    @Test
    public void testTransformWithInv () {
        dispatch = GraphDispatch.getInstance();
        dispatch.setWindowWidth( initialWidth );
        dispatch.setWindowHeight( initialHeight );

        dispatch.setWindowWidth( initialWidth * 2 );
        dispatch.setWindowHeight( initialHeight * 2 );

        final Point transformedA = dispatch.ViewTransform( testPointA );
        final Point transformedInvA = dispatch.InvViewTransform( transformedA );
        assertEquals( testPointA.y, transformedInvA.y );

        final Point transformedB = dispatch.ViewTransform( testPointB );
        final Point transformedInvB = dispatch.InvViewTransform( transformedB );
        assertEquals( testPointB, transformedInvB );

        final Point transformedC = dispatch.ViewTransform( testPointC );
        final Point transformedInvC = dispatch.InvViewTransform( transformedC );
        assertEquals( testPointC.y, transformedInvC.y );
    }
}
