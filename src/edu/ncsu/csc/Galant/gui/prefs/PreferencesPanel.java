package edu.ncsu.csc.Galant.gui.prefs;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;
import edu.ncsu.csc.Galant.gui.util.WindowUtil;
import edu.ncsu.csc.Galant.prefs.Preference;
import edu.ncsu.csc.Galant.prefs.PreferenceGroup;
import edu.ncsu.csc.Galant.prefs.PreferenceGroupVisitor;
import edu.ncsu.csc.Galant.prefs.PreferenceVisitor;

/**
 * <p> The panel in which preferences are displayed. There is a single panel
 * that is used whenever a preference dialog is displayed, which can be done
 * with {@link #SHOW_PREFS_DIALOG}.
 * </p>

 * <p> The panel consists of three main components:
 * </p>
 * <ul>
 * <li>The main component is the {@link PreferenceGroupPanel}
 * of the selected {@link PreferenceGroup}. This
 * displays the preferences in the current preference group.</li>
 * <li>To the left is a {@link JTree} showing the preference-group hierarchy,
 * which allows users to select a group.</li>
 * <li>At the bottom are the following buttons, all of which act on all
 * preferences, not just those shown:</li>
 * <ul>
 * <li>Apply: propogates the values the user set on the components to
 * their associated {@link Preference} objects and then to the backing
 * store.</li>
 * <li>Save: the same as Apply, but closes the dialog * afterwards.</li>
 * <li>Cancel: closes the dialog without applying the values * the user set,
 * which are lost.</li>
 * </ul>
 * </ul>
 */
