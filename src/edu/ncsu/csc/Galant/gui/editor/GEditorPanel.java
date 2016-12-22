package edu.ncsu.csc.Galant.gui.editor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AbstractDocument.BranchElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.StyleConstants;

import edu.ncsu.csc.Galant.GalantPreferences;
import edu.ncsu.csc.Galant.gui.editor.GTabbedPane.TabRenderer;

import edu.ncsu.csc.Galant.logging.LogHelper;
import edu.ncsu.csc.Galant.gui.util.ExceptionDialog;

/**
 * Abstract class for the editor window's text panes.
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 *
 */
public abstract class GEditorPanel extends JPanel implements DocumentListener, PropertyChangeListener {
	
	/**
	 * The wholistic instance of the editor associated with this panel.
	 * In any running instance of Galant, there should be only one.
	 * Thus once initialized, this should never change.
	 */
	protected final GTabbedPane parentPane;
	
	/**
	 * 
	 */
	protected JTextArea lineNumbers;
	
	/**
	 * The actual text editor pane associated with this edit session,
	 * as distinguished from the surrounding buttons, etc.
	 */
	protected JTextPane textPane;
	
	/**
	 * The renderer for the tab on this edit session, which should be updated
	 * if the file name changes or the file is dirtied.
	 */
	protected TabRenderer tbr;
	
	protected Runnable syntaxHighlighter;
	
	/**
	 * Is the current edit session dirty?
     *
     * @todo currently this is set way too often (even when a file is
     * initially loaded!); it needs to be set only when there is a real
     * change in the algorithm or graph being edited
	 */
	protected boolean isDirty;
	
	/**
	 * The name of the file including the .alg or .graphml extension.
	 */
	protected String fileName;
	
	/**
	 * The path and name of the file including the extension.
	 */
	protected String filePath;
	
	public GEditorPanel(GTabbedPane _parentPane, String _fileName, String content) {
		LogHelper.enterConstructor(getClass());
		parentPane = _parentPane;
		fileName = _fileName;
		isDirty = false;
		setLayout(new BorderLayout());
		
		textPane = new JTextPane() { 
                @Override
                public String getToolTipText(MouseEvent me) {
                    String content = (getText() + "  ").replace("\r\n", "\n");
                    viewToModel(me.getPoint());
                    try {
                        int loc = viewToModel(me.getPoint());
                        AttributeSet as = getStyledDocument().getCharacterElement(loc).getAttributes();
                        if(as.containsAttribute(StyleConstants.Foreground, GAlgorithmSyntaxHighlighting.apiKeywordColor)) {
                            while(loc >= 0 && Character.isJavaIdentifierPart(content.charAt(loc))) loc--;
                            loc++;
                            int begin = loc;
                            while(loc < content.length() && Character.isJavaIdentifierPart(content.charAt(loc))) loc++;
                            int end = loc;
                            return GAlgorithmSyntaxHighlighting.APIdictionary.get(content.substring(begin, end));
                        }
                    } finally {} return null;
                }
            };
		
		textPane.setText(content);
		textPane.setEditable(true);
		textPane.getDocument().addDocumentListener(this);
		
		JScrollPane scrollPane = new JScrollPane(textPane, 
                                                 ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,  ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane, BorderLayout.CENTER);
		
		final JTextArea lines = new JTextArea(" 001 ");
		
		lines.setBackground(Color.LIGHT_GRAY);
		lines.setEditable(false);
		
		textPane.getDocument().addDocumentListener(new DocumentListener(){
                public String getText(){
                    int caretPosition = textPane.getDocument().getLength();
                    Element root = textPane.getDocument().getDefaultRootElement();
                    String text = " 001" + " " + System.getProperty("line.separator") + " ";
                    for(int i = 2; i < root.getElementIndex( caretPosition ) + 2; i++){
                        if(i < 100) text += "0";
                        if(i < 10) text += "0";
                        text += i + " " + System.getProperty("line.separator") + " ";
                    }
                    return text;
                }
                @Override
                public void changedUpdate(DocumentEvent de) {lines.setText(getText());}
                @Override
                public void insertUpdate(DocumentEvent de) {lines.setText(getText());}
                @Override
                public void removeUpdate(DocumentEvent de) {lines.setText(getText());}
 
            });
		
		lineNumbers = lines;
		scrollPane.setRowHeaderView(lineNumbers);
		
		setFontSize(GalantPreferences.FONT_SIZE.get());
		
		ToolTipManager.sharedInstance().registerComponent(textPane);
		
		LogHelper.exitConstructor(getClass());
	}
	
