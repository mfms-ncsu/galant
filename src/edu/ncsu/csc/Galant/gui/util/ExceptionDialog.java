package edu.ncsu.csc.Galant.gui.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.Thread.UncaughtExceptionHandler;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * <p> Methods to support showing exceptions in dialog windows, so users
 * using a GUI actually know what happened. (They call
 * <code>printStackTrace()</code> as well as showing the exception in a
 * dialog, so don't worry if you still want the exceptions printed to the
 * console.) How to use: <ul> <li>In <code>main</code>, call {@link
 * #setDialogExceptionHandlerAsDefault()} so uncaught exceptions will appear
 * in dialogs.</li> <li>When you have to handle an exception that you don't
 * expect to come up and don't know what do do with, call one of the
 * <code>displayExceptionInDialog</code> methods instead of just saying
 * something like <code>e.printStackTrace();</code>.</li> </ul> </p>
 */
public class ExceptionDialog {
    /**
     * Displays <code>e</code> in a dialog with an option to see its call
     * stack. Calls {@link #displayExceptionInDialog(Throwable, String)}
     * with <code>null</code> as the second argument.
     * @param e the <code>Exception</code> to display.
     */
    public static void displayExceptionInDialog(Throwable e) {
        e.printStackTrace();
        displayExceptionInDialog(e, (String) null);
    }

    /**
     * Displays <code>e</code> in a dialog with an option to see its call
     * stack. Calls {@link #displayExceptionInDialog(Throwable, String,
     * Runnable)} with no <code>Runnable</code>.
     * @param e the <code>Exception</code> to display.
     * @param interpretation a message to show the user. It can include
     *        html. (If it does, it must include the {@literal <html>} tags.)
     *        If this is <code>null</code>, displays
     *        <code>e.toString()</code>.
     */
    public static void displayExceptionInDialog(Throwable e,
                                                String interpretation) {
        displayExceptionInDialog(e, interpretation, null);
    }

    /**
     * Displays <code>e</code> in a dialog with an option to see its call
     * stack. Calls {@link #displayExceptionInDialog(Throwable, String,
     * Runnable)}, passing <code>null</code> as the second argument.
     * @param e the <code>Exception</code> to display.
     * @param onClose a <code>Runnable</code> to run when the dialog is
     *        closed. If this is <code>null</code>, it is ignored.
     */
    public static void displayExceptionInDialog(Throwable e,
                                                Runnable onClose) {
        displayExceptionInDialog(e, null, onClose);
    }

    /**
     * Displays an error message in a dialog with an option to see
     * information about the <code>Throwable</code> that occured and the
     * choice to either continue or quit the program, and calls {@link
     * Throwable#printStackTrace()} on the <code>Throwable</code>.
     * @param e the <code>Throwable</code> that occured. If this is
     * <code>null</code>, does nothing.
     * @param interpretation a message to show the user. It can include
     *        html. (If it does, it must include the {@code <html>} tags.) If
     *        this is <code>null</code>, displays <code>e.toString()</code>.
     * @param onClose a <code>Runnable</code> to run when the dialog is
     *        closed. If this is <code>null</code>, it is ignored.
     */
    public static void displayExceptionInDialog(Throwable e,
                                                String interpretation,
                                                final Runnable onClose) {
        LogHelper.logDebug("-> displayExceptionInDialog(e, interpretation, onClose)");
        if ( e == null )
            return;

        final JPanel infoDisp = new JPanel(new BorderLayout());
        final JComponent info =
            getDisplayFor(e,
                          interpretation == null
                          ? new String[]{"at:"}
                          : new String[]{e.toString(), "at:"});
        JButton moreInfo
            = new JButton("<html><u>More Information&hellip;</u></html>");
        moreInfo.setContentAreaFilled(false);
        moreInfo.setBorderPainted(false);
        moreInfo.setFocusPainted(false);
        moreInfo.setForeground(Color.BLUE);
        JLabel message = new JLabel(interpretation == null
                                    ? e.toString()
                                    : interpretation);
        message.setToolTipText(message.getText());
        Object[] options = {"Continue", "Exit Galant"};
        JOptionPane pane =
            new JOptionPane(new Object[]{message, moreInfo, infoDisp},
                            JOptionPane.ERROR_MESSAGE,
                            JOptionPane.DEFAULT_OPTION,
                            null, options, options[0]);
        final JDialog dialog = pane.createDialog("Error");
        dialog.setResizable(true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        infoDisp.add(info);
        info.setVisible(false);
        moreInfo.addActionListener(new ActionListener(){
                boolean shown = false;
                @Override
                public void actionPerformed(ActionEvent e) {
                    info.setVisible(!shown);
                    Dimension old = dialog.getSize();
                    dialog.setSize(dialog.getLayout().
                                   preferredLayoutSize(dialog));
                    Point p = dialog.getLocation();
                    p.translate((old.width - dialog.getWidth()) / 2,
                                (old.height - dialog.getHeight()) / 2);
                    dialog.setLocation(p);
                    shown = ! shown;
                }
            });
        dialog.setSize(dialog.getLayout().preferredLayoutSize(dialog));
        dialog.setVisible(true);
        if ( onClose != null )
            onClose.run();
        Object selected = pane.getValue();
        if ( selected != null )
            if ( options[1].equals(selected) )
                System.exit(1);
        LogHelper.logDebug("<- displayExceptionInDialog(e, interpretation, onClose)");
    }

	/**
     * Returns a <code>JComponent</code> that displays <code>e</code>. This
     * consists of a <code>Box</code>, aligned vertically, containing the
     * following things: <ol> <li>the <code>String</code>s in
     * <code>intro</code>, wrapped in <code>JLabel</code>s.</li> <li>a
     * <code>JScrollPane</code> wrapped around a <code>JList</code> showing
     * the stack trace of <code>e</code>.</li> <li>if <code>e</code> has a
     * cause, the display for its cause, with an intro of: <ul> caused by:<br
     * /> <i>cause.toString()</i><br /> at: </ul> </li> </ol>
     * @param e the <code>Throwable</code> to get a display for. If this is
     * <code>null</code>, does nothing.
     * @param intro a series of <code>String</code>s to display before the
     * stack trace.
     */
    private static JComponent getDisplayFor(Throwable e, String... intro) {
        if(e == null)
            return null;
        Box display = new Box(BoxLayout.Y_AXIS);
        for(String str : intro)
            {
                JLabel label = new JLabel(str);
                label.setToolTipText(label.getText());
                display.add(label);
            }
        JList<StackTraceElement> stack
            = new JList<StackTraceElement>(e.getStackTrace());
        stack.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        display.add(new JScrollPane(stack));
        if(e.getCause() != null)
            display.add(getDisplayFor(e.getCause(), "caused by:", e.getCause().toString(), "at:"));
        return display;
    }

    private static UncaughtExceptionHandler dialogExceptionHandler = null;

    /**
     * Returns an <code>UncaughtExceptionHandler</code> that displays
     * uncaught exceptions in a dialog saying that "An unexpected exception
     * occured."
     * @return an <code>UncaughtExceptionHandler</code> that uses dialogs.
     * @see #displayExceptionInDialog(Throwable, String)
     */
    public static UncaughtExceptionHandler getDialogExceptionHandler() {
        if(dialogExceptionHandler == null)
            dialogExceptionHandler = new UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        displayExceptionInDialog(e,
                                                 "<html><font color = \"red\">An unexpected exception occured.</font></html>");
                    }
                };
        return dialogExceptionHandler;
    }

    /**
     * Sets the default uncaught exception handler to that returned by {@link #getDialogExceptionHandler()}.
     */
    public static void setDialogExceptionHandlerAsDefault() {
        Thread.setDefaultUncaughtExceptionHandler(getDialogExceptionHandler());
    }
}

//  [Last modified: 2016 12 14 at 17:22:51 GMT]
