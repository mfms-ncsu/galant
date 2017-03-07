package edu.ncsu.csc.Galant.gui.editor;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import edu.ncsu.csc.Galant.gui.util.ExceptionDialog;

/**
 * GraphML syntax highlighting for the textual graph editor
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 *
 */
public class GGraphSyntaxHighlighting implements Runnable {

  public static final String graphMLKeywordStyleName = "graphMLKeyword";

  public static final Color defaultGraphMLKeywordColor = new Color(0, 0, 255);

  public static Color graphMLKeywordColor = defaultGraphMLKeywordColor;

  private JTextPane textpane;

  public GGraphSyntaxHighlighting(JTextPane _textpane) {
    textpane = _textpane;

    StyledDocument doc = textpane.getStyledDocument();
    updateDocStyles(doc);
  }

  /**
   * An immutable list of all keywords employed in GraphML.
   */
  public static final String[] allGraphMLKeywords = new String[] {
    "desc", "locator", "data", "key", "default", "graphml",
    "graph", "node", "port", "edge", "hyperedge", "endpoint"
  };

  @Override
  public void run() {
    try {

      String content = textpane.getText().replace("\r\n", "\n");
      StyledDocument doc = textpane.getStyledDocument();
      doc.setCharacterAttributes(0, doc.getLength(), doc.getStyle("regular"), true);

      for ( String graphMLKeyword : allGraphMLKeywords ) {
        int index = 0;
        while ( ( index = content.indexOf(graphMLKeyword, index) ) != -1 ) {
          Character prev = (index > 0) ? content.charAt(index - 1) : ' ';
          Character next =
            (index + graphMLKeyword.length() < content.length() - 1) ? content.charAt(
               index + graphMLKeyword.length() ) : ' ';
          if ( ! Character.isLetter(prev) && ! Character.isLetter(next) &&
               ! prev.equals('.') && ! next.equals('.') )
            doc.setCharacterAttributes(index, graphMLKeyword.length(),
                                       doc.getStyle(graphMLKeywordStyleName), true);
          index += graphMLKeyword.length();
        }
      }
      textpane.setDocument(doc);
    } catch ( Exception e ) {
      ExceptionDialog.displayExceptionInDialog(e);
    }
  }

  private static void updateDocStyles(StyledDocument doc) {
    Style def =
      StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
    StyleConstants.setBold(def, true);
    Style regular = doc.addStyle("regular", def);
    StyleConstants.setFontFamily(def, "SansSerif");

    Style s = doc.addStyle(graphMLKeywordStyleName, regular);
    StyleConstants.setForeground(s, graphMLKeywordColor);
  }

}