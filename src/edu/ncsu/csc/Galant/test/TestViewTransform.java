package edu.ncsu.csc.Galant.test;

import static org.junit.Assert.assertEquals;

import java.awt.Point;

import org.junit.Test;

import edu.ncsu.csc.Galant.GraphDispatch;

public class TestViewTransform {

    GraphDispatch dispatch;
    int           initialWidth  = 800;
    int           initialHeight = 500;
    Point         testPointA    = new Point( 100, 100 );
    Point         testPointB    = new Point( 200, 200 );
    Point         testPointC    = new Point( 300, 300 );

    @Test
    public void testTransform () {
        dispatch = GraphDispatch.getInstance();

        // initialize the window size
        dispatch.setWindowWidth( initialWidth );
        dispatch.setWindowHeight( initialHeight );

        // double the window size, the x and y of the point should be doubled as
        // a result
        dispatch.setWindowWidth( initialWidth * 2 );
        dispatch.setWindowHeight( initialHeight * 2 );

        final Point transformedA = dispatch.ViewTransform( testPointA );
        assertEquals( 2 * testPointA.x, transformedA.x );
        assertEquals( 2 * testPointA.y, transformedA.y );

        final Point transformedB = dispatch.ViewTransform( testPointB );
        assertEquals( 2 * testPointB.x, transformedB.x );
        assertEquals( 2 * testPointB.y, transformedB.y );

        final Point transformedC = dispatch.ViewTransform( testPointC );
        assertEquals( 2 * testPointC.x, transformedC.x );
        assertEquals( 2 * testPointC.y, transformedC.y );
    }

    @Test
    public void testInvTransform () {
        dispatch = GraphDispatch.getInstance();
        dispatch.setWindowWidth( initialWidth );
        dispatch.setWindowHeight( initialHeight );

        dispatch.setWindowWidth( initialWidth * 2 );
        dispatch.setWindowHeight( initialHeight * 2 );

        final Point transformedA = dispatch.InvViewTransform( testPointA );
        assertEquals( testPointA.x / 2, transformedA.x );
        assertEquals( testPointA.y / 2, transformedA.y );

        final Point transformedB = dispatch.InvViewTransform( testPointB );
        assertEquals( testPointB.x / 2, transformedB.x );
        assertEquals( testPointB.y / 2, transformedB.y );

        final Point transformedC = dispatch.InvViewTransform( testPointC );
        assertEquals( testPointC.x / 2, transformedC.x );
        assertEquals( testPointC.y / 2, transformedC.y );

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
        assertEquals( testPointA, transformedInvA );

        final Point transformedB = dispatch.ViewTransform( testPointB );
        final Point transformedInvB = dispatch.InvViewTransform( transformedB );
        assertEquals( testPointB, transformedInvB );

        final Point transformedC = dispatch.ViewTransform( testPointC );
        final Point transformedInvC = dispatch.InvViewTransform( transformedC );
        assertEquals( testPointC, transformedInvC );
    }
}
