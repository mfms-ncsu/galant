package edu.ncsu.csc.Galant.gui.editor;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.ncsu.csc.Galant.Galant;
import edu.ncsu.csc.Galant.GalantPreferences;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.parser.GraphMLParser;
import edu.ncsu.csc.Galant.gui.editor.GTabbedPane.AlgorithmOrGraph;
import edu.ncsu.csc.Galant.gui.util.ExceptionDialog;
import edu.ncsu.csc.Galant.gui.util.WindowUtil;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * Frame for text editing in the editor window.
 * @author Jason Cockrell
 */
public class GEditorFrame extends JFrame implements WindowListener {
	
	public static final String FILENAME_EXTENSION_MESSAGE;
	public static final String GALANT = "Galant " + Galant.VERSION + ": Text Editor";
	public static final String CONFIRM = "One or more files have unsaved changes. Really close Galant?";
	
	public static final int defaultWidth = 800;
	public static final int defaultHeight = 600;
	
	private static GEditorFrame singleton;
	
	private final GTabbedPane tabbedPane;
	private final JFileChooser jfc;
	
	static
		{
			StringBuilder extMessageBuilder = new StringBuilder("Filenames must end with extension ");
			List<String> filenameExtensions = AlgorithmOrGraph.getAllFileExtensions();
			for(int i = 0; i < filenameExtensions.size(); i++)
				{
					extMessageBuilder.append("." + filenameExtensions.get(i));
					if(filenameExtensions.size() > 2 && i < filenameExtensions.size() - 1)
						{
							extMessageBuilder.append(",");
							if(i != filenameExtensions.size() - 2)
								extMessageBuilder.append(" ");
						}
					if(i == filenameExtensions.size() - 2)
						extMessageBuilder.append(" or ");
				}
			extMessageBuilder.append(".");
			FILENAME_EXTENSION_MESSAGE = extMessageBuilder.toString();
		}

	
	public GEditorFrame() {
		super(GALANT);
		singleton = this;
		setResizable(true);
		setName("text_editor");
		WindowUtil.preserveWindowBounds(this, GraphWindow.DEFAULT_WIDTH, 0, defaultWidth, defaultHeight);
		setLayout(new BorderLayout());

		tabbedPane = new GTabbedPane();
		jfc = new JFileChooser();		
		
		add(tabbedPane);
		add(new GEditorMenuBar(this), BorderLayout.NORTH);

		jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		jfc.setFileFilter(new FileNameExtensionFilter("", AlgorithmOrGraph.getAllFileExtensions().toArray(
			new String[AlgorithmOrGraph.getAllFileExtensions().size()])));
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		
		setVisible(true);
	}
	
