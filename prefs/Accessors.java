package edu.ncsu.csc.Galant.prefs;

import java.awt.Color;
import java.io.File;
import java.util.prefs.Preferences;

/** Concrete instances of some {@link BackingStoreAccessor}s. */
public class Accessors
	{
		private static Preferences node = Preference.PREFERENCES_NODE;
		private Accessors()
			{}

		/**
		 * Determines whether or not there is a value for the given key at the given node.
		 * @return <code>true</code> if it returns the default and <code>false</code> if there's a real value.
		 */
		private static boolean isDefault(Preferences node, String key)
			{
				String unique = new String(); // b/c of "new", doesn't == any other Strings
				return unique == node.get(key, unique);
			}

		public static final BackingStoreAccessor<Color> COLOR_ACCESSOR = new BackingStoreAccessor<Color>(){
			@Override
			public Color get(String key)
				{
					if(isDefault(node, key))
						return null;
					return new Color(node.getInt(key, 0), true);
				}

			@Override
			public void put(String key, Color value)
				{
					node.putInt(key, value.getRGB());
				}
		};

		public static final BackingStoreAccessor<Integer> INT_ACCESSOR = new BackingStoreAccessor<Integer>(){
			@Override
			public void put(String key, Integer value)
				{
					if(value == null)
						node.put(key, null);
					else
						node.putInt(key, value);
				}

			@Override
			public Integer get(String key)
				{
					if(isDefault(node, key))
						return null;
					return node.getInt(key, -1);
				}
		};

		public static final BackingStoreAccessor<File> FILE_ACCESSOR = new BackingStoreAccessor<File>(){
			@Override
			public void put(String key, File value)
				{
					node.put(key, value.getAbsolutePath());
				}

			@Override
			public File get(String key)
				{
					String pathName = node.get(key, null);
					if(pathName == null)
						return null;
					return new File(pathName);
				}
		};

		public static final BackingStoreAccessor<Boolean> BOOLEAN_ACCESSOR = new BackingStoreAccessor<Boolean>(){
			@Override
			public void put(String key, Boolean value)
				{
					if(value == null)
						node.put(key, null);
					else
						node.putBoolean(key, value);
				}

			@Override
			public Boolean get(String key)
				{
					if(isDefault(node, key))
						return null;
					return node.getBoolean(key, false);
				}
		};
	}