public class PreferencesPanel extends JPanel
	{
		private static final int
            DEFAULT_X = 300,
            DEFAULT_Y = 150,
            DEFAULT_WIDTH = 400,
            DEFAULT_HEIGHT = 400;
        private static final int DEFAULT_GROUP_PANEL_WIDTH = 200;
        private static final int DEFAULT_GROUP_PANEL_HEIGHT = 240;
		private static final PreferencesPanel PREFS_PANEL = new PreferencesPanel();

		/**
		 * Displays the preferences panel in a dialog with the given owner. Also resets the
		 * preferences' components to the main values, and sets the initial preference group to the
		 * first child of the root.
		 */
		public static final Action SHOW_PREFS_DIALOG = new AbstractAction("<html>Preferences&hellip;</html>"){
				{
					putValue(MNEMONIC_KEY, KeyEvent.VK_P);
					// accelerator includes shift because ctrl+P is too closely associated with
					// printing
					putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit
						.getDefaultToolkit().getMenuShortcutKeyMask() |
						InputEvent.SHIFT_DOWN_MASK));
				}
			@Override
			public void actionPerformed(ActionEvent e)
				{
					// Make sure components match their preferences
					PreferenceGroup.ROOT.doVisitsOnSubtree(new PreferenceVisitor(){
						@Override
						public void visit(Preference<?> preference)
							{
								PreferenceComponent.componentMap.get(preference).show();
							}
					});
					SwingUtilities.getWindowAncestor(PREFS_PANEL)
                 .setMinimumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
					SwingUtilities.getWindowAncestor(PREFS_PANEL).setVisible(true);
				}
		};

		private CardLayout cards = new CardLayout();
		private JPanel groupPanels = new JPanel(cards);
		private JTree groupTree;

		private PreferencesPanel()
			{
				super(new BorderLayout());

				// Add main preference-group display in center
				add(groupPanels);
				// Add all preference groups to card-layout panel
				PreferenceGroup.ROOT.accept(new PreferenceGroupVisitor(){
					@Override
					public void visit(PreferenceGroup group)
						{
							groupPanels.add(new PreferenceGroupPanel(group), group.pathToNode()
								.toString());
						}
				});

				// Add tree for selecting preference groups
				add(initPreferenceGroupTree(), BorderLayout.WEST);

				// Add apply/save/cancel buttons
				add(initButtons(), BorderLayout.SOUTH);
				// Initialize to the first child of the root
				setCurrentGroup(PreferenceGroup.ROOT.getChildAt(0));
				
				// Add to dialog
				initDialog();

            //this.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
			}

		private Component initPreferenceGroupTree()
			{
				groupTree = new JTree(PreferenceGroup.ROOT);
				// only want user to be able to select one group at a time
				groupTree.getSelectionModel().setSelectionMode(
					TreeSelectionModel.SINGLE_TREE_SELECTION);
				// don't want to show the root
				groupTree.setRootVisible(false);
				groupTree.setShowsRootHandles(true);
				// change which preferences are shown when a node is selected
				groupTree.addTreeSelectionListener(new TreeSelectionListener(){
					@Override
					public void valueChanged(TreeSelectionEvent e)
						{
							Object selected = e.getPath().getLastPathComponent();
							if(!(selected instanceof PreferenceGroup))
								throw new IllegalStateException(
									"A node in the tree that isn't a PreferenceGroup was selected.");
							setCurrentGroup((PreferenceGroup)selected);
						}
				});

				JScrollPane treeScroller = new JScrollPane(groupTree);
				treeScroller.setPreferredSize( new Dimension( DEFAULT_GROUP_PANEL_WIDTH, DEFAULT_GROUP_PANEL_HEIGHT ) );
				return treeScroller;
			}

		private static Component initButtons()
			{
				Box buttonPanel = new Box(BoxLayout.X_AXIS);

				// == Actions ==

				// apply
				final Action apply = new AbstractAction("Apply"){
					@Override
					public void actionPerformed(ActionEvent e)
						{
							// apply preferences for all components to the associated Preference
							// objects, and save them to the backing store.
							PreferenceGroup.ROOT.doVisitsOnSubtree(new PreferenceVisitor(){
								@Override
								public void visit(Preference<?> preference)
									{
										PreferenceComponent.componentMap.get(preference).apply();
										preference.store();
									}
							});
						}
				};
				apply.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);

				// close
				final Action close = new AbstractAction("Cancel"){
					@Override
					public void actionPerformed(ActionEvent e)
						{
							// close the window containing the preferences panel
							SwingUtilities.getWindowAncestor(PREFS_PANEL).dispose();
						}
				};
				close.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);

				// save
				final Action save = new AbstractAction("Save"){
					@Override
					public void actionPerformed(ActionEvent e)
						{
							// apply changes, then close dialog
							apply.actionPerformed(e);
							close.actionPerformed(e);
						}
				};
				save.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);

				// == Buttons ==

				// put the buttons to the right
				buttonPanel.add(Box.createHorizontalGlue());

				JButton applyButton = new JButton(apply);
				buttonPanel.add(applyButton);

				// a bit of spacing between Apply and Save
				buttonPanel.add(Box.createRigidArea(new Dimension(
					applyButton.getPreferredSize().width / 8, 0)));

				JButton saveButton = new JButton(save);
				buttonPanel.add(saveButton);

				// put some space between apply/save and cancel to decrease the likelihood that
				// someone will accidentally save or lose changes they didn't want to
				buttonPanel.add(Box.createRigidArea(new Dimension(
					saveButton.getPreferredSize().width / 4, 0)));

				JButton cancelButton = new JButton(close);
				buttonPanel.add(cancelButton);

				buttonPanel.setPreferredSize(new Dimension(DEFAULT_WIDTH,
					saveButton.getPreferredSize().height * 5 / 4));

				return buttonPanel;
			}
		
		private void initDialog()
			{
				JDialog dialog = new JDialog((Frame)null, "Preferences", false);
				dialog.setName("preferences_dialog");
				WindowUtil.preserveWindowBounds(dialog, DEFAULT_X, DEFAULT_Y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
				dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
				dialog.setContentPane(this);
			}

		/**
		 * Sets this <code>PreferencesDialog</code>'s current preference group to the given
		 * PreferenceGroup.
		 * @param currentGroup the new preference group.
		 */
		private void setCurrentGroup(PreferenceGroup currentGroup)
			{
				// set the main area to show the new group's container
				cards.show(groupPanels, currentGroup.pathToNode().toString());
				groupTree.setSelectionPath(currentGroup.pathToNode());
			}
	}

//  [Last modified: 2017 01 04 at 01:11:38 GMT]
