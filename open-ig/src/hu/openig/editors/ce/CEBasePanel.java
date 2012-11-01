/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * The base panel with some convenience support.
 * @author akarnokd, 2012.10.31.
 */
public class CEBasePanel extends JPanel {
	/** */
	private static final long serialVersionUID = 6893457628554154892L;
	/** The context. */
	protected final CEContext context;
	/**
	 * Constructor. Saves the context.
	 * @param context the context object
	 */
	public CEBasePanel(CEContext context) {
		this.context = context;
	}
	/**
	 * Get a translation for the given key.
	 * @param key the key
	 * @return the translation
	 */
	public String get(String key) {
		return context.get(key);
	}
	/**
	 * Format a translation for the given key and parameters.
	 * @param key the key
	 * @param params the parameters
	 * @return the translation
	 */
	public String format(String key, Object... params) {
		return context.format(key, params);
	}
	/**
	 * Creates an image icon for the given resource path.
	 * @param path the path
	 * @return the image icon
	 */
	public ImageIcon icon(String path) {
		URL u = getClass().getResource(path);
		if (u == null) {
			System.err.printf("Missing resource: %s%n", path);
		}
		return new ImageIcon(u);
	}
}
