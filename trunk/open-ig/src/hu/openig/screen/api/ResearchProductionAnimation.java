/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.api;

import hu.openig.model.ResearchType;

/**
 * Interface function to start playing a specific technology animation.
 * @author akarnokd, 2012.08.03.
 */
public interface ResearchProductionAnimation {
	/**
	 * Play animation for the given research.
	 * @param rt the target research
	 * @param switchTo change the current selected categories?
	 */
	void playAnim(ResearchType rt, boolean switchTo);
}
