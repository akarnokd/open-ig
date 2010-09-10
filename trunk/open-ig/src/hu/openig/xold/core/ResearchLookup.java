/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.core;

import java.awt.image.BufferedImage;
import java.io.File;


/**
 * Callback interface for looking up various research related images and labels.
 * @author karnokd, 2009.05.28.
 * @version $Revision 1.0$
 */
public interface ResearchLookup {
	/**
	 * Returns the name label for the research index.
	 * @param index the research index
	 * @return the name
	 */
	String getResearchName(int index);
	/**
	 * Returns the research description lines.
	 * @param index the research index
	 * @return the 3 element array of the research description
	 */
	String[] getResearchDescription(int index);
	/**
	 * Returns the information image of the given research image-index.
	 * @param index the index
	 * @return the image
	 */
	BufferedImage getInfoImage(int index);
	/**
	 * Returns the wired information image of the given research image-index.
	 * @param index the index
	 * @return the wired image
	 */
	BufferedImage getWiredInfoImage(int index);
	/** 
	 * Returns the small image used in research and production screens.
	 * @param imageIndex the index
	 * @return the image
	 */
	BufferedImage getSmallImage(int imageIndex);
	/**
	 * Returns the animation file name.
	 * @param imageIndex the image index
	 * @return the animation file
	 */
	File getAnimation(int imageIndex);
	/**
	 * Returns the wired animation file name.
	 * @param imageIndex the image index
	 * @return the wired animation file
	 */
	File getAnimationWired(int imageIndex);
}
