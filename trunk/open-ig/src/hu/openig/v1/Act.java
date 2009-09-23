/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author karnok, 2009.09.23.
 * @version $Revision 1.0$
 */
public abstract class Act implements ActionListener {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		act();
	}
	/** Perform the action. */
	public abstract void act();
}
