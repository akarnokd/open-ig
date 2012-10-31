/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

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
}
