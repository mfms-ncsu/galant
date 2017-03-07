package edu.ncsu.csc.Galant.gui.editor;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import edu.ncsu.csc.Galant.gui.prefs.PreferencesPanel;
import edu.ncsu.csc.Galant.gui.util.WindowUtil;
import edu.ncsu.csc.Galant.gui.util.ExceptionDialog;
import edu.ncsu.csc.Galant.GalantException;

/**
 * Menu bar for the text editor
 * @author Jason Cockrell, Ty Devries, Alex McCabe, Michael Owoc
 *
 */
public class GEditorMenuBar extends JMenuBar {

  private GEditorFrame parentFrame;

  public GEditorMenuBar(GEditorFrame _parentFrame) {
    super();
    parentFrame = _parentFrame;
    add( new GFileMenu() );
  }

  class GFileMenu extends JMenu {
    public GFileMenu() {
      super("File");
      setMnemonic(KeyEvent.VK_F);
      add( new GOpenItem() );
      add( new GSaveItem() );
      add( new GSaveAsItem() );
      add( new GLoadCompiledAlgorithm() );
      add( PreferencesPanel.SHOW_PREFS_DIALOG);
      add( WindowUtil.QUIT_ACTION);
    }
  }

  class GOpenItem extends JMenuItem implements ActionListener {
    public GOpenItem() {
      super("Open"); addActionListener(this);
      setMnemonic(KeyEvent.VK_O);
      setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_O,
                                              Toolkit.getDefaultToolkit().
                                              getMenuShortcutKeyMask() ) );
    }
    @Override
    public void actionPerformed(ActionEvent arg0) { parentFrame.open(); }
  }
  class GSaveAsItem extends JMenuItem implements ActionListener {
    public GSaveAsItem() {
      super("<html>Save As&hellip;</html>"); addActionListener(this);
      setMnemonic(KeyEvent.VK_A);
      setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                             Toolkit.getDefaultToolkit().
                                             getMenuShortcutKeyMask() |
                                             InputEvent.SHIFT_DOWN_MASK) );
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        parentFrame.saveAs();
      }
      catch ( GalantException ge ) {
        ge.report("");
        ExceptionDialog.displayExceptionInDialog( ge, ge.getMessage() );
      }
    }
  }
  class GSaveItem extends JMenuItem implements ActionListener {
    public GSaveItem() {
      super("Save"); addActionListener(this);
      setMnemonic(KeyEvent.VK_S);
      setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S,
                                              Toolkit.getDefaultToolkit().
                                              getMenuShortcutKeyMask() ) );
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        parentFrame.save();
      }
      catch ( GalantException ge ) {
        ge.report("");
        ExceptionDialog.displayExceptionInDialog( ge, ge.getMessage() );
      }
    }
  }
  class GLoadCompiledAlgorithm extends JMenuItem implements ActionListener {
    public GLoadCompiledAlgorithm() {
      super("Load Compiled Algorithm"); addActionListener(
        this); setMnemonic(KeyEvent.VK_D);
      setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_D,
                                              Toolkit.getDefaultToolkit().
                                              getMenuShortcutKeyMask() ) );
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      parentFrame.loadCompiledAlgorithm();
      /* parentFrame.open(); */ }
  }
}

// [Last modified: 2016 12 11 at 17:32:28 GMT]