	public void open() {
		jfc.setCurrentDirectory(GalantPreferences.DEFAULT_DIRECTORY.get());
		int returnVal = jfc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();
            GTabbedPane.AlgorithmOrGraph type = AlgorithmOrGraph.typeForFileName(file.getName());
            if(type != null) {
           	 Scanner scanner = null;
           	  try {
            	 scanner = new Scanner(file);
            	 scanner.useDelimiter("\\A");
            	 tabbedPane.addEditorTab(file.getName(), file.getPath(), scanner.hasNext() ? scanner.next() : "", type);

           	  } catch(Exception e) { ExceptionDialog.displayExceptionInDialog(e); }
           	  	finally { if(scanner != null) scanner.close(); }
            } else JOptionPane.showMessageDialog(this, FILENAME_EXTENSION_MESSAGE); 
        }
	}
	
	public void loadCompiledAlgorithm() {		
		// Filter for .class file
		FileNameExtensionFilter filter = new FileNameExtensionFilter(".class File","class");
		jfc.setFileFilter(filter);

		// Set the initial Diectory as where .class file temporary stored.
		jfc.setCurrentDirectory(GalantPreferences.OUTPUT_DIRECTORY.get());
		int returnVal = jfc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            if(jfc.getSelectedFile().isFile()) {
            	File file = jfc.getSelectedFile();

            	// Since files are filtered, no need to check the extension exception.
            	try{
            		 // The param content is not needed for compiled algorithm tab. We'll leave it null here. 
	            	 tabbedPane.addEditorTab(file.getName(), file.getPath(), null, AlgorithmOrGraph.CompiledAlgorithm); 
	            } catch(Exception e) { ExceptionDialog.displayExceptionInDialog(e); }
            }
        } 
	}

	public void saveAs() {
		GEditorPanel gaep = tabbedPane.getSelectedPanel();
		
		if (GGraphEditorPanel.class.isInstance(gaep)) {
			updateWorkingGraph((GGraphEditorPanel) gaep);
		}
		
		jfc.setCurrentDirectory(GalantPreferences.DEFAULT_DIRECTORY.get());
		File file = null;
		int returnVal = jfc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = jfc.getSelectedFile();
			save(file, gaep);
		}
	}
	
	public void save() {
		LogHelper.enterMethod(getClass(), "save()");
		GEditorPanel gaep = tabbedPane.getSelectedPanel();
		
		if (GGraphEditorPanel.class.isInstance(gaep)) {
			updateWorkingGraph((GGraphEditorPanel) gaep);
		}
		
		if(gaep.getFilePath() != null) {
			save(new File(gaep.getFilePath()), gaep);
		} else { saveAs(); }
		LogHelper.exitMethod(getClass(), "save()");
	}
		
	public void save(File file, GEditorPanel gaep) {
		LogHelper.enterMethod(getClass(), "save(File, GEditorPanel)");
		
		if (GGraphEditorPanel.class.isInstance(gaep)) {
			updateWorkingGraph((GGraphEditorPanel) gaep);
		}
		
		if(file != null && AlgorithmOrGraph.typeForFileName(file.getName()) != null) {
			FileWriter outfile = null;
           	try {
				outfile = new FileWriter(file);
           		outfile.write(gaep.getText());
           		gaep.setDirty(false);
           		gaep.setFileName(file.getName());
           		gaep.setFilePath(file.getPath());
           	} catch(Exception e) { ExceptionDialog.displayExceptionInDialog(e); } 
           	finally { try { if(outfile != null) outfile.close(); } catch (IOException e) { ExceptionDialog.displayExceptionInDialog(e); } }
        } else JOptionPane.showMessageDialog(this, FILENAME_EXTENSION_MESSAGE);
		LogHelper.exitMethod(getClass(), "save(File, GEditorPanel)");
	}
	
	public static GEditorFrame getSingleton() {return singleton;}

	private static void updateWorkingGraph(GGraphEditorPanel gep) {
		try {
			GraphMLParser parser = new GraphMLParser(gep.getText());
			GraphDispatch.getInstance().setWorkingGraph(parser.getGraph(), gep.getUUID());
		} catch (Exception e) {
			GraphDispatch.getInstance().setWorkingGraph(new Graph(), gep.getUUID());
		}
	}
	
	public void setFontSize(Integer size) {tabbedPane.setFontSize(size);}
	public void setTabSize(Integer size) {tabbedPane.setTabSize(size);}

	@Override
	public void windowActivated(WindowEvent e) {}
	@Override
	public void windowClosed(WindowEvent e) {}
	@Override
	public void windowClosing(WindowEvent e) {
		if(tabbedPane.isDirty()) {
		if(JOptionPane.showOptionDialog(this, CONFIRM, GTabbedPane.CONFIRM, 
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[] {GTabbedPane.YES, GTabbedPane.NO}, GTabbedPane.NO) == 0) 
			System.exit(0); }
		else System.exit(0);
	}
	@Override
	public void windowDeactivated(WindowEvent e) {}
	@Override
	public void windowDeiconified(WindowEvent e) {}
	@Override
	public void windowIconified(WindowEvent e) {}
	@Override
	public void windowOpened(WindowEvent e) {}
}

//  [Last modified: 2016 06 23 at 19:43:03 GMT]
