/**
 * Sets up the hierarchy of preferences and declares each available
 * preference using utilities provided in prefs
 * @see edu.ncsu.csc.Galant.prefs
 *
 * @todo The mechanism here is heavily dependent on specific GUI features --
 * it should be possible to select preferences via keyboard shortcuts.
 */

package edu.ncsu.csc.Galant;

import java.awt.Color;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

import edu.ncsu.csc.Galant.gui.editor.GEditorFrame;
import edu.ncsu.csc.Galant.gui.prefs.components.ColorPanel;
import edu.ncsu.csc.Galant.gui.prefs.components.FilePanel;
import edu.ncsu.csc.Galant.gui.prefs.components.PreferenceSpinner;
import edu.ncsu.csc.Galant.prefs.Accessors;
import edu.ncsu.csc.Galant.prefs.Preference;
import edu.ncsu.csc.Galant.prefs.PreferenceGroup;
import edu.ncsu.csc.Galant.gui.window.panels.GraphPanel;

/** 
 * The actual preferences available in the program. 
 * @author Alex McCabe
 */
public class GalantPreferences
{
	private GalantPreferences()
	{}

	// ======== Declaration ========

	// Editors

	public static final PreferenceGroup EDITORS;

	public static final Preference<Integer> FONT_SIZE;

	public static final Preference<Integer> TAB_SIZE;

	// - Algorithm editor

	public static final PreferenceGroup ALGORITHM_EDITOR;

	public static final Preference<Color> JAVA_KEYWORD_COLOR;

	public static final Preference<Color> API_CALL_COLOR;

	// -Text graph editor

	public static final PreferenceGroup TEXT_GRAPH_EDITOR;

	public static final Preference<Color> GML_KEYWORD_COLOR;

	// -Visual graph editor

	public static final PreferenceGroup VISUAL_GRAPH_EDITOR;

	public static final Preference<Integer> EDGE_WIDTH;

	public static final Preference<Integer> NODE_RADIUS;

// 	public static final Preference<Boolean> DISPLAY_NODE_ID;

	// Open/Save

	public static final PreferenceGroup OPEN_SAVE;

	public static final Preference<File> DEFAULT_DIRECTORY;

	// Compilation

	public static final PreferenceGroup COMPILATION;

	public static final Preference<File> OUTPUT_DIRECTORY;

	// ======== Initialization ========

	static
	{
		// -------- Editors --------

		EDITORS = PreferenceGroup.ROOT.addNewChild("Editors");

		FONT_SIZE =
				EDITORS.addPreference(new Preference<Integer>("Font Size", UIManager.getDefaults()
						.getFont("TextPane.font").getSize(), Accessors.INT_ACCESSOR));
		new PreferenceSpinner(FONT_SIZE, 1, null, 1){
			@Override
			public void apply()
			{
				super.apply();
				GEditorFrame.getSingleton().setFontSize(FONT_SIZE.get());
				// TODO: apply it to the current session
			}
		};

		// TODO find the default tab size (I'm just using "4" for now; we could just stick
		// with that, I guess)
		TAB_SIZE = EDITORS.addPreference(new Preference<Integer>("Tab Size", 4, Accessors.INT_ACCESSOR));
		new PreferenceSpinner(TAB_SIZE, 1, null, 1){
			@Override
			public void apply()
			{
				super.apply();
				// TODO: apply it to the current session
			}
		};

		// ---- Algorithm Editor ----

		ALGORITHM_EDITOR = EDITORS.addNewChild("Algorithm Editor");

		JAVA_KEYWORD_COLOR =
				ALGORITHM_EDITOR.addPreference(new Preference<Color>("Java Keyword Color", new Color(0, 0, 255),
						Accessors.COLOR_ACCESSOR));
		new ColorPanel(JAVA_KEYWORD_COLOR){
			@Override
			public void apply()
			{
				super.apply();
				// TODO: apply it to the current session
			}
		};

		API_CALL_COLOR =
				ALGORITHM_EDITOR.addPreference(new Preference<Color>("API Call Color", new Color(0, 255, 0),
						Accessors.COLOR_ACCESSOR));
		new ColorPanel(API_CALL_COLOR){
			@Override
			public void apply()
			{
				super.apply();
				// TODO: apply it to the current session
			}
		};

		// ---- Text Graph Editor ----

		TEXT_GRAPH_EDITOR = EDITORS.addNewChild("Textual Graph Editor");

		GML_KEYWORD_COLOR =
				TEXT_GRAPH_EDITOR.addPreference(new Preference<Color>("GraphML Keyword Color", new Color(0, 0, 255),
						Accessors.COLOR_ACCESSOR));
		new ColorPanel(GML_KEYWORD_COLOR){
			@Override
			public void apply()
			{
				super.apply();// TODO: apply it to the current session
			}
		};

		// ---- Visual Graph Editor ----

		VISUAL_GRAPH_EDITOR = EDITORS.addNewChild("Visual Graph Editor");

		EDGE_WIDTH =
				VISUAL_GRAPH_EDITOR.addPreference( new Preference<Integer>( "Edge Width", 
                                                                           GraphPanel.DEFAULT_WIDTH,
                                                                           Accessors.INT_ACCESSOR ) );
		new PreferenceSpinner(EDGE_WIDTH, 1, GraphPanel.MAXIMUM_WIDTH, 1){
			@Override
			public void apply()
			{
				GraphDispatch.getInstance().pushToGraphEditor();
				super.apply();
			}
		};

		NODE_RADIUS =
				VISUAL_GRAPH_EDITOR.addPreference( new Preference<Integer>( "Node radius",
                                                                            GraphPanel.DEFAULT_NODE_RADIUS,
                                                                            Accessors.INT_ACCESSOR ) );
		new PreferenceSpinner(NODE_RADIUS, 1, GraphPanel.MAXIMUM_NODE_RADIUS, 1) {
			@Override
			public void apply()
			{
				GraphDispatch.getInstance().pushToGraphEditor();
				super.apply();
			}
		};

		// -------- Open/Save --------

		OPEN_SAVE = PreferenceGroup.ROOT.addNewChild("Open/Save");

		DEFAULT_DIRECTORY =
				OPEN_SAVE.addPreference(new Preference<File>("Default Directory", FileSystemView.getFileSystemView()
						.getHomeDirectory(), Accessors.FILE_ACCESSOR));
		new FilePanel(DEFAULT_DIRECTORY, "Select", JFileChooser.DIRECTORIES_ONLY);

		// -------- Compilation --------

		COMPILATION = PreferenceGroup.ROOT.addNewChild("Compilation");

		OUTPUT_DIRECTORY =
				COMPILATION.addPreference(new Preference<File>("Output Directory", new File(".galant"),
						Accessors.FILE_ACCESSOR));
		new FilePanel(OUTPUT_DIRECTORY, "Select", JFileChooser.DIRECTORIES_ONLY);
	}

	/** Make sure all this stuff is actually called. */
	public static void initPrefs()
	{}
}

//  [Last modified: 2015 05 29 at 15:16:03 GMT]
