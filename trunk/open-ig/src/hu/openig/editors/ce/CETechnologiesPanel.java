/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.utils.XElement;

/**
 * The Technologies panel to edit contents of the <code>tech.xml</code>. 
 * @author akarnokd, 2012.10.31.
 */
public class CETechnologiesPanel extends CEBasePanel 
implements CEPanelPreferences, CEUndoRedoSupport, CEProblemLocator {
	/** */
	private static final long serialVersionUID = 8419996018754156220L;
	/**
	 * Constructor. Initializes the GUI.
	 * @param context the context object.
	 */
	public CETechnologiesPanel(CEContext context) {
		super(context);
		initGUI();
	}
	/**
	 * Construct the GUI.
	 */
	private void initGUI() {
		
	}
	@Override
	public void loadPreferences(XElement preferences) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void savePreferences(XElement preferences) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void restoreState(XElement state) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void locateProblem(XElement description) {
		// TODO Auto-generated method stub
		
	}
}