	protected void documentUpdated() {
		if(syntaxHighlighter != null) 
            try {
                SwingUtilities.invokeLater(syntaxHighlighter);
            } catch(Exception e){ExceptionDialog.displayExceptionInDialog(e);}
	}
	
	@Override
	public void changedUpdate(DocumentEvent arg0) {}
	@Override
	public void insertUpdate(DocumentEvent arg0) {
        LogHelper.disable();
        LogHelper.enterMethod(getClass(), "insertUpdate");
		setDirty(true);
		if(arg0.getChange(textPane.getStyledDocument().getDefaultRootElement()) instanceof AbstractDocument.ElementEdit) {
			AbstractDocument.ElementEdit ee = ((AbstractDocument.ElementEdit) arg0.getChange(textPane.getStyledDocument().getDefaultRootElement()));
			if(ee.getChildrenAdded().length > 0 && ee.getChildrenAdded()[0] instanceof AbstractDocument.BranchElement) {
				AbstractDocument.BranchElement branch = (BranchElement) ee.getChildrenAdded()[0];

				// preserve tabbed indentation across lines if the user is N
				// indents deep, add N indents to the beginning of each line
				String content = textPane.getText();
				int numConsecutiveTabs = 0;
				final int branchPoint = branch.getStartOffset();
				for(int ii = 0; ii < branchPoint-1; ii++) {
					if(content.charAt(ii) == '\t') numConsecutiveTabs++;
					if(content.charAt(ii) == '\n') numConsecutiveTabs = 0;
				}
				final int fNumTabs = numConsecutiveTabs;

				try{ SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() { try { for(int ii = 0; ii < fNumTabs; ii++)
                                    textPane.getStyledDocument().insertString(branchPoint, "\t", null);
                            } catch (BadLocationException e) { ExceptionDialog.displayExceptionInDialog(e); } }
                    }); } finally {}
			}
		}
		documentUpdated();
        LogHelper.exitMethod(getClass(), "insertUpdate");
        LogHelper.restoreState();
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {setDirty(true); documentUpdated();}
	
	public void setDirty(Boolean _isDirty) {
		isDirty = _isDirty;
		if(tbr != null) {tbr.updateLabel(fileName, isDirty);}
	}
	
	public void setFileName(String _fileName) {
		fileName = _fileName;
		if(tbr != null) {tbr.updateLabel(fileName, isDirty);}
	}
	
	public void setTabRenderer(TabRenderer _tbr) {tbr = _tbr;}
	public String getText() { return textPane.getText(); }
	public Boolean getDirty() {return isDirty;}
	public String getFileName() {return fileName;}
	public void setFilePath(String _filePath) {filePath = _filePath;}
	public String getFilePath() {return filePath;}
	
	@Override
	public String toString() {
		return this.getClass().getName() + ": " + fileName + ", " + getText().substring(0, 10);
	}
	
	public void setFontSize(Integer size) {
		if(size != null && size > 0) {
			if(textPane !=null) textPane.setFont(new Font(Font.DIALOG, Font.PLAIN, size));
			if(lineNumbers != null) lineNumbers.setFont(new Font(Font.DIALOG, Font.PLAIN, size));
		}
	}
	
	public void setTabSize(Integer size) {
		if(size != null && size > 0) 
			textPane.getStyledDocument()
				.putProperty(PlainDocument.tabSizeAttribute, size);
	}
}

//  [Last modified: 2016 12 22 at 17:20:00 GMT]
