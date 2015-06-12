package edu.ncsu.csc.Galant.prefs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * <p>
 * Represents a group of related preferences.
 * </p>
 * <p>
 * <code>PreferenceGroup</code>s are arranged in a tree structure; if one
 * <code>PreferenceGroup</code> relates to a more specific subset of the category of preferences
 * that another <code>PreferenceGroup</code> relates to, the first <code>PreferenceGroup</code>
 * should be the child of the second.
 * </p>
 * <p>
 * New <code>PreferenceGroup</code>s can only be created as children of existing
 * <code>PreferenceGroup</code>s, with the {@link #addNewChild(String)} method; there is a singleton
 * {@linkplain #ROOT root} that serves as the parent of any <code>PreferenceGroup</code>s that don't
 * have a logical ancestor.
 * </p>
 */
public class PreferenceGroup implements TreeNode
	{
		/** The singleton root of the <code>PreferenceGroup</code> tree. */
		public static final PreferenceGroup ROOT = new PreferenceGroup(null, "<root>"){
			@Override
			public TreePath pathToNode()
				{
					return new TreePath(this);
				};
		};

		private PreferenceGroup parent;
		private List<PreferenceGroup> children = new ArrayList<PreferenceGroup>();
		private List<Preference<?>> preferenceList = new ArrayList<Preference<?>>();
		private String label;

		private PreferenceGroup(PreferenceGroup parent, String label)
			{
				this.parent = parent;
				this.label = label;
			}

		/**
		 * Adds the given <code>PreferenceGroup</code> as a child of this
		 * <code>PreferenceGroup</code>.
		 * @return the added child.
		 */
		public PreferenceGroup addChild(PreferenceGroup child)
			{
				children.add(child);
				return child;
			}

		/**
		 * Creates a new <code>PreferenceGroup</code> with the given label and adds it as a child of
		 * this <code>PreferenceGroup</code>.
		 * @param label a string that describes the group to the user.
		 * @return the added child.
		 */
		public PreferenceGroup addNewChild(String label)
			{
				return addChild(new PreferenceGroup(this, label));
			}

		@Override
		public PreferenceGroup getChildAt(int index)
			{
				return children.get(index);
			}

		@Override
		public int getChildCount()
			{
				return children.size();
			}

		@Override
		public PreferenceGroup getParent()
			{
				return parent;
			}

		@Override
		public int getIndex(TreeNode node)
			{
				return children.indexOf(node);
			}

		@Override
		public boolean getAllowsChildren()
			{
				return true;
			}

		@Override
		public boolean isLeaf()
			{
				return children.isEmpty();
			}

		@Override
		public Enumeration<PreferenceGroup> children()
			{
				return Collections.enumeration(children);
			}

		/** @return this <code>PreferenceGroup</code>'s label. */
		public String getLabel()
			{
				return label;
			}

		/**
		 * <p>
		 * Adds the given {@code Preference} to this <code>PreferenceGroup</code>.
		 * </p>
		 * @return the given <code>Preference</code>.
		 */
		public <V> Preference<V> addPreference(Preference<V> preference)
			{
				preferenceList.add(preference);
				return preference;
			}

		public void accept(PreferenceGroupVisitor visitor)
			{
				visitor.visit(this);
				for(PreferenceGroup child : children)
					child.accept(visitor);
			}

		/**
		 * Sends the given {@link PreferenceVisitor} to visit each of this
		 * <code>PreferenceGroup</code>'s {@link Preference}s in order.
		 */
		public void doVisits(PreferenceVisitor visitor)
			{
				for(Preference<?> preference : preferenceList)
					preference.accept(visitor);
			}

		/**
		 * Sends the given {@link PreferenceVisitor} to visit each of this
		 * <code>PreferenceGroup</code>'s {@link Preference}s, then those of its children.
		 */
		public void doVisitsOnSubtree(final PreferenceVisitor visitor)
			{
				accept(new PreferenceGroupVisitor(){
					@Override
					public void visit(PreferenceGroup group)
						{
							group.doVisits(visitor);
						}
				});
			}

		/** Returns this <code>PrefenceGroup</code>'s label. */
		@Override
		public String toString()
			{
				return getLabel();
			}

		public TreePath pathToNode()
			{
				return getParent().pathToNode().pathByAddingChild(this);
			}
	}
