/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author karnok, 2009.10.10.
 * @version $Revision 1.0$
 */
public class TalkState {
	/** The start state. */
	public static final String START = "start";
	/** The end state. */
	public static final String END = "end";
	/** The state id. */
	public String id;
	/** The state picture. */
	public BufferedImage picture;
	/** The available talk speeches. */
	public final List<TalkSpeech> speeches = new ArrayList<TalkSpeech>();
}
