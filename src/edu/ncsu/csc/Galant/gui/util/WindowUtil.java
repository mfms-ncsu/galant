package edu.ncsu.csc.Galant.gui.util;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.ncsu.csc.Galant.GalantPreferences;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.gui.editor.GEditorFrame;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * Wrapper class for the windows used in the GUI.
 * 
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 */
public class WindowUtil {
	private WindowUtil() {
	}

	/**
	 * An {@link Action} that quits the program, with name "Quit", mnemonic Q, and
	 * accelerator
	 * ctrl/cmd + Q.
	 *
	 * @todo Oddly this invokes the dialog from the menu but not from the
	 *       keyboard shortcut.
	 */
	public static final Action QUIT_ACTION = new AbstractAction("Quit") {
		{
			putValue(MNEMONIC_KEY, KeyEvent.VK_Q);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit
					.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			GEditorFrame.getSingleton().dispatchEvent(
					new WindowEvent(GEditorFrame.getSingleton(),
							WindowEvent.WINDOW_CLOSING));
		}
	};

	public static final Action EXPORT_ACTION = new AbstractAction("Export") {
		{
			putValue(MNEMONIC_KEY, KeyEvent.VK_E);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			GraphDispatch gd = GraphDispatch.getInstance();
			Graph g = gd.getWorkingGraph();
			LogHelper.enterMethod(getClass(), "export()");
			JFileChooser jfc = new JFileChooser();
			FileNameExtensionFilter filtergraphml = new FileNameExtensionFilter(
					"GraphML file (.graphml)",
					"graphml");
			jfc.addChoosableFileFilter(filtergraphml);
			jfc.setCurrentDirectory(GalantPreferences.DEFAULT_DIRECTORY.get());
			File file = null;
			int returnVal = jfc.showSaveDialog(GraphWindow.getGraphFrame());
			if ( returnVal == JFileChooser.APPROVE_OPTION ) {
				file = jfc.getSelectedFile();
				if ( file != null ) {
					FileWriter outfile = null;
					try {
						if ( ! file.getPath().endsWith(".graphml") ) {
							file = new File(file.getPath() + ".graphml");
						}
						int state = gd.getAlgorithmExecutor().getDisplayState();
						outfile = new FileWriter(file);
						outfile.write(g.xmlString(state));
					} catch ( Exception ex ) {
						ExceptionDialog.displayExceptionInDialog(ex);
					} finally {
						try {
							if ( outfile != null ) {
								outfile.close();
							}
						} catch ( IOException ex ) {
							ExceptionDialog.displayExceptionInDialog(ex);
						}
					}
				}
			}
			LogHelper.exitMethod(getClass(), "export()");
		}
	};

	private static final Preferences WINDOW_PREFS = Preferences
			.userNodeForPackage(WindowUtil.class);

	/**
	 * Keeps track of the given window's location and dimensions, so modifications
	 * made by the
	 * user are preserved between sessions. Note that the window's
	 * {@linkplain Window#setName(String) name} is used to identify it, so it is
	 * recommended to
	 * set that to ensure reliability.
	 */
	public static void preserveWindowBounds(final Window window, final int defaultX,
			final int defaultY, final int defaultWidth, final int defaultHeight) {
		window.addComponentListener(new ComponentAdapter() {
			private static final String X = "x", Y = "y", WIDTH = "width",
					HEIGHT = "height";

			private boolean componentShown = false;

			@Override
			public void componentShown(ComponentEvent e) {
				Rectangle rect = new Rectangle(retrieveValue(window, X, defaultX),
						retrieveValue(
								window, Y, defaultY),
						retrieveValue(window, WIDTH, defaultWidth), retrieveValue(
								window, HEIGHT, defaultHeight));
				LogHelper.logDebug(
						"Bounds retrieved for " + window.getName() + ": " + rect);
				window.setBounds(rect);
				componentShown = true;
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				if ( componentShown ) {
					storeValue(window, X, window.getX());
					storeValue(window, Y, window.getY());
				}
			}

			@Override
			public void componentResized(ComponentEvent e) {
				if ( componentShown ) {
					LogHelper.logDebug(
							"Size stored for " + window.getName() + ": " +
									window.getSize());
					storeValue(window, WIDTH, window.getWidth());
					storeValue(window, HEIGHT, window.getHeight());
				}
			}

			private void storeValue(Window window, String attribute, int value) {
				WINDOW_PREFS.putInt(getKey(window, attribute), value);
			}

			private int retrieveValue(Window window, String attribute, int def) {
				return WINDOW_PREFS.getInt(getKey(window, attribute), def);
			}

			private String getKey(Window window, String attribute) {
				return window.getName() + "_" + attribute;
			}
		});
	}

	private static class MakeVisible extends WindowAdapter {
		private enum State {
			NORMAL {
				@Override
				public void act(Window otherWindow) {
					currentState = FOCUS_LINKED_FRAME;
					otherWindow.toFront();
				}
			},
			FOCUS_LINKED_FRAME {
				@Override
				public void act(Window otherWindow) {
					currentState = RESTORE_FOCUS;
					otherWindow.toFront();
				}
			},
			RESTORE_FOCUS {
				@Override
				public void act(Window otherWindow) {
					currentState = NORMAL;
				}
			};

			public abstract void act(Window otherWindow);
		}

		private static State currentState = State.NORMAL;
		private Window otherWindow;

		public MakeVisible(Window otherWindow) {
			this.otherWindow = otherWindow;
		}

		@Override
		public void windowGainedFocus(WindowEvent e) {
			currentState.act(otherWindow);
		}
	}

	public static void linkWindows() {
		final JFrame graphFrame = GraphWindow.getGraphFrame(),
				editorFrame = GEditorFrame.getSingleton();
		graphFrame.addWindowFocusListener(new MakeVisible(editorFrame));
		editorFrame.addWindowFocusListener(new MakeVisible(graphFrame));
	}
}
